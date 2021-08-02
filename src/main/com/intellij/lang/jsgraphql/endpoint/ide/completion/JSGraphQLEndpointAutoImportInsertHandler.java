/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointImportDeclaration;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Adds an automatic import when completing to a type that's not currently imported
 */
public class JSGraphQLEndpointAutoImportInsertHandler implements InsertHandler<LookupElement> {

    private final PsiFile fileToImport;

    public JSGraphQLEndpointAutoImportInsertHandler(PsiFile fileToImport) {
        this.fileToImport = fileToImport;
    }

    public void handleInsert(InsertionContext context, LookupElement item) {
        final Editor editor = context.getEditor();
        final Project project = editor.getProject();
        if (project != null) {

            final JSGraphQLEndpointImportDeclaration[] imports = PsiTreeUtil.getChildrenOfType(context.getFile(), JSGraphQLEndpointImportDeclaration.class);

            int insertionOffset = 0;
            if(imports != null && imports.length > 0) {
                JSGraphQLEndpointImportDeclaration lastImport = imports[imports.length - 1];
                insertionOffset = lastImport.getTextRange().getEndOffset();
            }

            final String name = JSGraphQLEndpointImportUtil.getImportName(project, fileToImport);
            String importDeclaration = "import \"" + name + "\"\n";
            if(insertionOffset > 0) {
                importDeclaration = "\n" + importDeclaration;
            }
            editor.getDocument().insertString(insertionOffset, importDeclaration);
        }
    }
}
