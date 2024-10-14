/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.validation;

import com.intellij.codeInsight.daemon.impl.quickfix.RenameElementFix;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil;
import com.intellij.lang.jsgraphql.ide.validation.fixes.GraphQLMissingTypeFix;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInspection;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLUnresolvedReferenceInspection;
import com.intellij.lang.jsgraphql.psi.GraphQLArgument;
import com.intellij.lang.jsgraphql.psi.GraphQLDirective;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaUtil;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import com.intellij.lang.jsgraphql.types.schema.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.EditDistance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInspection.createAnnotation;

public final class GraphQLValidationAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
    if (GraphQLInspection.isEditorInspectionHighlightingDisabled(psiElement.getProject(),
                                                                 annotationHolder.getCurrentAnnotationSession().getFile())) {
      return;
    }

    GraphQLSchemaInfo schemaInfo = GraphQLSchemaProvider.getInstance(psiElement.getProject()).getSchemaInfo(psiElement);
    if (schemaInfo.isTooComplex()) {
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

  private static void checkEnumValues(GraphQLEnumValue psiElement, @NotNull AnnotationHolder annotationHolder) {
    final GraphQLIdentifier nameIdentifier = psiElement.getNameIdentifier();
    final String enumValueName = nameIdentifier.getText();
    if ("true".equals(enumValueName) || "false".equals(enumValueName) || "null".equals(enumValueName)) {
      createAnnotation(
        annotationHolder,
        nameIdentifier,
        GraphQLBundle.message("graphql.validation.enum.values.can.not.be.named.0", enumValueName)
      );
    }
  }

  private static void checkDirectiveLocation(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
    final PsiReference reference = psiElement.getReference();
    if (reference != null && reference.resolve() != null) {
      return;
    }

    createAnnotation(
      annotationHolder, psiElement, GraphQLBundle.message("graphql.validation.unknown.directive.location.0", psiElement.getText()),
      GraphQLUnresolvedReferenceInspection.class, builder -> builder.highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
    );
  }

  private static void checkIdentifierReferences(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
    Project project = element.getProject();

    final PsiReference reference = element.getReference();
    if (reference != null && reference.resolve() != null) {
      return;
    }

    final PsiElement parent = element.getParent();
    final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(parent, GraphQLTypeScopeProvider.class);
    GraphQLType typeScope = null;
    if (typeScopeProvider != null) {
      typeScope = typeScopeProvider.getTypeScope();
      if (typeScope != null) {
        // unwrap non-nulls and lists for type and field hints
        typeScope = GraphQLSchemaUtil.getUnmodifiedType(typeScope);
      }
    }

    String message = null;

    // fixes to automatically rename misspelled identifiers
    final List<LocalQuickFix> fixes = new ArrayList<>();
    Consumer<List<String>> createFixes = (List<String> suggestions) ->
      suggestions.forEach(suggestion -> fixes.add(new RenameElementFix((PsiNamedElement)element, suggestion)));

    if (parent instanceof GraphQLField) {
      message = GraphQLBundle.message("graphql.validation.unknown.field.0", element.getText());
      if (typeScope != null) {
        String definitionType = "";
        if (typeScope instanceof GraphQLObjectType) {
          definitionType = GraphQLBundle.message("graphql.validation.object.type");
        }
        else if (typeScope instanceof GraphQLInterfaceType) {
          definitionType = GraphQLBundle.message("graphql.validation.interface.type");
        }
        if (!definitionType.isEmpty()) {
          definitionType += " ";
        }
        message += " " + GraphQLBundle.message("graphql.validation.on.0.type.1", definitionType, GraphQLSchemaUtil.getTypeName(typeScope));
        final List<String> suggestions = getFieldNameSuggestions(element.getText(), typeScope);
        if (suggestions != null && !suggestions.isEmpty()) {
          message += GraphQLBundle.message("graphql.validation.did.you.mean.0", formatSuggestions(suggestions));
          createFixes.accept(suggestions);
        }
      }
      else {
        // no type info available from the parent
        message += GraphQLBundle.message("graphql.validation.parent.selection.or.operation.does.not.resolve.to.a.valid.schema.type");
      }
    }
    else if (parent instanceof GraphQLFragmentSpread) {
      message = GraphQLBundle.message("graphql.validation.unknown.fragment.spread.0", element.getText());
    }
    else if (parent instanceof GraphQLArgument) {
      message = GraphQLBundle.message("graphql.validation.unknown.argument.0", element.getText());
      if (typeScope != null) {
        final List<String> suggestions = getArgumentNameSuggestions(element);
        if (!suggestions.isEmpty()) {
          message += GraphQLBundle.message("graphql.validation.did.you.mean.0", formatSuggestions(suggestions));
          createFixes.accept(suggestions);
        }
      }
    }
    else if (parent instanceof GraphQLDirective) {
      message = GraphQLBundle.message("graphql.validation.unknown.directive.0", element.getText());
    }
    else if (parent instanceof GraphQLObjectField) {
      message = GraphQLBundle.message("graphql.validation.unknown.field.0", element.getText());
      if (typeScope != null) {
        message += " " + GraphQLBundle.message("graphql.validation.on.input.type.0", GraphQLSchemaUtil.getTypeName(typeScope));
        final List<String> suggestions = getFieldNameSuggestions(element.getText(), typeScope);
        if (suggestions != null && !suggestions.isEmpty()) {
          message += GraphQLBundle.message("graphql.validation.did.you.mean.0", formatSuggestions(suggestions));
          createFixes.accept(suggestions);
        }
      }
    }
    else if (parent instanceof GraphQLTypeName) {
      message = GraphQLBundle.message("graphql.validation.unknown.type.0", element.getText());
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

  private static List<String> getArgumentNameSuggestions(PsiElement argument) {
    final GraphQLField field = PsiTreeUtil.getParentOfType(argument, GraphQLField.class);
    final GraphQLFieldDefinition fieldDefinition = ObjectUtils.tryCast(GraphQLResolveUtil.resolve(field), GraphQLFieldDefinition.class);
    if (fieldDefinition != null) {
      final GraphQLArgumentsDefinition argumentsDefinition = fieldDefinition.getArgumentsDefinition();
      if (argumentsDefinition != null) {
        final List<String> argumentNames = new ArrayList<>();
        argumentsDefinition.getInputValueDefinitionList().forEach(arg -> {
          if (arg.getName() != null) {
            argumentNames.add(arg.getName());
          }
        });
        return getSuggestions(argument.getText(), argumentNames);
      }
    }
    return Collections.emptyList();
  }

  private static List<String> getFieldNameSuggestions(String fieldName, GraphQLType typeScope) {
    List<String> fieldNames = null;
    if (typeScope instanceof GraphQLFieldsContainer) {
      fieldNames = ContainerUtil.map(((GraphQLFieldsContainer)typeScope).getFieldDefinitions(),
                                     com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition::getName);
    }
    else if (typeScope instanceof GraphQLInputFieldsContainer) {
      fieldNames = ContainerUtil.map(((GraphQLInputFieldsContainer)typeScope).getFieldDefinitions(), GraphQLInputObjectField::getName);
    }
    if (fieldNames != null) {
      return getSuggestions(fieldName, fieldNames);
    }
    return Collections.emptyList();
  }

  private static @NotNull List<String> getSuggestions(@Nullable String text, @NotNull List<String> candidates) {
    if (text == null) return Collections.emptyList();

    return candidates.stream()
      .filter(s -> !text.equals(s))
      .map(suggestion -> new Pair<>(suggestion, EditDistance.optimalAlignment(text, suggestion, false)))
      .filter(p -> p.second <= 2)
      .sorted(Comparator.comparingInt(p -> p.second))
      .map(p -> p.first).collect(Collectors.toList());
  }

  private static String formatSuggestions(List<String> suggestions) {
    if (suggestions != null && !suggestions.isEmpty()) {
      String suggestionDelimiter = GraphQLBundle.message("graphql.validation.field.name.suggestion.delimiter");
      return "\"" + String.join("\"" + suggestionDelimiter + " \"", suggestions) + "\"";
    }
    return null;
  }
}
