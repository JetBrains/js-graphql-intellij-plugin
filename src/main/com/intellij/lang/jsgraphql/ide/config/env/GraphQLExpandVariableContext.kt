package com.intellij.lang.jsgraphql.ide.config.env

import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawEndpoint
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawProjectConfig
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawSchemaPointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

internal class GraphQLExpandVariableContext(
  val project: Project,
  val dir: VirtualFile,
  val isLegacy: Boolean,
  val environment: GraphQLEnvironmentSnapshot,
) {
  val visited: MutableMap<Any, Any?> = mutableMapOf()
  val parser = GraphQLConfigEnvironmentParser.getInstance(project)
}

internal inline fun <reified T> expandVariables(value: T?, context: GraphQLExpandVariableContext): T? {
  if (value == null) return null

  if (context.visited.containsKey(value)) {
    return context.visited[value] as? T
  }

  val result = expandVariable(value, context)
  context.visited[value] = result
  return result as? T
}

private fun expandVariable(value: Any, context: GraphQLExpandVariableContext) = when (value) {
  is String -> context.parser.interpolate(value, context.isLegacy) { varName ->
    context.environment.variables[varName]
  }.trim()

  is Map<*, *> -> value.mapValues { expandVariables(it.value, context) }

  is List<*> -> value.map { expandVariables(it, context) }

  else -> value
}

internal fun extractEnvironmentVariables(
  project: Project,
  isLegacy: Boolean,
  vararg items: Any?,
): Collection<String> {
  val variables = mutableSetOf<String>()
  val parser = GraphQLConfigEnvironmentParser.getInstance(project)
  val visited = mutableSetOf<Any>()

  fun visitObject(obj: Any?) {
    if (obj == null || !visited.add(obj)) {
      return
    }

    when (obj) {
      is String -> parser.parse(obj, isLegacy)
        .variables
        .forEach {
          if (!it.name.isNullOrEmpty()) {
            variables.add(it.name)
          }
        }

      is Map<*, *> -> obj.forEach { _, value ->
        visitObject(value)
      }

      is List<*> -> obj.forEach {
        visitObject(it)
      }

      is GraphQLRawProjectConfig -> {
        visitObject(obj.schema)
        visitObject(obj.documents)
        visitObject(obj.include)
        visitObject(obj.exclude)
        visitObject(obj.extensions)
      }

      is GraphQLRawSchemaPointer -> {
        visitObject(obj.pattern)
        visitObject(obj.headers)
        visitObject(obj.introspect)
      }

      is GraphQLRawEndpoint -> {
        visitObject(obj.url)
        visitObject(obj.headers)
        visitObject(obj.introspect)
      }
    }
  }

  items.forEach { visitObject(it) }

  return variables
}
