package com.intellij.lang.jsgraphql.types.language;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@PublicApi
public abstract class AbstractDescribedNode<T extends Node> extends AbstractNode<T> implements DescribedNode<T> {

    protected Description description;

    public AbstractDescribedNode(SourceLocation sourceLocation,
                                 List<Comment> comments,
                                 IgnoredChars ignoredChars,
                                 Map<String, String> additionalData,
                                 Description description,
                                 @Nullable PsiElement element,
                                 @Nullable List<? extends Node> sourceNodes) {
        super(sourceLocation, comments, ignoredChars, additionalData, element, sourceNodes);
        this.description = description;
    }

    @Override
    public Description getDescription() {
        return description;
    }
}
