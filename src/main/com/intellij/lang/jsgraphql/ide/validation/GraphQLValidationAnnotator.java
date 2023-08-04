/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.validation;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.daemon.impl.quickfix.RenameElementFix;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil;
import com.intellij.lang.jsgraphql.ide.validation.fixes.GraphQLMissingTypeFix;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInspection;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLUnresolvedReferenceInspection;
import com.intellij.lang.jsgraphql.psi.GraphQLArgument;
import com.intellij.lang.jsgraphql.psi.GraphQLDirective;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaUtil;
import com.intellij.lang.jsgraphql.types.schema.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.text.EditDistance;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInspection.createAnnotation;

public class GraphQLValidationAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
    if (GraphQLInspection.isEditorInspectionHighlightingDisabled(psiElement.getProject(),
                                                                 annotationHolder.getCurrentAnnotationSession().getFile())) {
      return;
    }

    // identifiers - fields, fragment spreads, field arguments, directives, type names, input object fields
    if (psiElement instanceof GraphQLIdentifier) {
      checkIdentifierReferences(psiElement, annotationHolder);
    }

    // valid directive location names
    if (psiElement instanceof GraphQLDirectiveLocation) {
      checkDirectiveLocation(psiElement, annotationHolder);
    }

    // valid enum value names according to spec
    if (psiElement instanceof GraphQLEnumValue) {
      checkEnumValues((GraphQLEnumValue)psiElement, annotationHolder);
    }
  }

  private void checkEnumValues(GraphQLEnumValue psiElement, @NotNull AnnotationHolder annotationHolder) {
    final GraphQLIdentifier nameIdentifier = psiElement.getNameIdentifier();
    final String enumValueName = nameIdentifier.getText();
    if ("true".equals(enumValueName) || "false".equals(enumValueName) || "null".equals(enumValueName)) {
      createAnnotation(annotationHolder, nameIdentifier, "Enum values can not be named '" + enumValueName + "'");
    }
  }

  private void checkDirectiveLocation(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
    final PsiReference reference = psiElement.getReference();
    if (reference != null && reference.resolve() != null) {
      return;
    }

    createAnnotation(
      annotationHolder, psiElement, "Unknown directive location '" + psiElement.getText() + "'",
      GraphQLUnresolvedReferenceInspection.class, builder -> builder.highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
    );
  }

  private void checkIdentifierReferences(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
    Project project = element.getProject();

    final PsiReference reference = element.getReference();
    if (reference != null && reference.resolve() != null) {
      return;
    }

    final PsiElement parent = element.getParent();
    final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(parent, GraphQLTypeScopeProvider.class);
    com.intellij.lang.jsgraphql.types.schema.GraphQLType typeScope = null;
    if (typeScopeProvider != null) {
      typeScope = typeScopeProvider.getTypeScope();
      if (typeScope != null) {
        // unwrap non-nulls and lists for type and field hints
        typeScope = GraphQLSchemaUtil.getUnmodifiedType(typeScope);
      }
    }

    String message = null;

    // fixes to automatically rename misspelled identifiers
    final List<LocalQuickFix> fixes = Lists.newArrayList();
    Consumer<List<String>> createFixes = (List<String> suggestions) ->
      suggestions.forEach(suggestion -> fixes.add(new RenameElementFix((PsiNamedElement)element, suggestion)));

    if (parent instanceof GraphQLField) {
      message = "Unknown field \"" + element.getText() + "\"";
      if (typeScope != null) {
        String definitionType = "";
        if (typeScope instanceof GraphQLObjectType) {
          definitionType = "object ";
        }
        else if (typeScope instanceof GraphQLInterfaceType) {
          definitionType = "interface ";
        }
        message += " on " + definitionType + "type \"" + GraphQLSchemaUtil.getTypeName(typeScope) + "\"";
        final List<String> suggestions = getFieldNameSuggestions(element.getText(), typeScope);
        if (suggestions != null && !suggestions.isEmpty()) {
          message += ". Did you mean " + formatSuggestions(suggestions) + "?";
          createFixes.accept(suggestions);
        }
      }
      else {
        // no type info available from the parent
        message += ": The parent selection or operation does not resolve to a valid schema type";
      }
    }
    else if (parent instanceof GraphQLFragmentSpread) {
      message = "Unknown fragment spread \"" + element.getText() + "\"";
    }
    else if (parent instanceof GraphQLArgument) {
      message = "Unknown argument \"" + element.getText() + "\"";
      if (typeScope != null) {
        final List<String> suggestions = getArgumentNameSuggestions(element);
        if (!suggestions.isEmpty()) {
          message += ". Did you mean " + formatSuggestions(suggestions) + "?";
          createFixes.accept(suggestions);
        }
      }
    }
    else if (parent instanceof GraphQLDirective) {
      message = "Unknown directive \"" + element.getText() + "\"";
    }
    else if (parent instanceof GraphQLObjectField) {
      message = "Unknown field \"" + element.getText() + "\"";
      if (typeScope != null) {
        message += " on input type \"" + GraphQLSchemaUtil.getTypeName(typeScope) + "\"";
        final List<String> suggestions = getFieldNameSuggestions(element.getText(), typeScope);
        if (suggestions != null && !suggestions.isEmpty()) {
          message += ". Did you mean " + formatSuggestions(suggestions) + "?";
          createFixes.accept(suggestions);
        }
      }
    }
    else if (parent instanceof GraphQLTypeName) {
      message = "Unknown type \"" + element.getText() + "\"";
      fixes.addAll(GraphQLMissingTypeFix.getApplicableFixes((GraphQLIdentifier)element));
    }

    if (message == null) {
      return;
    }

    String finalMessage = message;
    createAnnotation(annotationHolder, element, message, GraphQLUnresolvedReferenceInspection.class, builder -> {
      builder = builder.highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);

      if (!fixes.isEmpty()) {
        final InspectionManager inspectionManager = InspectionManager.getInstance(project);
        final ProblemDescriptor problemDescriptor = inspectionManager.createProblemDescriptor(
          element,
          element,
          finalMessage,
          ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
          true,
          LocalQuickFix.EMPTY_ARRAY
        );
        for (LocalQuickFix fix : fixes) {
          builder = builder.newLocalQuickFix(fix, problemDescriptor).registerFix();
        }
      }

      return builder;
    });
  }

  private List<String> getArgumentNameSuggestions(PsiElement argument) {
    final GraphQLField field = PsiTreeUtil.getParentOfType(argument, GraphQLField.class);
    final PsiElement fieldDefinitionIdentifier = GraphQLResolveUtil.resolve(field);
    if (fieldDefinitionIdentifier != null) {
      GraphQLFieldDefinition fieldDefinition = PsiTreeUtil.getParentOfType(fieldDefinitionIdentifier, GraphQLFieldDefinition.class);
      if (fieldDefinition != null) {
        final GraphQLArgumentsDefinition argumentsDefinition = fieldDefinition.getArgumentsDefinition();
        if (argumentsDefinition != null) {
          final List<String> argumentNames = Lists.newArrayList();
          argumentsDefinition.getInputValueDefinitionList().forEach(arg -> {
            if (arg.getName() != null) {
              argumentNames.add(arg.getName());
            }
          });
          return getSuggestions(argument.getText(), argumentNames);
        }
      }
    }
    return Collections.emptyList();
  }

  private List<String> getFieldNameSuggestions(String fieldName, com.intellij.lang.jsgraphql.types.schema.GraphQLType typeScope) {
    List<String> fieldNames = null;
    if (typeScope instanceof GraphQLFieldsContainer) {
      fieldNames = ((GraphQLFieldsContainer)typeScope).getFieldDefinitions().stream()
        .map(com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition::getName).collect(Collectors.toList());
    }
    else if (typeScope instanceof GraphQLInputFieldsContainer) {
      fieldNames = ((GraphQLInputFieldsContainer)typeScope).getFieldDefinitions().stream().map(GraphQLInputObjectField::getName)
        .collect(Collectors.toList());
    }
    if (fieldNames != null) {
      return getSuggestions(fieldName, fieldNames);
    }
    return Collections.emptyList();
  }

  @NotNull
  private List<String> getSuggestions(@Nullable String text, @NotNull List<String> candidates) {
    if (text == null) return Collections.emptyList();

    return candidates.stream()
      .filter(s -> !text.equals(s))
      .map(suggestion -> new Pair<>(suggestion, EditDistance.optimalAlignment(text, suggestion, false)))
      .filter(p -> p.second <= 2)
      .sorted(Comparator.comparingInt(p -> p.second))
      .map(p -> p.first).collect(Collectors.toList());
  }

  private String formatSuggestions(List<String> suggestions) {
    if (suggestions != null && !suggestions.isEmpty()) {
      return "\"" + StringUtils.join(suggestions, "\", or \"") + "\"";
    }
    return null;
  }
}
