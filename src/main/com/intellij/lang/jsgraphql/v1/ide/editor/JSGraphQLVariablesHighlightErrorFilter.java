/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.editor;

import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.json.psi.JsonFile;
import com.intellij.lang.jsgraphql.ide.project.GraphQLUIProjectService;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class JSGraphQLVariablesHighlightErrorFilter extends HighlightErrorFilter {

    @Override
    public boolean shouldHighlightErrorElement(@NotNull PsiErrorElement element) {
        final PsiFile file = element.getContainingFile();
        if(file instanceof JsonFile) {
            if(Boolean.TRUE.equals(file.getVirtualFile().getUserData(GraphQLUIProjectService.IS_GRAPH_QL_VARIABLES_VIRTUAL_FILE))) {
                // this is the variables file for GraphQL, so ignore errors as long as it's empty
                return !file.getText().isEmpty();
            }
        }
        return true;
    }
}
