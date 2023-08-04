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
package com.intellij.lang.jsgraphql.types.schema.diff;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.lang.jsgraphql.types.language.Type;
import com.intellij.lang.jsgraphql.types.language.TypeDefinition;
import com.intellij.lang.jsgraphql.types.schema.diff.reporting.DifferenceReporter;

import java.util.*;

/*
 * A helper class that represents diff state (eg visited types) as well as helpers
 */
@Internal
class DiffCtx {
  final List<String> examinedTypes = new ArrayList<>();
  final Deque<String> currentTypes = new ArrayDeque<>();
  private final DifferenceReporter reporter;
  final Document oldDoc;
  final Document newDoc;

  DiffCtx(DifferenceReporter reporter, Document oldDoc, Document newDoc) {
    this.reporter = reporter;
    this.oldDoc = oldDoc;
    this.newDoc = newDoc;
  }

  void report(DiffEvent differenceEvent) {
    reporter.report(differenceEvent);
  }

  boolean examiningType(String typeName) {
    if (examinedTypes.contains(typeName)) {
      return true;
    }
    examinedTypes.add(typeName);
    currentTypes.push(typeName);
    return false;
  }

  void exitType() {
    currentTypes.pop();
  }

  <T extends TypeDefinition> Optional<T> getOldTypeDef(Type type, Class<T> typeDefClass) {
    return getType(SchemaDiff.getTypeName(type), typeDefClass, oldDoc);
  }

  <T extends TypeDefinition> Optional<T> getNewTypeDef(Type type, Class<T> typeDefClass) {
    return getType(SchemaDiff.getTypeName(type), typeDefClass, newDoc);
  }

  private <T extends TypeDefinition> Optional<T> getType(String typeName, Class<T> typeDefClass, Document doc) {
    if (typeName == null) {
      return Optional.empty();
    }
    return doc.getDefinitions().stream()
      .filter(def -> typeDefClass.isAssignableFrom(def.getClass()))
      .map(typeDefClass::cast)
      .filter(defT -> defT.getName().equals(typeName))
      .findFirst();
  }
}
