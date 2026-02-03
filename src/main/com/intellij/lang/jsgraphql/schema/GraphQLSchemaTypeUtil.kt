@file:JvmName("GraphQLSchemaUtil")
/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema

import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier
import com.intellij.lang.jsgraphql.psi.GraphQLListType
import com.intellij.lang.jsgraphql.psi.GraphQLNonNullType
import com.intellij.lang.jsgraphql.psi.GraphQLTypeOwner
import com.intellij.lang.jsgraphql.types.language.ArrayValue
import com.intellij.lang.jsgraphql.types.language.BooleanValue
import com.intellij.lang.jsgraphql.types.language.EnumValue
import com.intellij.lang.jsgraphql.types.language.FloatValue
import com.intellij.lang.jsgraphql.types.language.IntValue
import com.intellij.lang.jsgraphql.types.language.InterfaceTypeDefinition
import com.intellij.lang.jsgraphql.types.language.NullValue
import com.intellij.lang.jsgraphql.types.language.ObjectValue
import com.intellij.lang.jsgraphql.types.language.StringValue
import com.intellij.lang.jsgraphql.types.language.VariableReference
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition
import com.intellij.lang.jsgraphql.types.schema.GraphQLInterfaceType
import com.intellij.lang.jsgraphql.types.schema.GraphQLList
import com.intellij.lang.jsgraphql.types.schema.GraphQLModifiedType
import com.intellij.lang.jsgraphql.types.schema.GraphQLNonNull
import com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema
import com.intellij.lang.jsgraphql.types.schema.GraphQLType
import com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil
import com.intellij.lang.jsgraphql.types.schema.GraphQLUnionType
import com.intellij.lang.jsgraphql.types.schema.GraphQLUnmodifiedType
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

/**
 * Provides the IDL string version of a type including handling of types wrapped in non-null/list-types
 */
fun formatAsTypeReference(rawType: GraphQLType?): String {
  if (rawType == null) {
    return ""
  }

  val sb = StringBuilder()
  val stack = ArrayDeque<String>()

  var type: GraphQLType = rawType
  while (true) {
    if (type is GraphQLNonNull) {
      type = type.wrappedType
      stack.add("!")
    }
    else if (type is GraphQLList) {
      type = type.wrappedType
      sb.append("[")
      stack.add("]")
    }
    else if (type is GraphQLUnmodifiedType) {
      sb.append(type.name)
      break
    }
    else {
      fileLogger().error("Unknown type: $type")
      break
    }
  }

  while (!stack.isEmpty()) {
    sb.append(stack.removeLast())
  }

  return sb.toString()
}

fun getTypeDescription(type: GraphQLType?): String? {
  return type?.unmodified?.description
}

fun getTypeName(type: GraphQLType?): String {
  return type?.unmodified?.name.orEmpty()
}

/**
 * Gets the unmodified type by unwrapping all modifier types (non-null/list).
 * Returns the base unmodified type or null if the input is null or not an unmodified type.
 *
 * @return the unmodified base type, or null if not found
 */
val GraphQLType.unmodified: GraphQLUnmodifiedType?
  get() = unwrapType(this)

private fun unwrapType(type: GraphQLType?): GraphQLUnmodifiedType? {
  if (type is GraphQLModifiedType) {
    return unwrapType(type.wrappedType)
  }
  return type as? GraphQLUnmodifiedType
}

fun unwrapListType(rawType: GraphQLType?): GraphQLType? {
  var type = rawType
  while (GraphQLTypeUtil.isWrapped(type)) {
    if (GraphQLTypeUtil.isList(type)) {
      return GraphQLTypeUtil.unwrapOne(type)
    }
    type = GraphQLTypeUtil.unwrapOne(type)
  }
  return type
}

fun getValueTypeName(value: Any): String {
  return when (value) {
    is IntValue -> "Int"
    is FloatValue -> "Float"
    is StringValue -> "String"
    is EnumValue -> "Enum"
    is BooleanValue -> "Boolean"
    is NullValue -> "null"
    is ArrayValue -> "Array"
    is ObjectValue -> "Object"
    is VariableReference -> "Reference"
    else -> value.javaClass.getSimpleName()
  }
}

fun getSchemaOperationTypeNames(schema: GraphQLSchema): Set<String> {
  return setOf(
    schema.getQueryType()?.name ?: GraphQLKnownTypes.QUERY_TYPE,
    schema.getMutationType()?.name ?: GraphQLKnownTypes.MUTATION_TYPE,
    schema.getSubscriptionType()?.name ?: GraphQLKnownTypes.SUBSCRIPTION_TYPE,
  )
}

