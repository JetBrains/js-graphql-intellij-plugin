/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.documentation

import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.lang.documentation.DocumentationProviderEx
import com.intellij.lang.jsgraphql.GraphQLConstants
import com.intellij.lang.jsgraphql.ide.documentation.GraphQLDocumentationMarkdownRenderer.getDescriptionAsHTML
import com.intellij.lang.jsgraphql.psi.GraphQLDescriptionAware
import com.intellij.lang.jsgraphql.psi.GraphQLDirectiveDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLElement
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValue
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLInputObjectTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLInputObjectTypeExtensionDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLInputValueDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLNamedElement
import com.intellij.lang.jsgraphql.psi.GraphQLTypeNameDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLTypeSystemDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLTypedOperationDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLVariable
import com.intellij.lang.jsgraphql.psi.GraphQLVariableDefinition
import com.intellij.lang.jsgraphql.psi.findContainingTypeName
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider
import com.intellij.lang.jsgraphql.schema.formatAsTypeReference
import com.intellij.lang.jsgraphql.schema.getTypeDescription
import com.intellij.lang.jsgraphql.schema.getTypeName
import com.intellij.lang.jsgraphql.types.schema.GraphQLArgument
import com.intellij.lang.jsgraphql.types.schema.GraphQLEnumType
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldsContainer
import com.intellij.lang.jsgraphql.types.schema.GraphQLInputObjectType
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import org.jetbrains.annotations.Nls
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class GraphQLDocumentationProvider : DocumentationProviderEx() {
  override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): @Nls String? {
    if (isDocumentationSupported(element)) {
      return createQuickNavigateDocumentation(element)
    }
    return null
  }

  override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): @Nls String? {
    return createQuickNavigateDocumentation(element)
  }

  override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String, context: PsiElement?): PsiElement? {
    if (link.startsWith(GRAPHQL_DOC_PREFIX)) {
      return GraphQLDocumentationPsiElement(context, link)
    }
    return super.getDocumentationElementForLink(psiManager, link, context)
  }
}

private const val GRAPHQL_DOC_PREFIX = GraphQLConstants.GraphQL

@OptIn(ExperimentalContracts::class)
private fun isDocumentationSupported(element: PsiElement?): Boolean {
  contract {
    returns(true) implies (element is GraphQLElement)
  }
  return element is GraphQLElement
}

private fun createQuickNavigateDocumentation(element: PsiElement?): @NlsSafe String? {
  if (!isDocumentationSupported(element)) {
    return null
  }

  val typeRegistryService = GraphQLSchemaProvider.getInstance(element.project)
  val schema = typeRegistryService.getSchemaInfo(element).schema

  // TODO: remove a specific check for variables after refactoring them to be proper named elements with refactoring support
  if (element !is GraphQLNamedElement && element !is GraphQLVariable) {
    return null
  }

  return when (val parent = element.parent) {
    is GraphQLTypeNameDefinition -> getTypeDocumentation(element, schema, parent)
    is GraphQLFieldDefinition -> getFieldDocumentation(schema, parent)
    is GraphQLInputValueDefinition -> getArgumentDocumentation(schema, parent)
    is GraphQLEnumValue -> getEnumValueDocumentation(schema, parent)
    is GraphQLDirectiveDefinition -> getDirectiveDocumentation(schema, parent)
    is GraphQLTypedOperationDefinition -> getOperationDocumentation(parent)
    is GraphQLVariableDefinition -> getVariableDocumentation(parent)
    is GraphQLFragmentDefinition -> getFragmentDocumentation(parent)
    else -> null
  }
}

private fun getOperationDocumentation(target: GraphQLTypedOperationDefinition): String? {
  val operationName = target.name ?: return null
  val operationType = target.operationType.text

  return buildString {
    append(DocumentationMarkup.DEFINITION_START)
    append(operationType)
    append(" ")
    append(operationName)
    append(DocumentationMarkup.DEFINITION_END)

    appendDescription(target)
  }
}

private fun getVariableDocumentation(target: GraphQLVariableDefinition): String? {
  val name = target.variable.name ?: return null
  val type = target.type

  return buildString {
    append(DocumentationMarkup.DEFINITION_START)
    append("$").append(name)
    if (type != null) {
      append(": ")
      append(type.text)
    }
    append(DocumentationMarkup.DEFINITION_END)

    appendDescription(target)
  }
}

private fun getFragmentDocumentation(target: GraphQLFragmentDefinition): String? {
  val name = target.name ?: return null
  val condition = target.typeCondition?.typeName?.name ?: return null

  return buildString {
    append(DocumentationMarkup.DEFINITION_START)
    append("fragment ").append(name).append(" on ").append(condition)
    append(DocumentationMarkup.DEFINITION_END)

    appendDescription(target)
  }
}

private fun getDirectiveDocumentation(schema: GraphQLSchema, target: GraphQLDirectiveDefinition): String? {
  val directiveName = target.nameIdentifier ?: return null
  val schemaDirective = schema.getFirstDirective(directiveName.text) ?: return null

  return buildString {
    append(DocumentationMarkup.DEFINITION_START)
    append("@").append(schemaDirective.name)
    if (schemaDirective.isRepeatable) {
      append(" ").append(DocumentationMarkup.GRAYED_START).append("(repeatable)").append(DocumentationMarkup.GRAYED_END)
    }
    append(DocumentationMarkup.DEFINITION_END)

    appendDescription(schemaDirective.description)
  }
}

