/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.validation;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInspection;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;
import com.intellij.lang.jsgraphql.types.validation.ValidationError;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;
import com.intellij.lang.jsgraphql.types.validation.Validator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;

public final class GraphQLSchemaAnnotator implements Annotator {
  private static final Logger LOG = Logger.getInstance(GraphQLSchemaAnnotator.class);

  @Override
  public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
    if (!(psiElement instanceof GraphQLFile file)) return;

    final Project project = psiElement.getProject();

    if (GraphQLInspection.isEditorInspectionHighlightingDisabled(project, file)) return;

    try {
      GraphQLSchemaInfo schemaInfo = GraphQLSchemaProvider.getInstance(project).getSchemaInfo(psiElement);
      List<GraphQLError> schemaErrors = schemaInfo.getErrors(project);
      if (!schemaErrors.isEmpty()) {
        showSchemaErrors(annotationHolder, schemaErrors, file);
      }
      else {
        showDocumentErrors(annotationHolder, schemaInfo, file);
      }
    }
    catch (ProcessCanceledException e) {
      throw e;
    }
    catch (CancellationException e) {
      // ignore
    }
    catch (Exception e) {
      LOG.info(e);
    }
  }

  private static void showDocumentErrors(@NotNull AnnotationHolder annotationHolder,
                                         @NotNull GraphQLSchemaInfo schemaInfo,
                                         @NotNull GraphQLFile file) {
    List<? extends GraphQLError> errors = validateQueryDocument(schemaInfo, file);

    for (GraphQLError error : errors) {
      if (!(error instanceof ValidationError validationError)) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(String.format("Ignored validation error: type=%s, message=%s", error.getClass().getName(), error.getMessage()));
        }
        continue;
      }

      final ValidationErrorType validationErrorType = validationError.getValidationErrorType();
      if (validationErrorType == null) {
        continue;
      }

      switch (validationErrorType) {
        case DefaultForNonNullArgument,
          WrongType,
          SubSelectionRequired,
          SubSelectionNotAllowed,
          BadValueForDefaultArg,
          InlineFragmentTypeConditionInvalid,
          FragmentTypeConditionInvalid,
          UnknownArgument,
          NonInputTypeOnVariable,
          MissingFieldArgument,
          MissingDirectiveArgument,
          VariableTypeMismatch,
          MisplacedDirective,
          UndefinedVariable,
          UnusedVariable,
          FragmentCycle,
          FieldsConflict,
          InvalidFragmentType,
          LoneAnonymousOperationViolation,
          DuplicateFragmentName,
          DuplicateDirectiveName,
          DuplicateArgumentNames,
          DuplicateVariableName -> processValidationError(annotationHolder, file, validationError);

        case NonExecutableDefinition,
          UnknownType,
          UnusedFragment,
          DuplicateOperationName,
          NullValueForNonNullArgument,
          InvalidSyntax,
          FieldUndefined,
          UndefinedFragment,
          UnknownDirective -> {
        }
      }
    }
  }

  private static @NotNull List<? extends GraphQLError> validateQueryDocument(@NotNull GraphQLSchemaInfo schemaInfo,
                                                                             @NotNull GraphQLFile file) {
    return new Validator().validateDocument(schemaInfo.getSchema(), file.getDocument());
  }

  private static void showSchemaErrors(@NotNull AnnotationHolder annotationHolder,
                                       @NotNull List<GraphQLError> schemaErrors,
                                       @NotNull GraphQLFile file) {
    for (GraphQLError error : schemaErrors) {
      Collection<? extends PsiElement> elements = getElementsToAnnotate(file, error);
      for (PsiElement element : elements) {
        @NlsSafe String errorMessage = error.getMessage();
        createErrorAnnotation(annotationHolder, error, element, errorMessage);
      }
    }
  }

  private static @NotNull Collection<PsiElement> getElementsToAnnotate(@NotNull PsiFile containingFile, @NotNull GraphQLError error) {
    Node<?> node = error.getNode();
    if (node != null) {
      PsiElement element = node.getElement();
      if (element != null) {
        return element.isValid() && element.getContainingFile() == containingFile
               ? Collections.singletonList(element)
               : Collections.emptyList();
      }
    }

    List<SourceLocation> locations = error.getLocations();
    if (locations == null) {
      return Collections.emptyList();
    }

    String currentFileName = GraphQLPsiUtil.getPhysicalFileName(containingFile);
    return ContainerUtil.mapNotNull(locations, location -> {
      if (!currentFileName.equals(location.getSourceName())) {
        return null;
      }

      PsiElement element = location.getElement();
      if (element != null) {
        return element.isValid() && element.getContainingFile() == containingFile ? element : null;
      }

      int positionToOffset = location.getOffset();
      if (positionToOffset == -1) {
        return null;
      }

      PsiElement context = containingFile.getContext();
      if (context != null) {
        // injected file, so adjust the position
        positionToOffset -= context.getTextOffset();
      }
      return containingFile.findElementAt(positionToOffset);
    });
  }

  private static void processValidationError(@NotNull AnnotationHolder annotationHolder,
                                             @NotNull PsiFile containingFile,
                                             @NotNull ValidationError validationError) {
    for (PsiElement element : getElementsToAnnotate(containingFile, validationError)) {
      @NlsSafe String errorMessage = validationError.getMessage();
      final String message = Optional.ofNullable(validationError.getDescription()).orElse(errorMessage);
      createErrorAnnotation(annotationHolder, validationError, element, message);
    }
  }

  @SuppressWarnings("rawtypes")
  private static void createErrorAnnotation(@NotNull AnnotationHolder annotationHolder,
                                            @NotNull GraphQLError error,
                                            @NotNull PsiElement element,
                                            @Nullable @Nls String message) {
    if (message == null) return;
    if (ContainerUtil.exists(
      GraphQLErrorFilter.EP_NAME.getExtensionList(),
      filter -> filter.isGraphQLErrorSuppressed(element.getProject(), error, element))
    ) {
      return;
    }

    GraphQLInspection.createAnnotation(annotationHolder, element, message, error.getInspectionClass(), builder -> {
      builder = builder.range(getAnnotationAnchor(element));

      List<Node> references = ContainerUtil.filter(error.getReferences(), ref -> {
        if (ref.getSourceLocation() == null) return false;

        PsiElement refElement = ref.getElement();
        if (refElement != null && refElement.isValid()) {
          // NavigationLinkHandler can't handle non-physical PSI
          PsiFile psiFile = refElement.getContainingFile();
          return psiFile != null && psiFile.getViewProvider().isPhysical();
        }
        return true;
      });

      if (!references.isEmpty()) {
        builder = builder.tooltip(createTooltip(error, message, references.size() > 1));
      }
      return builder;
    });
  }

  @SuppressWarnings("rawtypes")
  private static @NotNull @NlsSafe String createTooltip(@NotNull GraphQLError error, @NotNull String message, boolean isMultiple) {
    StringBuilder sb = new StringBuilder();
    sb
      .append("<html>")
      .append(message)
      .append("<br/>");

    if (isMultiple) {
      sb.append("<br/>").append(GraphQLBundle.message("graphql.inspection.related.definitions"));
    }

    for (Node reference : error.getReferences()) {
      SourceLocation sourceLocation = reference.getSourceLocation();
      if (sourceLocation == null) continue;

      String navigationLabel;
      PsiElement referenceElement = reference.getElement();
      if (referenceElement != null && referenceElement.isValid()) {
        PsiElement annotationAnchor = getAnnotationAnchor(referenceElement);
        navigationLabel = GraphQLBundle.message("graphql.inspection.go.to.related.definition.name", annotationAnchor.getText());
      }
      else {
        navigationLabel = GraphQLBundle.message("graphql.inspection.go.to.related.definition.family.name");
      }

      sb
        .append("<br/>")
        .append("<a href=\"#navigation/")
        .append(sourceLocation.getNavigationLocation())
        .append("\">")
        .append(navigationLabel)
        .append("</a>");
    }
    sb.append("</html>");

    return sb.toString();
  }

  private static @NotNull PsiElement getAnnotationAnchor(@NotNull PsiElement element) {
    if (element instanceof PsiWhiteSpace) {
      PsiElement next = PsiTreeUtil.skipWhitespacesForward(element);
      if (next != null) {
        element = next;
      }
    }
    if (element instanceof GraphQLNamedTypeDefinition) {
      GraphQLTypeNameDefinition typeName = ((GraphQLNamedTypeDefinition)element).getTypeNameDefinition();
      if (typeName != null) {
        return typeName.getNameIdentifier();
      }
    }
    if (element instanceof GraphQLNamedTypeExtension) {
      GraphQLTypeName typeName = ((GraphQLNamedTypeExtension)element).getTypeName();
      if (typeName != null) {
        return typeName.getNameIdentifier();
      }
    }
    if (element instanceof GraphQLInlineFragment) {
      GraphQLTypeCondition typeCondition = ((GraphQLInlineFragment)element).getTypeCondition();
      if (typeCondition != null) {
        element = typeCondition;
      }
    }
    if (element instanceof GraphQLTypeCondition) {
      GraphQLTypeName typeName = ((GraphQLTypeCondition)element).getTypeName();
      if (typeName != null) {
        return typeName;
      }
    }
    if (element instanceof GraphQLDirective) {
      return element;
    }

    element = GraphQLPsiUtil.skipDescription(element);
    LeafElement leaf = TreeUtil.findFirstLeaf(element.getNode());
    if (leaf != null) {
      element = leaf.getPsi();
    }

    return element;
  }
}
