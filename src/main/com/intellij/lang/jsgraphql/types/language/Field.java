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
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.collect.ImmutableKit;
import com.intellij.lang.jsgraphql.types.util.TraversalControl;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableMap.copyOf;
import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.*;

/*
 * This is provided to a DataFetcher, therefore it is a public API.
 * This might change in the future.
 */
@PublicApi
public class Field extends AbstractNode<Field>
  implements Selection<Field>, SelectionSetContainer<Field>, DirectivesContainer<Field>, NamedNode<Field> {

  private final String name;
  private final String alias;
  private final ImmutableList<Argument> arguments;
  private final ImmutableList<Directive> directives;
  private final SelectionSet selectionSet;

  public static final String CHILD_ARGUMENTS = "arguments";
  public static final String CHILD_DIRECTIVES = "directives";
  public static final String CHILD_SELECTION_SET = "selectionSet";


  @Internal
  protected Field(String name,
                  String alias,
                  List<Argument> arguments,
                  List<Directive> directives,
                  SelectionSet selectionSet,
                  SourceLocation sourceLocation,
                  List<Comment> comments,
                  IgnoredChars ignoredChars,
                  Map<String, String> additionalData,
                  @Nullable List<? extends Node> sourceNodes) {
    super(sourceLocation, comments, ignoredChars, additionalData, sourceNodes);
    this.name = name;
    this.alias = alias;
    this.arguments = ImmutableList.copyOf(arguments);
    this.directives = ImmutableList.copyOf(directives);
    this.selectionSet = selectionSet;
  }


  /**
   * alternative to using a Builder for convenience
   *
   * @param name of the field
   */
  public Field(String name) {
    this(name, null, emptyList(), emptyList(), null, null, emptyList(), IgnoredChars.EMPTY, emptyMap(), null);
  }

  /**
   * alternative to using a Builder for convenience
   *
   * @param name      of the field
   * @param arguments to the field
   */
  public Field(String name, List<Argument> arguments) {
    this(name, null, arguments, emptyList(), null, null, emptyList(), IgnoredChars.EMPTY, emptyMap(), null);
  }

  /**
   * alternative to using a Builder for convenience
   *
   * @param name         of the field
   * @param arguments    to the field
   * @param selectionSet of the field
   */
  public Field(String name,
               List<Argument> arguments,
               SelectionSet selectionSet) {
    this(name, null, arguments, emptyList(), selectionSet, null, emptyList(), IgnoredChars.EMPTY, emptyMap(), null);
  }

  /**
   * alternative to using a Builder for convenience
   *
   * @param name         of the field
   * @param selectionSet of the field
   */
  public Field(String name, SelectionSet selectionSet) {
    this(name, null, emptyList(), emptyList(), selectionSet, null, emptyList(), IgnoredChars.EMPTY, emptyMap(), null);
  }

  @Override
  public List<Node> getChildren() {
    List<Node> result = new ArrayList<>();
    result.addAll(arguments);
    result.addAll(directives);
    if (selectionSet != null) {
      result.add(selectionSet);
    }
    return result;
  }

  @Override
  public NodeChildrenContainer getNamedChildren() {
    return NodeChildrenContainer.newNodeChildrenContainer()
      .children(CHILD_ARGUMENTS, arguments)
      .children(CHILD_DIRECTIVES, directives)
      .child(CHILD_SELECTION_SET, selectionSet)
      .build();
  }

  @Override
  public Field withNewChildren(NodeChildrenContainer newChildren) {
    return transform(builder ->
                       builder.arguments(newChildren.getChildren(CHILD_ARGUMENTS))
                         .directives(newChildren.getChildren(CHILD_DIRECTIVES))
                         .selectionSet(newChildren.getChildOrNull(CHILD_SELECTION_SET))
    );
  }

  @Override
  public String getName() {
    return name;
  }

  public String getAlias() {
    return alias;
  }

  public String getResultKey() {
    return alias != null ? alias : name;
  }

  public List<Argument> getArguments() {
    return arguments;
  }

  @Override
  public List<Directive> getDirectives() {
    return directives;
  }

  @Override
  public SelectionSet getSelectionSet() {
    return selectionSet;
  }


  @Override
  public boolean isEqualTo(Node o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Field that = (Field)o;

    return Objects.equals(this.name, that.name) && Objects.equals(this.alias, that.alias);
  }

  @Override
  public Field deepCopy() {
    return new Field(name,
                     alias,
                     deepCopy(arguments),
                     deepCopy(directives),
                     deepCopy(selectionSet),
                     getSourceLocation(),
                     getComments(),
                     getIgnoredChars(),
                     getAdditionalData(),
                     getSourceNodes()
    );
  }

  @Override
  public String toString() {
    return "Field{" +
           "name='" + name + '\'' +
           ", alias='" + alias + '\'' +
           ", arguments=" + arguments +
           ", directives=" + directives +
           ", selectionSet=" + selectionSet +
           '}';
  }

  @Override
  public TraversalControl accept(TraverserContext<Node> context, NodeVisitor visitor) {
    return visitor.visitField(this, context);
  }

  public static Builder newField() {
    return new Builder();
  }

  public static Builder newField(String name) {
    return new Builder().name(name);
  }

  public static Builder newField(String name, SelectionSet selectionSet) {
    return new Builder().name(name).selectionSet(selectionSet);
  }

  public Field transform(Consumer<Builder> builderConsumer) {
    Builder builder = new Builder(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  public static final class Builder implements NodeDirectivesBuilder {
    private SourceLocation sourceLocation;
    private ImmutableList<Comment> comments = emptyList();
    private String name;
    private String alias;
    private ImmutableList<Argument> arguments = emptyList();
    private ImmutableList<Directive> directives = emptyList();
    private SelectionSet selectionSet;
    private IgnoredChars ignoredChars = IgnoredChars.EMPTY;
    private ImmutableMap<String, String> additionalData = emptyMap();
    private @Nullable List<? extends Node> sourceNodes;

    private Builder() {
    }

    private Builder(Field existing) {
      this.sourceLocation = existing.getSourceLocation();
      this.comments = ImmutableList.copyOf(existing.getComments());
      this.name = existing.getName();
      this.alias = existing.getAlias();
      this.arguments = ImmutableList.copyOf(existing.getArguments());
      this.directives = ImmutableList.copyOf(existing.getDirectives());
      this.selectionSet = existing.getSelectionSet();
      this.ignoredChars = existing.getIgnoredChars();
      this.additionalData = copyOf(existing.getAdditionalData());
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

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder alias(String alias) {
      this.alias = alias;
      return this;
    }

    public Builder arguments(List<Argument> arguments) {
      this.arguments = ImmutableList.copyOf(arguments);
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
      this.additionalData = ImmutableMap.copyOf(assertNotNull(additionalData));
      return this;
    }

    public Builder additionalData(String key, String value) {
      this.additionalData = addToMap(this.additionalData, key, value);
      return this;
    }

    public Builder sourceNodes(@Nullable List<? extends Node> sourceNodes) {
      this.sourceNodes = sourceNodes;
      return this;
    }


    public Field build() {
      return new Field(name, alias, arguments, directives, selectionSet, sourceLocation, comments, ignoredChars, additionalData,
                       sourceNodes);
    }
  }
}
