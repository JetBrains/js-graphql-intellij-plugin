package com.intellij.lang.jsgraphql.types.scalar;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.BooleanValue;
import com.intellij.lang.jsgraphql.types.schema.Coercing;
import com.intellij.lang.jsgraphql.types.schema.CoercingParseLiteralException;
import com.intellij.lang.jsgraphql.types.schema.CoercingParseValueException;
import com.intellij.lang.jsgraphql.types.schema.CoercingSerializeException;

import java.math.BigDecimal;

import static com.intellij.lang.jsgraphql.types.Assert.assertShouldNeverHappen;
import static com.intellij.lang.jsgraphql.types.scalar.CoercingUtil.isNumberIsh;
import static com.intellij.lang.jsgraphql.types.scalar.CoercingUtil.typeName;

@Internal
public class GraphqlBooleanCoercing implements Coercing<Boolean, Boolean> {

    private Boolean convertImpl(Object input) {
        if (input instanceof Boolean) {
            return (Boolean) input;
        } else if (input instanceof String) {
            return Boolean.parseBoolean((String) input);
        } else if (isNumberIsh(input)) {
            BigDecimal value;
            try {
                value = new BigDecimal(input.toString());
            } catch (NumberFormatException e) {
                // this should never happen because String is handled above
                return assertShouldNeverHappen();
            }
            return value.compareTo(BigDecimal.ZERO) != 0;
        } else {
            return null;
        }

    }

    @Override
    public Boolean serialize(Object input) {
        Boolean result = convertImpl(input);
        if (result == null) {
            throw new CoercingSerializeException(
                    "Expected type 'Boolean' but was '" + typeName(input) + "'."
            );
        }
        return result;
    }

    @Override
    public Boolean parseValue(Object input) {
        Boolean result = convertImpl(input);
        if (result == null) {
            throw new CoercingParseValueException(
                    "Expected type 'Boolean' but was '" + typeName(input) + "'."
            );
        }
        return result;
    }

    @Override
    public Boolean parseLiteral(Object input) {
        if (!(input instanceof BooleanValue)) {
            throw new CoercingParseLiteralException(
                    "Expected AST type 'BooleanValue' but was '" + typeName(input) + "'."
            );
        }
        return ((BooleanValue) input).isValue();
    }
}
