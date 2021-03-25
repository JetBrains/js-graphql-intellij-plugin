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
        return definition.transform(builder ->
            builder
                .directives(toList(directives))
                .enumValueDefinitions(toList(enumValueDefinitions))
                .sourceNodes(myDefinitions)
        );
    }
}
