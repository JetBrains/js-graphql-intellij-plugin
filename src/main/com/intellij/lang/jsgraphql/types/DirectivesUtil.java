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
package com.intellij.lang.jsgraphql.types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.DirectiveDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLArgument;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirective;
import com.intellij.lang.jsgraphql.types.util.FpKit;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyList;
import static java.util.stream.Collectors.toList;

@Internal
public class DirectivesUtil {


  public static Map<String, GraphQLDirective> nonRepeatableDirectivesByName(List<GraphQLDirective> directives) {
    // filter the repeatable directives
    List<GraphQLDirective> singletonDirectives = directives.stream()
      .filter(d -> !d.isRepeatable()).collect(Collectors.toList());

    return FpKit.getByName(singletonDirectives, GraphQLDirective::getName);
  }

  public static Map<String, ImmutableList<GraphQLDirective>> allDirectivesByName(List<GraphQLDirective> directives) {

    return ImmutableMap.copyOf(FpKit.groupingBy(directives, GraphQLDirective::getName));
  }

  public static GraphQLDirective nonRepeatedDirectiveByNameWithAssert(Map<String, List<GraphQLDirective>> directives,
                                                                      String directiveName) {
    List<GraphQLDirective> directiveList = directives.get(directiveName);
    if (directiveList == null || directiveList.isEmpty()) {
      return null;
    }
    Assert.assertTrue(isAllNonRepeatable(directiveList),
                      () -> String.format("'%s' is a repeatable directive and you have used a non repeatable access method",
                                          directiveName));
    return directiveList.get(0);
  }

  public static Optional<GraphQLArgument> directiveWithArg(List<GraphQLDirective> directives, String directiveName, String argumentName) {
    GraphQLDirective directive = nonRepeatableDirectivesByName(directives).get(directiveName);
    GraphQLArgument argument = null;
    if (directive != null) {
      argument = directive.getArgument(argumentName);
    }
    return Optional.ofNullable(argument);
  }


  public static boolean isAllNonRepeatable(List<GraphQLDirective> directives) {
    if (directives == null || directives.isEmpty()) {
      return false;
    }
    for (GraphQLDirective graphQLDirective : directives) {
      if (graphQLDirective.isRepeatable()) {
        return false;
      }
    }
    return true;
  }

  public static List<GraphQLDirective> enforceAdd(List<GraphQLDirective> targetList, GraphQLDirective newDirective) {
    assertNotNull(targetList, () -> "directive list can't be null");
    assertNotNull(newDirective, () -> "directive can't be null");

    targetList.add(newDirective);
    return targetList;
  }

  public static List<GraphQLDirective> enforceAddAll(List<GraphQLDirective> targetList, List<GraphQLDirective> newDirectives) {
    assertNotNull(targetList, () -> "directive list can't be null");
    assertNotNull(newDirectives, () -> "directive list can't be null");
    targetList.addAll(newDirectives);
    return targetList;
  }

  public static GraphQLDirective getFirstDirective(String name, Map<String, List<GraphQLDirective>> allDirectivesByName) {
    List<GraphQLDirective> directives = allDirectivesByName.getOrDefault(name, emptyList());
    if (directives.isEmpty()) {
      return null;
    }
    return directives.get(0);
  }

  /**
   * A holder class that breaks a list of directives into maps to be more easily accessible in using classes
   */
  public static class DirectivesHolder {

    private final ImmutableMap<String, List<GraphQLDirective>> allDirectivesByName;
    private final ImmutableMap<String, GraphQLDirective> nonRepeatableDirectivesByName;
    private final List<GraphQLDirective> allDirectives;

    public DirectivesHolder(Collection<GraphQLDirective> allDirectives) {
      this.allDirectives = ImmutableList.copyOf(allDirectives);
      this.allDirectivesByName = ImmutableMap.copyOf(FpKit.groupingBy(allDirectives, GraphQLDirective::getName));
      // filter out the repeatable directives
      List<GraphQLDirective> nonRepeatableDirectives = allDirectives.stream()
        .filter(d -> !d.isRepeatable()).collect(Collectors.toList());
      this.nonRepeatableDirectivesByName = ImmutableMap.copyOf(FpKit.getByName(nonRepeatableDirectives, GraphQLDirective::getName));
    }

    public ImmutableMap<String, List<GraphQLDirective>> getAllDirectivesByName() {
      return allDirectivesByName;
    }

    public ImmutableMap<String, GraphQLDirective> getDirectivesByName() {
      return nonRepeatableDirectivesByName;
    }

    public List<GraphQLDirective> getDirectives() {
      return allDirectives;
    }

    public GraphQLDirective getDirective(String directiveName) {
      List<GraphQLDirective> directiveList = allDirectivesByName.get(directiveName);
      if (directiveList == null || directiveList.isEmpty()) {
        return null;
      }
      Assert.assertTrue(isAllNonRepeatable(directiveList),
                        () -> String.format("'%s' is a repeatable directive and you have used a non repeatable access method",
                                            directiveName));
      return directiveList.get(0);
    }

    public List<GraphQLDirective> getDirectives(String directiveName) {
      return allDirectivesByName.getOrDefault(directiveName, emptyList());
    }
  }

  public static List<Directive> nonRepeatableDirectivesOnly(Map<String, DirectiveDefinition> directiveDefinitionMap,
                                                            List<Directive> directives) {
    return directives.stream().filter(directive -> {
      String directiveName = directive.getName();
      DirectiveDefinition directiveDefinition = directiveDefinitionMap.get(directiveName);
      return directiveDefinition == null || !directiveDefinition.isRepeatable();
    }).collect(toList());
  }
}
