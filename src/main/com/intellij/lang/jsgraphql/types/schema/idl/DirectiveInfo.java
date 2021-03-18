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
package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.Directives;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirective;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Info on all the directives provided by graphql specification
 */
@PublicApi
public class DirectiveInfo {

    /**
     * A set of directives which provided by graphql specification
     */
    public static final Set<GraphQLDirective> GRAPHQL_SPECIFICATION_DIRECTIVES = new LinkedHashSet<>();

    /**
     * A map from directive name to directive which provided by specification
     */
    public static final Map<String, GraphQLDirective> GRAPHQL_SPECIFICATION_DIRECTIVE_MAP = new LinkedHashMap<>();

    static {
        GRAPHQL_SPECIFICATION_DIRECTIVES.add(Directives.IncludeDirective);
        GRAPHQL_SPECIFICATION_DIRECTIVES.add(Directives.SkipDirective);
        GRAPHQL_SPECIFICATION_DIRECTIVES.add(Directives.DeprecatedDirective);
        GRAPHQL_SPECIFICATION_DIRECTIVES.add(Directives.SpecifiedByDirective);
    }

    static {
        GRAPHQL_SPECIFICATION_DIRECTIVE_MAP.put(Directives.IncludeDirective.getName(), Directives.IncludeDirective);
        GRAPHQL_SPECIFICATION_DIRECTIVE_MAP.put(Directives.SkipDirective.getName(), Directives.SkipDirective);
        GRAPHQL_SPECIFICATION_DIRECTIVE_MAP.put(Directives.DeprecatedDirective.getName(), Directives.DeprecatedDirective);
        GRAPHQL_SPECIFICATION_DIRECTIVE_MAP.put(Directives.SpecifiedByDirective.getName(), Directives.SpecifiedByDirective);
    }


    /**
     * Returns true if a directive with provided directiveName has been defined in graphql specification
     *
     * @param directiveName the name of directive in question
     *
     * @return true if the directive provided by graphql specification, and false otherwise
     */
    public static boolean isGraphqlSpecifiedDirective(String directiveName) {
        return GRAPHQL_SPECIFICATION_DIRECTIVE_MAP.containsKey(directiveName);
    }

    /**
     * Returns true if the provided directive has been defined in graphql specification
     *
     * @param graphQLDirective the directive in question
     *
     * @return true if the directive provided by graphql specification, and false otherwise
     */
    public static boolean isGraphqlSpecifiedDirective(GraphQLDirective graphQLDirective) {
        return isGraphqlSpecifiedDirective(graphQLDirective.getName());
    }


}
