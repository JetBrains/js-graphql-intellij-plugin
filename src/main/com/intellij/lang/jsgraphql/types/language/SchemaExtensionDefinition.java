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
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.collect.ImmutableKit;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyList;

@PublicApi
public class SchemaExtensionDefinition extends SchemaDefinition {

  protected SchemaExtensionDefinition(List<Directive> directives,
                                      List<OperationTypeDefinition> operationTypeDefinitions,
                                      SourceLocation sourceLocation,
                                      List<Comment> comments,
                                      IgnoredChars ignoredChars,
                                      Map<String, String> additionalData,
                                      @Nullable List<? extends Node> sourceNodes) {
    super(directives, operationTypeDefinitions, sourceLocation, comments, ignoredChars, additionalData, null, sourceNodes);
  }

  @Override
  public SchemaExtensionDefinition withNewChildren(NodeChildrenContainer newChildren) {
    return transformExtension(builder -> builder
      .directives(newChildren.getChildren(CHILD_DIRECTIVES))
      .operationTypeDefinitions(newChildren.getChildren(CHILD_OPERATION_TYPE_DEFINITIONS))
    );
  }

  @Override
  public SchemaExtensionDefinition deepCopy() {
    return new SchemaExtensionDefinition(deepCopy(getDirectives()), deepCopy(getOperationTypeDefinitions()), getSourceLocation(),
                                         getComments(),
                                         getIgnoredChars(), getAdditionalData(), getSourceNodes());
  }

  @Override
  public String toString() {
    return "SchemaExtensionDefinition{" +
           "directives=" + getDirectives() +
           ", operationTypeDefinitions=" + getOperationTypeDefinitions() +
           "}";
  }

  public SchemaExtensionDefinition transformExtension(Consumer<Builder> builderConsumer) {
    Builder builder = new Builder(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  public static Builder newSchemaExtensionDefinition() {
    return new Builder();
  }

  public static final class Builder implements NodeDirectivesBuilder {
    private SourceLocation sourceLocation;
    private ImmutableList<Comment> comments = emptyList();
    private ImmutableList<Directive> directives = emptyList();
    private ImmutableList<OperationTypeDefinition> operationTypeDefinitions = emptyList();
    private IgnoredChars ignoredChars = IgnoredChars.EMPTY;
    private Map<String, String> additionalData = new LinkedHashMap<>();
    private @Nullable List<? extends Node> sourceNodes;

    private Builder() {
    }

    private Builder(SchemaDefinition existing) {
      this.sourceLocation = existing.getSourceLocation();
      this.comments = ImmutableList.copyOf(existing.getComments());
      this.directives = ImmutableList.copyOf(existing.getDirectives());
      this.operationTypeDefinitions = ImmutableList.copyOf(existing.getOperationTypeDefinitions());
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

    @Override
    public Builder directives(List<Directive> directives) {
      this.directives = ImmutableList.copyOf(directives);
      return this;
    }

    public Builder directive(Directive directive) {
      this.directives = ImmutableKit.addToList(directives, directive);
      return this;
    }

    public Builder operationTypeDefinitions(List<OperationTypeDefinition> operationTypeDefinitions) {
      this.operationTypeDefinitions = ImmutableList.copyOf(operationTypeDefinitions);
      return this;
    }

    public Builder operationTypeDefinition(OperationTypeDefinition operationTypeDefinition) {
      this.operationTypeDefinitions = ImmutableKit.addToList(operationTypeDefinitions, operationTypeDefinition);
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

    public SchemaExtensionDefinition build() {
      return new SchemaExtensionDefinition(directives,
                                           operationTypeDefinitions,
                                           sourceLocation,
                                           comments,
                                           ignoredChars,
                                           additionalData,
                                           sourceNodes);
    }
  }
}
