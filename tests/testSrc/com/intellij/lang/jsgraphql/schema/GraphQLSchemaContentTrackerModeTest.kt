package com.intellij.lang.jsgraphql.schema

import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerEx
import com.intellij.lang.jsgraphql.GraphQLExecutionMode
import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.ide.highlighting.GraphQLHighlightingRestarter
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider
import com.intellij.lang.jsgraphql.installCountingRestarter
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 * The schema content tracker of the synchronous mode: a change bumps the stamp and notifies the listeners at once,
 * with no debounce and no highlighting restart.
 */
@TestDataPath($$"$PROJECT_ROOT/plugins/js-graphql/tests/testData/graphql/schema/providerMode")
@TestApplication
class GraphQLSchemaContentTrackerModeTest {
  private val graphQLFixture by graphQLTestFixture("/schema/providerMode", loadConfiguration = false)

  private val project: Project get() = graphQLFixture.project

  private val tracker: GraphQLSchemaContentTracker get() = GraphQLSchemaContentTracker.getInstance(project)

  @Test
  fun testUpdateNotifiesAtOnce() {
    assertTrue(GraphQLExecutionMode.isSynchronous)
    val restarter = installCountingRestarter(project, graphQLFixture.testRootDisposable)
    val notifications = subscribe()

    val stamp = tracker.modificationCount
    tracker.update()
    assertThat(tracker.modificationCount).isGreaterThan(stamp)
    assertEquals(1, notifications.get())

    tracker.update()
    assertEquals(2, notifications.get())
    assertEquals(0, restarter.count.get())
  }

  @Test
  fun testSchemaEditVisibleAtOnce(): Unit = timeoutRunBlocking {
    val restarter = installCountingRestarter(project, graphQLFixture.testRootDisposable)
    copyProject()
    val schemaFile = findPsiFile("schema.graphql")
    val query = findPsiFile("query.graphql")
    val scope = readAction { GraphQLScopeProvider.getInstance(project).getResolveScope(query, true) }
    val provider = GraphQLSchemaProvider.getInstance(project)
    assertNull(readAction { provider.getSchemaInfo(scope) }.schema.getType("TrackerModeScalar"))
    val notifications = subscribe()
    val stamp = tracker.modificationCount

    writeCommandAction(project, "Add scalar") {
      val documentManager = PsiDocumentManager.getInstance(project)
      val document = documentManager.getDocument(schemaFile)!!
      document.insertString(document.textLength, "\nscalar TrackerModeScalar\n")
      documentManager.commitDocument(document)
    }

    assertThat(tracker.modificationCount).isGreaterThan(stamp)
    assertThat(notifications.get()).isPositive()
    assertNotNull(readAction { provider.getSchemaInfo(scope) }.schema.getType("TrackerModeScalar"))
    assertEquals(0, restarter.count.get())
  }

  /**
   * The real restarter must not touch the daemon: a daemon restart marks all files dirty, which fails while dirt is not allowed.
   */
  @Test
  fun testRestarterIsNoOpInSynchronousMode() {
    val restarter = GraphQLHighlightingRestarter.getInstance(project)
    val fileStatusMap = DaemonCodeAnalyzerEx.getInstanceEx(project).fileStatusMap

    fileStatusMap.runAllowingDirt<RuntimeException>(false) {
      restarter.restart("test")
    }

    // positive control: the asynchronous mode restarts the daemon
    val asynchronousMode = Disposer.newDisposable()
    try {
      GraphQLExecutionMode.setSynchronousForTests(false, asynchronousMode)
      assertThrows(AssertionError::class.java) {
        fileStatusMap.runAllowingDirt<RuntimeException>(false) {
          restarter.restart("test")
        }
      }
    }
    finally {
      Disposer.dispose(asynchronousMode)
    }
    assertTrue(GraphQLExecutionMode.isSynchronous)
  }

  private fun subscribe(): AtomicInteger {
    val notifications = AtomicInteger()
    project.messageBus.connect(graphQLFixture.testRootDisposable).subscribe(
      GraphQLSchemaContentChangeListener.TOPIC,
      object : GraphQLSchemaContentChangeListener {
        override fun onSchemaChanged() {
          notifications.incrementAndGet()
        }
      },
    )
    return notifications
  }

  private suspend fun copyProject() {
    edtWriteAction { graphQLFixture.fixture.copyDirectoryToProject("project", "") }
    IndexingTestUtil.waitUntilIndexesAreReady(project)
  }

  private suspend fun findPsiFile(path: String): PsiFile {
    val file = graphQLFixture.fixture.findFileInTempDir(path)
    return readAction { PsiManager.getInstance(project).findFile(file) }!!
  }
}
