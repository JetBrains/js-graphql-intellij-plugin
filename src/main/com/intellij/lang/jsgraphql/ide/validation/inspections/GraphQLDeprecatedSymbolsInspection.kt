package com.intellij.lang.jsgraphql.ide.validation.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil
import com.intellij.lang.jsgraphql.psi.GraphQLArgument
import com.intellij.lang.jsgraphql.psi.GraphQLDirectivesAware
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValue
import com.intellij.lang.jsgraphql.psi.GraphQLField
import com.intellij.lang.jsgraphql.psi.GraphQLNamedElement
import com.intellij.lang.jsgraphql.psi.GraphQLObjectField
import com.intellij.lang.jsgraphql.psi.GraphQLStringValue
import com.intellij.lang.jsgraphql.psi.GraphQLVisitor
import com.intellij.lang.jsgraphql.schema.GraphQLKnownTypes
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.asSafely

class GraphQLDeprecatedSymbolsInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : GraphQLVisitor() {
      override fun visitField(field: GraphQLField) {
        super.visitField(field)
        checkForDeprecation(field)
      }

      override fun visitArgument(argument: GraphQLArgument) {
        super.visitArgument(argument)
        checkForDeprecation(argument)
      }

      override fun visitObjectField(objectField: GraphQLObjectField) {
        super.visitObjectField(objectField)
        checkForDeprecation(objectField)
      }

      override fun visitEnumValue(enumValue: GraphQLEnumValue) {
        super.visitEnumValue(enumValue)
        checkForDeprecation(enumValue)
      }

      private fun checkForDeprecation(namedElement: GraphQLNamedElement) {
        val identifier = namedElement.nameIdentifier ?: return
        val directive = GraphQLResolveUtil.resolve(namedElement).asSafely<GraphQLDirectivesAware>()
                          ?.directives?.find { it.name == GraphQLKnownTypes.DIRECTIVE_DEPRECATED } ?: return

        val deprecationReason = directive
          .arguments?.argumentList
          ?.find { it.name == GraphQLKnownTypes.DIRECTIVE_DEPRECATED_REASON }
          ?.value.asSafely<GraphQLStringValue>()?.valueAsString

        val description = if (!deprecationReason.isNullOrBlank())
          GraphQLBundle.message("graphql.inspection.deprecated.symbols.description.template", deprecationReason)
        else
          GraphQLBundle.message("graphql.inspection.deprecated.symbols.description.template.default")

        holder.registerProblem(identifier, description, ProblemHighlightType.LIKE_DEPRECATED)
      }
    }
  }
}