package com.intellij.lang.jsgraphql.config

import com.intellij.lang.jsgraphql.GraphQLExecutionMode
import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigContributor
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigListener
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLConfigCustomLoader
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider
import com.intellij.lang.jsgraphql.installCountingRestarter
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.extensions.LoadingOrder
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * The configuration load of the synchronous mode: the configuration is loaded on demand under the read action of the caller,
 * with no startup activity, no debounced collector and no daemon restart.
 */
@TestDataPath($$"$PROJECT_ROOT/plugins/js-graphql/tests/testData/graphql/config/syncLoad")
@TestApplication
class GraphQLConfigSynchronousLoadTest {
  private val graphQLFixture by graphQLTestFixture("/config/syncLoad", loadConfiguration = false)

  private val project: Project get() = graphQLFixture.project

  private val provider: GraphQLConfigProvider get() = GraphQLConfigProvider.getInstance(project)

  @Test
  fun testFirstAccessLoads(): Unit = timeoutRunBlocking {
    assertTrue(GraphQLExecutionMode.isSynchronous)
    val restarter = installCountingRestarter(project, graphQLFixture.testRootDisposable)
    copyProject()
    assertFalse(provider.isUpToDate)

    val query = findPsiFile("query.graphql")
    val scope = readAction { GraphQLScopeProvider.getInstance(project).getResolveScope(query) }

    assertTrue(provider.isInitialized)
    assertTrue(provider.isUpToDate)
    assertThat(scope).isNotSameAs(GlobalSearchScope.EMPTY_SCOPE)
    assertTrue(readAction { scope.contains(graphQLFixture.fixture.findFileInTempDir("schema.graphql")) })

    val projectConfig = readAction { provider.resolveProjectConfig(query) }
    assertNotNull(projectConfig)
    assertEquals(graphQLFixture.fixture.findFileInTempDir(".graphqlrc.yml"), projectConfig!!.file)
    assertEquals(0, restarter.count.get())
  }

  @Test
  fun testInvalidateFileIsLazy(): Unit = timeoutRunBlocking {
    val restarter = installCountingRestarter(project, graphQLFixture.testRootDisposable)
    copyProject()
    assertTrue(readAction { provider.ensureInitialized() })
    val modificationCount = provider.modificationCount

    provider.invalidate(graphQLFixture.fixture.findFileInTempDir(".graphqlrc.yml"))

    assertTrue(provider.isInitialized)
    assertFalse(provider.isUpToDate)
    assertEquals(modificationCount, provider.modificationCount)

    assertTrue(readAction { provider.ensureInitialized() })
    assertTrue(provider.isUpToDate)
    assertThat(provider.modificationCount).isGreaterThan(modificationCount)
    assertEquals(0, restarter.count.get())
  }

  @Test
  fun testInvalidateAllDropsData(): Unit = timeoutRunBlocking {
    val restarter = installCountingRestarter(project, graphQLFixture.testRootDisposable)
    copyProject()
    assertTrue(readAction { provider.ensureInitialized() })
    assertTrue(provider.hasExplicitConfiguration)

    provider.invalidate()

    assertFalse(provider.isInitialized)
    assertFalse(provider.isUpToDate)
    assertFalse(provider.hasExplicitConfiguration)
    assertThat(provider.getAllConfigs()).isEmpty()

    assertTrue(readAction { provider.ensureInitialized() })
    assertTrue(provider.isUpToDate)
    assertTrue(provider.hasExplicitConfiguration)
    assertEquals(0, restarter.count.get())
  }

  @Test
  fun testReloadLoadsWithoutDaemonRestart(): Unit = timeoutRunBlocking {
    val restarter = installCountingRestarter(project, graphQLFixture.testRootDisposable)
    copyProject()

    provider.reload()
    assertTrue(provider.isUpToDate)
    assertTrue(provider.hasExplicitConfiguration)

    provider.invalidate()
    provider.reload()
    assertTrue(provider.isUpToDate)
    assertTrue(provider.hasExplicitConfiguration)

    assertEquals(0, restarter.count.get())
  }

