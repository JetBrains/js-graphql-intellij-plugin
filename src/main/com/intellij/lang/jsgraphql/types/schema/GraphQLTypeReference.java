package com.intellij.lang.jsgraphql.types.schema;


import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.util.TraversalControl;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

import static com.intellij.lang.jsgraphql.types.Assert.assertValidName;

/**
 * A special type to allow a object/interface types to reference itself. It's replaced with the real type
 * object when the schema is built.
 */
@PublicApi
public class GraphQLTypeReference implements GraphQLNamedOutputType, GraphQLNamedInputType {

    /**
     * A factory method for creating type references so that when used with static imports allows
     * more readable code such as
     * {@code .type(typeRef(GraphQLString)) }
     *
     * @param typeName the name of the type to reference
     *
     * @return a GraphQLTypeReference of that named type
     */
    public static GraphQLTypeReference typeRef(String typeName) {
        return new GraphQLTypeReference(typeName);
    }

    private final String name;

    public GraphQLTypeReference(String name) {
        assertValidName(name);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Node getDefinition() {
        return null;
    }

    @Override
    public String toString() {
        return "GraphQLTypeReference{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public TraversalControl accept(TraverserContext<GraphQLSchemaElement> context, GraphQLTypeVisitor visitor) {
        return visitor.visitGraphQLTypeReference(this, context);
    }
}
