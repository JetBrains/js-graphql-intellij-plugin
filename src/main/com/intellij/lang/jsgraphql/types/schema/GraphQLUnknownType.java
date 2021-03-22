package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.language.DescribedNode;
import com.intellij.lang.jsgraphql.types.language.Description;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.language.TypeDefinition;
import com.intellij.lang.jsgraphql.types.util.TraversalControl;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("rawtypes")
public class GraphQLUnknownType implements GraphQLNamedOutputType, GraphQLNamedInputType {

    public static final String UNKNOWN = "<Unknown>";

    private final @NotNull String myName;
    private final @Nullable TypeDefinition myDefinition;
    private final GraphQLError myError;

    public GraphQLUnknownType(@Nullable String name, @Nullable GraphQLError error) {
        this(name, null, error);
    }

    public GraphQLUnknownType(@Nullable TypeDefinition definition, @Nullable GraphQLError error) {
        this(definition != null ? definition.getName() : null, definition, error);
    }

    private GraphQLUnknownType(@Nullable String name, @Nullable TypeDefinition definition, @Nullable GraphQLError error) {
        myName = ObjectUtils.coalesce(name, UNKNOWN);
        myDefinition = definition;
        myError = error;
    }

    @Override
    public @NotNull String getName() {
        return myName;
    }

    @Override
    public String getDescription() {
        if (myDefinition instanceof DescribedNode) {
            Description description = ((DescribedNode<?>) myDefinition).getDescription();
            return description != null ? description.getContent() : null;
        }
        return null;
    }

    public @Nullable GraphQLError getError() {
        return myError;
    }

    @Override
    public Node getDefinition() {
        return myDefinition;
    }

    @Override
    public String toString() {
        return "GraphQLUnknownType{" +
            "name='" + myName + '\'' +
            '}';
    }

    @Override
    public TraversalControl accept(TraverserContext<GraphQLSchemaElement> context, GraphQLTypeVisitor visitor) {
        return visitor.visitGraphQLInvalidType(this, context);
    }
}

