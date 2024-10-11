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

import java.util.*;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyList;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyMap;
import static com.intellij.lang.jsgraphql.types.language.NodeChildrenContainer.newNodeChildrenContainer;

@PublicApi
public class InputValueDefinition extends AbstractDescribedNode<InputValueDefinition>
  implements DirectivesContainer<InputValueDefinition>, NamedNode<InputValueDefinition> {
  private final String name;
  private final Type type;
  private final Value defaultValue;
  private final ImmutableList<Directive> directives;

  public static final String CHILD_TYPE = "type";
  public static final String CHILD_DEFAULT_VALUE = "defaultValue";
  public static final String CHILD_DIRECTIVES = "directives";

  @Internal
  protected InputValueDefinition(String name,
                                 Type type,
                                 Value defaultValue,
                                 List<Directive> directives,
                                 Description description,
                                 SourceLocation sourceLocation,
                                 List<Comment> comments,
                                 IgnoredChars ignoredChars,
                                 Map<String, String> additionalData,
                                 @Nullable List<? extends Node> sourceNodes) {
    super(sourceLocation, comments, ignoredChars, additionalData, description, sourceNodes);
    this.name = name;
    this.type = type;
    this.defaultValue = defaultValue;
    this.directives = ImmutableList.copyOf(directives);
  }

  /**
   * alternative to using a Builder for convenience
   *
   * @param name of the input value
   * @param type of the input value
   */
  public InputValueDefinition(String name, Type type) {
    this(name, type, null, emptyList(), null, null, emptyList(), IgnoredChars.EMPTY, emptyMap(), null);
  }

  /**
   * alternative to using a Builder for convenience
   *
   * @param name         of the input value
   * @param type         of the input value
   * @param defaultValue of the input value
   */

  public InputValueDefinition(String name, Type type, Value defaultValue) {
    this(name, type, defaultValue, emptyList(), null, null, emptyList(), IgnoredChars.EMPTY, emptyMap(), null);
  }

  public Type getType() {
    return type;
  }

  @Override
  public String getName() {
    return name;
  }

  public Value getDefaultValue() {
    return defaultValue;
  }

  public List<Directive> getDirectives() {
    return directives;
  }

  @Override
  public List<Node> getChildren() {
    List<Node> result = new ArrayList<>();
    result.add(type);
    if (defaultValue != null) {
      result.add(defaultValue);
    }
    result.addAll(directives);
    return result;
  }

  @Override
  public NodeChildrenContainer getNamedChildren() {
    return newNodeChildrenContainer()
      .child(CHILD_TYPE, type)
      .child(CHILD_DEFAULT_VALUE, defaultValue)
      .children(CHILD_DIRECTIVES, directives)
      .build();
  }

  @Override
  public InputValueDefinition withNewChildren(NodeChildrenContainer newChildren) {
    return transform(builder -> builder
      .type(newChildren.getChildOrNull(CHILD_TYPE))
      .defaultValue(newChildren.getChildOrNull(CHILD_DEFAULT_VALUE))
      .directives(newChildren.getChildren(CHILD_DIRECTIVES))

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

    InputValueDefinition that = (InputValueDefinition)o;

    return Objects.equals(this.name, that.name);
  }

  @Override
  public InputValueDefinition deepCopy() {
    return new InputValueDefinition(name,
                                    deepCopy(type),
                                    deepCopy(defaultValue),
                                    deepCopy(directives),
                                    description,
                                    getSourceLocation(),
                                    getComments(),
                                    getIgnoredChars(),
                                    getAdditionalData(),
                                    getSourceNodes());
  }

  @Override
  public String toString() {
    return "InputValueDefinition{" +
           "name='" + name + '\'' +
           ", type=" + type +
           ", defaultValue=" + defaultValue +
           ", directives=" + directives +
           '}';
  }

  @Override
  public TraversalControl accept(TraverserContext<Node> context, NodeVisitor visitor) {
    return visitor.visitInputValueDefinition(this, context);
  }

  public static Builder newInputValueDefinition() {
    return new Builder();
  }

  public InputValueDefinition transform(Consumer<Builder> builderConsumer) {
    Builder builder = new Builder(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  public static final class Builder implements NodeDirectivesBuilder {
    private SourceLocation sourceLocation;
    private ImmutableList<Comment> comments = emptyList();
    private String name;
    private Type type;
    private Value defaultValue;
    private Description description;
    private ImmutableList<Directive> directives = emptyList();
    private IgnoredChars ignoredChars = IgnoredChars.EMPTY;
    private Map<String, String> additionalData = new LinkedHashMap<>();
    private @Nullable List<? extends Node> sourceNodes;

    private Builder() {
    }

    private Builder(InputValueDefinition existing) {
      this.sourceLocation = existing.getSourceLocation();
      this.comments = ImmutableList.copyOf(existing.getComments());
      this.name = existing.getName();
      this.type = existing.getType();
      this.defaultValue = existing.getDefaultValue();
      this.description = existing.getDescription();
      this.directives = ImmutableList.copyOf(existing.getDirectives());
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

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder type(Type type) {
      this.type = type;
      return this;
    }

    public Builder defaultValue(Value defaultValue) {
      this.defaultValue = defaultValue;
      return this;
    }

    public Builder description(Description description) {
      this.description = description;
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

    public InputValueDefinition build() {
      return new InputValueDefinition(name,
                                      type,
                                      defaultValue,
                                      directives,
                                      description,
                                      sourceLocation,
                                      comments,
                                      ignoredChars,
                                      additionalData,
                                      sourceNodes);
    }
  }
}
