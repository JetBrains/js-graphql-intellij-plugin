package com.intellij.lang.jsgraphql.ide.introspection

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.GraphQLSettings
import com.intellij.lang.jsgraphql.GraphQLSettings.Companion.getSettings
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLQueryRunner.Companion.parseResponseJson
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLQueryRunner.Companion.prepareQueryPayload
import com.intellij.lang.jsgraphql.ide.notifications.handleIntrospectionError
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.getOrLogException
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.blockingContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.CancellationException
import kotlin.io.path.name

@Service(Service.Level.PROJECT)
class GraphQLIntrospectionQueryExecutor(private val project: Project, private val coroutineScope: CoroutineScope) {

  companion object {
    private val LOG = logger<GraphQLIntrospectionQueryExecutor>()

    @JvmStatic
    fun getInstance(project: Project): GraphQLIntrospectionQueryExecutor = project.service()
  }

  /**
   * Note: [com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionService.performIntrospectionQuery]
   * is the preferred method for handling user-initiated introspection queries.
   * Use it instead of direct query execution.
   */
  fun runIntrospectionQuery(endpoint: GraphQLConfigEndpoint, retry: Runnable? = null) {
    coroutineScope.launch {
      withBackgroundProgress(project, GraphQLBundle.message("graphql.progress.executing.introspection.query")) {
        val schemaPath = endpoint.schemaPointer?.outputPath
                         ?: run { LOG.warn("Schema path is null, unable to run introspection query"); return@withBackgroundProgress }
        val capabilities = detectSchemaCapabilities(endpoint, retry) ?: run {
          LOG.warn("Unable to request schema capabilities for endpoint '${endpoint.url ?: "null"}'")
          return@withBackgroundProgress
        }
        val introspectionQuery = composeIntrospectionQuery(capabilities, getSettings(project))
        val rawIntrospectionResponse = withContext(Dispatchers.IO) {
          val queryRunner = GraphQLQueryRunner.getInstance(project)
          queryRunner.sendRequest(endpoint, prepareQueryPayload(introspectionQuery), retry)
        } ?: return@withBackgroundProgress
        val parsedIntrospection = blockingContext {
          GraphQLIntrospectionService.parseIntrospectionOutput(project, endpoint, schemaPath, rawIntrospectionResponse)
        } ?: return@withBackgroundProgress
        createIntrospectionOutput(schemaPath, parsedIntrospection, endpoint, rawIntrospectionResponse)
      }
    }
  }

  private suspend fun detectSchemaCapabilities(
    endpoint: GraphQLConfigEndpoint,
    retry: Runnable? = null,
  ): EnumSet<GraphQLSchemaCapability>? = when (capabilitiesDetectionStrategy) {
    GraphQLSchemaCapabilitiesDetectionStrategy.ADAPTIVE -> fetchSchemaCapabilitiesFromServer(endpoint, retry)
    GraphQLSchemaCapabilitiesDetectionStrategy.LATEST -> EnumSet.allOf(GraphQLSchemaCapability::class.java)
    GraphQLSchemaCapabilitiesDetectionStrategy.LEGACY -> EnumSet.noneOf(GraphQLSchemaCapability::class.java)
  }

  private suspend fun fetchSchemaCapabilitiesFromServer(
    endpoint: GraphQLConfigEndpoint,
    retry: Runnable? = null,
  ): EnumSet<GraphQLSchemaCapability>? {
    val response = withContext(Dispatchers.IO) {
      GraphQLQueryRunner.getInstance(project)
        .sendRequest(endpoint, prepareQueryPayload(INTROSPECTION_SCHEMA_CAPABILITIES_QUERY), retry)
    } ?: return null

    val schemaCapabilities = runCatching {
      LOG.debug { "Received schema capabilities response: $response" }
      parseSchemaCapabilities(parseResponseJson(response))
    }.getOrLogException { LOG.warn("Error during parsing schema capabilities response: $response", it) }

    return schemaCapabilities ?: EnumSet.allOf(GraphQLSchemaCapability::class.java)
  }

  private val capabilitiesDetectionStrategy: GraphQLSchemaCapabilitiesDetectionStrategy
    get() {
      val option = Registry.get("graphql.introspection.detect.schema.capabilities").selectedOption
                   ?: return GraphQLSchemaCapabilitiesDetectionStrategy.ADAPTIVE

      return try {
        GraphQLSchemaCapabilitiesDetectionStrategy.valueOf(option.uppercase(Locale.getDefault()))
      }
      catch (_: IllegalArgumentException) {
        GraphQLSchemaCapabilitiesDetectionStrategy.ADAPTIVE
      }
    }

  private suspend fun createIntrospectionOutput(
    schemaPath: String,
    parsedIntrospection: GraphQLIntrospectionService.IntrospectionOutput,
    endpoint: GraphQLConfigEndpoint,
    rawIntrospectionResponse: String,
  ) {
    try {
      val filePath = Paths.get(FileUtil.toSystemDependentName(schemaPath))
      val dirPath = filePath.parent.also { Files.createDirectories(it) }
      val dir = if (dirPath != null) LocalFileSystem.getInstance().refreshAndFindFileByNioFile(dirPath) else null
      if (dir == null) {
        throw IOException("unable to create target directory: path=$schemaPath")
      }

      withContext(Dispatchers.EDT) {
        blockingContext {
          GraphQLIntrospectionService.createOrUpdateIntrospectionOutputFile(project, parsedIntrospection, filePath.name, dir)
        }
      }
    }
    catch (exception: CancellationException) {
      throw exception
    }
    catch (e: Exception) {
      handleIntrospectionError(project, endpoint, e, null, rawIntrospectionResponse)
    }
  }

  private fun composeIntrospectionQuery(capabilities: EnumSet<GraphQLSchemaCapability>, settings: GraphQLSettings): String {
    val query = settings.introspectionQuery
    if (query.isNotBlank()) {
      return query
    }

    // this is still needed since we can have an issue with parsing values returned from server,
    // especially for custom JSON types, more https://github.com/JetBrains/js-graphql-intellij-plugin/issues/217
    if (!settings.isEnableIntrospectionDefaultValues) {
      capabilities.remove(GraphQLSchemaCapability.INPUT_VALUE_DEFAULT_VALUE)
    }

    return buildIntrospectionQueryFromTemplate(capabilities)
  }
}

private enum class GraphQLSchemaCapabilitiesDetectionStrategy {
  ADAPTIVE,
  LATEST,
  LEGACY,
}