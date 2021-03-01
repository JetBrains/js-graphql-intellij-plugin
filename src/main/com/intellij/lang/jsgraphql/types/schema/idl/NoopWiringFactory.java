package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.schema.DataFetcher;
import com.intellij.lang.jsgraphql.types.schema.GraphQLScalarType;
import com.intellij.lang.jsgraphql.types.schema.TypeResolver;

import static com.intellij.lang.jsgraphql.types.Assert.assertShouldNeverHappen;

@Internal
public class NoopWiringFactory implements WiringFactory {

    @Override
    public boolean providesScalar(ScalarWiringEnvironment environment) {
        return false;
    }

    @Override
    public GraphQLScalarType getScalar(ScalarWiringEnvironment environment) {
        return assertShouldNeverHappen();
    }

    @Override
    public boolean providesTypeResolver(InterfaceWiringEnvironment environment) {
        return false;
    }

    @Override
    public TypeResolver getTypeResolver(InterfaceWiringEnvironment environment) {
        return assertShouldNeverHappen();
    }

    @Override
    public boolean providesTypeResolver(UnionWiringEnvironment environment) {
        return false;
    }

    @Override
    public TypeResolver getTypeResolver(UnionWiringEnvironment environment) {
        return assertShouldNeverHappen();
    }

    @Override
    public boolean providesDataFetcher(FieldWiringEnvironment environment) {
        return false;
    }

    @Override
    public DataFetcher getDataFetcher(FieldWiringEnvironment environment) {
        return assertShouldNeverHappen();
    }

    @Override
    public DataFetcher getDefaultDataFetcher(FieldWiringEnvironment environment) {
        return null;
    }
}
