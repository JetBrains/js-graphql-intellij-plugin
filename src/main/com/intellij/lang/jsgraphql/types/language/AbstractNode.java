package com.intellij.lang.jsgraphql.types.language;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.map;

@PublicApi
public abstract class AbstractNode<T extends Node> implements Node<T> {

    private final SourceLocation sourceLocation;
    private final ImmutableList<Comment> comments;
    private final IgnoredChars ignoredChars;
    private final ImmutableMap<String, String> additionalData;

    private final @Nullable PsiElement myElement;
    private final @NotNull List<Node> mySourceNodes;

    public AbstractNode(SourceLocation sourceLocation, List<Comment> comments, IgnoredChars ignoredChars) {
        this(sourceLocation, comments, ignoredChars, Collections.emptyMap(), null, Collections.emptyList());
    }

    public AbstractNode(SourceLocation sourceLocation,
                        List<Comment> comments,
                        IgnoredChars ignoredChars,
                        Map<String, String> additionalData,
                        @Nullable PsiElement element,
                        @Nullable List<? extends Node> sourceNodes) {
        Assert.assertNotNull(comments, () -> "comments can't be null");
        Assert.assertNotNull(ignoredChars, () -> "ignoredChars can't be null");
        Assert.assertNotNull(additionalData, () -> "additionalData can't be null");

        this.sourceLocation = sourceLocation;
        this.additionalData = ImmutableMap.copyOf(additionalData);
        this.comments = ImmutableList.copyOf(comments);
        this.ignoredChars = ignoredChars;

        myElement = element;
        mySourceNodes = sourceNodes == null ? Collections.emptyList() : ImmutableList.copyOf(sourceNodes);
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    @Override
    public List<Comment> getComments() {
        return comments;
    }

    @Override
    public IgnoredChars getIgnoredChars() {
        return ignoredChars;
    }


    public Map<String, String> getAdditionalData() {
        return additionalData;
    }

    @SuppressWarnings("unchecked")
    protected <V extends Node> V deepCopy(V nullableObj) {
        if (nullableObj == null) {
            return null;
        }
        return (V) nullableObj.deepCopy();
    }

    @SuppressWarnings("unchecked")
    protected <V extends Node> List<V> deepCopy(List<? extends Node> list) {
        if (list == null) {
            return null;
        }
        return map(list, n -> (V) n.deepCopy());
    }

    @Override
    public @Nullable PsiElement getElement() {
        if (myElement != null) {
            return myElement;
        }

        Node node = ContainerUtil.getFirstItem(getSourceNodes());
        return node != null ? node.getElement() : null;
    }

    @Override
    public @NotNull Set<PsiElement> getElements() {
        Stream<PsiElement> nodesStream = getSourceNodes().stream().map(Node::getElement);
        return Stream.concat(nodesStream, Stream.of(myElement))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    @Override
    public @NotNull List<Node> getSourceNodes() {
        return mySourceNodes;
    }
}