package com.intellij.lang.jsgraphql.config

import com.intellij.lang.jsgraphql.GraphQLExecutionMode
import com.intellij.lang.jsgraphql.GraphQLTestFixture
import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigListener
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.highlighting.GraphQLHighlightingRestarter
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaCacheChangeListener
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaContentChangeListener
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaContentTracker
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import com.intellij.testFramework.replaceService
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

private const val CONFIG_RELOAD_REASON = "GraphQLConfigProvider.reload"
private const val SCHEMA_BUILD_REASON = "GraphQLSchemaProvider.SchemaComputation.ensureStarted"
private const val SCHEMA_CONTENT_REASON = "GraphQLSchemaContentTracker.notifySchemaContentChanged"

// the setup wait (10 s) plus the waits of a test (5 s each) do not fit the default 10 s
private val TEST_TIMEOUT: Duration = 60.seconds

/**
 * The asynchronous mode of the IDE: the configuration is loaded by the debounced collector, the schema is built by a background job,
 * the schema content changes are debounced, and each of them restarts the highlighting when it is done.
 *
 * The state services read the mode when they are created, so the fixture switches the mode before the project is built.
 * Dumb mode holds the background loads (they wait for a smart read action), so the "not loaded yet" state is checked with no race.
 */
@TestDataPath($$"$PROJECT_ROOT/plugins/js-graphql/tests/testData/graphql/config/syncLoad")
@TestApplication
class GraphQLAsynchronousModeTest {
  private val graphQLFixture by asynchronousGraphQLTestFixture("/config/syncLoad")

  private val project: Project get() = graphQLFixture.project

  private val provider: GraphQLConfigProvider get() = GraphQLConfigProvider.getInstance(project)

  @Test
  fun testScopeIsEmptyUntilCollectorLoads(): Unit = timeoutRunBlocking(TEST_TIMEOUT) {
    assertFalse(GraphQLExecutionMode.isSynchronous)
    copyProject()
    awaitConfigLoaded()
    val query = findPsiFile("query.graphql")
    val schema = graphQLFixture.fixture.findFileInTempDir("schema.graphql")

    // a restart counts only after the configuration change of the reload below was published
    val configPublished = AtomicBoolean()
    val restartsAfterPublish = AtomicInteger()
    installRestarter { reason ->
      if (reason == CONFIG_RELOAD_REASON && configPublished.get()) {
        restartsAfterPublish.incrementAndGet()
      }
    }
    project.messageBus.connect(graphQLFixture.testRootDisposable).subscribe(GraphQLConfigListener.TOPIC, object : GraphQLConfigListener {
      override fun onConfigurationChanged() {
        configPublished.set(true)
      }
    })

    inDumbMode {
      provider.invalidate()

      assertFalse(provider.isInitialized)
      // the asynchronous mode does not load inline
      assertFalse(readAction { provider.ensureInitialized() })
      assertSame(GlobalSearchScope.EMPTY_SCOPE, readAction { GraphQLScopeProvider.getInstance(project).getResolveScope(query, false) })
      assertFalse(provider.isInitialized)
      assertFalse(configPublished.get())
    }

    provider.requestReload()
    awaitCondition("the configuration is loaded by the collector") { provider.isInitialized }

    val scope = readAction { GraphQLScopeProvider.getInstance(project).getResolveScope(query, false) }
    assertThat(scope).isNotSameAs(GlobalSearchScope.EMPTY_SCOPE)
    assertTrue(readAction { scope.contains(schema) })
    assertTrue(configPublished.get())
    awaitCondition("the highlighting is restarted after the configuration change") { restartsAfterPublish.get() >= 1 }
  }

  @Test
  fun testSchemaFallbackUntilBackgroundBuild(): Unit = timeoutRunBlocking(TEST_TIMEOUT) {
    assertFalse(GraphQLExecutionMode.isSynchronous)
    Registry.get("graphql.schema.build.timeout").setValue(0, graphQLFixture.testRootDisposable)
    copyProject()
    awaitConfigLoaded()
    val scope = getScope()
    val schemaProvider = GraphQLSchemaProvider.getInstance(project)

    val schemaRestarts = AtomicInteger()
    installRestarter { reason ->
      if (reason == SCHEMA_BUILD_REASON) {
        schemaRestarts.incrementAndGet()
      }
    }
    val schemaCacheChanges = AtomicInteger()
    project.messageBus.connect(graphQLFixture.testRootDisposable).subscribe(
      GraphQLSchemaCacheChangeListener.TOPIC,
      object : GraphQLSchemaCacheChangeListener {
        override fun onSchemaCacheChanged() {
          schemaCacheChanges.incrementAndGet()
        }
      },
    )

    inDumbMode {
      // the job waits for the smart mode, and the zero timeout returns at once
      assertEmptyFallback(readAction { schemaProvider.getSchemaInfo(scope) })
      assertEmptyFallback(readAction { schemaProvider.getCachedSchemaInfo(scope) })
      assertEquals(0, schemaCacheChanges.get())
      assertEquals(0, schemaRestarts.get())
    }

    awaitCondition("the schema is built by the background job") { schemaCacheChanges.get() >= 1 }
    assertFullSchema(readAction { schemaProvider.getCachedSchemaInfo(scope) })
    // the job restarts the highlighting before it publishes the cache change
    assertThat(schemaRestarts.get()).isGreaterThanOrEqualTo(1)
  }

