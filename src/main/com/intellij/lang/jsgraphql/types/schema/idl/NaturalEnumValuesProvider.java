package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.PublicApi;

/**
 * Simple EnumValuesProvided which maps the GraphQL Enum name to the Java Enum instance.
 */
@PublicApi
public class NaturalEnumValuesProvider<T extends Enum<T>> implements EnumValuesProvider {


    private final Class<T> enumType;

    public NaturalEnumValuesProvider(Class<T> enumType) {
        Assert.assertNotNull(enumType, () -> "enumType can't be null");
        this.enumType = enumType;
    }

    @Override
    public T getValue(String name) {
        return Enum.valueOf(enumType, name);
    }
}
