package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.util.containers.ContainerUtil;
import graphql.language.Directive;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumTypeExtensionDefinition;
import graphql.language.EnumValueDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionUtil.*;

public class GraphQLEnumTypeDefinitionBuilder
    extends GraphQLExtendableDefinitionBuilder<EnumTypeDefinition, EnumTypeExtensionDefinition> {

    private final Set<String> myDirectives = new HashSet<>();
    private final Set<String> myEnumValueDefinitions = new HashSet<>();

    @NotNull
    @Override
    protected EnumTypeDefinition buildDefinitionImpl() {
        Map<String, Directive> directives = new LinkedHashMap<>();
        Map<String, EnumValueDefinition> enumValueDefinitions = new LinkedHashMap<>();

        for (EnumTypeDefinition definition : myDefinitions) {
            mergeNodes(directives, mapNamedNodesByKey(definition.getDirectives()));
            mergeNodes(enumValueDefinitions, mapNamedNodesByKey(definition.getEnumValueDefinitions()));
        }

        EnumTypeDefinition definition = ContainerUtil.getFirstItem(myDefinitions);

        myDirectives.addAll(directives.keySet());
        myEnumValueDefinitions.addAll(enumValueDefinitions.keySet());

        return definition.transform(builder ->
            builder
                .directives(toList(directives))
                .enumValueDefinitions(toList(enumValueDefinitions))
        );
    }

    @NotNull
    @Override
    protected Collection<EnumTypeExtensionDefinition> buildExtensionsImpl() {
        return ContainerUtil.map(myExtensions, extension -> {
            Map<String, Directive> directives = mergeExtensionNodes(mapNamedNodesByKey(extension.getDirectives()), myDirectives);
            Map<String, EnumValueDefinition> enumValueDefinitions =
                mergeExtensionNodes(mapNamedNodesByKey(extension.getEnumValueDefinitions()), myEnumValueDefinitions);

            return extension.transformExtension(builder ->
                builder.directives(toList(directives)).enumValueDefinitions(toList(enumValueDefinitions))
            );
        });
    }
}
