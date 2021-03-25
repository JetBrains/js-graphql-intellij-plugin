package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.InputObjectTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.InputObjectTypeExtensionDefinition;
import com.intellij.lang.jsgraphql.types.language.InputValueDefinition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionUtil.*;

public class GraphQLInputObjectTypeCompositeDefinition
    extends GraphQLExtendableCompositeDefinition<InputObjectTypeDefinition, InputObjectTypeExtensionDefinition> {

    @NotNull
    @Override
    protected InputObjectTypeDefinition mergeDefinitions() {
        Map<String, Directive> directives = new LinkedHashMap<>();
        Map<String, InputValueDefinition> inputValueDefinitions = new LinkedHashMap<>();

        for (InputObjectTypeDefinition definition : myDefinitions) {
            mergeNodes(directives, mapNamedNodesByKey(definition.getDirectives()));
            mergeNodes(inputValueDefinitions, mapNamedNodesByKey(definition.getInputValueDefinitions()));
        }

        InputObjectTypeDefinition definition = ContainerUtil.getFirstItem(myDefinitions);
        return definition.transform(builder ->
            builder
                .directives(toList(directives))
                .inputValueDefinitions(toList(inputValueDefinitions))
                .sourceNodes(myDefinitions)
        );
    }
}