  @Test
  fun testReentrantContributorDoesNotDeadlock(): Unit = timeoutRunBlocking {
    copyProject()
    val query = findPsiFile("query.graphql")
    val calls = AtomicInteger()
    val innerScope = AtomicReference<GlobalSearchScope>()
    GraphQLConfigContributor.EP_NAME.point.registerExtension(object : GraphQLConfigContributor {
      override fun contributeConfigs(project: Project): Collection<GraphQLConfig> {
        calls.incrementAndGet()
        innerScope.compareAndSet(null, GraphQLScopeProvider.getInstance(project).getResolveScope(query))
        return emptyList()
      }
    }, graphQLFixture.testRootDisposable)

    assertTrue(readAction { provider.ensureInitialized() })

    assertTrue(provider.isUpToDate)
    assertThat(calls.get()).isPositive()
    // the nested access does not load again; it sees the state of the previous load, which is NotLoaded here
    assertSame(GlobalSearchScope.EMPTY_SCOPE, innerScope.get())
    assertThat(readAction { GraphQLScopeProvider.getInstance(project).getResolveScope(query) })
      .isNotSameAs(GlobalSearchScope.EMPTY_SCOPE)
  }

  /**
   * `FilenameIndex` does not depend on the file content, so the config discovery itself works in dumb mode.
   * A dumb-unaware contributor stands for any index access of the load that needs smart mode.
   */
  @Test
  fun testDumbModePropagatesIndexNotReady(): Unit = timeoutRunBlocking {
    copyProject()
    GraphQLConfigContributor.EP_NAME.point.registerExtension(object : GraphQLConfigContributor {
      override fun contributeConfigs(project: Project): Collection<GraphQLConfig> {
        if (DumbService.isDumb(project)) {
          throw IndexNotReadyException.create()
        }
        return emptyList()
      }
    }, graphQLFixture.testRootDisposable)
    assertFalse(provider.isInitialized)

    DumbModeTestUtils.runInDumbModeSynchronously(project) {
      assertThrows<IndexNotReadyException> {
        runReadAction { provider.ensureInitialized() }
      }
    }

    assertFalse(provider.isInitialized)
    assertFalse(provider.isUpToDate)
    IndexingTestUtil.waitUntilIndexesAreReady(project)
    assertTrue(readAction { provider.ensureInitialized() })
    assertTrue(provider.isUpToDate)
  }

  /**
   * A load cancelled after a config entry was already replaced must not lose the change: the retry sees the matching timestamp,
   * so it must still publish the change and invalidate the scopes.
   */
  @Test
  fun testCancelledLoadKeepsChange(): Unit = timeoutRunBlocking {
    copyProject()
    val failNextPoll = AtomicBoolean()
    GraphQLConfigContributor.EP_NAME.point.registerExtension(object : GraphQLConfigContributor {
      override fun contributeConfigs(project: Project): Collection<GraphQLConfig> {
        if (failNextPoll.compareAndSet(true, false)) {
          throw ProcessCanceledException()
        }
        return emptyList()
      }
    }, graphQLFixture.testRootDisposable)
    val query = findPsiFile("query.graphql")
    val schema = graphQLFixture.fixture.findFileInTempDir("schema.graphql")
    val other = graphQLFixture.fixture.findFileInTempDir("other.graphql")
    // loads, and consumes the invalidation of the contributor registration
    assertTrue(readAction { GraphQLScopeProvider.getInstance(project).getResolveScope(query).contains(schema) })

    val configChanges = AtomicInteger()
    project.messageBus.connect(graphQLFixture.testRootDisposable).subscribe(GraphQLConfigListener.TOPIC, object : GraphQLConfigListener {
      override fun onConfigurationChanged() {
        configChanges.incrementAndGet()
      }
    })
    val configFile = graphQLFixture.fixture.findFileInTempDir(".graphqlrc.yml")
    edtWriteAction {
      // an explicit newer timestamp, so that the change does not depend on the file system clock resolution
      configFile.setBinaryContent("schema: other.graphql\ndocuments: query.graphql\n".toByteArray(), -1, configFile.timeStamp + 1000)
    }
    assertFalse(provider.isUpToDate)

    failNextPoll.set(true)
    assertThrows<ProcessCanceledException> {
      runReadAction { provider.ensureInitialized() }
    }
    assertFalse(provider.isUpToDate)
    assertEquals(0, configChanges.get())

    val scope = readAction { GraphQLScopeProvider.getInstance(project).getResolveScope(query) }
    assertTrue(provider.isUpToDate)
    assertEquals(1, configChanges.get())
    assertTrue(readAction { scope.contains(other) })
    assertFalse(readAction { scope.contains(schema) })
  }

