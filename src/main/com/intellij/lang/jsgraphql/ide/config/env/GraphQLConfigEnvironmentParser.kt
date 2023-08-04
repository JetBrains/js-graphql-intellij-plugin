package com.intellij.lang.jsgraphql.ide.config.env

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class GraphQLConfigEnvironmentParser(private val project: Project) {
  companion object {
    @JvmStatic
    fun getInstance(project: Project) = project.service<GraphQLConfigEnvironmentParser>()
  }

  private val cache =
    CachedValuesManager.getManager(project).createCachedValue {
      CachedValueProvider.Result.create(
        ConcurrentHashMap<ConfigValue, GraphQLConfigValueNode>(),
        GraphQLConfigProvider.getInstance(project),
      )
    }

  private val regex = Regex("\\$\\{([^}]*)}")

  fun containsVariables(raw: String?, isLegacy: Boolean): Boolean {
    return raw?.let { parse(it, isLegacy) }?.variables?.isNotEmpty() ?: false
  }

  fun parse(text: String, isLegacy: Boolean): GraphQLConfigValueNode {
    return cache.value.computeIfAbsent(ConfigValue(text, isLegacy)) {
      if (it.isLegacy) {
        parseLegacy(it.text)
      }
      else {
        parse(it.text)
      }
    }
  }

  private fun parse(text: String): GraphQLConfigValueNode {
    val variables = regex.findAll(text).map { match ->
      val parts = match.groups[1]?.value?.split(':', limit = 2) ?: emptyList()
      val name = parts.getOrNull(0)
      val value = parts.getOrNull(1)?.let { StringUtil.unquoteString(it) }
      GraphQLConfigVariableNode(name, match.range, value)
    }.toList()

    return GraphQLConfigValueNode(text, variables)
  }

  private fun parseLegacy(text: String): GraphQLConfigValueNode {
    val variables = regex.findAll(text).map {
      val parts = it.groups[1]?.value?.split(':', limit = 2) ?: emptyList()
      val name = parts.getOrNull(1)
      GraphQLConfigVariableNode(name, it.range)
    }.toList()

    return GraphQLConfigValueNode(text, variables, true)
  }

  fun interpolate(raw: String, isLegacy: Boolean, provider: (name: String) -> String?): String {
    val node = parse(raw, isLegacy)
    if (node.variables.isEmpty()) {
      return raw
    }

    return regex.replace(node.text) {
      val variableNode = node.ranges[it.range]

      variableNode?.name?.let(provider)
      ?: variableNode?.defaultValue
      ?: it.value
    }
  }

  private data class ConfigValue(val text: String, val isLegacy: Boolean)

  data class GraphQLConfigValueNode(
    val text: String,
    val variables: List<GraphQLConfigVariableNode>,
    val isLegacy: Boolean = false,
  ) {
    val ranges = variables.associateBy { it.range }
  }

  data class GraphQLConfigVariableNode(
    val name: String?,
    val range: IntRange,
    val defaultValue: String? = null,
  )
}
