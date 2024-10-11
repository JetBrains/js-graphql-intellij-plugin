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
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.util.TraversalControl;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyList;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyMap;
import static com.intellij.lang.jsgraphql.types.language.NodeChildrenContainer.newNodeChildrenContainer;

@PublicApi
public class NonNullType extends AbstractNode<NonNullType> implements Type<NonNullType> {

  private final Type type;

  public static final String CHILD_TYPE = "type";

  @Internal
  protected NonNullType(Type type,
                        SourceLocation sourceLocation,
                        List<Comment> comments,
                        IgnoredChars ignoredChars,
                        Map<String, String> additionalData,
                        @Nullable List<? extends Node> sourceNodes) {
    super(sourceLocation, comments, ignoredChars, additionalData, sourceNodes);
    this.type = type;
  }

  /**
   * alternative to using a Builder for convenience
   *
   * @param type the wrapped type
   */
  public NonNullType(Type type) {
    this(type, null, emptyList(), IgnoredChars.EMPTY, emptyMap(), null);
  }

  public Type getType() {
    return type;
  }

  @Override
  public List<Node> getChildren() {
    return ImmutableList.of(type);
  }

  @Override
  public NodeChildrenContainer getNamedChildren() {
    return newNodeChildrenContainer()
      .child(CHILD_TYPE, type)
      .build();
  }

  @Override
  public NonNullType withNewChildren(NodeChildrenContainer newChildren) {
    return transform(builder -> builder
      .type((Type)newChildren.getChildOrNull(CHILD_TYPE))
    );
  }

  @Override
  public boolean isEqualTo(Node o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    return true;
  }

  @Override
  public NonNullType deepCopy() {
    return new NonNullType(deepCopy(type), getSourceLocation(), getComments(), getIgnoredChars(), getAdditionalData(),
                           getSourceNodes());
  }

  @Override
  public String toString() {
    return "NonNullType{" +
           "type=" + type +
           '}';
  }

  @Override
  public TraversalControl accept(TraverserContext<Node> context, NodeVisitor visitor) {
    return visitor.visitNonNullType(this, context);
  }

  public static Builder newNonNullType() {
    return new Builder();
  }

  public static Builder newNonNullType(Type type) {
    return new Builder().type(type);
  }

  public NonNullType transform(Consumer<Builder> builderConsumer) {
    Builder builder = new Builder(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  public static final class Builder implements NodeBuilder {
    private SourceLocation sourceLocation;
    private Type type;
    private ImmutableList<Comment> comments = emptyList();
    private IgnoredChars ignoredChars = IgnoredChars.EMPTY;
    private Map<String, String> additionalData = new LinkedHashMap<>();
    private @Nullable List<? extends Node> sourceNodes;

    private Builder() {
    }

    private Builder(NonNullType existing) {
      this.sourceLocation = existing.getSourceLocation();
      this.comments = ImmutableList.copyOf(existing.getComments());
      this.type = existing.getType();
      this.ignoredChars = existing.getIgnoredChars();
      this.additionalData = new LinkedHashMap<>(existing.getAdditionalData());
      this.sourceNodes = existing.getSourceNodes();
    }


    public Builder sourceLocation(SourceLocation sourceLocation) {
      this.sourceLocation = sourceLocation;
      return this;
    }

    public Builder type(ListType type) {
      this.type = type;
      return this;
    }

    public Builder type(TypeName type) {
      this.type = type;
      return this;
    }

    public Builder type(Type type) {
      if (!(type instanceof ListType) && !(type instanceof TypeName)) {
        throw new IllegalArgumentException("unexpected type");
      }
      this.type = type;
      return this;
    }

    public Builder comments(List<Comment> comments) {
      this.comments = ImmutableList.copyOf(comments);
      return this;
    }

    public Builder ignoredChars(IgnoredChars ignoredChars) {
      this.ignoredChars = ignoredChars;
      return this;
    }

    public Builder additionalData(Map<String, String> additionalData) {
      this.additionalData = assertNotNull(additionalData);
      return this;
    }

    public Builder additionalData(String key, String value) {
      this.additionalData.put(key, value);
      return this;
    }

    public Builder sourceNodes(@Nullable List<? extends Node> sourceNodes) {
      this.sourceNodes = sourceNodes;
      return this;
    }


    public NonNullType build() {
      return new NonNullType(type, sourceLocation, comments, ignoredChars, additionalData, sourceNodes);
    }
  }
}
