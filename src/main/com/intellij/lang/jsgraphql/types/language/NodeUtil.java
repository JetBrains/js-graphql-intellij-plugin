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
import com.intellij.lang.jsgraphql.types.execution.UnknownOperationException;
import com.intellij.lang.jsgraphql.types.util.FpKit;
import com.intellij.lang.jsgraphql.types.util.NodeLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.intellij.lang.jsgraphql.types.util.FpKit.mergeFirst;

/**
 * Helper class for working with {@link Node}s
 */
@Internal
public class NodeUtil {

  public static boolean isEqualTo(String thisStr, String thatStr) {
    if (null == thisStr) {
      if (null != thatStr) {
        return false;
      }
    }
    else if (!thisStr.equals(thatStr)) {
      return false;
    }
    return true;
  }

  public static <T extends NamedNode<T>> T findNodeByName(List<T> namedNodes, String name) {
    for (T namedNode : namedNodes) {
      if (Objects.equals(namedNode.getName(), name)) {
        return namedNode;
      }
    }
    return null;
  }

  public static Map<String, ImmutableList<Directive>> allDirectivesByName(List<Directive> directives) {
    return FpKit.groupingBy(directives, Directive::getName);
  }

  public static Map<String, Argument> argumentsByName(List<Argument> arguments) {
    return FpKit.getByName(arguments, Argument::getName, mergeFirst());
  }

  public static class GetOperationResult {
    public OperationDefinition operationDefinition;
    public Map<String, FragmentDefinition> fragmentsByName;
  }

  public static Map<String, FragmentDefinition> getFragmentsByName(Document document) {
    Map<String, FragmentDefinition> fragmentsByName = new LinkedHashMap<>();

    for (Definition definition : document.getDefinitions()) {
      if (definition instanceof FragmentDefinition fragmentDefinition) {
        fragmentsByName.put(fragmentDefinition.getName(), fragmentDefinition);
      }
    }
    return fragmentsByName;
  }

  public static GetOperationResult getOperation(Document document, String operationName) {


    Map<String, FragmentDefinition> fragmentsByName = new LinkedHashMap<>();
    Map<String, OperationDefinition> operationsByName = new LinkedHashMap<>();

    for (Definition definition : document.getDefinitions()) {
      if (definition instanceof OperationDefinition operationDefinition) {
        operationsByName.put(operationDefinition.getName(), operationDefinition);
      }
      if (definition instanceof FragmentDefinition fragmentDefinition) {
        fragmentsByName.put(fragmentDefinition.getName(), fragmentDefinition);
      }
    }
    if (operationName == null && operationsByName.size() > 1) {
      throw new UnknownOperationException("Must provide operation name if query contains multiple operations.");
    }
    OperationDefinition operation;

    if (operationName == null || operationName.isEmpty()) {
      operation = operationsByName.values().iterator().next();
    }
    else {
      operation = operationsByName.get(operationName);
    }
    if (operation == null) {
      throw new UnknownOperationException(String.format("Unknown operation named '%s'.", operationName));
    }
    GetOperationResult result = new GetOperationResult();
    result.fragmentsByName = fragmentsByName;
    result.operationDefinition = operation;
    return result;
  }

  public static void assertNewChildrenAreEmpty(NodeChildrenContainer newChildren) {
    if (!newChildren.isEmpty()) {
      throw new IllegalArgumentException("Cannot pass non-empty newChildren to Node that doesn't hold children");
    }
  }

  public static Node removeChild(Node node, NodeLocation childLocationToRemove) {
    NodeChildrenContainer namedChildren = node.getNamedChildren();
    NodeChildrenContainer newChildren =
      namedChildren.transform(builder -> builder.removeChild(childLocationToRemove.getName(), childLocationToRemove.getIndex()));
    return node.withNewChildren(newChildren);
  }
}
