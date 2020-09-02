package com.intellij.lang.jsgraphql.schema;

import graphql.language.AstPrinter;
import graphql.language.NamedNode;
import graphql.language.Node;
import graphql.language.Type;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public static <T extends Node<T>> Map<String, T> mergeExtensionNodes(@NotNull Map<String, T> nodes,
                                                                         @NotNull Set<String> declaredNames) {
        nodes.keySet().removeAll(declaredNames);
        declaredNames.addAll(nodes.keySet());
        return nodes;
    }

    public static <T extends Node<?>> List<T> toList(@NotNull Map<String, T> target) {
        return new ArrayList<>(target.values());
    }
}
