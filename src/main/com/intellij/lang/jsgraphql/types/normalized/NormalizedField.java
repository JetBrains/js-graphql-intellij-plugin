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
package com.intellij.lang.jsgraphql.types.normalized;

import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil;
import com.intellij.lang.jsgraphql.types.schema.GraphQLUnmodifiedType;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.simplePrint;

@Internal
public class NormalizedField {
  private final String alias;
  private final Map<String, Object> arguments;
  private final GraphQLObjectType objectType;
  private final GraphQLFieldDefinition fieldDefinition;
  private final List<NormalizedField> children;
  private final boolean isConditional;
  private final int level;
  private NormalizedField parent;


  private NormalizedField(Builder builder) {
    this.alias = builder.alias;
    this.arguments = builder.arguments;
    this.objectType = builder.objectType;
    this.fieldDefinition = assertNotNull(builder.fieldDefinition);
    this.children = builder.children;
    this.level = builder.level;
    this.parent = builder.parent;
    // can be null for the top level fields
    if (parent == null) {
      this.isConditional = false;
    }
    else {
      GraphQLUnmodifiedType parentType = GraphQLTypeUtil.unwrapAll(parent.getFieldDefinition().getType());
      this.isConditional = parentType != this.objectType;
    }
  }

  /**
   * All merged fields have the same name.
   * <p>
   * WARNING: This is not always the key in the execution result, because of possible aliases. See {@link #getResultKey()}
   *
   * @return the name of of the merged fields.
   */
  public String getName() {
    return getFieldDefinition().getName();
  }

  /**
   * Returns the key of this MergedFieldWithType for the overall result.
   * This is either an alias or the FieldWTC name.
   *
   * @return the key for this MergedFieldWithType.
   */
  public String getResultKey() {
    if (alias != null) {
      return alias;
    }
    return getName();
  }

  public String getAlias() {
    return alias;
  }

  public boolean isConditional() {
    return isConditional;
  }

  public Map<String, Object> getArguments() {
    return arguments;
  }


  public static Builder newQueryExecutionField() {
    return new Builder();
  }


  public GraphQLFieldDefinition getFieldDefinition() {
    return fieldDefinition;
  }


  public NormalizedField transform(Consumer<Builder> builderConsumer) {
    Builder builder = new Builder(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  public GraphQLObjectType getObjectType() {
    return objectType;
  }

  public String printDetails() {

    StringBuilder result = new StringBuilder();
    if (getAlias() != null) {
      result.append(getAlias()).append(": ");
    }
    return result + objectType.getName() + "." + fieldDefinition.getName() + ": " + simplePrint(fieldDefinition.getType()) +
           " (conditional: " + this.isConditional + ")";
  }

  public String print() {
    StringBuilder result = new StringBuilder();
    result.append("(");
    if (getAlias() != null) {
      result.append(getAlias()).append(":");
    }
    return result + objectType.getName() + "." + fieldDefinition.getName() + ")";
  }

  public String printFullPath() {
    StringBuilder result = new StringBuilder();
    NormalizedField cur = this;
    while (cur != null) {
      if (result.length() > 0) {
        result.insert(0, "/");
      }
      result.insert(0, cur.print());
      cur = cur.getParent();
    }
    return result.toString();
  }

  public List<String> getListOfResultKeys() {
    LinkedList<String> list = new LinkedList<>();
    NormalizedField current = this;
    while (current != null) {
      list.addFirst(current.getResultKey());
      current = current.parent;
    }
    return list;
  }

  public List<NormalizedField> getChildren() {
    return children;
  }

  public int getLevel() {
    return level;
  }

  public NormalizedField getParent() {
    return parent;
  }

  public void replaceParent(NormalizedField newParent) {
    this.parent = newParent;
  }

  public boolean isIntrospectionField() {
    return getFieldDefinition().getName().startsWith("__") || getObjectType().getName().startsWith("__");
  }

  @Override
  public String toString() {
    return "NormalizedField{" +
           objectType.getName() + "." + fieldDefinition.getName() +
           ", alias=" + alias +
           ", level=" + level +
           ", conditional=" + isConditional +
           ", children=" + children.stream().map(NormalizedField::toString).collect(Collectors.joining("\n")) +
           '}';
  }

  public List<NormalizedField> getChildren(int includingRelativeLevel) {
    List<NormalizedField> result = new ArrayList<>();
    Assert.assertTrue(includingRelativeLevel >= 1, () -> "relative level must be >= 1");

    this.getChildren().forEach(child -> {
      traverseImpl(child, result::add, 1, includingRelativeLevel);
    });
    return result;
  }

  public void traverseSubTree(Consumer<NormalizedField> consumer) {
    this.getChildren().forEach(child -> {
      traverseImpl(child, consumer, 1, Integer.MAX_VALUE);
    });
  }

  private void traverseImpl(NormalizedField root,
                            Consumer<NormalizedField> consumer,
                            int curRelativeLevel,
                            int abortAfter) {
    if (curRelativeLevel > abortAfter) {
      return;
    }
    consumer.accept(root);
    root.getChildren().forEach(child -> {
      traverseImpl(child, consumer, curRelativeLevel + 1, abortAfter);
    });
  }

  public static class Builder {
    private GraphQLObjectType objectType;
    private GraphQLFieldDefinition fieldDefinition;
    private List<NormalizedField> children = new ArrayList<>();
    private int level;
    private NormalizedField parent;
    private String alias;
    private Map<String, Object> arguments = Collections.emptyMap();

    private Builder() {

    }

    private Builder(NormalizedField existing) {
      this.alias = existing.alias;
      this.arguments = existing.arguments;
      this.objectType = existing.getObjectType();
      this.fieldDefinition = existing.getFieldDefinition();
      this.children = existing.getChildren();
      this.level = existing.getLevel();
      this.parent = existing.getParent();
    }

    public Builder objectType(GraphQLObjectType objectType) {
      this.objectType = objectType;
      return this;
    }


    public Builder alias(String alias) {
      this.alias = alias;
      return this;
    }

    public Builder arguments(Map<String, Object> arguments) {
      this.arguments = arguments == null ? Collections.emptyMap() : arguments;
      return this;
    }


    public Builder fieldDefinition(GraphQLFieldDefinition fieldDefinition) {
      this.fieldDefinition = fieldDefinition;
      return this;
    }


    public Builder children(List<NormalizedField> children) {
      this.children.clear();
      this.children.addAll(children);
      return this;
    }

    public Builder level(int level) {
      this.level = level;
      return this;
    }

    public Builder parent(NormalizedField parent) {
      this.parent = parent;
      return this;
    }

    public NormalizedField build() {
      return new NormalizedField(this);
    }
  }
}
