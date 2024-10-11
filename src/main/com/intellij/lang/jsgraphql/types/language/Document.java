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
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyList;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyMap;
import static com.intellij.lang.jsgraphql.types.language.NodeChildrenContainer.newNodeChildrenContainer;

@PublicApi
public class Document extends AbstractNode<Document> {

  private final ImmutableList<Definition> definitions;

  public static final String CHILD_DEFINITIONS = "definitions";

  @Internal
  protected Document(List<Definition> definitions,
                     SourceLocation sourceLocation,
                     List<Comment> comments,
                     IgnoredChars ignoredChars,
                     Map<String, String> additionalData,
                     @Nullable List<? extends Node> sourceNodes) {
    super(sourceLocation, comments, ignoredChars, additionalData, sourceNodes);
    this.definitions = ImmutableList.copyOf(definitions);
  }

  /**
   * alternative to using a Builder for convenience
   *
   * @param definitions the definitions that make up this document
   */
  public Document(List<Definition> definitions) {
    this(definitions, null, emptyList(), IgnoredChars.EMPTY, emptyMap(), null);
  }

  public List<Definition> getDefinitions() {
    return definitions;
  }

  /**
   * Returns a list of definitions of the specific type.  It uses {@link Class#isAssignableFrom(Class)} for the test
   *
   * @param definitionClass the definition class
   * @param <T>             the type of definition
   * @return a list of definitions of that class or empty list
   */
  public <T extends Definition> List<T> getDefinitionsOfType(Class<T> definitionClass) {
    return definitions.stream()
      .filter(d -> definitionClass.isAssignableFrom(d.getClass()))
      .map(definitionClass::cast)
      .collect(ImmutableList.toImmutableList());
  }

  @Override
  public List<Node> getChildren() {
    return ImmutableList.copyOf(definitions);
  }

  @Override
  public NodeChildrenContainer getNamedChildren() {
    return newNodeChildrenContainer()
      .children(CHILD_DEFINITIONS, definitions)
      .build();
  }

  @Override
  public Document withNewChildren(NodeChildrenContainer newChildren) {
    return transform(builder -> builder
      .definitions(newChildren.getChildren(CHILD_DEFINITIONS))
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
  public Document deepCopy() {
    return new Document(deepCopy(definitions), getSourceLocation(), getComments(), getIgnoredChars(), getAdditionalData(),
                        getSourceNodes());
  }

  @Override
  public String toString() {
    return "Document{" +
           "definitions=" + definitions +
           '}';
  }

  @Override
  public TraversalControl accept(TraverserContext<Node> context, NodeVisitor visitor) {
    return visitor.visitDocument(this, context);
  }

  public static Builder newDocument() {
    return new Builder();
  }

  public Document transform(Consumer<Builder> builderConsumer) {
    Builder builder = new Builder(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  public static final class Builder implements NodeBuilder {
    private ImmutableList<Definition> definitions = emptyList();
    private SourceLocation sourceLocation;
    private ImmutableList<Comment> comments = emptyList();
    private IgnoredChars ignoredChars = IgnoredChars.EMPTY;
    private Map<String, String> additionalData = new LinkedHashMap<>();
    private @Nullable List<? extends Node> sourceNodes;

    private Builder() {
    }

    private Builder(Document existing) {
      this.sourceLocation = existing.getSourceLocation();
      this.comments = ImmutableList.copyOf(existing.getComments());
      this.definitions = ImmutableList.copyOf(existing.getDefinitions());
      this.ignoredChars = existing.getIgnoredChars();
      this.additionalData = new LinkedHashMap<>(existing.getAdditionalData());
      this.sourceNodes = existing.getSourceNodes();
    }

    public Builder definitions(List<Definition> definitions) {
      this.definitions = ImmutableList.copyOf(definitions);
      return this;
    }

    public Builder definition(Definition definition) {
      this.definitions = ImmutableKit.addToList(definitions, definition);
      return this;
    }

    public Builder sourceLocation(SourceLocation sourceLocation) {
      this.sourceLocation = sourceLocation;
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


    public Document build() {
      return new Document(definitions, sourceLocation, comments, ignoredChars, additionalData, sourceNodes);
    }
  }
}
