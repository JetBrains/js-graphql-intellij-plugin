package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.EnumTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.EnumTypeExtensionDefinition;
import com.intellij.lang.jsgraphql.types.language.EnumValueDefinition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionUtil.*;

public class GraphQLEnumTypeCompositeDefinition
    extends GraphQLExtendableCompositeDefinition<EnumTypeDefinition, EnumTypeExtensionDefinition> {

    private final Set<String> myDirectives = new HashSet<>();
    private final Set<String> myEnumValueDefinitions = new HashSet<>();

    @NotNull
    @Override
    protected EnumTypeDefinition mergeDefinitions() {
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
                .sourceNodes(myDefinitions)
        );
    }

    @Override
    protected @NotNull List<EnumTypeExtensionDefinition> processExtensions() {
        return ContainerUtil.map(myExtensions, extension -> {
            Map<String, Directive> directives = mergeExtensionNodes(mapNamedNodesByKey(extension.getDirectives()), myDirectives);
            Map<String, EnumValueDefinition> enumValueDefinitions =
                mergeExtensionNodes(mapNamedNodesByKey(extension.getEnumValueDefinitions()), myEnumValueDefinitions);

            return extension.transformExtension(builder ->
                builder
                    .directives(toList(directives))
                    .enumValueDefinitions(toList(enumValueDefinitions))
                    .sourceNodes(Collections.singletonList(extension))
            );
        });
    }
}