  @Test
  fun testSchemaContentUpdateIsDebounced(): Unit = timeoutRunBlocking(TEST_TIMEOUT) {
    assertFalse(GraphQLExecutionMode.isSynchronous)
    awaitConfigLoaded()
    val tracker = GraphQLSchemaContentTracker.getInstance(project)
    val contentRestarts = AtomicInteger()
    installRestarter { reason ->
      if (reason == SCHEMA_CONTENT_REASON) {
        contentRestarts.incrementAndGet()
      }
    }
    val notifications = ConcurrentLinkedQueue<TimeSource.Monotonic.ValueTimeMark>()
    project.messageBus.connect(graphQLFixture.testRootDisposable).subscribe(
      GraphQLSchemaContentChangeListener.TOPIC,
      object : GraphQLSchemaContentChangeListener {
        override fun onSchemaChanged() {
          notifications.add(TimeSource.Monotonic.markNow())
        }
      },
    )

    val stamp = tracker.modificationCount
    val updated = TimeSource.Monotonic.markNow()
    tracker.update()

    // No "unchanged right after update()" check: a thread pause after update() would let the 500 ms debounce fire first.
    // The delay of the first notification pins the debounce instead; a pause can only make it longer.
    // 400 ms, not 500 ms: the tolerance covers the timer granularity.
    awaitCondition("the schema content change is published by the collector") { notifications.isNotEmpty() }
    assertThat(notifications.first() - updated).isGreaterThanOrEqualTo(400.milliseconds)
    assertThat(tracker.modificationCount).isGreaterThan(stamp)
    awaitCondition("the highlighting is restarted after the schema content change") { contentRestarts.get() >= 1 }
  }

  private fun assertEmptyFallback(schemaInfo: GraphQLSchemaInfo) {
    val queryType = schemaInfo.schema.queryType
    assertNotNull(queryType)
    assertThat(queryType!!.fieldDefinitions).isEmpty()
  }

  private fun assertFullSchema(schemaInfo: GraphQLSchemaInfo) {
    val queryType = schemaInfo.schema.queryType
    assertNotNull(queryType)
    assertNotNull(queryType!!.getFieldDefinition("hello"))
  }

  /**
   * Replaces the restarter of the project with one that calls [onRestart] with the reason of each restart, until the end of the test.
   */
  private fun installRestarter(onRestart: (String) -> Unit) {
    val restarter = GraphQLHighlightingRestarter.createForTests(project, onRestart)
    project.replaceService(GraphQLHighlightingRestarter::class.java, restarter, graphQLFixture.testRootDisposable)
  }

  /**
   * Requests a load and waits until all requested loads are done, so that no load of the setup is in flight.
   * The request does not depend on the startup activity or on the config file listener.
   */
  private suspend fun awaitConfigLoaded() {
    provider.requestReload()
    awaitCondition("the configuration of the setup is loaded", 10.seconds) { provider.isInitialized && provider.isUpToDate }
  }

  /**
   * Runs [block] in dumb mode. The background loads wait for the smart mode, so they cannot complete in [block].
   */
  private suspend fun inDumbMode(block: suspend () -> Unit) {
    val token = DumbModeTestUtils.startEternalDumbModeTask(project)
    try {
      block()
    }
    finally {
      DumbModeTestUtils.endEternalDumbModeTaskAndWaitForSmartMode(project, token)
    }
    IndexingTestUtil.waitUntilIndexesAreReady(project)
  }

  private suspend fun awaitCondition(message: String, timeout: Duration = 5.seconds, condition: suspend () -> Boolean) {
    val reached = withTimeoutOrNull(timeout) {
      while (!condition()) {
        delay(20.milliseconds)
      }
      true
    }
    assertTrue(reached == true, "Timed out after $timeout: $message")
  }

  private suspend fun copyProject() {
    edtWriteAction { graphQLFixture.fixture.copyDirectoryToProject("project", "") }
    IndexingTestUtil.waitUntilIndexesAreReady(project)
  }

  private suspend fun getScope(): GlobalSearchScope {
    val query = findPsiFile("query.graphql")
    val scope = readAction { GraphQLScopeProvider.getInstance(project).getResolveScope(query, true) }
    assertThat(scope).isNotSameAs(GlobalSearchScope.EMPTY_SCOPE)
    return scope
  }

  private suspend fun findPsiFile(path: String): PsiFile {
    val file = graphQLFixture.fixture.findFileInTempDir(path)
    return readAction { PsiManager.getInstance(project).findFile(file) }!!
  }
}

/**
 * Creates a [GraphQLTestFixture] whose project is built in the asynchronous mode, and restores the mode when the test ends.
 * The mode is switched before the project and its services are created; the fixture does not load the configuration itself.
 */
private fun asynchronousGraphQLTestFixture(basePath: String): TestFixture<GraphQLTestFixture> =
  testFixture("asynchronousGraphQLTestFixture") {
    val asynchronousMode = Disposer.newDisposable("GraphQL asynchronous mode")
    GraphQLExecutionMode.setSynchronousForTests(false, asynchronousMode)
    val graphQLFixture = try {
      graphQLTestFixture(basePath, loadConfiguration = false).init()
    }
    catch (e: Throwable) {
      Disposer.dispose(asynchronousMode)
      throw e
    }
    // the dependent fixture is torn down first, so the mode is restored before the project is closed
    initialized(graphQLFixture) {
      Disposer.dispose(asynchronousMode)
    }
  }
