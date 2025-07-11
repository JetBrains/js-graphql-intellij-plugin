@file:JvmName("GraphQLTestUtils")

package com.intellij.lang.jsgraphql

import com.intellij.ide.scratch.ScratchRootType
import com.intellij.lang.jsgraphql.GraphQLSettings.GraphQLSettingsState
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLConfigEnvironment
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesUpdater
import com.intellij.lang.jsgraphql.schema.library.GraphQLBundledLibraryTypes
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibrary
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryDescriptor
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.utils.coroutines.waitCoroutinesBlocking
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

fun withExternalLibrary(
  project: Project,
  library: GraphQLLibrary,
  consumer: Runnable,
  disposable: Disposable,
) {
  val libraryManager = GraphQLLibraryManager.getInstance(project)
  runWithModalProgressBlocking(project, "libraryManager.registerExternalLibrary(library)") {
    libraryManager.registerExternalLibrary(library)
  }
  Disposer.register(disposable) {
    runWithModalProgressBlocking(project, "libraryManager.unregisterExternalLibrary(library)") {
      libraryManager.unregisterExternalLibrary(library)
    }
  }
  consumer.run()
}

fun withLibrary(
  project: Project,
  libraryDescriptor: GraphQLLibraryDescriptor,
  testCase: Runnable,
  disposable: Disposable,
) {
  withSettings(project, { settings: GraphQLSettings ->
    if (libraryDescriptor === GraphQLBundledLibraryTypes.RELAY) {
      settings.isRelaySupportEnabled = true
    }
    else if (libraryDescriptor === GraphQLBundledLibraryTypes.FEDERATION) {
      settings.isFederationSupportEnabled = true
    }
    else if (libraryDescriptor === GraphQLBundledLibraryTypes.APOLLO_KOTLIN) {
      settings.isApolloKotlinSupportEnabled = true
    }
    else {
      throw IllegalArgumentException("Unexpected library: $libraryDescriptor")
    }
    syncLibrariesBlocking(project)
    testCase.run()
  }, { syncLibrariesBlocking(project) }, disposable)
}

private fun syncLibrariesBlocking(project: Project) {
  runWithModalProgressBlocking(project, "") {
    GraphQLLibraryManager.getInstance(project).syncLibraries()
  }

  PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
  IndexingTestUtil.waitUntilIndexesAreReady(project)
  DumbService.getInstance(project).waitForSmartMode()
}

fun withCustomEnv(project: Project, env: Map<String, String?>, runnable: Runnable) {
  val before = GraphQLConfigEnvironment.getEnvVariable
  GraphQLConfigEnvironment.getEnvVariable = Function { env[it] }
  try {
    GraphQLConfigEnvironment.getInstance(project).notifyEnvironmentChanged()
    reloadConfigBlocking(project)
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

fun reloadProjectConfiguration(project: Project) {
  PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
  IndexingTestUtil.waitUntilIndexesAreReady(project)

  // initializing config provider
  reloadConfigBlocking(project)
  syncLibrariesBlocking(project)
  // reload again to ensure that the new schemas and libraries are picked up, and the scope is updated
  reloadConfigBlocking(project)

  convertJsonSchemasToSdlBlocking(project)
}

internal fun reloadConfigBlocking(project: Project) {
  PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
  runWithModalProgressBlocking(project, "") {
    GraphQLConfigProvider.getInstance(project).reload()
  }
  PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
}

internal fun convertJsonSchemasToSdlBlocking(project: Project) {
  runWithModalProgressBlocking(project, "") {
    GraphQLGeneratedSourcesUpdater.getInstance(project).runJsonSchemaFilesGeneration()
  }
  PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
  waitCoroutinesBlocking(GraphQLGeneratedSourcesManager.getInstance(project).coroutineScope)
  PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
}
