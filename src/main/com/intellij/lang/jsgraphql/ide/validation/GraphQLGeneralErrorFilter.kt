package com.intellij.lang.jsgraphql.ide.validation

import com.intellij.lang.jsgraphql.psi.GraphQLTypeSystemDefinition
import com.intellij.lang.jsgraphql.schema.GraphQLKnownTypes
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager
import com.intellij.lang.jsgraphql.types.GraphQLError
import com.intellij.lang.jsgraphql.types.language.NamedNode
import com.intellij.lang.jsgraphql.types.schema.idl.errors.QueryOperationMissingError
import com.intellij.lang.jsgraphql.types.schema.idl.errors.TypeExtensionMissingBaseTypeError
import com.intellij.lang.jsgraphql.types.validation.ValidationError
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class GraphQLGeneralErrorFilter : GraphQLErrorFilter {

  override fun isGraphQLErrorSuppressed(
    project: Project,
    error: GraphQLError,
    element: PsiElement?,
  ): Boolean {
    val namedNode = error.node as? NamedNode<*>

    if (error is QueryOperationMissingError) {
      return true
    }

    if (error is TypeExtensionMissingBaseTypeError && namedNode?.name == GraphQLKnownTypes.QUERY_TYPE) {
      val roots = GraphQLLibraryManager.getInstance(project).libraryRoots.map { it.path }.toSet()
      return namedNode.sourceLocation?.sourceName in roots
    }

    if (error is ValidationError && error.validationErrorType == ValidationErrorType.MisplacedDirective) {
      // graphql-java KnownDirectives rule only recognizes executable directive locations, so ignore
      // the error if we're inside a type definition
      return PsiTreeUtil.getParentOfType(element, GraphQLTypeSystemDefinition::class.java) != null
    }

    return false
  }

}
