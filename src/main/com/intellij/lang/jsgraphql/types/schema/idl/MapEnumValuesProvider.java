package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.Map;

@PublicApi
public class MapEnumValuesProvider implements EnumValuesProvider {


    private final Map<String, Object> values;

    public MapEnumValuesProvider(Map<String, Object> values) {
        Assert.assertNotNull(values, () -> "values can't be null");
        this.values = values;
    }

    @Override
    public Object getValue(String name) {
        return values.get(name);
    }
}
