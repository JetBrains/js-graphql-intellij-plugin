package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.util.containers.ContainerUtil;
import graphql.language.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionUtil.*;

public class GraphQLObjectTypeDefinitionBuilder extends GraphQLExtendableDefinitionBuilder<ObjectTypeDefinition, ObjectTypeExtensionDefinition> {

    /**
     * `implements` for duplicated types are ignored, because
     * additional interfaces to implement is one more reason to fail building schema.
     * Used only for removing duplicated `implements` from extensions.
     */
    private final Set<String> myImplements = new HashSet<>();
    private final Set<String> myDirectives = new HashSet<>();
    private final Set<String> myFieldDefinitions = new HashSet<>();

    @NotNull
    @Override
    protected ObjectTypeDefinition buildDefinitionImpl() {
        Map<String, Directive> directives = new LinkedHashMap<>();
        Map<String, FieldDefinition> fieldDefinitions = new LinkedHashMap<>();

        for (ObjectTypeDefinition definition : myDefinitions) {
            mergeNodes(directives, mapNamedNodesByKey(definition.getDirectives()));
            mergeNodes(fieldDefinitions, mapNamedNodesByKey(definition.getFieldDefinitions()));
        }

        ObjectTypeDefinition definition = ContainerUtil.getFirstItem(myDefinitions);

        myDirectives.addAll(directives.keySet());
        myFieldDefinitions.addAll(fieldDefinitions.keySet());
        myImplements.addAll(mapTypeNodesByKey(definition.getImplements()).keySet());

        return definition.transform(builder ->
            builder
                .directives(toList(directives))
                .fieldDefinitions(toList(fieldDefinitions))
        );
    }

    @NotNull
    @Override
    protected Collection<ObjectTypeExtensionDefinition> buildExtensionsImpl() {
        return ContainerUtil.map(myExtensions, extension -> {
            Map<String, Directive> directives = mergeExtensionNodes(mapNamedNodesByKey(extension.getDirectives()), myDirectives);
            Map<String, FieldDefinition> fieldDefinitions =
                mergeExtensionNodes(mapNamedNodesByKey(extension.getFieldDefinitions()), myFieldDefinitions);
            @SuppressWarnings({"unchecked", "rawtypes"})
            Map<String, Type> implementz = mergeExtensionNodes(mapTypeNodesByKey(extension.getImplements()), myImplements);

            return extension.transformExtension(builder ->
                builder.directives(toList(directives)).fieldDefinitions(toList(fieldDefinitions)).implementz(toList(implementz))
            );
        });
    }
}
