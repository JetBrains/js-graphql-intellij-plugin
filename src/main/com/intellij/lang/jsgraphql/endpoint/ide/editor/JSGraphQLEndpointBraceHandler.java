/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.editor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.codeInsight.template.impl.editorActions.TypedActionHandlerBase;
import com.intellij.lang.jsgraphql.JSGraphQLKeywords;
import com.intellij.lang.jsgraphql.JSGraphQLTokenTypes;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointFile;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Automatically inserts a closing '}' when typing '{' and the following tokens represents a schema definition, e.g. type, interface etc.
 */
public class JSGraphQLEndpointBraceHandler extends TypedActionHandlerBase {

	public JSGraphQLEndpointBraceHandler(@Nullable TypedActionHandler originalHandler) {
		super(originalHandler);
	}

	@Override
	public void execute(@NotNull Editor editor, char charTyped, @NotNull DataContext dataContext) {
		if (this.myOriginalHandler != null) {
			this.myOriginalHandler.execute(editor, charTyped, dataContext);
		}
		if (charTyped == '{') {
			final PsiFile psiFile = (PsiFile) dataContext.getData(PlatformDataKeys.PSI_FILE.getName());
			if (psiFile instanceof JSGraphQLEndpointFile) {
				final Caret caret = (Caret) dataContext.getData(PlatformDataKeys.CARET.getName());
				if (caret != null) {
					final PsiElement element = psiFile.findElementAt(caret.getOffset());
					if (element == null) {
						return;
					}
					PsiElement next = PsiTreeUtil.nextVisibleLeaf(element);
					while (next != null) {
						final IElementType elementType = next.getNode().getElementType();
						if (elementType == JSGraphQLTokenTypes.COMMENT || element == JSGraphQLTokenTypes.META) {
							// skip past comments and meta
							next = PsiTreeUtil.nextVisibleLeaf(next);
							continue;
						} else if (elementType == JSGraphQLTokenTypes.LBRACE || elementType == JSGraphQLTokenTypes.RBRACE) {
							return;
						} else {
							switch (next.getText()) {
								case JSGraphQLKeywords.TYPE:
								case JSGraphQLKeywords.INTERFACE:
								case JSGraphQLKeywords.SCALAR:
								case JSGraphQLKeywords.INPUT:
								case JSGraphQLKeywords.UNION:
								case JSGraphQLKeywords.ENUM:
									// next token represents a schema definition, so we should close this opening '{' immediately
									//EditorModificationUtil.insertStringAtCaret(editor, "}", false, false);
									break;
							}
						}
						next = PsiTreeUtil.nextVisibleLeaf(next);
					}
					System.out.println(element);
				}
			}
		}
	}
}
