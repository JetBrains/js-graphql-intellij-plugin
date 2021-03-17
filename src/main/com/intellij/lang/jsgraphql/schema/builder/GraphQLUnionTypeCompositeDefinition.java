package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.util.containers.ContainerUtil;
import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.Type;
import com.intellij.lang.jsgraphql.types.language.UnionTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.UnionTypeExtensionDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionUtil.*;

public class GraphQLUnionTypeCompositeDefinition
    extends GraphQLExtendableCompositeDefinition<UnionTypeDefinition, UnionTypeExtensionDefinition> {

    private final Set<String> myDirectives = new HashSet<>();
    private final Set<String> myMemberTypes = new HashSet<>();

    @NotNull
    @Override
    protected UnionTypeDefinition mergeDefinitions() {
        Map<String, Directive> directives = new LinkedHashMap<>();

        @SuppressWarnings("rawtypes")
        Map<String, Type> memberTypes = new LinkedHashMap<>();

        for (UnionTypeDefinition definition : myDefinitions) {
            mergeNodes(directives, mapNamedNodesByKey(definition.getDirectives()));
            mergeNodes(memberTypes, mapTypeNodesByKey(definition.getMemberTypes()));
        }

        UnionTypeDefinition definition = ContainerUtil.getFirstItem(myDefinitions);

        myDirectives.addAll(directives.keySet());
        myMemberTypes.addAll(memberTypes.keySet());

        return definition.transform(builder ->
            builder
                .directives(toList(directives))
                .memberTypes(toList(memberTypes))
        );
    }

    @Override
    protected @NotNull List<UnionTypeExtensionDefinition> processExtensions() {
        return ContainerUtil.map(myExtensions, extension -> {
            Map<String, Directive> directives = mergeExtensionNodes(mapNamedNodesByKey(extension.getDirectives()), myDirectives);
            @SuppressWarnings({"unchecked", "rawtypes"})
            Map<String, Type> memberTypes = mergeExtensionNodes(mapTypeNodesByKey(extension.getMemberTypes()), myMemberTypes);

            return extension.transformExtension(builder ->
                builder.directives(toList(directives)).memberTypes(toList(memberTypes))
            );
        });
    }
}
