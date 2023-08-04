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
package com.intellij.lang.jsgraphql.types.validation;


import com.google.common.collect.ImmutableList;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.*;

import java.util.*;

@Internal
public class RulesVisitor implements DocumentVisitor {

  private final ImmutableList<AbstractRule> rules;
  private final ValidationContext validationContext;
  private boolean subVisitor;
  private final List<AbstractRule> rulesVisitingFragmentSpreads = new ArrayList<>();
  private final Map<Node, List<AbstractRule>> rulesToSkipByUntilNode = new IdentityHashMap<>();
  private final Set<AbstractRule> rulesToSkip = new LinkedHashSet<>();

  public RulesVisitor(ValidationContext validationContext, List<AbstractRule> rules) {
    this(validationContext, rules, false);
  }

  public RulesVisitor(ValidationContext validationContext, List<AbstractRule> rules, boolean subVisitor) {
    this.validationContext = validationContext;
    this.subVisitor = subVisitor;
    this.rules = ImmutableList.copyOf(rules);
    this.subVisitor = subVisitor;
    findRulesVisitingFragmentSpreads();
  }

  private void findRulesVisitingFragmentSpreads() {
    for (AbstractRule rule : rules) {
      if (rule.isVisitFragmentSpreads()) {
        rulesVisitingFragmentSpreads.add(rule);
      }
    }
  }

  @Override
  public void enter(Node node, List<Node> ancestors) {
    validationContext.getTraversalContext().enter(node, ancestors);
    Set<AbstractRule> tmpRulesSet = new LinkedHashSet<>(this.rules);
    tmpRulesSet.removeAll(rulesToSkip);
    List<AbstractRule> rulesToConsider = new ArrayList<>(tmpRulesSet);
    if (node instanceof Document) {
      checkDocument((Document)node, rulesToConsider);
    }
    else if (node instanceof Argument) {
      checkArgument((Argument)node, rulesToConsider);
    }
    else if (node instanceof TypeName) {
      checkTypeName((TypeName)node, rulesToConsider);
    }
    else if (node instanceof VariableDefinition) {
      checkVariableDefinition((VariableDefinition)node, rulesToConsider);
    }
    else if (node instanceof Field) {
      checkField((Field)node, rulesToConsider);
    }
    else if (node instanceof InlineFragment) {
      checkInlineFragment((InlineFragment)node, rulesToConsider);
    }
    else if (node instanceof Directive) {
      checkDirective((Directive)node, ancestors, rulesToConsider);
    }
    else if (node instanceof FragmentSpread) {
      checkFragmentSpread((FragmentSpread)node, rulesToConsider, ancestors);
    }
    else if (node instanceof FragmentDefinition) {
      checkFragmentDefinition((FragmentDefinition)node, rulesToConsider);
    }
    else if (node instanceof OperationDefinition) {
      checkOperationDefinition((OperationDefinition)node, rulesToConsider);
    }
    else if (node instanceof VariableReference) {
      checkVariable((VariableReference)node, rulesToConsider);
    }
    else if (node instanceof SelectionSet) {
      checkSelectionSet((SelectionSet)node, rulesToConsider);
    }
  }

  private void checkDocument(Document node, List<AbstractRule> rules) {
    for (AbstractRule rule : rules) {
      rule.checkDocument(node);
    }
  }


  private void checkArgument(Argument node, List<AbstractRule> rules) {
    for (AbstractRule rule : rules) {
      rule.checkArgument(node);
    }
  }

  private void checkTypeName(TypeName node, List<AbstractRule> rules) {
    for (AbstractRule rule : rules) {
      rule.checkTypeName(node);
    }
  }


  private void checkVariableDefinition(VariableDefinition variableDefinition, List<AbstractRule> rules) {
    for (AbstractRule rule : rules) {
      rule.checkVariableDefinition(variableDefinition);
    }
  }

