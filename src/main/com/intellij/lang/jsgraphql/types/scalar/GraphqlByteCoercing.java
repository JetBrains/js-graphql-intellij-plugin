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
package com.intellij.lang.jsgraphql.types.scalar;

import com.intellij.lang.jsgraphql.schema.GraphQLSchemaUtil;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.IntValue;
import com.intellij.lang.jsgraphql.types.schema.Coercing;
import com.intellij.lang.jsgraphql.types.schema.CoercingParseLiteralException;
import com.intellij.lang.jsgraphql.types.schema.CoercingParseValueException;
import com.intellij.lang.jsgraphql.types.schema.CoercingSerializeException;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.intellij.lang.jsgraphql.types.scalar.CoercingUtil.isNumberIsh;
import static com.intellij.lang.jsgraphql.types.scalar.CoercingUtil.typeName;

@Internal
public class GraphqlByteCoercing implements Coercing<Byte, Byte> {

  private static final BigInteger BYTE_MAX = BigInteger.valueOf(Byte.MAX_VALUE);
  private static final BigInteger BYTE_MIN = BigInteger.valueOf(Byte.MIN_VALUE);

  private Byte convertImpl(Object input) {
    if (input instanceof Byte) {
      return (Byte)input;
    }
    else if (isNumberIsh(input)) {
      BigDecimal value;
      try {
        value = new BigDecimal(input.toString());
      }
      catch (NumberFormatException e) {
        return null;
      }
      try {
        return value.byteValueExact();
      }
      catch (ArithmeticException e) {
        return null;
      }
    }
    else {
      return null;
    }
  }

  @Override
  public Byte serialize(Object input) {
    Byte result = convertImpl(input);
    if (result == null) {
      throw new CoercingSerializeException(
        "Expected type 'Byte' but was '" + typeName(input) + "'."
      );
    }
    return result;
  }

  @Override
  public Byte parseValue(Object input) {
    Byte result = convertImpl(input);
    if (result == null) {
      throw new CoercingParseValueException(
        "Expected type 'Byte' but was '" + typeName(input) + "'."
      );
    }
    return result;
  }

  @Override
  public Byte parseLiteral(Object input) {
    if (!(input instanceof IntValue)) {
      throw new CoercingParseLiteralException(
        "Expected type 'Int' but was '" + GraphQLSchemaUtil.getValueTypeName(input) + "'."
      );
    }
    BigInteger value = ((IntValue)input).getValue();
    if (value.compareTo(BYTE_MIN) < 0 || value.compareTo(BYTE_MAX) > 0) {
      throw new CoercingParseLiteralException(
        "Expected value to be in the Byte range but it was '" + value + "'"
      );
    }
    return value.byteValue();
  }
}
