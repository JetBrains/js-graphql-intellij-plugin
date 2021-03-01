package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.util.containers.ContainerUtil;
import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.InputObjectTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.InputObjectTypeExtensionDefinition;
import com.intellij.lang.jsgraphql.types.language.InputValueDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionUtil.*;

public class GraphQLInputObjectTypeDefinitionBuilder
    extends GraphQLExtendableDefinitionBuilder<InputObjectTypeDefinition, InputObjectTypeExtensionDefinition> {

    private final Set<String> myDirectives = new HashSet<>();
    private final Set<String> myInputValueDefinitions = new HashSet<>();

    @NotNull
    @Override
    protected InputObjectTypeDefinition buildDefinitionImpl() {
        Map<String, Directive> directives = new LinkedHashMap<>();
        Map<String, InputValueDefinition> inputValueDefinitions = new LinkedHashMap<>();

        for (InputObjectTypeDefinition definition : myDefinitions) {
            mergeNodes(directives, mapNamedNodesByKey(definition.getDirectives()));
            mergeNodes(inputValueDefinitions, mapNamedNodesByKey(definition.getInputValueDefinitions()));
        }

        InputObjectTypeDefinition definition = ContainerUtil.getFirstItem(myDefinitions);

        myDirectives.addAll(directives.keySet());
        myInputValueDefinitions.addAll(inputValueDefinitions.keySet());

        return definition.transform(builder ->
            builder
                .directives(toList(directives))
                .inputValueDefinitions(toList(inputValueDefinitions))
        );
    }

    @NotNull
    @Override
    protected Collection<InputObjectTypeExtensionDefinition> buildExtensionsImpl() {
        return ContainerUtil.map(myExtensions, extension -> {
            Map<String, Directive> directives = mergeExtensionNodes(mapNamedNodesByKey(extension.getDirectives()), myDirectives);
            Map<String, InputValueDefinition> inputValueDefinitions =
                mergeExtensionNodes(mapNamedNodesByKey(extension.getInputValueDefinitions()), myInputValueDefinitions);

            return extension.transformExtension(builder ->
                builder.directives(toList(directives)).inputValueDefinitions(toList(inputValueDefinitions))
            );
        });
    }
}
