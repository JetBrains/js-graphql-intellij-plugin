package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.ScalarTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.ScalarTypeExtensionDefinition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionUtil.*;

public class GraphQLScalarTypeCompositeDefinition
    extends GraphQLExtendableCompositeDefinition<ScalarTypeDefinition, ScalarTypeExtensionDefinition> {

    @NotNull
    @Override
    protected ScalarTypeDefinition mergeDefinitions() {
        Map<String, Directive> directives = new LinkedHashMap<>();

        for (ScalarTypeDefinition definition : myDefinitions) {
            mergeNodes(directives, mapNamedNodesByKey(definition.getDirectives()));
        }

        ScalarTypeDefinition definition = ContainerUtil.getFirstItem(myDefinitions);
        return definition.transform(builder -> builder.directives(toList(directives)).sourceNodes(myDefinitions));
    }
}
