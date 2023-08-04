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
      return (Boolean)input;
    }
    else if (input instanceof String) {
      return Boolean.parseBoolean((String)input);
    }
    else if (isNumberIsh(input)) {
      BigDecimal value;
      try {
        value = new BigDecimal(input.toString());
      }
      catch (NumberFormatException e) {
        // this should never happen because String is handled above
        return assertShouldNeverHappen();
      }
      return value.compareTo(BigDecimal.ZERO) != 0;
    }
    else {
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
        "Expected type 'Boolean' but was '" + GraphQLSchemaUtil.getValueTypeName(input) + "'."
      );
    }
    return ((BooleanValue)input).isValue();
  }
}
