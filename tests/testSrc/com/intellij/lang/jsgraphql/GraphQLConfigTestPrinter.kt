package com.intellij.lang.jsgraphql

import com.google.gson.GsonBuilder
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLSchemaPointer
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

/**
 * Prints [config] as JSON. The paths of the config are relative to [rootDir].
 */
class GraphQLConfigTestPrinter(private val config: GraphQLProjectConfig, private val rootDir: VirtualFile) {
  private val root: MutableMap<String, Any?> = mutableMapOf()

  fun print(): String {
    root["name"] = config.name
    root["dir"] = VfsUtilCore.getRelativePath(config.dir, rootDir)
    root["file"] = config.file?.let { VfsUtilCore.getRelativePath(it, rootDir) }.orEmpty()
    root["isDefault"] = config.isDefault
    root["isLegacy"] = config.isLegacy
    root["isRootEmpty"] = config.isRootEmpty
    root["schema"] = config.schema.map { buildSchemaPointer(it) }
    root["documents"] = config.documents
    root["include"] = config.include
    root["exclude"] = config.exclude
    root["extensions"] = config.extensions
    root["endpoints"] = config.endpoints.map { buildEndpoint(it) }
    root["environmentVariables"] = config.environment.variables

    return GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(root) + "\n"
  }

  private fun buildSchemaPointer(pointer: GraphQLSchemaPointer): Map<String, Any?> {
    return buildMap {
      this["pattern"] = pointer.pattern
      this["filePath"] = pointer.filePath
      this["globPath"] = pointer.globPath
      this["url"] = pointer.url
      this["headers"] = pointer.headers
      this["isRemote"] = pointer.isRemote
    }
  }

  private fun buildEndpoint(endpoint: GraphQLConfigEndpoint): Any {
    return buildMap {
      this["key"] = endpoint.key
      this["displayName"] = endpoint.displayName
      this["projectName"] = endpoint.projectName
      this["url"] = endpoint.url
      this["headers"] = endpoint.headers
      this["introspect"] = endpoint.introspect
    }
  }
}
