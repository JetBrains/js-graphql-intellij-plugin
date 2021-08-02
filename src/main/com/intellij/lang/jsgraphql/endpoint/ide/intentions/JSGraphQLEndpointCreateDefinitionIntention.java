/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.intentions;

import com.google.common.collect.Sets;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.ide.DataManager;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.lang.jsgraphql.endpoint.doc.psi.JSGraphQLEndpointDocPsiUtil;
import com.intellij.lang.jsgraphql.endpoint.psi.*;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class JSGraphQLEndpointCreateDefinitionIntention extends PsiElementBaseIntentionAction {

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }

    protected abstract @NotNull IElementType getSupportedDefinitionType();

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {

        final JSGraphQLEndpointNamedTypePsiElement unknownNamedType = getUnknownNamedType(element);
        if (unknownNamedType != null) {
            JSGraphQLEndpointNamedTypeDefinition definition = PsiTreeUtil.getParentOfType(element, JSGraphQLEndpointNamedTypeDefinition.class);
            if (definition == null) {
                // nearest type before cursor if not inside a type definition
                definition = PsiTreeUtil.getPrevSiblingOfType(unknownNamedType, JSGraphQLEndpointNamedTypeDefinition.class);
            }
            if (definition != null) {
                final IElementType type = getSupportedDefinitionType();
                final String definitionText;
                final boolean insertBefore = (type == JSGraphQLEndpointTokenTypes.INPUT);
                Ref<Integer> caretOffsetAfterInsert = new Ref<>();
                boolean indent = false;
                if (type == JSGraphQLEndpointTokenTypes.UNION) {
                    definitionText = "\n\nunion " + unknownNamedType.getText() + " = \n";
                    caretOffsetAfterInsert.set(definitionText.length() - 1);
                } else if (type == JSGraphQLEndpointTokenTypes.SCALAR) {
                    definitionText = "\n\nscalar " + unknownNamedType.getText() + "\n";
                } else {
                    // all other types are <name> { ... }
                    final String beforeLines = insertBefore ? "" : "\n\n";
                    final String afterLines = insertBefore ? "\n\n" : "\n";
                    final int caretOffsetDelta = insertBefore ? 4 : 3; // we want the caret to be placed before closing '}' and the trailing newlines
                    definitionText = beforeLines + type.toString().toLowerCase() + " " + unknownNamedType.getText() + " {\n\n}" + afterLines;
                    caretOffsetAfterInsert.set(definitionText.length() - caretOffsetDelta);
                    indent = true;
                }
                final Document document = editor.getDocument();
                final int insertOffset;
                if (insertBefore) {
                    final PsiComment documentationStartElement = JSGraphQLEndpointDocPsiUtil.getDocumentationStartElement(definition);
                    if (documentationStartElement != null) {
                        insertOffset = documentationStartElement.getTextRange().getStartOffset();
                    } else {
                        insertOffset = definition.getTextRange().getStartOffset();
                    }
                } else {
                    insertOffset = definition.getTextRange().getEndOffset();
                }
                document.insertString(insertOffset, definitionText);
                if (caretOffsetAfterInsert.get() != null) {
                    // move caret to new position
                    PsiDocumentManager.getInstance(element.getProject()).commitDocument(document);
                    editor.getCaretModel().moveToOffset(insertOffset + caretOffsetAfterInsert.get());
                    if (indent) {
                        AnAction editorLineEnd = ActionManager.getInstance().getAction("EditorLineEnd");
                        if (editorLineEnd != null) {
                            final AnActionEvent actionEvent = AnActionEvent.createFromDataContext(
                                "GraphQLEndpointCreateDefinition",
                                null,
                                DataManager.getInstance().getDataContext(editor.getComponent())
                            );
                            editorLineEnd.actionPerformed(actionEvent);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        final JSGraphQLEndpointNamedTypePsiElement unknownNamedType = getUnknownNamedType(element);
        if (unknownNamedType != null) {
            final Set<IElementType> definitionTypes = Sets.newHashSet();
            final JSGraphQLEndpointArgumentsDefinition arguments = PsiTreeUtil.getParentOfType(unknownNamedType, JSGraphQLEndpointArgumentsDefinition.class);
            if (arguments != null || PsiTreeUtil.getParentOfType(unknownNamedType, JSGraphQLEndpointInputObjectTypeDefinition.class) != null) {
                // field arguments or inside input type
                definitionTypes.add(JSGraphQLEndpointTokenTypes.INPUT);
                definitionTypes.add(JSGraphQLEndpointTokenTypes.ENUM);
                definitionTypes.add(JSGraphQLEndpointTokenTypes.SCALAR);
            } else if (PsiTreeUtil.getParentOfType(unknownNamedType, JSGraphQLEndpointImplementsInterfaces.class) != null) {
                // inside 'implements' so must be an interface
                definitionTypes.add(JSGraphQLEndpointTokenTypes.INTERFACE);
            } else {
                definitionTypes.add(JSGraphQLEndpointTokenTypes.TYPE);
                definitionTypes.add(JSGraphQLEndpointTokenTypes.INTERFACE);
                definitionTypes.add(JSGraphQLEndpointTokenTypes.UNION);
                definitionTypes.add(JSGraphQLEndpointTokenTypes.ENUM);
                definitionTypes.add(JSGraphQLEndpointTokenTypes.SCALAR);
            }
            return definitionTypes.contains(getSupportedDefinitionType());
        }
        return false;
    }

    private JSGraphQLEndpointNamedTypePsiElement getUnknownNamedType(PsiElement element) {
        if (element instanceof PsiWhiteSpace || element.getNode().getElementType() == JSGraphQLEndpointTokenTypes.RPAREN) {
            // lean left in case there's a quick-fix possibility before the cursor
            element = PsiTreeUtil.prevVisibleLeaf(element);
        }
        if (element != null && element.getParent() instanceof JSGraphQLEndpointNamedTypePsiElement) {
            PsiReference reference = element.getParent().getReference();
            if (reference != null && reference.resolve() == null) {
                return (JSGraphQLEndpointNamedTypePsiElement) element.getParent();
            }
        }
        return null;
    }
}
