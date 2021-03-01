package com.intellij.lang.jsgraphql.types.scalar;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.FloatValue;
import com.intellij.lang.jsgraphql.types.language.IntValue;
import com.intellij.lang.jsgraphql.types.language.StringValue;
import com.intellij.lang.jsgraphql.types.schema.Coercing;
import com.intellij.lang.jsgraphql.types.schema.CoercingParseLiteralException;
import com.intellij.lang.jsgraphql.types.schema.CoercingParseValueException;
import com.intellij.lang.jsgraphql.types.schema.CoercingSerializeException;

import java.math.BigDecimal;

import static com.intellij.lang.jsgraphql.types.scalar.CoercingUtil.isNumberIsh;
import static com.intellij.lang.jsgraphql.types.scalar.CoercingUtil.typeName;

@Internal
public class GraphqlBigDecimalCoercing implements Coercing<BigDecimal, BigDecimal> {

    private BigDecimal convertImpl(Object input) {
        if (isNumberIsh(input)) {
            try {
                return new BigDecimal(input.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;

    }

    @Override
    public BigDecimal serialize(Object input) {
        BigDecimal result = convertImpl(input);
        if (result == null) {
            throw new CoercingSerializeException(
                    "Expected type 'BigDecimal' but was '" + typeName(input) + "'."
            );
        }
        return result;
    }

    @Override
    public BigDecimal parseValue(Object input) {
        BigDecimal result = convertImpl(input);
        if (result == null) {
            throw new CoercingParseValueException(
                    "Expected type 'BigDecimal' but was '" + typeName(input) + "'."
            );
        }
        return result;
    }

    @Override
    public BigDecimal parseLiteral(Object input) {
        if (input instanceof StringValue) {
            try {
                return new BigDecimal(((StringValue) input).getValue());
            } catch (NumberFormatException e) {
                throw new CoercingParseLiteralException(
                        "Unable to turn AST input into a 'BigDecimal' : '" + input + "'"
                );
            }
        } else if (input instanceof IntValue) {
            return new BigDecimal(((IntValue) input).getValue());
        } else if (input instanceof FloatValue) {
            return ((FloatValue) input).getValue();
        }
        throw new CoercingParseLiteralException(
                "Expected AST type 'IntValue', 'StringValue' or 'FloatValue' but was '" + typeName(input) + "'."
        );
    }
}
