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
public class ObjectTypeDefinition extends AbstractDescribedNode<ObjectTypeDefinition>
  implements ImplementingTypeDefinition<ObjectTypeDefinition>, DirectivesContainer<ObjectTypeDefinition>, NamedNode<ObjectTypeDefinition> {
  private final String name;
  private final ImmutableList<Type> implementz;
  private final ImmutableList<Directive> directives;
  private final ImmutableList<FieldDefinition> fieldDefinitions;

  public static final String CHILD_IMPLEMENTZ = "implementz";
  public static final String CHILD_DIRECTIVES = "directives";
  public static final String CHILD_FIELD_DEFINITIONS = "fieldDefinitions";

  @Internal
  protected ObjectTypeDefinition(String name,
                                 List<Type> implementz,
                                 List<Directive> directives,
                                 List<FieldDefinition> fieldDefinitions,
                                 Description description,
                                 SourceLocation sourceLocation,
                                 List<Comment> comments,
                                 IgnoredChars ignoredChars,
                                 Map<String, String> additionalData,
                                 @Nullable List<? extends Node> sourceNodes) {
    super(sourceLocation, comments, ignoredChars, additionalData, description, sourceNodes);
    this.name = name;
    this.implementz = ImmutableList.copyOf(implementz);
    this.directives = ImmutableList.copyOf(directives);
    this.fieldDefinitions = ImmutableList.copyOf(fieldDefinitions);
  }

  /**
   * alternative to using a Builder for convenience
   *
   * @param name of the object type
   */
  public ObjectTypeDefinition(String name) {
    this(name, emptyList(), emptyList(), emptyList(), null, null, emptyList(), IgnoredChars.EMPTY, emptyMap(), null);
  }

  @Override
  public List<Type> getImplements() {
    return implementz;
  }

  @Override
  public List<Directive> getDirectives() {
    return directives;
  }

  @Override
  public List<FieldDefinition> getFieldDefinitions() {
    return fieldDefinitions;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public List<Node> getChildren() {
    List<Node> result = new ArrayList<>();
    result.addAll(implementz);
    result.addAll(directives);
    result.addAll(fieldDefinitions);
    return result;
  }

  @Override
  public NodeChildrenContainer getNamedChildren() {
    return newNodeChildrenContainer()
      .children(CHILD_IMPLEMENTZ, implementz)
      .children(CHILD_DIRECTIVES, directives)
      .children(CHILD_FIELD_DEFINITIONS, fieldDefinitions)
      .build();
  }

  @Override
  public ObjectTypeDefinition withNewChildren(NodeChildrenContainer newChildren) {
    return transform(builder -> builder.implementz(newChildren.getChildren(CHILD_IMPLEMENTZ))
      .directives(newChildren.getChildren(CHILD_DIRECTIVES))
      .fieldDefinitions(newChildren.getChildren(CHILD_FIELD_DEFINITIONS)));
  }

  @Override
  public boolean isEqualTo(Node o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ObjectTypeDefinition that = (ObjectTypeDefinition)o;

    return Objects.equals(this.name, that.name);
  }

  @Override
  public ObjectTypeDefinition deepCopy() {
    return new ObjectTypeDefinition(name,
                                    deepCopy(implementz),
                                    deepCopy(directives),
                                    deepCopy(fieldDefinitions),
                                    description,
                                    getSourceLocation(),
                                    getComments(),
                                    getIgnoredChars(),
                                    getAdditionalData(),
                                    getSourceNodes());
  }

  @Override
  public String toString() {
    return "ObjectTypeDefinition{" +
           "name='" + name + '\'' +
           ", implements=" + implementz +
           ", directives=" + directives +
           ", fieldDefinitions=" + fieldDefinitions +
           '}';
  }

  @Override
  public TraversalControl accept(TraverserContext<Node> context, NodeVisitor visitor) {
    return visitor.visitObjectTypeDefinition(this, context);
  }

  public static Builder newObjectTypeDefinition() {
    return new Builder();
  }

  public ObjectTypeDefinition transform(Consumer<Builder> builderConsumer) {
    Builder builder = new Builder(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  public static final class Builder implements NodeDirectivesBuilder {
    private SourceLocation sourceLocation;
    private ImmutableList<Comment> comments = emptyList();
    private String name;
    private Description description;
    private ImmutableList<Type> implementz = emptyList();
    private ImmutableList<Directive> directives = emptyList();
    private ImmutableList<FieldDefinition> fieldDefinitions = emptyList();
    private IgnoredChars ignoredChars = IgnoredChars.EMPTY;
    private Map<String, String> additionalData = new LinkedHashMap<>();
    private @Nullable List<? extends Node> sourceNodes;

    private Builder() {
    }

    private Builder(ObjectTypeDefinition existing) {
      this.sourceLocation = existing.getSourceLocation();
      this.comments = ImmutableList.copyOf(existing.getComments());
      this.name = existing.getName();
      this.description = existing.getDescription();
      this.directives = ImmutableList.copyOf(existing.getDirectives());
      this.implementz = ImmutableList.copyOf(existing.getImplements());
      this.fieldDefinitions = ImmutableList.copyOf(existing.getFieldDefinitions());
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

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder description(Description description) {
      this.description = description;
      return this;
    }

    public Builder implementz(List<Type> implementz) {
      this.implementz = ImmutableList.copyOf(implementz);
      return this;
    }

    public Builder implementz(Type implement) {
      this.implementz = ImmutableKit.addToList(implementz, implement);
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

    public Builder fieldDefinitions(List<FieldDefinition> fieldDefinitions) {
      this.fieldDefinitions = ImmutableList.copyOf(fieldDefinitions);
      return this;
    }

    public Builder fieldDefinition(FieldDefinition fieldDefinition) {
      this.fieldDefinitions = ImmutableKit.addToList(fieldDefinitions, fieldDefinition);
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

    public ObjectTypeDefinition build() {
      return new ObjectTypeDefinition(name,
                                      implementz,
                                      directives,
                                      fieldDefinitions,
                                      description,
                                      sourceLocation,
                                      comments,
                                      ignoredChars,
                                      additionalData,
                                      sourceNodes);
    }
  }
}
