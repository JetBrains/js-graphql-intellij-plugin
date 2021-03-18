package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.OperationTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.SchemaDefinition;
import com.intellij.lang.jsgraphql.types.language.SchemaExtensionDefinition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionUtil.*;

public class GraphQLSchemaTypeCompositeDefinition
    extends GraphQLExtendableCompositeDefinition<SchemaDefinition, SchemaExtensionDefinition> {

    private final Set<String> myDirectives = new HashSet<>();
    private final Set<String> myOperationTypeDefinitions = new HashSet<>();

    @NotNull
    @Override
    protected SchemaDefinition mergeDefinitions() {
        Map<String, Directive> directives = new LinkedHashMap<>();
        Map<String, OperationTypeDefinition> operationTypeDefinitions = new LinkedHashMap<>();

        for (SchemaDefinition definition : myDefinitions) {
            mergeNodes(directives, mapNamedNodesByKey(definition.getDirectives()));
            mergeNodes(operationTypeDefinitions, mapNamedNodesByKey(definition.getOperationTypeDefinitions()));
        }

        SchemaDefinition definition = ContainerUtil.getFirstItem(myDefinitions);

        myDirectives.addAll(directives.keySet());
        myOperationTypeDefinitions.addAll(operationTypeDefinitions.keySet());

        return definition.transform(builder -> builder
            .directives(toList(directives))
            .operationTypeDefinitions(toList(operationTypeDefinitions))
            .sourceNodes(myDefinitions)
        );
    }

    @Override
    protected @NotNull List<SchemaExtensionDefinition> processExtensions() {
        return ContainerUtil.map(myExtensions, extension -> {
            Map<String, Directive> directives = mergeExtensionNodes(mapNamedNodesByKey(extension.getDirectives()), myDirectives);
            Map<String, OperationTypeDefinition> operationTypeDefinitions =
                mergeExtensionNodes(mapNamedNodesByKey(extension.getOperationTypeDefinitions()), myOperationTypeDefinitions);

            return extension.transformExtension(builder ->
                builder
                    .directives(toList(directives))
                    .operationTypeDefinitions(toList(operationTypeDefinitions))
                    .sourceNodes(Collections.singletonList(extension))
            );
        });
    }
}