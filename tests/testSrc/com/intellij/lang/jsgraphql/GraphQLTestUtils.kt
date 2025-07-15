@file:JvmName("GraphQLTestUtils")

package com.intellij.lang.jsgraphql

import com.intellij.graphql.javascript.workspace.GraphQLNodeModulesLibraryUpdater
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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.waitForSmartMode
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.util.ui.UIUtil
import java.io.File
import java.util.function.Function

const val GRAPHQL_TEST_RELATIVE_PATH = "tests/testData/graphql"

fun getTestDataPath(path: String): String {
  return listOf(PluginPathManager.getPluginHomePath("js-graphql"), GRAPHQL_TEST_RELATIVE_PATH, path)
    .joinToString(File.separator)
    .let { FileUtil.normalize(it) }
}

suspend fun withSettings(
  project: Project,
  action: suspend (GraphQLSettings) -> Unit,
  onCompletion: (suspend () -> Unit)?,
) {
  val settings = GraphQLSettings.getSettings(project)
  val previousState = settings.state
  val tempState = GraphQLSettingsState()
  settings.loadState(tempState)
  try {
    action.invoke(settings)
  }
  finally {
    settings.loadState(previousState)
    onCompletion?.invoke()
  }
}

suspend fun withExternalLibrary(
  project: Project,
  library: GraphQLLibrary,
  action: suspend () -> Unit,
) {
  val libraryManager = GraphQLLibraryManager.getInstance(project)
  libraryManager.registerExternalLibrary(library)
  try {
    action()
  }
  finally {
    libraryManager.unregisterExternalLibrary(library)
  }
}

suspend fun withLibrary(
  project: Project,
  libraryDescriptor: GraphQLLibraryDescriptor,
  action: suspend () -> Unit,
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
    syncLibraries(project)
    action()
  }, { syncLibraries(project) })
}

private suspend fun syncLibraries(project: Project) {
  GraphQLLibraryManager.getInstance(project).syncLibraries()
  GraphQLNodeModulesLibraryUpdater.getInstance(project).updateNodeModulesEntity()
  IndexingTestUtil.waitUntilIndexesAreReady(project)
  project.waitForSmartMode()
}

suspend fun withCustomEnv(project: Project, env: Map<String, String?>, action: suspend () -> Unit) {
  val before = GraphQLConfigEnvironment.getEnvVariable
  GraphQLConfigEnvironment.getEnvVariable = Function { env[it] }
  try {
    GraphQLConfigEnvironment.getInstance(project).notifyEnvironmentChanged()
    reloadConfig(project)
    action()
  }
  finally {
    GraphQLConfigEnvironment.getEnvVariable = before
  }
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

suspend fun reloadGraphQLConfiguration(project: Project) {
  runInEdtAndWait { UIUtil.dispatchAllInvocationEvents() }
  IndexingTestUtil.waitUntilIndexesAreReady(project)
  project.waitForSmartMode()

  // initializing config provider
  reloadConfig(project)
  syncLibraries(project)
  // reload again to ensure that the new schemas and libraries are picked up, and the scope is updated
  reloadConfig(project)

  convertJsonSchemasToSdl(project)
}

internal suspend fun CodeInsightTestFixture.reloadGraphQLConfiguration() {
  reloadGraphQLConfiguration(project)
}

internal suspend fun reloadConfig(project: Project) {
  GraphQLConfigProvider.getInstance(project).reload()
}

internal suspend fun convertJsonSchemasToSdl(project: Project) {
  GraphQLGeneratedSourcesUpdater.getInstance(project).runJsonSchemaFilesGeneration()
  GraphQLGeneratedSourcesManager.getInstance(project).awaitPendingTasks()
}
