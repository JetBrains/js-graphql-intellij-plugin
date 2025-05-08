@file:JvmName("GraphQLTestUtils")

package com.intellij.lang.jsgraphql

import com.intellij.graphql.javascript.workspace.GraphQLNodeModulesLibraryUpdater
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.lang.jsgraphql.GraphQLSettings.GraphQLSettingsState
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLConfigEnvironment
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryDescriptor
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryTypes
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.utils.coroutines.waitCoroutinesBlocking
import com.intellij.util.concurrency.ThreadingAssertions
import com.intellij.util.concurrency.annotations.RequiresEdt
import java.io.File
import java.util.function.Consumer
import java.util.function.Function

const val GRAPHQL_TEST_RELATIVE_PATH = "tests/testData/graphql"

fun getTestDataPath(path: String): String {
  return listOf(PluginPathManager.getPluginHomePath("js-graphql"), GRAPHQL_TEST_RELATIVE_PATH, path)
    .joinToString(File.separator)
    .let { FileUtil.normalize(it) }
}

fun withSettings(
  project: Project,
  consumer: Consumer<GraphQLSettings>,
  disposable: Disposable,
) {
  withSettings(project, consumer, null, disposable)
}

fun withSettings(
  project: Project,
  consumer: Consumer<GraphQLSettings>,
  onDispose: Runnable?,
  disposable: Disposable,
) {
  val settings = GraphQLSettings.getSettings(project)
  val previousState = settings.state
  Disposer.register(disposable) {
    settings.loadState(previousState)
    onDispose?.run()
  }
  val tempState = GraphQLSettingsState()
  settings.loadState(tempState)
  consumer.accept(settings)
}

fun withLibrary(
  project: Project,
  libraryDescriptor: GraphQLLibraryDescriptor,
  testCase: Runnable,
  disposable: Disposable,
) {
  withSettings(project, { settings: GraphQLSettings ->
    if (libraryDescriptor === GraphQLLibraryTypes.RELAY) {
      settings.isRelaySupportEnabled = true
    }
    else if (libraryDescriptor === GraphQLLibraryTypes.FEDERATION) {
      settings.isFederationSupportEnabled = true
    }
    else if (libraryDescriptor === GraphQLLibraryTypes.APOLLO_KOTLIN) {
      settings.isApolloKotlinSupportEnabled = true
    }
    else {
      throw IllegalArgumentException("Unexpected library: $libraryDescriptor")
    }
    updateLibraries(project)
    testCase.run()
  }, { updateLibraries(project) }, disposable)
}

private fun updateLibraries(project: Project) {
  GraphQLLibraryManager.getInstance(project).notifyLibrariesChanged()
  PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
  IndexingTestUtil.waitUntilIndexesAreReady(project);
}

fun withCustomEnv(project: Project, env: Map<String, String?>, runnable: Runnable) {
  val before = GraphQLConfigEnvironment.getEnvVariable
  GraphQLConfigEnvironment.getEnvVariable = Function { env[it] }
  try {
    GraphQLConfigEnvironment.getInstance(project).notifyEnvironmentChanged()
    PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
    runnable.run()
  }
  finally {
    GraphQLConfigEnvironment.getEnvVariable = before
  }
}

@RequiresEdt
fun createTestScratchFile(
  fixture: CodeInsightTestFixture,
  path: String,
  projectName: String?,
  query: String?,
): VirtualFile? {
  return createTestScratchFile(fixture, createOverrideConfigComment(path, projectName), query)
}

fun createTestScratchFile(
  fixture: CodeInsightTestFixture,
  comment: String,
  query: String?,
): VirtualFile? {
  val text = "$comment\n\n${query.orEmpty()}"

  return ScratchRootType.getInstance()
    .createScratchFile(fixture.project, "scratch.graphql", GraphQLLanguage.INSTANCE, text)
    ?.also { file ->
      Disposer.register(fixture.testRootDisposable) {
        ApplicationManager.getApplication().runWriteAction { file.delete(null) }
      }
    }
}

fun reloadConfiguration(project: Project) {
  ThreadingAssertions.assertEventDispatchThread()
  GraphQLConfigProvider.getInstance(project).scheduleConfigurationReload()
  PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
  waitCoroutinesBlocking(GraphQLNodeModulesLibraryUpdater.getInstance(project).cs)
}
