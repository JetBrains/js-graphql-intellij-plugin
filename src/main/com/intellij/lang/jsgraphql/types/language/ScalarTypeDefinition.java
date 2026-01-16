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

@PublicApi
public class ScalarTypeDefinition extends AbstractDescribedNode<ScalarTypeDefinition>
  implements TypeDefinition<ScalarTypeDefinition>, DirectivesContainer<ScalarTypeDefinition>, NamedNode<ScalarTypeDefinition> {

  private final String name;
  private final ImmutableList<Directive> directives;
  private final @Nullable String specifiedByURL;

  public static final String CHILD_DIRECTIVES = "directives";

  @Internal
  protected ScalarTypeDefinition(String name,
                                 List<Directive> directives,
                                 @Nullable String specifiedByURL,
                                 Description description,
                                 SourceLocation sourceLocation,
                                 List<Comment> comments,
                                 IgnoredChars ignoredChars,
                                 Map<String, String> additionalData,
                                 @Nullable List<? extends Node> sourceNodes) {
    super(sourceLocation, comments, ignoredChars, additionalData, description, sourceNodes);
    this.name = name;
    this.directives = ImmutableList.copyOf(directives);
    this.specifiedByURL = specifiedByURL;
  }

  /**
   * alternative to using a Builder for convenience
   *
   * @param name of the scalar
   */
  public ScalarTypeDefinition(String name) {
    this(name, emptyList(), null, null, null, emptyList(), IgnoredChars.EMPTY, emptyMap(), null);
  }

  @Override
  public List<Directive> getDirectives() {
    return directives;
  }

  @Override
  public String getName() {
    return name;
  }

  @Nullable
  public String getSpecifiedByURL() {
    return specifiedByURL;
  }

  @Override
  public List<Node> getChildren() {
    return ImmutableList.copyOf(directives);
  }

  @Override
  public NodeChildrenContainer getNamedChildren() {
    return newNodeChildrenContainer()
      .children(CHILD_DIRECTIVES, directives)
      .build();
  }

  @Override
  public ScalarTypeDefinition withNewChildren(NodeChildrenContainer newChildren) {
    return transform(builder -> builder
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

    ScalarTypeDefinition that = (ScalarTypeDefinition)o;

    return Objects.equals(this.name, that.name);
  }

  @Override
  public ScalarTypeDefinition deepCopy() {
    return new ScalarTypeDefinition(name, deepCopy(directives), specifiedByURL, description, getSourceLocation(), getComments(), getIgnoredChars(),
                                    getAdditionalData(), getSourceNodes());
  }

  @Override
  public String toString() {
    return "ScalarTypeDefinition{" +
           "name='" + name + '\'' +
           ", directives=" + directives +
           ", specifiedByURL='" + specifiedByURL + '\'' +
           '}';
  }

  @Override
  public TraversalControl accept(TraverserContext<Node> context, NodeVisitor visitor) {
    return visitor.visitScalarTypeDefinition(this, context);
  }

  public static Builder newScalarTypeDefinition() {
    return new Builder();
  }

  public ScalarTypeDefinition transform(Consumer<Builder> builderConsumer) {
    Builder builder = new Builder(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  public static final class Builder implements NodeDirectivesBuilder {
    private SourceLocation sourceLocation;
    private ImmutableList<Comment> comments = emptyList();
    private String name;
    private Description description;
    private ImmutableList<Directive> directives = emptyList();
    private @Nullable String specifiedByURL;
    private IgnoredChars ignoredChars = IgnoredChars.EMPTY;
    private Map<String, String> additionalData = new LinkedHashMap<>();
    private @Nullable List<? extends Node> sourceNodes;

    private Builder() {
    }

    private Builder(ScalarTypeDefinition existing) {
      this.sourceLocation = existing.getSourceLocation();
      this.comments = ImmutableList.copyOf(existing.getComments());
      this.name = existing.getName();
      this.description = existing.getDescription();
      this.directives = ImmutableList.copyOf(existing.getDirectives());
      this.specifiedByURL = existing.getSpecifiedByURL();
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

    public Builder description(Description description) {
      this.description = description;
      return this;
    }

    public Builder specifiedByURL(@Nullable String specifiedByURL) {
      this.specifiedByURL = specifiedByURL;
      return this;
    }

    @Override
    public Builder directives(List<Directive> directives) {
      this.directives = ImmutableList.copyOf(directives);
      return this;
    }

    @Override
    public Builder directive(Directive directive) {
      this.directives = ImmutableKit.addToList(directives, directive);
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

    public ScalarTypeDefinition build() {
      return new ScalarTypeDefinition(name,
                                      directives,
                                      specifiedByURL,
                                      description,
                                      sourceLocation,
                                      comments,
                                      ignoredChars,
                                      additionalData,
                                      sourceNodes);
    }
  }
}
