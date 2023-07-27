package com.intellij.lang.jsgraphql.ide.validation

import com.intellij.lang.jsgraphql.types.GraphQLError
import com.intellij.lang.jsgraphql.types.language.DirectiveDefinition
import com.intellij.lang.jsgraphql.types.language.EnumTypeDefinition
import com.intellij.lang.jsgraphql.types.language.ObjectTypeDefinition
import com.intellij.lang.jsgraphql.types.language.ScalarTypeDefinition
import com.intellij.lang.jsgraphql.types.schema.idl.errors.DirectiveRedefinitionError
import com.intellij.lang.jsgraphql.types.schema.idl.errors.TypeRedefinitionError
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

/**
 * Suppress redefinition errors for built-in GraphQL types/directives, as it is valid to have them defined in an application's schema.
 */
class GraphQLBuiltInRedefinitionErrorFilter : GraphQLErrorFilter {
    private val builtInScalars = listOf("Int", "Float", "String", "Boolean", "ID")
    private val builtInDirectives = listOf("skip", "include", "deprecated", "defer", "specifiedBy")

    override fun isGraphQLErrorSuppressed(project: Project, error: GraphQLError, element: PsiElement?): Boolean {
        return when (error) {
            is TypeRedefinitionError -> {
                when (val node = error.node) {
                    is ScalarTypeDefinition -> {
                        node.name in builtInScalars
                    }

                    is ObjectTypeDefinition -> {
                        node.name.startsWith("__")
                    }

                    is EnumTypeDefinition -> {
                        node.name.startsWith("__")
                    }

                    else -> false
                }
            }

            is DirectiveRedefinitionError -> {
                (error.node as DirectiveDefinition).name in builtInDirectives
            }

            else -> false
        }
    }
}
