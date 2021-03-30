package com.intellij.lang.jsgraphql.ide.validation;

import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDirectivesAware;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.validation.ValidationError;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraphQLGeneralErrorFilter implements GraphQLErrorFilter {
    @Override
    public boolean isIgnored(@NotNull Project project,
                             @Nullable GraphQLError error,
                             @Nullable PsiElement element) {
        if (!(error instanceof ValidationError)) {
            return false;
        }

        ValidationErrorType errorType = ((ValidationError) error).getValidationErrorType();
        if (isInsideTemplateElement(element)) {
            // error due to template placeholder replacement, so we can ignore it for '___' replacement variables
            if (errorType == ValidationErrorType.UndefinedVariable) {
                return true;
            }
        }

        if (errorType == ValidationErrorType.SubSelectionRequired) {
            // apollo client 2.5 doesn't require sub selections for client fields
            final GraphQLDirectivesAware directivesAware = PsiTreeUtil.getParentOfType(element, GraphQLDirectivesAware.class);
            if (directivesAware != null) {
                boolean ignoreError = false;
                for (GraphQLDirective directive : directivesAware.getDirectives()) {
                    if ("client".equals(directive.getName())) {
                        ignoreError = true;
                    }
                }
                if (ignoreError) {
                    return true;
                }
            }
        }

        if (errorType == ValidationErrorType.MisplacedDirective) {
            // graphql-java KnownDirectives rule only recognizes executable directive locations, so ignore
            // the error if we're inside a type definition
            return PsiTreeUtil.getParentOfType(element, GraphQLTypeSystemDefinition.class) != null;
        }
        return false;
    }

    boolean isInsideTemplateElement(@Nullable PsiElement psiElement) {
        return PsiTreeUtil.findFirstParent(
            psiElement, false,
            el -> el instanceof GraphQLTemplateDefinition || el instanceof GraphQLTemplateSelection || el instanceof GraphQLTemplateVariable
        ) != null;
    }

}
