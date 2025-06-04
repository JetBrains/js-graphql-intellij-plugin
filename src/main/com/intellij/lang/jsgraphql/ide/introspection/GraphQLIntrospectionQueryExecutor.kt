package com.intellij.lang.jsgraphql.ide.introspection

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLQueryClient.Companion.parseResponseJson
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLQueryClient.Companion.prepareQueryPayload
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLSchemaCapability.*
import com.intellij.lang.jsgraphql.ide.notifications.handleIntrospectionError
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.getOrLogException
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.options.advanced.AdvancedSettings
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
        val introspectionQuery = composeIntrospectionQuery(capabilities)
        val rawIntrospectionResponse = withContext(Dispatchers.IO) {
          val queryRunner = GraphQLQueryClient.getInstance(project)
          queryRunner.sendRequest(endpoint, prepareQueryPayload(introspectionQuery), retry)
        } ?: return@withBackgroundProgress
        val parsedIntrospection =
          GraphQLIntrospectionService.parseIntrospectionOutput(project, endpoint, schemaPath, rawIntrospectionResponse)
          ?: return@withBackgroundProgress
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
      GraphQLQueryClient.getInstance(project)
        .sendRequest(endpoint, prepareQueryPayload(INTROSPECTION_SCHEMA_CAPABILITIES_QUERY), retry)
    } ?: return null

    val schemaCapabilities = runCatching {
      LOG.debug { "Received schema capabilities response: $response" }
      parseSchemaCapabilities(parseResponseJson(response))
    }.getOrLogException { LOG.warn("Error during parsing schema capabilities response: $response", it) }

    return schemaCapabilities ?: EnumSet.allOf(GraphQLSchemaCapability::class.java)
  }

  private val capabilitiesDetectionStrategy: GraphQLSchemaCapabilitiesDetectionStrategy
    get() = AdvancedSettings.getEnum("graphql.introspection.detect.schema.capabilities", GraphQLSchemaCapabilitiesDetectionStrategy::class.java)

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
        GraphQLIntrospectionService.createOrUpdateIntrospectionOutputFile(project, parsedIntrospection, filePath.name, dir)
      }
    }
    catch (exception: CancellationException) {
      throw exception
    }
    catch (e: Exception) {
      handleIntrospectionError(project, endpoint, e, null, rawIntrospectionResponse)
    }
  }

  private fun composeIntrospectionQuery(capabilities: EnumSet<GraphQLSchemaCapability>): String {
    val query = Registry.stringValue("graphql.introspection.custom.query")
    if (query.isNotBlank()) {
      return query
    }

    // this is still needed since we can have an issue with parsing values returned from server,
    // especially for custom JSON types, more https://github.com/JetBrains/js-graphql-intellij-plugin/issues/217
    if (AdvancedSettings.getBoolean("graphql.introspection.skip.default.values")) {
      capabilities.remove(INPUT_VALUE_DEFAULT_VALUE)
    }

    return buildIntrospectionQueryFromTemplate(capabilities)
  }

  private fun parseSchemaCapabilities(introspectionResponse: JsonObject): EnumSet<GraphQLSchemaCapability> {
    val responseObject = if (introspectionResponse.has("data"))
      introspectionResponse.getAsJsonObject("data")!!
    else
      introspectionResponse

    if (introspectionResponse.has("errors")) {
      throw IllegalArgumentException(
        GraphQLBundle.message(
          "graphql.introspection.capabilities.detection.failed.errors",
          introspectionResponse.get("errors")?.toString() ?: "[]"
        )
      )
    }

    val typeDefinition = responseObject.getTypeNonNull("__Type")
    val fieldDefinition = responseObject.getTypeNonNull("__Field")
    val directiveDefinition = responseObject.getTypeNonNull("__Directive")
    val inputValueDefinition = responseObject.getTypeNonNull("__InputValue")

    val capabilities = EnumSet.noneOf(GraphQLSchemaCapability::class.java)
    if (typeDefinition.hasArgument("inputFields", "includeDeprecated")) {
      capabilities.add(INCLUDE_DEPRECATED_INPUT_FIELDS)
    }
    if (fieldDefinition.hasArgument("args", "includeDeprecated")) {
      capabilities.add(INCLUDE_DEPRECATED_FIELD_ARGS)
    }
    if (directiveDefinition.hasArgument("args", "includeDeprecated")) {
      capabilities.add(INCLUDE_DEPRECATED_DIRECTIVE_ARGS)
    }
    if (inputValueDefinition.hasField("isDeprecated")) {
      capabilities.add(INPUT_VALUE_IS_DEPRECATED)
    }
    if (inputValueDefinition.hasField("deprecationReason")) {
      capabilities.add(INPUT_VALUE_DEPRECATION_REASON)
    }
    if (inputValueDefinition.hasField("defaultValue")) {
      capabilities.add(INPUT_VALUE_DEFAULT_VALUE)
    }
    if (directiveDefinition.hasField("isRepeatable")) {
      capabilities.add(DIRECTIVE_IS_REPEATABLE)
    }
    return capabilities
  }

  private fun JsonElement.getTypeNonNull(typeName: String): JsonObject =
    getType(typeName) ?: throw IllegalArgumentException("Missing $typeName type definition in introspection response")

  private fun JsonElement.getType(typeName: String): JsonObject? {
    if (!isJsonObject) return null
    val schema = asJsonObject.get("__schema") as? JsonObject
    val typesElement = schema?.get("types") as? JsonArray
    return typesElement?.find { it.isJsonObject && it.asJsonObject.get("name")?.asString == typeName } as? JsonObject
  }

  private fun JsonElement.getField(fieldName: String): JsonObject? {
    if (!isJsonObject) return null
    val fieldsElement = asJsonObject.get("fields") as? JsonArray
    return fieldsElement?.find { it.isJsonObject && it.asJsonObject.get("name")?.asString == fieldName } as? JsonObject
  }

  private fun JsonElement.getArgument(fieldName: String, argName: String): JsonObject? {
    val typeField = getField(fieldName)
    val argsElement = typeField?.get("args") as? JsonArray
    return argsElement?.find { it.isJsonObject && it.asJsonObject.get("name")?.asString == argName } as? JsonObject
  }

  private fun JsonElement.hasField(fieldName: String): Boolean =
    getField(fieldName) != null

  private fun JsonElement.hasArgument(fieldName: String, argName: String): Boolean =
    getArgument(fieldName, argName) != null
}
