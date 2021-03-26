package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionUtil.*;

public class GraphQLObjectTypeCompositeDefinition extends GraphQLExtendableCompositeDefinition<ObjectTypeDefinition, ObjectTypeExtensionDefinition> {
    @NotNull
    @Override
    protected ObjectTypeDefinition mergeDefinitions() {
        List<Directive> directives = new ArrayList<>();
        Map<String, FieldDefinition> fieldDefinitions = new LinkedHashMap<>();

        for (ObjectTypeDefinition definition : myDefinitions) {
            directives.addAll(definition.getDirectives());
            mergeNodes(fieldDefinitions, mapNamedNodesByKey(definition.getFieldDefinitions()));
        }

        ObjectTypeDefinition definition = ContainerUtil.getFirstItem(myDefinitions);
        return definition.transform(builder ->
            builder
                .directives(directives)
                .fieldDefinitions(toList(fieldDefinitions))
                .sourceNodes(myDefinitions)
        );
    }
}
