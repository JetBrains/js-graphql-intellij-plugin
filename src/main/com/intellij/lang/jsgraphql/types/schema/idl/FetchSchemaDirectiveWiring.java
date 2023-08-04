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

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.schema.*;

import java.util.List;
import java.util.Optional;

import static com.intellij.lang.jsgraphql.types.DirectivesUtil.directiveWithArg;
import static com.intellij.lang.jsgraphql.types.schema.FieldCoordinates.coordinates;

/**
 * This adds ' @fetch(from : "otherName") ' support so you can rename what property is read for a given field
 */
@Internal
public class FetchSchemaDirectiveWiring implements SchemaDirectiveWiring {

  public static final String FETCH = "fetch";

  @Override
  public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldDefinition field = environment.getElement();
    String fetchName = atFetchFromSupport(field.getName(), field.getDirectives());
    DataFetcher dataFetcher = new PropertyDataFetcher(fetchName);

    environment.getCodeRegistry().dataFetcher(coordinates(environment.getFieldsContainer(), field), dataFetcher);
    return field;
  }


  private String atFetchFromSupport(String fieldName, List<GraphQLDirective> directives) {
    // @fetch(from : "name")
    Optional<GraphQLArgument> from = directiveWithArg(directives, FETCH, "from");
    return from.map(arg -> String.valueOf(arg.getValue())).orElse(fieldName);
  }
}
