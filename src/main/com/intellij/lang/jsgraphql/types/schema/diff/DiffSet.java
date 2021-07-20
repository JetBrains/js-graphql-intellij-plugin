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
package com.intellij.lang.jsgraphql.types.schema.diff;

import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.ExecutionResult;
import com.intellij.lang.jsgraphql.types.GraphQL;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.introspection.IntrospectionQuery;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;

import java.util.Map;

/**
 * Represents 2 schemas that can be diffed.  The {@link SchemaDiff} code
 * assumes that that schemas to be diffed are the result of a
 * {@link com.intellij.lang.jsgraphql.types.introspection.IntrospectionQuery}.
 */
@PublicApi
public class DiffSet {

    private final Map<String, Object> introspectionOld;
    private final Map<String, Object> introspectionNew;

    public DiffSet(Map<String, Object> introspectionOld, Map<String, Object> introspectionNew) {
        this.introspectionOld = introspectionOld;
        this.introspectionNew = introspectionNew;
    }

    /**
     * @return the old API as an introspection result
     */
    public Map<String, Object> getOld() {
        return introspectionOld;
    }

    /**
     * @return the new API as an introspection result
     */
    public Map<String, Object> getNew() {
        return introspectionNew;
    }


    /**
     * Creates a diff set out of the result of 2 introspection queries.
     *
     * @param introspectionOld the older introspection query
     * @param introspectionNew the newer introspection query
     *
     * @return a diff set representing them
     */
    public static DiffSet diffSet(Map<String, Object> introspectionOld, Map<String, Object> introspectionNew) {
        return new DiffSet(introspectionOld, introspectionNew);
    }

    /**
     * Creates a diff set out of the result of 2 schemas.
     *
     * @param schemaOld the older schema
     * @param schemaNew the newer schema
     *
     * @return a diff set representing them
     */
    public static DiffSet diffSet(GraphQLSchema schemaOld, GraphQLSchema schemaNew) {
        Map<String, Object> introspectionOld = introspect(schemaOld);
        Map<String, Object> introspectionNew = introspect(schemaNew);
        return diffSet(introspectionOld, introspectionNew);
    }

    private static Map<String, Object> introspect(GraphQLSchema schema) {
        GraphQL gql = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = gql.execute(IntrospectionQuery.INTROSPECTION_QUERY);
        Assert.assertTrue(result.getErrors().size() == 0, () -> "The schema has errors during Introspection");
        return result.getData();
    }
}
