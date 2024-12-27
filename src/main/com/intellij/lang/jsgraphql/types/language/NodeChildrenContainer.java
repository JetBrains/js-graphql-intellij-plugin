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

import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyList;

/**
 * Container of children of a {@link Node}.
 */
@PublicApi
public class NodeChildrenContainer {

  private final Map<String, List<Node>> children = new LinkedHashMap<>();

  private NodeChildrenContainer(Map<String, List<Node>> children) {
    this.children.putAll(assertNotNull(children));
  }

  public <T extends Node> List<T> getChildren(String key) {
    return (List<T>)children.getOrDefault(key, emptyList());
  }

  public <T extends Node> T getChildOrNull(String key) {
    List<? extends Node> result = children.getOrDefault(key, emptyList());
    if (result.size() > 1) {
      throw new IllegalStateException("children " + key + " is not a single value");
    }
    return !result.isEmpty() ? (T)result.get(0) : null;
  }

  public Map<String, List<Node>> getChildren() {
    return new LinkedHashMap<>(children);
  }

  public static Builder newNodeChildrenContainer() {
    return new Builder();
  }

  public static Builder newNodeChildrenContainer(Map<String, ? extends List<? extends Node>> childrenMap) {
    return new Builder().children(childrenMap);
  }

  public static Builder newNodeChildrenContainer(NodeChildrenContainer existing) {
    return new Builder(existing);
  }

  public NodeChildrenContainer transform(Consumer<Builder> builderConsumer) {
    Builder builder = new Builder(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  public boolean isEmpty() {
    return this.children.isEmpty();
  }

  public static class Builder {
    private final Map<String, List<Node>> children = new LinkedHashMap<>();

    private Builder() {

    }

    private Builder(NodeChildrenContainer other) {
      this.children.putAll(other.children);
    }

    public Builder child(String key, Node child) {
      // we allow null here to make the actual nodes easier
      if (child == null) {
        return this;
      }
      children.computeIfAbsent(key, (k) -> new ArrayList<>());
      children.get(key).add(child);
      return this;
    }

    public Builder children(String key, List<? extends Node> children) {
      this.children.computeIfAbsent(key, (k) -> new ArrayList<>());
      this.children.get(key).addAll(children);
      return this;
    }

    public Builder children(Map<String, ? extends List<? extends Node>> children) {
      this.children.clear();
      this.children.putAll((Map<? extends String, ? extends List<Node>>)children);
      return this;
    }

    public Builder replaceChild(String key, int index, Node newChild) {
      assertNotNull(newChild);
      this.children.get(key).set(index, newChild);
      return this;
    }

    public Builder removeChild(String key, int index) {
      this.children.get(key).remove(index);
      return this;
    }

    public NodeChildrenContainer build() {
      return new NodeChildrenContainer(this.children);
    }
  }
}
