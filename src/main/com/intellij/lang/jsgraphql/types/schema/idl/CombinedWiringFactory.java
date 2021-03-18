/*
    The MIT License (MIT)

    Copyright (c) 2015 Andreas Marek and Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
    (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
    publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
    so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.schema.DataFetcher;
import com.intellij.lang.jsgraphql.types.schema.DataFetcherFactory;
import com.intellij.lang.jsgraphql.types.schema.GraphQLScalarType;
import com.intellij.lang.jsgraphql.types.schema.TypeResolver;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.Assert.assertShouldNeverHappen;

/**
 * This combines a number of {@link WiringFactory}s together to act as one.  It asks each one
 * whether it handles a type and delegates to the first one to answer yes.
 */
@PublicApi
public class CombinedWiringFactory implements WiringFactory {
    private final List<WiringFactory> factories;

    public CombinedWiringFactory(List<WiringFactory> factories) {
        assertNotNull(factories, () -> "You must provide a list of wiring factories");
        this.factories = new ArrayList<>(factories);
    }

    @Override
    public boolean providesTypeResolver(InterfaceWiringEnvironment environment) {
        for (WiringFactory factory : factories) {
            if (factory.providesTypeResolver(environment)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TypeResolver getTypeResolver(InterfaceWiringEnvironment environment) {
        for (WiringFactory factory : factories) {
            if (factory.providesTypeResolver(environment)) {
                return factory.getTypeResolver(environment);
            }
        }
        return assertShouldNeverHappen();
    }

    @Override
    public boolean providesTypeResolver(UnionWiringEnvironment environment) {
        for (WiringFactory factory : factories) {
            if (factory.providesTypeResolver(environment)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TypeResolver getTypeResolver(UnionWiringEnvironment environment) {
        for (WiringFactory factory : factories) {
            if (factory.providesTypeResolver(environment)) {
                return factory.getTypeResolver(environment);
            }
        }
        return assertShouldNeverHappen();
    }

    @Override
    public boolean providesDataFetcherFactory(FieldWiringEnvironment environment) {
        for (WiringFactory factory : factories) {
            if (factory.providesDataFetcherFactory(environment)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T> DataFetcherFactory<T> getDataFetcherFactory(FieldWiringEnvironment environment) {
        for (WiringFactory factory : factories) {
            if (factory.providesDataFetcherFactory(environment)) {
                return factory.getDataFetcherFactory(environment);
            }
        }
        return assertShouldNeverHappen();
    }

    @Override
    public boolean providesDataFetcher(FieldWiringEnvironment environment) {
        for (WiringFactory factory : factories) {
            if (factory.providesDataFetcher(environment)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DataFetcher getDataFetcher(FieldWiringEnvironment environment) {
        for (WiringFactory factory : factories) {
            if (factory.providesDataFetcher(environment)) {
                return factory.getDataFetcher(environment);
            }
        }
        return assertShouldNeverHappen();
    }

    @Override
    public boolean providesScalar(ScalarWiringEnvironment environment) {
        for (WiringFactory factory : factories) {
            if (factory.providesScalar(environment)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public GraphQLScalarType getScalar(ScalarWiringEnvironment environment) {
        for (WiringFactory factory : factories) {
            if (factory.providesScalar(environment)) {
                return factory.getScalar(environment);
            }
        }
        return assertShouldNeverHappen();
    }

    @Override
    public boolean providesSchemaDirectiveWiring(SchemaDirectiveWiringEnvironment environment) {
        for (WiringFactory factory : factories) {
            if (factory.providesSchemaDirectiveWiring(environment)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SchemaDirectiveWiring getSchemaDirectiveWiring(SchemaDirectiveWiringEnvironment environment) {
        for (WiringFactory factory : factories) {
            if (factory.providesSchemaDirectiveWiring(environment)) {
                return factory.getSchemaDirectiveWiring(environment);
            }
        }
        return assertShouldNeverHappen();
    }

    @Override
    public DataFetcher getDefaultDataFetcher(FieldWiringEnvironment environment) {
        for (WiringFactory factory : factories) {
            if (factory.getDefaultDataFetcher(environment) != null) {
                return factory.getDefaultDataFetcher(environment);
            }
        }
        return null;
    }

}
