package com.intellij.lang.jsgraphql.types.language;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

@PublicApi
public class Description implements Serializable {
    public final String content;
    public final SourceLocation sourceLocation;
    public final boolean multiLine;
    private @Nullable final PsiElement element;

    public Description(String content, SourceLocation sourceLocation, boolean multiLine) {
        this(content, sourceLocation, multiLine, null);
    }

    public Description(String content, SourceLocation sourceLocation, boolean multiLine, @Nullable PsiElement element) {
        this.content = content;
        this.sourceLocation = sourceLocation;
        this.multiLine = multiLine;
        this.element = element;
    }

    public String getContent() {
        return content;
    }

    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public boolean isMultiLine() {
        return multiLine;
    }

    public @Nullable PsiElement getElement() {
        return element;
    }
}
