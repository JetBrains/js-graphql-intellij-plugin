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
package com.intellij.lang.jsgraphql.types.language;

import com.intellij.lang.jsgraphql.types.AssertException;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.Scalars;
import com.intellij.lang.jsgraphql.types.schema.*;
import com.intellij.lang.jsgraphql.types.util.FpKit;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.isList;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.isNonNull;

@Internal
public class AstValueHelper {

  /**
   * Produces a GraphQL Value AST given a Java value.
   * <p>
   * A GraphQL type must be provided, which will be used to interpret different
   * Java values.
   *
   * <pre>
   * |      Value    | GraphQL Value        |
   * | ------------- | -------------------- |
   * | Object        | Input Object         |
   * | Array         | List                 |
   * | Boolean       | Boolean              |
   * | String        | String / Enum Value  |
   * | Number        | Int / Float          |
   * | Mixed         | Enum Value           |
   * </pre>
   *
   * @param value - the java value to be converted into graphql ast
   * @param type  the graphql type of the object
   * @return a grapql language ast {@link Value}
   */
  public static Value<?> astFromValue(Object value, GraphQLType type) {
    if (value == null) {
      return null;
    }

    if (isNonNull(type)) {
      return handleNonNull(value, (GraphQLNonNull)type);
    }

    // Convert JavaScript array to GraphQL list. If the GraphQLType is a list, but
    // the value is not an array, convert the value using the list's item type.
    if (isList(type)) {
      return handleList(value, (GraphQLList)type);
    }

    // Populate the fields of the input object by creating ASTs from each value
    // in the JavaScript object according to the fields in the input type.
    if (type instanceof GraphQLInputObjectType) {
      return handleInputObject(value, (GraphQLInputObjectType)type);
    }

    if (!(type instanceof GraphQLScalarType || type instanceof GraphQLEnumType)) {
      throw new AssertException("Must provide Input Type, cannot use: " + type.getClass());
    }

    // Since value is an internally represented value, it must be serialized
    // to an externally represented value before converting into an AST.
    final Object serialized = serialize(type, value);
    if (isNullish(serialized)) {
      return null;
    }

    // Others serialize based on their corresponding JavaScript scalar types.
    if (serialized instanceof Boolean) {
      return BooleanValue.newBooleanValue().value((Boolean)serialized).build();
    }

    String stringValue = serialized.toString();
    // numbers can be Int or Float values.
    if (serialized instanceof Number) {
      return handleNumber(stringValue);
    }

    if (serialized instanceof String) {
      // Enum types use Enum literals.
      if (type instanceof GraphQLEnumType) {
        return EnumValue.newEnumValue().name(stringValue).build();
      }

      // ID types can use Int literals.
      if (type == Scalars.GraphQLID && stringValue.matches("^[0-9]+$")) {
        return IntValue.newIntValue().value(new BigInteger(stringValue)).build();
      }

      return StringValue.newStringValue().value(stringValue).build();
    }

    throw new AssertException("'Cannot convert value to AST: " + serialized);
  }

  private static Value<?> handleInputObject(Object javaValue, GraphQLInputObjectType type) {
    List<GraphQLInputObjectField> fields = type.getFields();
    List<ObjectField> fieldNodes = new ArrayList<>();
    fields.forEach(field -> {
      String fieldName = field.getName();
      GraphQLInputType fieldType = field.getType();
      Object fieldValueObj = getPropertyValue(javaValue, fieldName);
      Value<?> nodeValue = astFromValue(fieldValueObj, fieldType);
      if (nodeValue != null) {
        fieldNodes.add(ObjectField.newObjectField().name(fieldName).value(nodeValue).build());
      }
    });
    return ObjectValue.newObjectValue().objectFields(fieldNodes).build();
  }

  private static Object getPropertyValue(Object javaValue, String fieldName) {
    if (javaValue instanceof Map<?, ?> map) {
      return map.get(fieldName);
    }
    return null;
  }

  private static Value<?> handleNumber(String stringValue) {
    if (stringValue.matches("^[0-9]+$")) {
      return IntValue.newIntValue().value(new BigInteger(stringValue)).build();
    }
    else {
      return FloatValue.newFloatValue().value(new BigDecimal(stringValue)).build();
    }
  }

  @SuppressWarnings("rawtypes")
  private static Value<?> handleList(Object _value, GraphQLList type) {
    GraphQLType itemType = type.getWrappedType();
    boolean isIterable = _value instanceof Iterable;
    if (isIterable || (_value != null && _value.getClass().isArray())) {
      Iterable<?> iterable = isIterable ? (Iterable<?>)_value : FpKit.toCollection(_value);
      List<Value> valuesNodes = new ArrayList<>();
      for (Object item : iterable) {
        Value<?> itemNode = astFromValue(item, itemType);
        if (itemNode != null) {
          valuesNodes.add(itemNode);
        }
      }
      return ArrayValue.newArrayValue().values(valuesNodes).build();
    }
    return astFromValue(_value, itemType);
  }

  private static Value<?> handleNonNull(Object _value, GraphQLNonNull type) {
    GraphQLType wrappedType = type.getWrappedType();
    return astFromValue(_value, wrappedType);
  }

  private static Object serialize(GraphQLType type, Object value) {
    if (type instanceof GraphQLScalarType) {
      return ((GraphQLScalarType)type).getCoercing().serialize(value);
    }
    else {
      return ((GraphQLEnumType)type).serialize(value);
    }
  }

  private static boolean isNullish(Object serialized) {
    if (serialized instanceof Number) {
      return Double.isNaN(((Number)serialized).doubleValue());
    }
    return serialized == null;
  }
}
