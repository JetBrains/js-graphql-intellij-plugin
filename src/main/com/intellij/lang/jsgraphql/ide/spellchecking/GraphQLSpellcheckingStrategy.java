package com.intellij.lang.jsgraphql.ide.spellchecking;

import com.intellij.lang.jsgraphql.psi.GraphQLStringLiteral;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;

public class GraphQLSpellcheckingStrategy extends SpellcheckingStrategy {
    @Override
    public @NotNull Tokenizer getTokenizer(PsiElement element) {
        if (element.getParent() instanceof PsiNameIdentifierOwner) {
            return EMPTY_TOKENIZER;
        }
        if (element instanceof GraphQLStringLiteral) {
            return TEXT_TOKENIZER;
        }
        return super.getTokenizer(element);
    }
}
