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
package com.intellij.lang.jsgraphql.types.execution.directives;

import com.google.common.collect.ImmutableMap;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.execution.ValuesResolver;
import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.schema.GraphQLArgument;
import com.intellij.lang.jsgraphql.types.schema.GraphQLCodeRegistry;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirective;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This turns AST directives into runtime directives with resolved types and so on
 */
@Internal
public class DirectivesResolver {

  private final ValuesResolver valuesResolver = new ValuesResolver();

  public DirectivesResolver() {
  }

  public Map<String, GraphQLDirective> resolveDirectives(List<Directive> directives, GraphQLSchema schema, Map<String, Object> variables) {
    GraphQLCodeRegistry codeRegistry = schema.getCodeRegistry();
    Map<String, GraphQLDirective> directiveMap = new LinkedHashMap<>();
    directives.forEach(directive -> {
      GraphQLDirective protoType = schema.getDirective(directive.getName());
      if (protoType != null) {
        GraphQLDirective newDirective =
          protoType.transform(builder -> buildArguments(builder, codeRegistry, protoType, directive, variables));
        directiveMap.put(newDirective.getName(), newDirective);
      }
    });
    return ImmutableMap.copyOf(directiveMap);
  }

  private void buildArguments(GraphQLDirective.Builder directiveBuilder,
                              GraphQLCodeRegistry codeRegistry,
                              GraphQLDirective protoType,
                              Directive fieldDirective,
                              Map<String, Object> variables) {
    Map<String, Object> argumentValues =
      valuesResolver.getArgumentValues(codeRegistry, protoType.getArguments(), fieldDirective.getArguments(), variables);
    directiveBuilder.clearArguments();
    protoType.getArguments().forEach(protoArg -> {
      if (argumentValues.containsKey(protoArg.getName())) {
        Object argValue = argumentValues.get(protoArg.getName());
        GraphQLArgument newArgument = protoArg.transform(argBuilder -> argBuilder.value(argValue));
        directiveBuilder.argument(newArgument);
      }
      else {
        // this means they can ask for the argument default value because the argument on the directive
        // object is present - but null
        GraphQLArgument newArgument = protoArg.transform(argBuilder -> argBuilder.value(null));
        directiveBuilder.argument(newArgument);
      }
    });
  }
}