  private void checkField(Field field, List<AbstractRule> rules) {
    for (AbstractRule rule : rules) {
      rule.checkField(field);
    }
  }

  private void checkInlineFragment(InlineFragment inlineFragment, List<AbstractRule> rules) {
    for (AbstractRule rule : rules) {
      rule.checkInlineFragment(inlineFragment);
    }
  }

  private void checkDirective(Directive directive, List<Node> ancestors, List<AbstractRule> rules) {
    for (AbstractRule rule : rules) {
      rule.checkDirective(directive, ancestors);
    }
  }

  private void checkFragmentSpread(FragmentSpread fragmentSpread, List<AbstractRule> rules, List<Node> ancestors) {
    for (AbstractRule rule : rules) {
      rule.checkFragmentSpread(fragmentSpread);
    }
    List<AbstractRule> rulesVisitingFragmentSpreads = getRulesVisitingFragmentSpreads(rules);
    if (rulesVisitingFragmentSpreads.size() > 0) {
      FragmentDefinition fragment = validationContext.getFragment(fragmentSpread.getName());
      if (fragment != null && !ancestors.contains(fragment)) {
        new LanguageTraversal(ancestors).traverse(fragment, new RulesVisitor(validationContext, rulesVisitingFragmentSpreads, true));
      }
    }
  }

  private List<AbstractRule> getRulesVisitingFragmentSpreads(List<AbstractRule> rules) {
    List<AbstractRule> result = new ArrayList<>();
    for (AbstractRule rule : rules) {
      if (rule.isVisitFragmentSpreads()) result.add(rule);
    }
    return result;
  }


  private void checkFragmentDefinition(FragmentDefinition fragmentDefinition, List<AbstractRule> rules) {
    if (!subVisitor) {
      rulesToSkipByUntilNode.put(fragmentDefinition, new ArrayList<>(rulesVisitingFragmentSpreads));
      rulesToSkip.addAll(rulesVisitingFragmentSpreads);
    }


    for (AbstractRule rule : rules) {
      if (!subVisitor && (rule.isVisitFragmentSpreads())) continue;
      rule.checkFragmentDefinition(fragmentDefinition);
    }
  }

  private void checkOperationDefinition(OperationDefinition operationDefinition, List<AbstractRule> rules) {
    for (AbstractRule rule : rules) {
      rule.checkOperationDefinition(operationDefinition);
    }
  }

  private void checkSelectionSet(SelectionSet selectionSet, List<AbstractRule> rules) {
    for (AbstractRule rule : rules) {
      rule.checkSelectionSet(selectionSet);
    }
  }

  private void checkVariable(VariableReference variableReference, List<AbstractRule> rules) {
    for (AbstractRule rule : rules) {
      rule.checkVariable(variableReference);
    }
  }


  @Override
  public void leave(Node node, List<Node> ancestors) {
    validationContext.getTraversalContext().leave(node, ancestors);

    if (node instanceof Document) {
      documentFinished((Document)node);
    }
    else if (node instanceof OperationDefinition) {
      leaveOperationDefinition((OperationDefinition)node);
    }
    else if (node instanceof SelectionSet) {
      leaveSelectionSet((SelectionSet)node);
    }

    if (rulesToSkipByUntilNode.containsKey(node)) {
      rulesToSkip.removeAll(rulesToSkipByUntilNode.get(node));
      rulesToSkipByUntilNode.remove(node);
    }
  }

  private void leaveSelectionSet(SelectionSet selectionSet) {
    for (AbstractRule rule : rules) {
      rule.leaveSelectionSet(selectionSet);
    }
  }

  private void leaveOperationDefinition(OperationDefinition operationDefinition) {
    for (AbstractRule rule : rules) {
      rule.leaveOperationDefinition(operationDefinition);
    }
  }

  private void documentFinished(Document document) {
    for (AbstractRule rule : rules) {
      rule.documentFinished(document);
    }
  }
}
