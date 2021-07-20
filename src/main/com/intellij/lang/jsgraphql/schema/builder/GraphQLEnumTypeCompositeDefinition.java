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
        List<Directive> directives = new ArrayList<>();
        Map<String, EnumValueDefinition> enumValueDefinitions = new LinkedHashMap<>();

        for (EnumTypeDefinition definition : myDefinitions) {
            directives.addAll(definition.getDirectives());
            mergeNodes(enumValueDefinitions, mapNamedNodesByKey(definition.getEnumValueDefinitions()));
        }

        EnumTypeDefinition definition = ContainerUtil.getFirstItem(myDefinitions);
        return definition.transform(builder ->
            builder
                .directives(directives)
                .enumValueDefinitions(toList(enumValueDefinitions))
                .sourceNodes(myDefinitions)
        );
    }
}
