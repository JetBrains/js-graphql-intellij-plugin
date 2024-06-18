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
package com.intellij.lang.jsgraphql.types.validation.rules;


import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Definition;
import com.intellij.lang.jsgraphql.types.language.FragmentDefinition;
import com.intellij.lang.jsgraphql.types.language.FragmentSpread;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.validation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Internal
public class NoFragmentCycles extends AbstractRule {

  private final Map<String, List<FragmentSpread>> fragmentSpreads = new LinkedHashMap<>();


  public NoFragmentCycles(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
    super(validationContext, validationErrorCollector);
    prepareFragmentMap();
  }

  private void prepareFragmentMap() {
    List<Definition> definitions = getValidationContext().getDocument().getDefinitions();
    for (Definition definition : definitions) {
      if (definition instanceof FragmentDefinition fragmentDefinition) {
        fragmentSpreads.put(fragmentDefinition.getName(), gatherSpreads(fragmentDefinition));
      }
    }
  }


  private List<FragmentSpread> gatherSpreads(FragmentDefinition fragmentDefinition) {
    final List<FragmentSpread> fragmentSpreads = new ArrayList<>();
    DocumentVisitor visitor = new DocumentVisitor() {
      @Override
      public void enter(Node node, List<Node> path) {
        if (node instanceof FragmentSpread) {
          fragmentSpreads.add((FragmentSpread)node);
        }
      }

      @Override
      public void leave(Node node, List<Node> path) {

      }
    };

    new LanguageTraversal().traverse(fragmentDefinition, visitor);
    return fragmentSpreads;
  }


  @Override
  public void checkFragmentDefinition(FragmentDefinition fragmentDefinition) {
    List<FragmentSpread> spreadPath = new ArrayList<>();
    detectCycleRecursive(fragmentDefinition.getName(), fragmentDefinition.getName(), spreadPath);
  }

  private void detectCycleRecursive(String fragmentName, String initialName, List<FragmentSpread> spreadPath) {
    List<FragmentSpread> fragmentSpreads = this.fragmentSpreads.get(fragmentName);
    if (fragmentSpreads == null) {
      // KnownFragmentNames will have picked this up.  Lets not NPE
      return;
    }

    outer:
    for (FragmentSpread fragmentSpread : fragmentSpreads) {

      if (fragmentSpread.getName().equals(initialName)) {
        String message = "Fragment cycles not allowed";
        addError(ValidationErrorType.FragmentCycle, spreadPath, message);
        continue;
      }
      for (FragmentSpread spread : spreadPath) {
        if (spread.equals(fragmentSpread)) {
          continue outer;
        }
      }
      spreadPath.add(fragmentSpread);
      detectCycleRecursive(fragmentSpread.getName(), initialName, spreadPath);
      spreadPath.remove(spreadPath.size() - 1);
    }
  }
}
