/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema

import com.intellij.lang.jsgraphql.psi.*
import com.intellij.lang.jsgraphql.types.language.*
import com.intellij.lang.jsgraphql.types.schema.*
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition
import com.intellij.lang.jsgraphql.types.schema.GraphQLType
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.Stack

object GraphQLSchemaUtil {
  /**
   * Provides the IDL string version of a type including handling of types wrapped in non-null/list-types
   */
  @JvmStatic
  fun typeString(rawType: GraphQLType?): String {
    if (rawType == null) {
      return ""
    }

    val sb = StringBuilder()
    val stack = Stack<String>()

    var type: GraphQLType = rawType
    while (true) {
      if (type is GraphQLNonNull) {
        type = type.wrappedType
        stack.push("!")
      }
      else if (type is GraphQLList) {
        type = type.wrappedType
        sb.append("[")
        stack.push("]")
      }
      else if (type is GraphQLUnmodifiedType) {
        sb.append(type.name)
        break
      }
      else {
        sb.append(type.toString())
        break
      }
    }
    while (!stack.isEmpty()) {
      sb.append(stack.pop())
    }

    return sb.toString()
  }

  @JvmStatic
  fun getTypeDescription(type: GraphQLType?): String? {
    return (type as? GraphQLNamedSchemaElement)?.description
  }

  @JvmStatic
  fun getTypeName(type: GraphQLType?): String {
    return (type as? GraphQLNamedSchemaElement)?.name.orEmpty()
  }

  /**
   * Gets the raw named type that sits within a non-null/list modifier type, or the type as-is if no unwrapping is needed
   *
   * @param type the type to unwrap
   * @return the raw type as-is, or the type wrapped inside a non-null/list modifier type
   */
  @JvmStatic
  fun getUnmodifiedType(type: GraphQLType?): GraphQLUnmodifiedType? {
    if (type is GraphQLModifiedType) {
      return getUnmodifiedType(type.wrappedType)
    }
    return type as? GraphQLUnmodifiedType
  }

  @JvmStatic
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

  @JvmStatic
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

  @JvmStatic
  fun getSchemaOperationTypeNames(schema: GraphQLSchema): Set<String> {
    return setOf(
      schema.getQueryType()?.name ?: GraphQLKnownTypes.QUERY_TYPE,
      schema.getMutationType()?.name ?: GraphQLKnownTypes.MUTATION_TYPE,
      schema.getSubscriptionType()?.name ?: GraphQLKnownTypes.SUBSCRIPTION_TYPE,
    )
  }

  @JvmStatic
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
  @JvmStatic
  fun isFragmentApplicableInTypeScope(
    typeDefinitionRegistry: TypeDefinitionRegistry,
    fragmentCandidate: GraphQLFragmentDefinition,
    requiredTypeScope: GraphQLType,
  ): Boolean {
    // unwrap non-nullable and list types
    var typeScope = getUnmodifiedType(requiredTypeScope) ?: return false

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
    val requiredTypeScope = getUnmodifiedType(rawRequiredTypeScope)

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

  @JvmStatic
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
}
