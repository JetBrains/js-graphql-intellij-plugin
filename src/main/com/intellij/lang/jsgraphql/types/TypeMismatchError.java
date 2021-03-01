package com.intellij.lang.jsgraphql.types;

import com.intellij.lang.jsgraphql.types.execution.ResultPath;
import com.intellij.lang.jsgraphql.types.introspection.Introspection;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;
import com.intellij.lang.jsgraphql.types.schema.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

@PublicApi
public class TypeMismatchError implements GraphQLError {

    private final String message;
    private final List<Object> path;
    private final GraphQLType expectedType;

    public TypeMismatchError(ResultPath path, GraphQLType expectedType) {
        this.path = assertNotNull(path).toList();
        this.expectedType = assertNotNull(expectedType);
        this.message = mkMessage(path, expectedType);
    }

    private String mkMessage(ResultPath path, GraphQLType expectedType) {
        String expectedTypeKind = GraphQLTypeToTypeKindMapping.getTypeKindFromGraphQLType(expectedType).name();
        return format("Can't resolve value (%s) : type mismatch error, expected type %s", path, expectedTypeKind);
    }

    static class GraphQLTypeToTypeKindMapping {

        private static final Map<Class<? extends GraphQLType>, Introspection.TypeKind> registry = new LinkedHashMap<Class<? extends GraphQLType>, Introspection.TypeKind>() {{
            put(GraphQLEnumType.class, Introspection.TypeKind.ENUM);
            put(GraphQLList.class, Introspection.TypeKind.LIST);
            put(GraphQLObjectType.class, Introspection.TypeKind.OBJECT);
            put(GraphQLScalarType.class, Introspection.TypeKind.SCALAR);
            put(GraphQLInputObjectType.class, Introspection.TypeKind.INPUT_OBJECT);
            put(GraphQLInterfaceType.class, Introspection.TypeKind.INTERFACE);
            put(GraphQLNonNull.class, Introspection.TypeKind.NON_NULL);
            put(GraphQLUnionType.class, Introspection.TypeKind.UNION);
        }};

        private GraphQLTypeToTypeKindMapping() {
        }

        public static Introspection.TypeKind getTypeKindFromGraphQLType(GraphQLType type) {
            return registry.containsKey(type.getClass()) ? registry.get(type.getClass()) : Assert.assertShouldNeverHappen("Unknown kind of type: " + type);
        }
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.DataFetchingException;
    }

    @Override
    public List<Object> getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "TypeMismatchError{" +
                "path=" + path +
                ", expectedType=" + expectedType +
                '}';
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return GraphqlErrorHelper.equals(this, o);
    }

    @Override
    public int hashCode() {
        return GraphqlErrorHelper.hashCode(this);
    }
}
