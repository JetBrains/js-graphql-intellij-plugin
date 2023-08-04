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
import com.intellij.lang.jsgraphql.types.language.FloatValue;
import com.intellij.lang.jsgraphql.types.language.IntValue;
import com.intellij.lang.jsgraphql.types.language.StringValue;
import com.intellij.lang.jsgraphql.types.schema.Coercing;
import com.intellij.lang.jsgraphql.types.schema.CoercingParseLiteralException;
import com.intellij.lang.jsgraphql.types.schema.CoercingParseValueException;
import com.intellij.lang.jsgraphql.types.schema.CoercingSerializeException;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.intellij.lang.jsgraphql.types.scalar.CoercingUtil.isNumberIsh;
import static com.intellij.lang.jsgraphql.types.scalar.CoercingUtil.typeName;

@Internal
public class GraphqlBigIntegerCoercing implements Coercing<BigInteger, BigInteger> {

  private BigInteger convertImpl(Object input) {
    if (isNumberIsh(input)) {
      BigDecimal value;
      try {
        value = new BigDecimal(input.toString());
      }
      catch (NumberFormatException e) {
        return null;
      }
      try {
        return value.toBigIntegerExact();
      }
      catch (ArithmeticException e) {
        return null;
      }
    }
    return null;
  }

  @Override
  public BigInteger serialize(Object input) {
    BigInteger result = convertImpl(input);
    if (result == null) {
      throw new CoercingSerializeException(
        "Expected type 'BigInteger' but was '" + typeName(input) + "'."
      );
    }
    return result;
  }

  @Override
  public BigInteger parseValue(Object input) {
    BigInteger result = convertImpl(input);
    if (result == null) {
      throw new CoercingParseValueException(
        "Expected type 'BigInteger' but was '" + typeName(input) + "'."
      );
    }
    return result;
  }

  @Override
  public BigInteger parseLiteral(Object input) {
    if (input instanceof StringValue) {
      try {
        return new BigDecimal(((StringValue)input).getValue()).toBigIntegerExact();
      }
      catch (NumberFormatException | ArithmeticException e) {
        throw new CoercingParseLiteralException(
          "Unable to turn input into a 'BigInteger'"
        );
      }
    }
    else if (input instanceof IntValue) {
      return ((IntValue)input).getValue();
    }
    else if (input instanceof FloatValue) {
      try {
        return ((FloatValue)input).getValue().toBigIntegerExact();
      }
      catch (ArithmeticException e) {
        throw new CoercingParseLiteralException(
          "Unable to turn input into a 'BigInteger'"
        );
      }
    }
    throw new CoercingParseLiteralException(
      "Expected type 'Int', 'String' or 'Float' but was '" + GraphQLSchemaUtil.getValueTypeName(input) + "'."
    );
  }
}
