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
import com.intellij.lang.jsgraphql.types.language.FragmentDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;

import java.util.LinkedHashMap;
import java.util.Map;

@Internal
public class FieldCollectorNormalizedQueryParams {
  private final GraphQLSchema graphQLSchema;
  private final Map<String, FragmentDefinition> fragmentsByName;
  private final Map<String, Object> variables;

  public GraphQLSchema getGraphQLSchema() {
    return graphQLSchema;
  }

  public Map<String, FragmentDefinition> getFragmentsByName() {
    return fragmentsByName;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }


  private FieldCollectorNormalizedQueryParams(GraphQLSchema graphQLSchema,
                                              Map<String, Object> variables,
                                              Map<String, FragmentDefinition> fragmentsByName) {
    this.fragmentsByName = fragmentsByName;
    this.graphQLSchema = graphQLSchema;
    this.variables = variables;
  }

  public static Builder newParameters() {
    return new Builder();
  }

  public static class Builder {
    private GraphQLSchema graphQLSchema;
    private final Map<String, FragmentDefinition> fragmentsByName = new LinkedHashMap<>();
    private final Map<String, Object> variables = new LinkedHashMap<>();

    /**
     * @see FieldCollectorNormalizedQueryParams#newParameters()
     */
    private Builder() {

    }

    public Builder schema(GraphQLSchema graphQLSchema) {
      this.graphQLSchema = graphQLSchema;
      return this;
    }

    public Builder fragments(Map<String, FragmentDefinition> fragmentsByName) {
      this.fragmentsByName.putAll(fragmentsByName);
      return this;
    }

    public Builder variables(Map<String, Object> variables) {
      this.variables.putAll(variables);
      return this;
    }

    public FieldCollectorNormalizedQueryParams build() {
      Assert.assertNotNull(graphQLSchema, () -> "You must provide a schema");
      return new FieldCollectorNormalizedQueryParams(graphQLSchema, variables, fragmentsByName);
    }
  }
}
