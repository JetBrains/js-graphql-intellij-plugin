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
import com.intellij.lang.jsgraphql.types.collect.ImmutableKit;
import com.intellij.lang.jsgraphql.types.util.TraversalControl;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyList;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyMap;
import static com.intellij.lang.jsgraphql.types.language.NodeChildrenContainer.newNodeChildrenContainer;

@PublicApi
public class InlineFragment extends AbstractNode<InlineFragment>
  implements Selection<InlineFragment>, SelectionSetContainer<InlineFragment>, DirectivesContainer<InlineFragment> {
  private final @Nullable TypeName typeCondition;
  private final ImmutableList<Directive> directives;
  private final SelectionSet selectionSet;

  public static final String CHILD_TYPE_CONDITION = "typeCondition";
  public static final String CHILD_DIRECTIVES = "directives";
  public static final String CHILD_SELECTION_SET = "selectionSet";

  @Internal
  protected InlineFragment(@Nullable TypeName typeCondition,
                           List<Directive> directives,
                           SelectionSet selectionSet,
                           SourceLocation sourceLocation,
                           List<Comment> comments,
                           IgnoredChars ignoredChars,
                           Map<String, String> additionalData,
                           @Nullable List<? extends Node> sourceNodes) {
    super(sourceLocation, comments, ignoredChars, additionalData, sourceNodes);
    this.typeCondition = typeCondition;
    this.directives = ImmutableList.copyOf(directives);
    this.selectionSet = selectionSet;
  }

  /**
   * alternative to using a Builder for convenience
   *
   * @param typeCondition the type condition of the inline fragment
   */
  public InlineFragment(TypeName typeCondition) {
    this(typeCondition, emptyList(), null, null, emptyList(), IgnoredChars.EMPTY, emptyMap(), null);
  }

  /**
   * alternative to using a Builder for convenience
   *
   * @param typeCondition the type condition of the inline fragment
   * @param selectionSet  of the inline fragment
   */
  public InlineFragment(TypeName typeCondition, SelectionSet selectionSet) {
    this(typeCondition, emptyList(), selectionSet, null, emptyList(), IgnoredChars.EMPTY, emptyMap(), null);
  }

  public @Nullable TypeName getTypeCondition() {
    return typeCondition;
  }

  public List<Directive> getDirectives() {
    return directives;
  }

  @Override
  public SelectionSet getSelectionSet() {
    return selectionSet;
  }

  @Override
  public List<Node> getChildren() {
    List<Node> result = new ArrayList<>();
    if (typeCondition != null) {
      result.add(typeCondition);
    }
    result.addAll(directives);
    result.add(selectionSet);
    return result;
  }

  @Override
  public NodeChildrenContainer getNamedChildren() {
    return newNodeChildrenContainer()
      .child(CHILD_TYPE_CONDITION, typeCondition)
      .children(CHILD_DIRECTIVES, directives)
      .child(CHILD_SELECTION_SET, selectionSet)
      .build();
  }

  @Override
  public InlineFragment withNewChildren(NodeChildrenContainer newChildren) {
    return transform(builder -> builder
      .typeCondition(newChildren.getChildOrNull(CHILD_TYPE_CONDITION))
      .directives(newChildren.getChildren(CHILD_DIRECTIVES))
      .selectionSet(newChildren.getChildOrNull(CHILD_SELECTION_SET))
    );
  }

  @Override
  public boolean isEqualTo(Node o) {
    if (this == o) {
      return true;
    }
    return o != null && getClass() == o.getClass();
  }

  @Override
  public InlineFragment deepCopy() {
    return new InlineFragment(
      deepCopy(typeCondition),
      deepCopy(directives),
      deepCopy(selectionSet),
      getSourceLocation(),
      getComments(),
      getIgnoredChars(),
      getAdditionalData(),
      getSourceNodes());
  }

  @Override
  public String toString() {
    return "InlineFragment{" +
           "typeCondition='" + typeCondition + '\'' +
           ", directives=" + directives +
           ", selectionSet=" + selectionSet +
           '}';
  }

  @Override
  public TraversalControl accept(TraverserContext<Node> context, NodeVisitor visitor) {
    return visitor.visitInlineFragment(this, context);
  }

  public static Builder newInlineFragment() {
    return new Builder();
  }

  public InlineFragment transform(Consumer<Builder> builderConsumer) {
    Builder builder = new Builder(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  public static final class Builder implements NodeDirectivesBuilder {
    private SourceLocation sourceLocation;
    private ImmutableList<Comment> comments = emptyList();
    private TypeName typeCondition;
    private ImmutableList<Directive> directives = emptyList();
    private SelectionSet selectionSet;
    private IgnoredChars ignoredChars = IgnoredChars.EMPTY;
    private Map<String, String> additionalData = new LinkedHashMap<>();
    private @Nullable List<? extends Node> sourceNodes;

    private Builder() {
    }


    private Builder(InlineFragment existing) {
      this.sourceLocation = existing.getSourceLocation();
      this.comments = ImmutableList.copyOf(existing.getComments());
      this.typeCondition = existing.getTypeCondition();
      this.directives = ImmutableList.copyOf(existing.getDirectives());
      this.selectionSet = existing.getSelectionSet();
      this.ignoredChars = existing.getIgnoredChars();
      this.additionalData = new LinkedHashMap<>(existing.getAdditionalData());
      this.sourceNodes = existing.getSourceNodes();
    }


    public Builder sourceLocation(SourceLocation sourceLocation) {
      this.sourceLocation = sourceLocation;
      return this;
    }

    public Builder comments(List<Comment> comments) {
      this.comments = ImmutableList.copyOf(comments);
      return this;
    }

    public Builder typeCondition(@Nullable TypeName typeCondition) {
      this.typeCondition = typeCondition;
      return this;
    }

    @Override
    public Builder directives(List<Directive> directives) {
      this.directives = ImmutableList.copyOf(directives);
      return this;
    }

    public Builder directive(Directive directive) {
      this.directives = ImmutableKit.addToList(directives, directive);
      return this;
    }


    public Builder selectionSet(SelectionSet selectionSet) {
      this.selectionSet = selectionSet;
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

    public InlineFragment build() {
      return new InlineFragment(typeCondition, directives, selectionSet, sourceLocation, comments, ignoredChars, additionalData,
                                sourceNodes);
    }
  }
}
