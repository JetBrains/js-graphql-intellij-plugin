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

import java.util.ArrayList;
import java.util.List;

/**
 * A reporter that captures all the difference events as they occur
 */
@PublicApi
public class CapturingReporter implements DifferenceReporter {
  private final List<DiffEvent> events = new ArrayList<>();
  private final List<DiffEvent> infos = new ArrayList<>();
  private final List<DiffEvent> breakages = new ArrayList<>();
  private final List<DiffEvent> dangers = new ArrayList<>();

  @Override
  public void report(DiffEvent differenceEvent) {
    events.add(differenceEvent);

    if (differenceEvent.getLevel() == DiffLevel.BREAKING) {
      breakages.add(differenceEvent);
    }
    else if (differenceEvent.getLevel() == DiffLevel.DANGEROUS) {
      dangers.add(differenceEvent);
    }
    else if (differenceEvent.getLevel() == DiffLevel.INFO) {
      infos.add(differenceEvent);
    }
  }

  @Override
  public void onEnd() {
  }

  public List<DiffEvent> getEvents() {
    return new ArrayList<>(events);
  }

  public List<DiffEvent> getInfos() {
    return new ArrayList<>(infos);
  }

  public List<DiffEvent> getBreakages() {
    return new ArrayList<>(breakages);
  }

  public List<DiffEvent> getDangers() {
    return new ArrayList<>(dangers);
  }

  public int getInfoCount() {
    return infos.size();
  }

  public int getBreakageCount() {
    return breakages.size();
  }

  public int getDangerCount() {
    return dangers.size();
  }
}
