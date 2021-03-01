package com.intellij.lang.jsgraphql.types.scalar;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.StringValue;
import com.intellij.lang.jsgraphql.types.schema.Coercing;
import com.intellij.lang.jsgraphql.types.schema.CoercingParseLiteralException;

import static com.intellij.lang.jsgraphql.types.scalar.CoercingUtil.typeName;

@Internal
public class GraphqlStringCoercing implements Coercing<String, String> {
    @Override
    public String serialize(Object input) {
        return input.toString();
    }

    @Override
    public String parseValue(Object input) {
        return serialize(input);
    }

    @Override
    public String parseLiteral(Object input) {
        if (!(input instanceof StringValue)) {
            throw new CoercingParseLiteralException(
                    "Expected AST type 'StringValue' but was '" + typeName(input) + "'."
            );
        }
        return ((StringValue) input).getValue();
    }
}
