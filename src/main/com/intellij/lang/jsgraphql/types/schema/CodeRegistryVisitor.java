package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.util.TraversalControl;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

import static com.intellij.lang.jsgraphql.types.Assert.assertTrue;
import static com.intellij.lang.jsgraphql.types.schema.FieldCoordinates.coordinates;
import static com.intellij.lang.jsgraphql.types.util.TraversalControl.CONTINUE;

/**
 * This ensure that all fields have data fetchers and that unions and interfaces have type resolvers
 */
@Internal
class CodeRegistryVisitor extends GraphQLTypeVisitorStub {
    private final GraphQLCodeRegistry.Builder codeRegistry;

    CodeRegistryVisitor(GraphQLCodeRegistry.Builder codeRegistry) {
        this.codeRegistry = codeRegistry;
    }

    @Override
    public TraversalControl visitGraphQLFieldDefinition(GraphQLFieldDefinition node, TraverserContext<GraphQLSchemaElement> context) {
        GraphQLFieldsContainer parentContainerType = (GraphQLFieldsContainer) context.getParentContext().thisNode();
        DataFetcher<?> dataFetcher = node.getDataFetcher();
        if (dataFetcher != null) {
            FieldCoordinates coordinates = coordinates(parentContainerType, node);
            codeRegistry.dataFetcherIfAbsent(coordinates, dataFetcher);
        }

        return CONTINUE;
    }

    @Override
    public TraversalControl visitGraphQLInterfaceType(GraphQLInterfaceType node, TraverserContext<GraphQLSchemaElement> context) {
        TypeResolver typeResolver = node.getTypeResolver();
        if (typeResolver != null) {
            codeRegistry.typeResolverIfAbsent(node, typeResolver);
        }
        assertTrue(codeRegistry.getTypeResolver(node) != null,
                () -> String.format("You MUST provide a type resolver for the interface type '%s'",node.getName()));
        return CONTINUE;
    }

    @Override
    public TraversalControl visitGraphQLUnionType(GraphQLUnionType node, TraverserContext<GraphQLSchemaElement> context) {
        TypeResolver typeResolver = node.getTypeResolver();
        if (typeResolver != null) {
            codeRegistry.typeResolverIfAbsent(node, typeResolver);
        }
        assertTrue(codeRegistry.getTypeResolver(node) != null,
                () -> String.format("You MUST provide a type resolver for the union type '%s'", node.getName()));
        return CONTINUE;
    }
}
