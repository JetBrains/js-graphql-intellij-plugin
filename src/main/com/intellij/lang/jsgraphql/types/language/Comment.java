package com.intellij.lang.jsgraphql.types.language;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

@PublicApi
public class Comment implements Serializable {
    public final String content;
    public final SourceLocation sourceLocation;
    private final @Nullable PsiElement element;

    public Comment(String content, SourceLocation sourceLocation) {
        this(content, sourceLocation, null);
    }

    public Comment(String content, SourceLocation sourceLocation, @Nullable PsiElement element) {
        this.content = content;
        this.sourceLocation = sourceLocation;
        this.element = element;
    }

    public String getContent() {
        return content;
    }

    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public @Nullable PsiElement getElement() {
        return element;
    }
}
