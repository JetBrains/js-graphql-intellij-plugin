/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.validation.fixes;

import com.google.common.collect.Lists;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.lang.jsgraphql.psi.GraphQLDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier;
import com.intellij.lang.jsgraphql.psi.GraphQLInputValueDefinition;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl;
import com.intellij.psi.util.PsiEditorUtil;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Fix to create a new type definition while authoring schemas as SDL.
 */
public class GraphQLMissingTypeFix extends LocalQuickFixAndIntentionActionOnPsiElement {

    private final GraphQLTypeKind typeKind;
    private final String typeName;

    enum GraphQLTypeKind {
        TYPE,
        INTERFACE,
        UNION,
        SCALAR,
        ENUM,
        INPUT
    }

    private GraphQLMissingTypeFix(@NotNull GraphQLIdentifier element, GraphQLTypeKind typeKind) {
        super(element);
        typeName = element.getText();
        this.typeKind = typeKind;
    }

    @NotNull
    @Override
    public String getText() {
        return "Add \"" + typeKind.name().toLowerCase() + ' ' + this.typeName + "\"";
    }

    @Override
    public void invoke(@NotNull Project project,
                       @NotNull PsiFile file,
                       @Nullable("is null when called from inspection") Editor editor,
                       @NotNull PsiElement startElement,
                       @NotNull PsiElement endElement) {
        if (editor == null) {
            editor = PsiEditorUtil.Service.getInstance().findEditorByPsiElement(startElement);
            if (editor == null) {
                return;
            }
        }
        final GraphQLDefinition definition = PsiTreeUtil.getParentOfType(startElement, GraphQLDefinition.class);
        if (definition == null) {
            return;
        }
        editor.getCaretModel().moveToOffset(definition.getTextRange().getEndOffset() + 1);
        String code = typeKind.name().toLowerCase() + " " + typeName;
        String caret = "__caret__";
        switch (typeKind) {
            case ENUM:
            case TYPE:
            case INTERFACE:
            case INPUT:
                code += " {\n" + caret + "\n}";
                break;
            case SCALAR:
                code += caret;
                break;
            case UNION:
                code += " = " + caret;
                break;
        }
        final PsiFile codeFile = PsiFileFactory.getInstance(project).createFileFromText("", GraphQLLanguage.INSTANCE, code);
        CodeStyleManagerImpl.getInstance(project).reformat(codeFile);
        assert codeFile.getViewProvider().getDocument() != null;
        CodeStyleManagerImpl.getInstance(project).reformat(codeFile);
        final Document document = codeFile.getViewProvider().getDocument();
        if (document != null) {
            code = document.getText();
        }
        int caretDelta = 0;
        if (code.contains(caret)) {
            caretDelta = code.indexOf(caret) + 1;
            code = code.replace(caret, "");
        }

        final String lineBefore = definition.getNextSibling() instanceof PsiWhiteSpace ? "" : "\n";

        EditorModificationUtil.insertStringAtCaret(editor, lineBefore + "\n" + code + "\n", false, caretDelta);
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return "Create missing type definition";
    }

    public static List<GraphQLMissingTypeFix> getApplicableFixes(GraphQLIdentifier typeName) {
        final List<GraphQLMissingTypeFix> fixes = Lists.newArrayList();
        final GraphQLInputValueDefinition inputValueDefinition = PsiTreeUtil.getParentOfType(typeName, GraphQLInputValueDefinition.class);
        if (inputValueDefinition != null) {
            // input types: input object, enum, scalar
            fixes.add(new GraphQLMissingTypeFix(typeName, GraphQLTypeKind.INPUT));
            fixes.add(new GraphQLMissingTypeFix(typeName, GraphQLTypeKind.ENUM));
            fixes.add(new GraphQLMissingTypeFix(typeName, GraphQLTypeKind.SCALAR));
        } else {
            // output types: typeKind, interface, enum, union, scalar
            fixes.add(new GraphQLMissingTypeFix(typeName, GraphQLTypeKind.TYPE));
            fixes.add(new GraphQLMissingTypeFix(typeName, GraphQLTypeKind.INTERFACE));
            fixes.add(new GraphQLMissingTypeFix(typeName, GraphQLTypeKind.ENUM));
            fixes.add(new GraphQLMissingTypeFix(typeName, GraphQLTypeKind.SCALAR));
            fixes.add(new GraphQLMissingTypeFix(typeName, GraphQLTypeKind.UNION));
        }
        return fixes;
    }
}
