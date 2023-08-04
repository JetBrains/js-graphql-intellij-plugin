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
package com.intellij.lang.jsgraphql.types;

import com.intellij.lang.jsgraphql.types.language.SourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.map;

/**
 * This little helper allows GraphQlErrors to implement
 * common things (hashcode/ equals ) and to specification more easily
 */
@SuppressWarnings("SimplifiableIfStatement")
@Internal
public class GraphqlErrorHelper {

  public static Map<String, Object> toSpecification(GraphQLError error) {
    Map<String, Object> errorMap = new LinkedHashMap<>();
    errorMap.put("message", error.getMessage());
    if (error.getLocations() != null) {
      errorMap.put("locations", locations(error.getLocations()));
    }
    if (error.getPath() != null) {
      errorMap.put("path", error.getPath());
    }

    Map<String, Object> extensions = error.getExtensions();
    ErrorClassification errorClassification = error.getErrorType();
    //
    // we move the ErrorClassification into extensions which allows
    // downstream people to see them but still be spec compliant
    if (errorClassification != null) {
      if (extensions != null) {
        extensions = new LinkedHashMap<>(extensions);
      }
      else {
        extensions = new LinkedHashMap<>();
      }
      // put in the classification unless its already there
      if (!extensions.containsKey("classification")) {
        extensions.put("classification", errorClassification.toSpecification(error));
      }
    }

    if (extensions != null) {
      errorMap.put("extensions", extensions);
    }
    return errorMap;
  }

  public static Object locations(List<SourceLocation> locations) {
    return map(locations, GraphqlErrorHelper::location);
  }

  public static Object location(SourceLocation location) {
    Map<String, Integer> map = new LinkedHashMap<>();
    map.put("line", location.getLine());
    map.put("column", location.getColumn());
    return map;
  }

  public static int hashCode(GraphQLError dis) {
    int result = 1;
    result = 31 * result + Objects.hashCode(dis.getMessage());
    result = 31 * result + Objects.hashCode(dis.getLocations());
    result = 31 * result + Objects.hashCode(dis.getPath());
    result = 31 * result + Objects.hashCode(dis.getErrorType());
    return result;
  }

  public static boolean equals(GraphQLError dis, Object o) {
    if (dis == o) {
      return true;
    }
    if (o == null || dis.getClass() != o.getClass()) return false;

    GraphQLError dat = (GraphQLError)o;

    if (!Objects.equals(dis.getMessage(), dat.getMessage())) {
      return false;
    }
    if (!Objects.equals(dis.getLocations(), dat.getLocations())) {
      return false;
    }
    if (!Objects.equals(dis.getPath(), dat.getPath())) {
      return false;
    }
    return dis.getErrorType() == dat.getErrorType();
  }
}
