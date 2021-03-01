package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.schema.*;

@PublicApi
public class MockedWiringFactory implements WiringFactory {

    @Override
    public boolean providesTypeResolver(InterfaceWiringEnvironment environment) {
        return true;
    }

    @Override
    public TypeResolver getTypeResolver(InterfaceWiringEnvironment environment) {
        return env -> {
            throw new UnsupportedOperationException("Not implemented...this is only a mocked wiring");
        };
    }

    @Override
    public boolean providesTypeResolver(UnionWiringEnvironment environment) {
        return true;
    }

    @Override
    public TypeResolver getTypeResolver(UnionWiringEnvironment environment) {
        return env -> {
            throw new UnsupportedOperationException("Not implemented...this is only a mocked wiring");
        };
    }

    @Override
    public boolean providesDataFetcher(FieldWiringEnvironment environment) {
        return true;
    }

    @Override
    public DataFetcher getDataFetcher(FieldWiringEnvironment environment) {
        return new PropertyDataFetcher(environment.getFieldDefinition().getName());
    }

    @Override
    public boolean providesScalar(ScalarWiringEnvironment environment) {
        if (ScalarInfo.isGraphqlSpecifiedScalar(environment.getScalarTypeDefinition().getName())) {
            return false;
        }
        return true;
    }

    public GraphQLScalarType getScalar(ScalarWiringEnvironment environment) {
        return GraphQLScalarType.newScalar().name(environment.getScalarTypeDefinition().getName()).coercing(new Coercing() {
            @Override
            public Object serialize(Object dataFetcherResult) throws CoercingSerializeException {
                throw new UnsupportedOperationException("Not implemented...this is only a mocked wiring");
            }

            @Override
            public Object parseValue(Object input) throws CoercingParseValueException {
                throw new UnsupportedOperationException("Not implemented...this is only a mocked wiring");
            }

            @Override
            public Object parseLiteral(Object input) throws CoercingParseLiteralException {
                throw new UnsupportedOperationException("Not implemented...this is only a mocked wiring");
            }
        }).build();
    }
}
