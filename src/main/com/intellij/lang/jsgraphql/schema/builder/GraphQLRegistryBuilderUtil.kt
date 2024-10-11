@file:JvmName("GraphQLRegistryBuilderUtil")

package com.intellij.lang.jsgraphql.schema.builder

import com.intellij.lang.jsgraphql.types.language.AstPrinter
import com.intellij.lang.jsgraphql.types.language.NamedNode
import com.intellij.lang.jsgraphql.types.language.Node
import com.intellij.lang.jsgraphql.types.language.Type
import java.util.function.Function

fun <T : NamedNode<*>> mapNamedNodesByKey(nodes: List<T>): Map<String, T> {
  return mapNodesByKey<T>(nodes) { it.name }
}

fun mapTypeNodesByKey(nodes: List<Type<*>>): Map<String, Type<*>> {
  return mapNodesByKey<Type<*>>(nodes) { AstPrinter.printAst(it) }
}

fun <T : Node<*>> mapNodesByKey(nodes: List<T>, keyMapper: Function<T, String?>): Map<String, T> {
  val map = mutableMapOf<String, T>()
  for (node in nodes) {
    val key = keyMapper.apply(node) ?: continue
    map.merge(key, node) { oldValue, newValue -> oldValue }
  }
  return map
}

fun <T : Node<*>> mergeNodes(target: MutableMap<String, T>, source: Map<String, T>) {
  source.forEach { (key, value) -> target.merge(key, value) { oldValue, newValue -> oldValue } }
}
