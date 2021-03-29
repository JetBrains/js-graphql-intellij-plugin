package com.intellij.lang.jsgraphql.ide.validation;

import com.intellij.lang.jsgraphql.psi.GraphQLTypeSystemDefinition;
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
                             @NotNull PsiElement element) {
        if (error instanceof ValidationError) {
            ValidationErrorType errorType = ((ValidationError) error).getValidationErrorType();
            if (errorType == ValidationErrorType.MisplacedDirective) {
                // graphql-java KnownDirectives rule only recognizes executable directive locations, so ignore
                // the error if we're inside a type definition
                return PsiTreeUtil.getParentOfType(element, GraphQLTypeSystemDefinition.class) != null;
            }
        }
        return false;
    }

}
