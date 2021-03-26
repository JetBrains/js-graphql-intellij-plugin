package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.Type;
import com.intellij.lang.jsgraphql.types.language.UnionTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.UnionTypeExtensionDefinition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionUtil.*;

public class GraphQLUnionTypeCompositeDefinition
    extends GraphQLExtendableCompositeDefinition<UnionTypeDefinition, UnionTypeExtensionDefinition> {

    @NotNull
    @Override
    protected UnionTypeDefinition mergeDefinitions() {
        List<Directive> directives = new ArrayList<>();

        @SuppressWarnings("rawtypes")
        Map<String, Type> memberTypes = new LinkedHashMap<>();

        for (UnionTypeDefinition definition : myDefinitions) {
            directives.addAll(definition.getDirectives());
            mergeNodes(memberTypes, mapTypeNodesByKey(definition.getMemberTypes()));
        }

        UnionTypeDefinition definition = ContainerUtil.getFirstItem(myDefinitions);
        return definition.transform(builder ->
            builder
                .directives(directives)
                .memberTypes(toList(memberTypes))
                .sourceNodes(myDefinitions)
        );
    }
}