fun hasRequiredArgs(field: GraphQLFieldDefinition?): Boolean {
  if (field == null) {
    return false
  }

  var hasRequiredArgs = false
  for (fieldArgument in field.arguments) {
    if (fieldArgument.type is GraphQLNonNull) {
      hasRequiredArgs = true
      break
    }
  }
  return hasRequiredArgs
}

/**
 * Gets whether the specified fragment candidate is valid to spread inside the specified required type scope
 *
 * @param typeDefinitionRegistry registry with available schema types, used to resolve union members and interface implementations
 * @param fragmentCandidate      the fragment to check for being able to validly spread under the required type scope
 * @param requiredTypeScope      the type scope in which the fragment is a candidate to spread
 * @return true if the fragment candidate is valid to be spread inside the type scope
 */
fun isFragmentApplicableInTypeScope(
  typeDefinitionRegistry: TypeDefinitionRegistry,
  fragmentCandidate: GraphQLFragmentDefinition,
  requiredTypeScope: GraphQLType,
): Boolean {
  // unwrap non-nullable and list types
  val typeScope = unwrapType(requiredTypeScope) ?: return false

  val typeCondition = fragmentCandidate.typeCondition
  val typeName = typeCondition?.typeName ?: return false

  val fragmentTypeName = typeName.name.orEmpty()
  if (fragmentTypeName == getTypeName(typeScope)) {
    // direct match, e.g. User scope, fragment on User
    return true
  }

  // check whether compatible based on interfaces and unions
  return isCompatibleFragment(typeDefinitionRegistry, typeScope, fragmentTypeName)
}

/**
 * Gets whether a fragment type condition name is compatible with the required type scope
 *
 * @param typeDefinitionRegistry registry with available schema types, used to resolve union members and interface implementations
 * @param rawRequiredTypeScope   the type scope in which the fragment is a candidate to spread
 * @param fragmentTypeName       the name of the type that a candidate fragment applies to
 * @return true if the candidate type condtion name is compatible inside the required type scope
 */
private fun isCompatibleFragment(
  typeDefinitionRegistry: TypeDefinitionRegistry,
  rawRequiredTypeScope: GraphQLType?,
  fragmentTypeName: String?,
): Boolean {
  val requiredTypeScope = unwrapType(rawRequiredTypeScope)

  if (requiredTypeScope is GraphQLInterfaceType) {
    // also include fragments on types implementing the interface scope
    val typeScopeDefinition = typeDefinitionRegistry.types()[requiredTypeScope.name] as? InterfaceTypeDefinition
    if (typeScopeDefinition != null) {
      val implementations = typeDefinitionRegistry.getImplementationsOf(typeScopeDefinition)
      for (implementation in implementations) {
        if (implementation.name == fragmentTypeName) {
          return true
        }
      }
    }
  }
  else if (requiredTypeScope is GraphQLObjectType) {
    // include fragments on the interfaces implemented by the object type
    for (outputType in requiredTypeScope.interfaces) {
      if (outputType.name == fragmentTypeName) {
        return true
      }
    }
  }
  else if (requiredTypeScope is GraphQLUnionType) {
    for (outputType in requiredTypeScope.types) {
      // check each type in the union for compatibility
      if (outputType.name == fragmentTypeName ||
          isCompatibleFragment(typeDefinitionRegistry, outputType, fragmentTypeName)
      ) {
        return true
      }
    }
  }
  return false
}

fun computeDeclaredType(owner: GraphQLTypeOwner): GraphQLType? {
  val psiType = owner.type
  if (psiType == null) {
    return null
  }

  // TODO: implement in PSI properly
  val typeIdentifier = PsiTreeUtil.findChildOfType(psiType, GraphQLIdentifier::class.java)
  if (typeIdentifier == null) {
    return null
  }

  val schema = GraphQLSchemaProvider.getInstance(owner.project).getSchemaInfo(owner).schema
  var schemaType = schema.getType(typeIdentifier.text)
  if (schemaType == null) {
    return null
  }

  var parent: PsiElement? = typeIdentifier
  while (parent != null && parent !== psiType) {
    if (parent is GraphQLListType) {
      schemaType = GraphQLList(schemaType)
    }
    else if (parent is GraphQLNonNullType) {
      schemaType = GraphQLNonNull(schemaType)
    }
    parent = parent.parent
  }
  return schemaType
}

