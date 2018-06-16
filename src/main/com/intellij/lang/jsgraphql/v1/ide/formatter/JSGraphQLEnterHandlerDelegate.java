/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.formatter;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.jsgraphql.v1.ide.injection.JSGraphQLLanguageInjectionUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;

/**
 * - Indents inserted line on enter key inside injected GraphQL psi elements
 */
public class JSGraphQLEnterHandlerDelegate extends EnterHandlerDelegateAdapter {

    private final static Logger log = Logger.getInstance(JSGraphQLEnterHandlerDelegate.class);

    @Override
    public Result preprocessEnter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull Ref<Integer> caretOffsetRef, @NotNull Ref<Integer> caretAdvance, @NotNull DataContext dataContext, EditorActionHandler originalHandler) {

        try {

            // only process if we're inside a JSFile that has GraphQL language injection on the current psi element
            boolean doProcess = false;
            boolean injected = false;
            if(file instanceof JSFile) {
                Integer caretOffset = caretOffsetRef.get();
                final PsiElement psiAtOffset = PsiUtilCore.getElementAtOffset(file, caretOffset);
                PsiElement psiToCheck = psiAtOffset;
                while(psiToCheck != null) {
                    if (JSGraphQLLanguageInjectionUtil.isJSGraphQLLanguageInjectionTarget(psiToCheck)) {
                        doProcess = true;
                        injected = true;
                        break;
                    }
                    psiToCheck = psiToCheck.getParent();
                }
            }

            if(doProcess) {

                // based on EnterBetweenBracesHandler
                Document document = editor.getDocument();
                CharSequence text = document.getCharsSequence();
                int caretOffset = caretOffsetRef.get();
                if (!CodeInsightSettings.getInstance().SMART_INDENT_ON_ENTER) {
                    return Result.Continue;
                }

                int prevCharOffset = CharArrayUtil.shiftBackward(text, caretOffset - 1, " \t");
                int nextCharOffset = CharArrayUtil.shiftForward(text, caretOffset, " \t");
                boolean withinText = prevCharOffset >= 0 && text.length() > nextCharOffset;

                if(withinText) {
                    final char c1 = text.charAt(prevCharOffset);
                    final char c2 = text.charAt(nextCharOffset);
                    if(isBracePair(c1, c2) || (injected && isBackTickPair(c1, c2))) {
                        originalHandler.execute(editor, editor.getCaretModel().getCurrentCaret(), dataContext);
                        PsiDocumentManager.getInstance(file.getProject()).commitDocument(document);
                        try {
                            CodeStyleManager.getInstance(file.getProject()).adjustLineIndent(file, editor.getCaretModel().getOffset());
                        } catch (IncorrectOperationException e) {
                            log.error(e);
                        }
                    }
                }

            }

            return Result.Continue;

        } catch (Throwable e) {
            log.error("Unable to apply enter action", e);
        }
        return Result.Continue;
    }

    protected boolean isBracePair(char c1, char c2) {
        return (c1 == '(' && c2 == ')') || (c1 == '{' && c2 == '}');
    }

    protected boolean isBackTickPair(char c1, char c2) {
        return (c1 == '`' && c2 == '`');
    }
}
