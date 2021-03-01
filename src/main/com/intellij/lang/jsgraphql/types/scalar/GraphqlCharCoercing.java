package com.intellij.lang.jsgraphql.types.scalar;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.StringValue;
import com.intellij.lang.jsgraphql.types.schema.Coercing;
import com.intellij.lang.jsgraphql.types.schema.CoercingParseLiteralException;
import com.intellij.lang.jsgraphql.types.schema.CoercingParseValueException;
import com.intellij.lang.jsgraphql.types.schema.CoercingSerializeException;

import static com.intellij.lang.jsgraphql.types.scalar.CoercingUtil.typeName;

@Internal
public class GraphqlCharCoercing implements Coercing<Character, Character> {

    private Character convertImpl(Object input) {
        if (input instanceof String && ((String) input).length() == 1) {
            return ((String) input).charAt(0);
        } else if (input instanceof Character) {
            return (Character) input;
        } else {
            return null;
        }

    }

    @Override
    public Character serialize(Object input) {
        Character result = convertImpl(input);
        if (result == null) {
            throw new CoercingSerializeException(
                    "Expected type 'Char' but was '" + typeName(input) + "'."
            );
        }
        return result;
    }

    @Override
    public Character parseValue(Object input) {
        Character result = convertImpl(input);
        if (result == null) {
            throw new CoercingParseValueException(
                    "Expected type 'Char' but was '" + typeName(input) + "'."
            );
        }
        return result;
    }

    @Override
    public Character parseLiteral(Object input) {
        if (!(input instanceof StringValue)) {
            throw new CoercingParseLiteralException(
                    "Expected AST type 'StringValue' but was '" + typeName(input) + "'."
            );
        }
        String value = ((StringValue) input).getValue();
        if (value.length() != 1) {
            throw new CoercingParseLiteralException(
                    "Empty 'StringValue' provided."
            );
        }
        return value.charAt(0);
    }
}