private fun getEnumValueDocumentation(schema: GraphQLSchema, target: GraphQLEnumValue): String? {
  val enumName = findContainingTypeName(target) ?: return null
  val schemaType = schema.getType(enumName) as? GraphQLEnumType ?: return null
  val enumValueName = target.name

  return buildString {
    append(DocumentationMarkup.DEFINITION_START)
    append(enumName).append(".").append(enumValueName)
    append(DocumentationMarkup.DEFINITION_END)

    for (enumValueDefinition in schemaType.values) {
      if (enumValueDefinition.name == enumValueName) {
        val description = enumValueDefinition.description ?: break

        appendDescription(description)
        break
      }
    }
  }
}

private fun getArgumentDocumentation(schema: GraphQLSchema, target: GraphQLInputValueDefinition): String? {
  // input value definition defines an argument on a field or a directive, or a field on an input type
  val inputValueName = target.name ?: return null
  val definition = PsiTreeUtil.getParentOfType(
    target,
    GraphQLFieldDefinition::class.java,
    GraphQLDirectiveDefinition::class.java,
    GraphQLInputObjectTypeDefinition::class.java,
    GraphQLInputObjectTypeExtensionDefinition::class.java
  ) ?: return null

  when (definition) {
    is GraphQLFieldDefinition -> {
      val typeName = findContainingTypeName(target) ?: return null
      val schemaType = schema.getType(typeName)
      val fieldDefinitions = if (schemaType is GraphQLFieldsContainer) schemaType.fieldDefinitions else return null
      val fieldName = definition.name
      for (fieldDefinition in fieldDefinitions) {
        if (fieldDefinition.name == fieldName) {
          for (argument in fieldDefinition.arguments) {
            if (argument.name == inputValueName) {
              return getArgumentDocumentation(inputValueName, argument)
            }
          }
        }
      }
    }
    is GraphQLDirectiveDefinition -> {
      val directiveName = definition.nameIdentifier ?: return null
      val schemaDirective = schema.getFirstDirective(directiveName.text) ?: return null
      for (argument in schemaDirective.arguments) {
        if (inputValueName == argument.name) {
          return getArgumentDocumentation(inputValueName, argument)
        }
      }
    }
    is GraphQLInputObjectTypeDefinition, is GraphQLInputObjectTypeExtensionDefinition -> {
      val inputTypeName = findContainingTypeName(target)
      val schemaType = schema.getType(inputTypeName) as? GraphQLInputObjectType ?: return null
      for (inputObjectField in schemaType.fieldDefinitions) {
        if (inputValueName == inputObjectField.name) {
          val type = inputObjectField.type
          return buildString {
            append(DocumentationMarkup.DEFINITION_START)
            append(getTypeName(schemaType)).append(".")
            append(inputValueName)
              .append(if (type != null) ": " else "")
              .append(if (type != null) formatAsTypeReference(type) else "")
            append(DocumentationMarkup.DEFINITION_END)

            appendDescription(inputObjectField.description)
          }
        }
      }
    }
  }

  return null
}

private fun getArgumentDocumentation(inputValueName: String?, argument: GraphQLArgument): String {
  return buildString {
    append(DocumentationMarkup.DEFINITION_START)
    val argumentType = argument.type
    append(inputValueName)
      .append(if (argumentType != null) ": " else " ")
      .append(if (argumentType != null) formatAsTypeReference(argumentType) else "")
    append(DocumentationMarkup.DEFINITION_END)

    appendDescription(argument.description)
  }
}

private fun StringBuilder.appendDescription(target: GraphQLDescriptionAware) {
  val description = target.description ?: return
  appendDescription(description.content)
}

private fun StringBuilder.appendDescription(description: String?) {
  if (description == null) return

  append(DocumentationMarkup.CONTENT_START)
    .append(getDescriptionAsHTML(description))
    .append(DocumentationMarkup.CONTENT_END)
}

private fun getFieldDocumentation(schema: GraphQLSchema, target: GraphQLFieldDefinition): String? {
  val definition = target.parentOfType<GraphQLTypeSystemDefinition>()
  val typeName = PsiTreeUtil.findChildOfType(definition, GraphQLNamedElement::class.java) ?: return null
  val containerType = schema.getType(typeName.name) as? GraphQLFieldsContainer ?: return null
  val fieldName = target.name ?: return null
  val fieldDefinition = containerType.getFieldDefinition(fieldName)
  val type = fieldDefinition?.type

  return buildString {
    append(DocumentationMarkup.DEFINITION_START)
    append(getTypeName(containerType)).append(".")
    append(fieldName)
    if (type != null) {
      append(": ")
      append(formatAsTypeReference(type))
    }
    append(DocumentationMarkup.DEFINITION_END)

    appendDescription(fieldDefinition?.description)
  }
}

private fun getTypeDocumentation(targetIdentifier: PsiElement, schema: GraphQLSchema, target: GraphQLTypeNameDefinition): String? {
  val namedElement = targetIdentifier as? GraphQLNamedElement ?: return null
  val schemaType = schema.getType(namedElement.name) ?: return null
  return buildString {
    append(DocumentationMarkup.DEFINITION_START)
    val keyword = PsiTreeUtil.prevVisibleLeaf(target)
    if (keyword != null) {
      append(keyword.text).append(" ")
    }
    append(targetIdentifier.text)
    append(DocumentationMarkup.DEFINITION_END)

    appendDescription(getTypeDescription(schemaType))
  }
}