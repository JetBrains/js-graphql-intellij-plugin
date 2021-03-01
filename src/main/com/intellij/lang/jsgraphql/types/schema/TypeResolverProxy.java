package com.intellij.lang.jsgraphql.types.schema;


import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.TypeResolutionEnvironment;

@Internal
public class TypeResolverProxy implements TypeResolver {

    private TypeResolver typeResolver;

    public TypeResolver getTypeResolver() {
        return typeResolver;
    }

    public void setTypeResolver(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    @Override
    public GraphQLObjectType getType(TypeResolutionEnvironment env) {
        return typeResolver != null ? typeResolver.getType(env) : null;
    }
}
