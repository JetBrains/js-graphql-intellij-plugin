@file:JvmName("GraphQLTestUtils")

package com.intellij.lang.jsgraphql

import com.intellij.lang.jsgraphql.GraphQLSettings.GraphQLSettingsState
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLConfigEnvironment
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryDescriptor
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryTypes
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.PlatformTestUtil
import java.io.File
import java.util.function.Consumer
import java.util.function.Function

const val GRAPHQL_TEST_BASE_PATH = "test-resources/testData/graphql"

fun getTestDataPath(path: String): String {
    return listOf(GRAPHQL_TEST_BASE_PATH, path).joinToString(if (path.startsWith(File.separator)) "" else File.separator)
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
    disposable: Disposable
) {
    val settings = GraphQLSettings.getSettings(project)
    val previousState = settings.state!!
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
    disposable: Disposable
) {
    withSettings(project, { settings: GraphQLSettings ->
        if (libraryDescriptor === GraphQLLibraryTypes.RELAY) {
            settings.isRelaySupportEnabled = true
        } else if (libraryDescriptor === GraphQLLibraryTypes.FEDERATION) {
            settings.isFederationSupportEnabled = true
        } else if (libraryDescriptor === GraphQLLibraryTypes.APOLLO_KOTLIN) {
            settings.isApolloKotlinSupportEnabled = true
        } else {
            throw IllegalArgumentException("Unexpected library: $libraryDescriptor")
        }
        updateLibraries(project)
        testCase.run()
    }, { updateLibraries(project) }, disposable)
}

private fun updateLibraries(project: Project) {
    GraphQLLibraryManager.getInstance(project).notifyLibrariesChanged()
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
}

fun withCustomEnv(env: Map<String, String?>, runnable: Runnable) {
    val before = GraphQLConfigEnvironment.getEnvVariable
    GraphQLConfigEnvironment.getEnvVariable = Function { env[it] }
    try {
        runnable.run()
    } finally {
        GraphQLConfigEnvironment.getEnvVariable = before
    }
}
