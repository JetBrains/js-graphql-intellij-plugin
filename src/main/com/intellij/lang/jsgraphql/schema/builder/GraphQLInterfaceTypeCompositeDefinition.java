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
        List<Directive> directives = new ArrayList<>();
        Map<String, FieldDefinition> fieldDefinitions = new LinkedHashMap<>();

        for (InterfaceTypeDefinition definition : myDefinitions) {
            directives.addAll(definition.getDirectives());
            mergeNodes(fieldDefinitions, mapNamedNodesByKey(definition.getFieldDefinitions()));
        }

        InterfaceTypeDefinition definition = ContainerUtil.getFirstItem(myDefinitions);
        return definition.transform(builder ->
            builder
                .directives(directives)
                .definitions(toList(fieldDefinitions))
                .sourceNodes(myDefinitions)
        );
    }
}
