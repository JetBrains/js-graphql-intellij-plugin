package com.intellij.lang.jsgraphql.ide.validation;

import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface GraphQLErrorFilter {
    ExtensionPointName<GraphQLErrorFilter> EP_NAME = new ExtensionPointName<>("com.intellij.lang.jsgraphql.errorFilter");

    static boolean isErrorIgnored(@NotNull Project project, @Nullable GraphQLError error, @NotNull PsiElement element) {
        return EP_NAME.extensions().anyMatch(extension -> extension.isIgnored(project, error, element));
    }

    boolean isIgnored(@NotNull Project project, @Nullable GraphQLError error, @Nullable PsiElement element);
}
