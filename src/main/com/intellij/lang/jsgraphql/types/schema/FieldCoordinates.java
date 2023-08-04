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
package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.AssertException;
import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.Objects;

import static com.intellij.lang.jsgraphql.types.Assert.assertTrue;
import static com.intellij.lang.jsgraphql.types.Assert.assertValidName;

/**
 * A field in graphql is uniquely located within a parent type and hence code elements
 * like {@link com.intellij.lang.jsgraphql.types.schema.DataFetcher} need to be specified using those coordinates.
 */
@PublicApi
public class FieldCoordinates {

  private final boolean systemCoordinates;
  private final String typeName;
  private final String fieldName;

  private FieldCoordinates(String typeName, String fieldName, boolean systemCoordinates) {
    this.typeName = typeName;
    this.fieldName = fieldName;
    this.systemCoordinates = systemCoordinates;
  }

  public String getTypeName() {
    return typeName;
  }

  public String getFieldName() {
    return fieldName;
  }


  /**
   * Checks the validity of the field coordinate names.  The validity checks vary by coordinate type.  Standard
   * coordinates validate both the {@code typeName} and {@code fieldName}, while system coordinates do not have
   * a parent so they only validate the {@code fieldName}.
   *
   * @throws AssertException if the coordinates are NOT valid; otherwise, returns normally.
   */
  public void assertValidNames() throws AssertException {
    if (systemCoordinates) {
      assertTrue((null != fieldName) &&
                 fieldName.startsWith("__"), () -> "Only __ system fields can be addressed without a parent type");
      assertValidName(fieldName);
    }
    else {
      assertValidName(typeName);
      assertValidName(fieldName);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FieldCoordinates that = (FieldCoordinates)o;
    return Objects.equals(typeName, that.typeName) &&
           Objects.equals(fieldName, that.fieldName);
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + Objects.hashCode(typeName);
    result = 31 * result + Objects.hashCode(fieldName);
    return result;
  }

  @Override
  public String toString() {
    return typeName + ':' + fieldName + '\'';
  }

  /**
   * Creates new field coordinates
   *
   * @param parentType      the container of the field
   * @param fieldDefinition the field definition
   * @return new field coordinates represented by the two parameters
   */
  public static FieldCoordinates coordinates(GraphQLFieldsContainer parentType, GraphQLFieldDefinition fieldDefinition) {
    return new FieldCoordinates(parentType.getName(), fieldDefinition.getName(), false);
  }

  /**
   * Creates new field coordinates
   *
   * @param parentType the container of the field
   * @param fieldName  the field name
   * @return new field coordinates represented by the two parameters
   */
  public static FieldCoordinates coordinates(String parentType, String fieldName) {
    return new FieldCoordinates(parentType, fieldName, false);
  }

  /**
   * The exception to the general rule is the system __xxxx Introspection fields which have no parent type and
   * are able to be specified on any type
   *
   * @param fieldName the name of the system field which MUST start with __
   * @return the coordinates
   */
  public static FieldCoordinates systemCoordinates(String fieldName) {
    return new FieldCoordinates(null, fieldName, true);
  }
}
