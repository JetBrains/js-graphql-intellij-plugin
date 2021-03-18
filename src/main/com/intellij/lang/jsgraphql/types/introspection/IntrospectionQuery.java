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
package com.intellij.lang.jsgraphql.types.introspection;

import com.intellij.lang.jsgraphql.types.PublicApi;

@PublicApi
public interface IntrospectionQuery {

    String INTROSPECTION_QUERY = "\n" +
            "  query IntrospectionQuery {\n" +
            "    __schema {\n" +
            "      queryType { name }\n" +
            "      mutationType { name }\n" +
            "      subscriptionType { name }\n" +
            "      types {\n" +
            "        ...FullType\n" +
            "      }\n" +
            "      directives {\n" +
            "        name\n" +
            "        description\n" +
            "        locations\n" +
            "        args {\n" +
            "          ...InputValue\n" +
            "        }\n" +
            "        isRepeatable\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "\n" +
            "  fragment FullType on __Type {\n" +
            "    kind\n" +
            "    name\n" +
            "    description\n" +
            "    fields(includeDeprecated: true) {\n" +
            "      name\n" +
            "      description\n" +
            "      args {\n" +
            "        ...InputValue\n" +
            "      }\n" +
            "      type {\n" +
            "        ...TypeRef\n" +
            "      }\n" +
            "      isDeprecated\n" +
            "      deprecationReason\n" +
            "    }\n" +
            "    inputFields {\n" +
            "      ...InputValue\n" +
            "    }\n" +
            "    interfaces {\n" +
            "      ...TypeRef\n" +
            "    }\n" +
            "    enumValues(includeDeprecated: true) {\n" +
            "      name\n" +
            "      description\n" +
            "      isDeprecated\n" +
            "      deprecationReason\n" +
            "    }\n" +
            "    possibleTypes {\n" +
            "      ...TypeRef\n" +
            "    }\n" +
            "  }\n" +
            "\n" +
            "  fragment InputValue on __InputValue {\n" +
            "    name\n" +
            "    description\n" +
            "    type { ...TypeRef }\n" +
            "    defaultValue\n" +
            "  }\n" +
            "\n" +
            //
            // The depth of the types is actually an arbitrary decision.  It could be any depth in fact.  This depth
            // was taken from GraphIQL https://github.com/graphql/graphiql/blob/master/src/utility/introspectionQueries.js
            // which uses 7 levels and hence could represent a type like say [[[[[Float!]]]]]
            //
            "fragment TypeRef on __Type {\n" +
            "    kind\n" +
            "    name\n" +
            "    ofType {\n" +
            "      kind\n" +
            "      name\n" +
            "      ofType {\n" +
            "        kind\n" +
            "        name\n" +
            "        ofType {\n" +
            "          kind\n" +
            "          name\n" +
            "          ofType {\n" +
            "            kind\n" +
            "            name\n" +
            "            ofType {\n" +
            "              kind\n" +
            "              name\n" +
            "              ofType {\n" +
            "                kind\n" +
            "                name\n" +
            "                ofType {\n" +
            "                  kind\n" +
            "                  name\n" +
            "                }\n" +
            "              }\n" +
            "            }\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "\n";
}
