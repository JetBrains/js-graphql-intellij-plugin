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

    private final Set<String> myDirectives = new HashSet<>();

    @NotNull
    @Override
    protected ScalarTypeDefinition mergeDefinitions() {
        Map<String, Directive> directives = new LinkedHashMap<>();

        for (ScalarTypeDefinition definition : myDefinitions) {
            mergeNodes(directives, mapNamedNodesByKey(definition.getDirectives()));
        }

        ScalarTypeDefinition definition = ContainerUtil.getFirstItem(myDefinitions);
        myDirectives.addAll(directives.keySet());
        return definition.transform(builder -> builder.directives(toList(directives)).sourceNodes(myDefinitions));
    }

    @Override
    protected @NotNull List<ScalarTypeExtensionDefinition> processExtensions() {
        return ContainerUtil.map(myExtensions, extension -> {
            Map<String, Directive> directives = mergeExtensionNodes(mapNamedNodesByKey(extension.getDirectives()), myDirectives);

            return extension.transformExtension(builder ->
                builder
                    .directives(toList(directives))
                    .sourceNodes(Collections.singletonList(extension))
            );
        });
    }
}
