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
