/*
    The MIT License (MIT)

    Copyright (c) 2015 Andreas Marek and Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
    (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
    publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
    so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.intellij.lang.jsgraphql.types.language;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.map;

@PublicApi
public abstract class AbstractNode<T extends Node> implements Node<T> {

  private final SourceLocation sourceLocation;
  private final ImmutableList<Comment> comments;
  private final IgnoredChars ignoredChars;
  private final ImmutableMap<String, String> additionalData;

  private final @NotNull List<Node> mySourceNodes;

  public AbstractNode(SourceLocation sourceLocation, List<Comment> comments, IgnoredChars ignoredChars) {
    this(sourceLocation, comments, ignoredChars, Collections.emptyMap(), Collections.emptyList());
  }

  public AbstractNode(SourceLocation sourceLocation,
                      List<Comment> comments,
                      IgnoredChars ignoredChars,
                      Map<String, String> additionalData,
                      @Nullable List<? extends Node> sourceNodes) {
    Assert.assertNotNull(comments, () -> "comments can't be null");
    Assert.assertNotNull(ignoredChars, () -> "ignoredChars can't be null");
    Assert.assertNotNull(additionalData, () -> "additionalData can't be null");

    this.sourceLocation = sourceLocation;
    this.additionalData = ImmutableMap.copyOf(additionalData);
    this.comments = ImmutableList.copyOf(comments);
    this.ignoredChars = ignoredChars;

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
    return (V)nullableObj.deepCopy();
  }

  @SuppressWarnings("unchecked")
  protected <V extends Node> List<V> deepCopy(List<? extends Node> list) {
    if (list == null) {
      return null;
    }
    return map(list, n -> (V)n.deepCopy());
  }

  @Override
  public @Nullable PsiElement getElement() {
    // remains only for binary compatibility, the method will be removed soon
    return null;
  }

  @Override
  public @NotNull List<Node> getSourceNodes() {
    return mySourceNodes;
  }
}
