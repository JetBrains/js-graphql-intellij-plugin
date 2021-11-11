package com.intellij.lang.jsgraphql.schema;

import com.intellij.lang.jsgraphql.types.language.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GraphQLTypeDefinitionUtil {

    private GraphQLTypeDefinitionUtil() {
    }

    public static <T extends NamedNode<T>> Map<String, T> mapNamedNodesByKey(@NotNull List<T> nodes) {
        return mapNodesByKey(nodes, NamedNode::getName);
    }

    @SuppressWarnings("rawtypes")
    public static Map<String, Type> mapTypeNodesByKey(@NotNull List<Type> nodes) {
        //noinspection unchecked
        return mapNodesByKey(nodes, AstPrinter::printAst);
    }

    public static <T extends Node<T>> Map<String, T> mapNodesByKey(@NotNull List<T> nodes, @NotNull Function<T, String> keyMapper) {
        return nodes.stream().collect(Collectors.toMap(keyMapper, value -> value, (oldValue, newValue) -> oldValue));
    }

    public static <T extends Node<?>> void mergeNodes(@NotNull Map<String, T> target, @NotNull Map<String, T> source) {
        source.forEach((key, value) -> target.merge(key, value, (oldValue, newValue) -> oldValue));
    }

    public static <T extends Node<?>> List<T> toList(@NotNull Map<String, T> target) {
        return new ArrayList<>(target.values());
    }

    public static boolean isExtension(@Nullable SDLDefinition<?> definition) {
        return definition instanceof SchemaExtensionDefinition ||
            definition instanceof InputObjectTypeExtensionDefinition ||
            definition instanceof ObjectTypeExtensionDefinition ||
            definition instanceof InterfaceTypeExtensionDefinition ||
            definition instanceof ScalarTypeExtensionDefinition ||
            definition instanceof UnionTypeExtensionDefinition ||
            definition instanceof EnumTypeExtensionDefinition;
    }

    @Nullable
    public static Description getTypeDefinitionDescription(@NotNull TypeDefinition<?> typeDefinition) {
        Description description = null;
        if (typeDefinition instanceof ObjectTypeDefinition) {
            description = ((ObjectTypeDefinition) typeDefinition).getDescription();
        } else if (typeDefinition instanceof InterfaceTypeDefinition) {
            description = ((InterfaceTypeDefinition) typeDefinition).getDescription();
        } else if (typeDefinition instanceof EnumTypeDefinition) {
            description = ((EnumTypeDefinition) typeDefinition).getDescription();
        } else if (typeDefinition instanceof ScalarTypeDefinition) {
            description = ((ScalarTypeDefinition) typeDefinition).getDescription();
        } else if (typeDefinition instanceof InputObjectTypeDefinition) {
            description = ((InputObjectTypeDefinition) typeDefinition).getDescription();
        } else if (typeDefinition instanceof UnionTypeDefinition) {
            description = ((UnionTypeDefinition) typeDefinition).getDescription();
        }
        return description;

    }
}
