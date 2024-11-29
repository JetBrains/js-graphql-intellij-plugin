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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyList;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyMap;
import static com.intellij.lang.jsgraphql.types.language.NodeChildrenContainer.newNodeChildrenContainer;
import static com.intellij.lang.jsgraphql.types.language.NodeUtil.argumentsByName;

@PublicApi
public class Directive extends AbstractNode<Directive> implements NamedNode<Directive> {
  private final String name;
  private final ImmutableList<Argument> arguments;

  public static final String CHILD_ARGUMENTS = "arguments";

  @Internal
  protected Directive(String name,
                      List<Argument> arguments,
                      SourceLocation sourceLocation,
                      List<Comment> comments,
                      IgnoredChars ignoredChars,
                      Map<String, String> additionalData,
                      @Nullable List<? extends Node> sourceNodes) {
    super(sourceLocation, comments, ignoredChars, additionalData, sourceNodes);
    this.name = name;
    this.arguments = ImmutableList.copyOf(arguments);
  }

  /**
   * alternative to using a Builder for convenience
   *
   * @param name      of the directive
   * @param arguments of the directive
   */
  public Directive(String name, List<Argument> arguments) {
    this(name, arguments, null, emptyList(), IgnoredChars.EMPTY, emptyMap(), null);
  }


  /**
   * alternative to using a Builder for convenience
   *
   * @param name of the directive
   */
  public Directive(String name) {
    this(name, emptyList(), null, emptyList(), IgnoredChars.EMPTY, emptyMap(), null);
  }

  public List<Argument> getArguments() {
    return arguments;
  }

  public Map<String, Argument> getArgumentsByName() {
    // the spec says that args MUST be unique within context
    return argumentsByName(arguments);
  }

  public Argument getArgument(String argumentName) {
    return NodeUtil.findNodeByName(arguments, argumentName);
  }

  @Override
  public String getName() {
    return name;
  }


  @Override
  public List<Node> getChildren() {
    return ImmutableList.copyOf(arguments);
  }

  @Override
  public NodeChildrenContainer getNamedChildren() {
    return newNodeChildrenContainer()
      .children(CHILD_ARGUMENTS, arguments)
      .build();
  }

  @Override
  public Directive withNewChildren(NodeChildrenContainer newChildren) {
    return transform(builder -> builder
      .arguments(newChildren.getChildren(CHILD_ARGUMENTS))
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

    Directive that = (Directive)o;

    return Objects.equals(this.name, that.name);
  }

  @Override
  public Directive deepCopy() {
    return new Directive(name, deepCopy(arguments), getSourceLocation(), getComments(), getIgnoredChars(), getAdditionalData(),
                         getSourceNodes());
  }

  @Override
  public String toString() {
    return "Directive{" +
           "name='" + name + '\'' +
           ", arguments=" + arguments +
           '}';
  }

  @Override
  public TraversalControl accept(TraverserContext<Node> context, NodeVisitor visitor) {
    return visitor.visitDirective(this, context);
  }

  public static Builder newDirective() {
    return new Builder();
  }

  public Directive transform(Consumer<Builder> builderConsumer) {
    Builder builder = new Builder(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  public static final class Builder implements NodeBuilder {
    private SourceLocation sourceLocation;
    private ImmutableList<Comment> comments = emptyList();
    private String name;
    private ImmutableList<Argument> arguments = emptyList();
    private IgnoredChars ignoredChars = IgnoredChars.EMPTY;
    private Map<String, String> additionalData = new LinkedHashMap<>();
    private @Nullable List<? extends Node> sourceNodes;

    private Builder() {
    }

    private Builder(Directive existing) {
      this.sourceLocation = existing.getSourceLocation();
      this.comments = ImmutableList.copyOf(existing.getComments());
      this.name = existing.getName();
      this.arguments = ImmutableList.copyOf(existing.getArguments());
      this.ignoredChars = existing.getIgnoredChars();
      this.additionalData = new LinkedHashMap<>(existing.getAdditionalData());
      this.sourceNodes = existing.getSourceNodes();
    }


    @Override
    public Builder sourceLocation(SourceLocation sourceLocation) {
      this.sourceLocation = sourceLocation;
      return this;
    }

    @Override
    public Builder comments(List<Comment> comments) {
      this.comments = ImmutableList.copyOf(comments);
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder arguments(List<Argument> arguments) {
      this.arguments = ImmutableList.copyOf(arguments);
      return this;
    }

    public Builder argument(Argument argument) {
      this.arguments = ImmutableKit.addToList(arguments, argument);
      return this;
    }

    @Override
    public Builder ignoredChars(IgnoredChars ignoredChars) {
      this.ignoredChars = ignoredChars;
      return this;
    }

    @Override
    public Builder additionalData(Map<String, String> additionalData) {
      this.additionalData = assertNotNull(additionalData);
      return this;
    }

    @Override
    public Builder additionalData(String key, String value) {
      this.additionalData.put(key, value);
      return this;
    }

    @Override
    public Builder sourceNodes(@Nullable List<? extends Node> sourceNodes) {
      this.sourceNodes = sourceNodes;
      return this;
    }


    public Directive build() {
      return new Directive(name, arguments, sourceLocation, comments, ignoredChars, additionalData, sourceNodes);
    }
  }
}
