package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.util.containers.ContainerUtil;
import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.ScalarTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.ScalarTypeExtensionDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionUtil.*;

public class GraphQLScalarTypeDefinitionBuilder
    extends GraphQLExtendableDefinitionBuilder<ScalarTypeDefinition, ScalarTypeExtensionDefinition> {

    private final Set<String> myDirectives = new HashSet<>();

    @NotNull
    @Override
    protected ScalarTypeDefinition buildDefinitionImpl() {
        Map<String, Directive> directives = new LinkedHashMap<>();

        for (ScalarTypeDefinition definition : myDefinitions) {
            mergeNodes(directives, mapNamedNodesByKey(definition.getDirectives()));
        }

        ScalarTypeDefinition definition = ContainerUtil.getFirstItem(myDefinitions);
        myDirectives.addAll(directives.keySet());
        return definition.transform(builder -> builder.directives(toList(directives)));
    }

    @NotNull
    @Override
    protected Collection<ScalarTypeExtensionDefinition> buildExtensionsImpl() {
        return ContainerUtil.map(myExtensions, extension -> {
            Map<String, Directive> directives = mergeExtensionNodes(mapNamedNodesByKey(extension.getDirectives()), myDirectives);

            return extension.transformExtension(builder -> builder.directives(toList(directives)));
        });
    }
}
