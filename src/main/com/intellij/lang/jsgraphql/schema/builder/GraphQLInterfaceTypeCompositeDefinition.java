package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionUtil.*;

public class GraphQLInterfaceTypeCompositeDefinition
    extends GraphQLExtendableCompositeDefinition<InterfaceTypeDefinition, InterfaceTypeExtensionDefinition> {

    @NotNull
    @Override
    protected InterfaceTypeDefinition mergeDefinitions() {
        Map<String, Directive> directives = new LinkedHashMap<>();
        Map<String, FieldDefinition> fieldDefinitions = new LinkedHashMap<>();

        for (InterfaceTypeDefinition definition : myDefinitions) {
            mergeNodes(directives, mapNamedNodesByKey(definition.getDirectives()));
            mergeNodes(fieldDefinitions, mapNamedNodesByKey(definition.getFieldDefinitions()));
        }

        InterfaceTypeDefinition definition = ContainerUtil.getFirstItem(myDefinitions);
        return definition.transform(builder ->
            builder
                .directives(toList(directives))
                .definitions(toList(fieldDefinitions))
                .implementz(definition.getImplements()) // https://github.com/graphql-java/graphql-java/issues/1974
                .sourceNodes(myDefinitions)
        );
    }
}
