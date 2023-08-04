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

import com.intellij.lang.jsgraphql.types.PublicApi;

import java.io.Serializable;
import java.util.Objects;

@PublicApi
public class IgnoredChar implements Serializable {

  public enum IgnoredCharKind {
    SPACE, COMMA, TAB, CR, LF, OTHER
  }

  private final String value;
  private final IgnoredCharKind kind;
  private final SourceLocation sourceLocation;


  public IgnoredChar(String value, IgnoredCharKind kind, SourceLocation sourceLocation) {
    this.value = value;
    this.kind = kind;
    this.sourceLocation = sourceLocation;
  }

  public String getValue() {
    return value;
  }

  public IgnoredCharKind getKind() {
    return kind;
  }

  public SourceLocation getSourceLocation() {
    return sourceLocation;
  }

  @Override
  public String toString() {
    return "IgnoredChar{" +
           "value='" + value + '\'' +
           ", kind=" + kind +
           ", sourceLocation=" + sourceLocation +
           '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IgnoredChar that = (IgnoredChar)o;
    return Objects.equals(value, that.value) &&
           kind == that.kind &&
           Objects.equals(sourceLocation, that.sourceLocation);
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + Objects.hashCode(value);
    result = 31 * result + Objects.hashCode(kind);
    result = 31 * result + Objects.hashCode(sourceLocation);
    return result;
  }
}
