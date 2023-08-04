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
package com.intellij.lang.jsgraphql.types.schema.diff.reporting;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.schema.diff.DiffEvent;
import com.intellij.lang.jsgraphql.types.schema.diff.DiffLevel;

import java.io.PrintStream;

/**
 * A reporter that prints its output to a PrintStream
 */
@PublicApi
public class PrintStreamReporter implements DifferenceReporter {

  int breakageCount = 0;
  int dangerCount = 0;
  final PrintStream out;

  public PrintStreamReporter() {
    this(System.out);
  }

  public PrintStreamReporter(PrintStream out) {
    this.out = out;
  }

  @Override
  public void report(DiffEvent differenceEvent) {
    if (differenceEvent.getLevel() == DiffLevel.BREAKING) {
      breakageCount++;
    }
    if (differenceEvent.getLevel() == DiffLevel.DANGEROUS) {
      dangerCount++;
    }

    printEvent(differenceEvent);
  }

  private void printEvent(DiffEvent event) {
    String indent = event.getLevel() == DiffLevel.INFO ? "\t" : "";
    String level = event.getLevel() == DiffLevel.INFO ? "info" : event.getLevel().toString();
    String objectName = event.getTypeName();
    if (event.getFieldName() != null) {
      objectName = objectName + "." + event.getFieldName();
    }
    out.printf(
      "%s%s - '%s' : '%s' : %s%n",
      indent, level, event.getTypeKind(), objectName, event.getReasonMsg());
  }

  @Override
  public void onEnd() {
    out.println("\n");
    out.printf("%d errors%n", breakageCount);
    out.printf("%d warnings%n", dangerCount);
    out.println("\n");
  }
}