  @Test
  fun testReloadSavesEditedConfigDocument(): Unit = timeoutRunBlocking {
    copyProject()
    val query = findPsiFile("query.graphql")
    val schema = graphQLFixture.fixture.findFileInTempDir("schema.graphql")
    val other = graphQLFixture.fixture.findFileInTempDir("other.graphql")
    assertTrue(readAction { GraphQLScopeProvider.getInstance(project).getResolveScope(query).contains(schema) })

    val configFile = graphQLFixture.fixture.findFileInTempDir(".graphqlrc.yml")
    edtWriteAction {
      FileDocumentManager.getInstance().getDocument(configFile)!!.setText("schema: other.graphql\ndocuments: query.graphql\n")
    }
    provider.reload()

    val scope = readAction { GraphQLScopeProvider.getInstance(project).getResolveScope(query) }
    assertTrue(readAction { scope.contains(other) })
    assertFalse(readAction { scope.contains(schema) })
  }

  /**
   * A load cancelled inside the config loader after [GraphQLConfigProvider.invalidate] of the file must not lose the invalidation:
   * the timestamp is unchanged, so only the per-file flag makes the retry load the file again.
   * The cancellation comes from a custom loader of a JS config, the loader that can block (a Node process in production).
   */
  @Test
  fun testCancelledLoadKeepsFileInvalidation(): Unit = timeoutRunBlocking {
    val failNextLoad = AtomicBoolean()
    GraphQLConfigCustomLoader.EP_NAME.point.registerExtension(object : GraphQLConfigCustomLoader {
      override fun accepts(file: VirtualFile): Boolean = file.name == "graphql.config.js"

      // the test config holds only the schema file name
      override fun load(project: Project, file: VirtualFile): Map<*, *> {
        if (failNextLoad.compareAndSet(true, false)) {
          throw ProcessCanceledException()
        }
        return mapOf("schema" to VfsUtilCore.loadText(file).trim(), "documents" to "query.graphql")
      }
    }, LoadingOrder.FIRST, graphQLFixture.testRootDisposable)
    copyProject("jsProject")
    val query = findPsiFile("query.graphql")
    val schema = graphQLFixture.fixture.findFileInTempDir("schema.graphql")
    val other = graphQLFixture.fixture.findFileInTempDir("other.graphql")
    assertTrue(readAction { GraphQLScopeProvider.getInstance(project).getResolveScope(query).contains(schema) })

    val configFile = graphQLFixture.fixture.findFileInTempDir("graphql.config.js")
    edtWriteAction {
      // the same timestamp on purpose: only the invalidation makes the file reload
      configFile.setBinaryContent("other.graphql\n".toByteArray(), -1, configFile.timeStamp)
    }
    provider.invalidate(configFile)

    failNextLoad.set(true)
    assertThrows<ProcessCanceledException> {
      runReadAction { provider.ensureInitialized() }
    }
    assertFalse(provider.isUpToDate)

    val scope = readAction { GraphQLScopeProvider.getInstance(project).getResolveScope(query) }
    assertTrue(provider.isUpToDate)
    assertTrue(readAction { scope.contains(other) })
    assertFalse(readAction { scope.contains(schema) })
  }

  private suspend fun copyProject(dir: String = "project") {
    edtWriteAction { graphQLFixture.fixture.copyDirectoryToProject(dir, "") }
    IndexingTestUtil.waitUntilIndexesAreReady(project)
  }

  private suspend fun findPsiFile(path: String): PsiFile {
    val file = graphQLFixture.fixture.findFileInTempDir(path)
    return readAction { PsiManager.getInstance(project).findFile(file) }!!
  }
}
