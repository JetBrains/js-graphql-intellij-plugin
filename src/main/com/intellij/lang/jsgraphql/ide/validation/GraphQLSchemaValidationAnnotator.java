/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.validation;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.ide.project.GraphQLInjectionSearchHelper;
import com.intellij.lang.jsgraphql.ide.validation.fixes.GraphQLNavigateToRelatedDefinition;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDirectivesAware;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLTypeNameDefinitionOwnerPsiElement;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLTypeNameExtensionOwnerPsiElement;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;
import com.intellij.lang.jsgraphql.types.validation.ValidationError;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;
import com.intellij.lang.jsgraphql.types.validation.Validator;
import com.intellij.lang.jsgraphql.utils.GraphQLUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;

public class GraphQLSchemaValidationAnnotator implements Annotator {
    private static final Logger LOG = Logger.getInstance(GraphQLSchemaValidationAnnotator.class);

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (!(psiElement instanceof GraphQLFile)) return;

        final PsiFile file = ((GraphQLFile) psiElement);
        final Project project = psiElement.getProject();

        try {
            GraphQLSchemaInfo schemaInfo = GraphQLSchemaProvider.getInstance(project).getSchemaInfo(psiElement);
            if (schemaInfo.hasErrors()) {
                showSchemaErrors(annotationHolder, schemaInfo, file);
            }

            showDocumentErrors(annotationHolder, schemaInfo, file);
        } catch (ProcessCanceledException e) {
            throw e;
        } catch (CancellationException e) {
            // ignore
        } catch (Exception e) {
            LOG.info(e);
        }
    }

    private void showDocumentErrors(@NotNull AnnotationHolder annotationHolder,
                                    @NotNull GraphQLSchemaInfo schemaInfo,
                                    @NotNull PsiFile file) {
        List<? extends GraphQLError> errors = validateQueryDocument(schemaInfo, file);

        for (GraphQLError error : errors) {
            if (!(error instanceof ValidationError)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Ignored validation error: type=%s, message=%s", error.getClass().getName(), error.getMessage()));
                }
                continue;
            }

            final ValidationError validationError = (ValidationError) error;
            final ValidationErrorType validationErrorType = validationError.getValidationErrorType();
            if (validationErrorType == null) {
                continue;
            }

            switch (validationErrorType) {
                case DefaultForNonNullArgument:
                case WrongType:
                case SubSelectionRequired:
                case SubSelectionNotAllowed:
                case BadValueForDefaultArg:
                case InlineFragmentTypeConditionInvalid:
                case FragmentTypeConditionInvalid:
                case UnknownArgument:
                case NonInputTypeOnVariable:
                case MissingFieldArgument:
                case MissingDirectiveArgument:
                case VariableTypeMismatch:
                case MisplacedDirective:
                case UndefinedVariable:
                case UnusedVariable:
                case FragmentCycle:
                case FieldsConflict:
                case InvalidFragmentType:
                case LoneAnonymousOperationViolation:
                    processValidationError(annotationHolder, file, validationError, validationErrorType);
                    break;
                default:
                    // remaining rules are handled above using psi references
                    break;
            }
        }
    }

    private @NotNull List<? extends GraphQLError> validateQueryDocument(@NotNull GraphQLSchemaInfo schemaInfo, @NotNull PsiFile file) {
        // adjust source locations for injected GraphQL since the annotator works on the entire editor buffer (e.g. tsx with graphql tagged templates)
        int lineDelta = 0;
        int firstLineColumnDelta = 0;
        if (file.getContext() != null) {
            final LogicalPosition logicalPosition = getLogicalPositionFromOffset(file, file.getContext().getTextOffset());
            if (logicalPosition.line > 0 || logicalPosition.column > 0) {
                // logical positions can be used as deltas between graphql-java and intellij since graphql-java is 1-based and intellij is 0-based
                lineDelta = logicalPosition.line;
                firstLineColumnDelta = logicalPosition.column;
            }
        }
        final Document document = GraphQLUtil.parseDocument(replacePlaceholdersWithValidGraphQL(file), lineDelta, firstLineColumnDelta);
        return new Validator().validateDocument(schemaInfo.getSchema(), document);
    }

    private void showSchemaErrors(@NotNull AnnotationHolder annotationHolder,
                                  @NotNull GraphQLSchemaInfo schemaInfo, @NotNull PsiFile file) {
        for (GraphQLError error : schemaInfo.getErrors()) {
            Collection<? extends PsiElement> elements = getElementsToAnnotate(file, error);
            for (PsiElement element : elements) {
                createErrorAnnotation(annotationHolder, error, element, error.getMessage());
            }
        }
    }

    private @NotNull Collection<PsiElement> getElementsToAnnotate(@NotNull PsiFile containingFile, @NotNull GraphQLError error) {
        String currentFileName = GraphQLPsiUtil.getFileName(containingFile);

        Node<?> node = error.getNode();
        if (node != null) {
            List<PsiElement> elements = new ArrayList<>();
            if (error.showOnMultipleDeclarations()) {
                elements.addAll(node.getElements());
            } else {
                elements.add(node.getElement());
            }

            if (error.showOnReferences()) {
                for (Node reference : error.getReferences()) {
                    elements.add(reference.getElement());
                }
            }

            List<PsiElement> elementsToAnnotate = elements.stream()
                .filter(el -> el != null && el.isValid() && el.getContainingFile() == containingFile)
                .distinct()
                .collect(Collectors.toList());

            if (!elementsToAnnotate.isEmpty()) {
                return elementsToAnnotate;
            }
        }

        List<SourceLocation> locations = error.getLocations();
        if (locations == null) {
            return Collections.emptyList();
        }

        return ContainerUtil.mapNotNull(locations, location -> {
            if (!currentFileName.equals(location.getSourceName())) {
                return null;
            }

            int positionToOffset = getOffsetFromSourceLocation(containingFile, location);
            if (positionToOffset == -1) {
                return null;
            }

            PsiElement context = containingFile.getContext();
            if (context != null) {
                // injected file, so adjust the position
                positionToOffset = positionToOffset - context.getTextOffset();
            }
            return containingFile.findElementAt(positionToOffset);
        });
    }

    private void processValidationError(@NotNull AnnotationHolder annotationHolder,
                                        @NotNull PsiFile containingFile,
                                        @NotNull ValidationError validationError,
                                        @NotNull ValidationErrorType validationErrorType) {
        for (SourceLocation location : validationError.getLocations()) {
            final int positionToOffset = getOffsetFromSourceLocation(containingFile, location);
            if (positionToOffset == -1) {
                continue;
            }
            int injectionOffset = 0;
            if (containingFile.getContext() != null) {
                injectionOffset = containingFile.getContext().getTextOffset();
            }
            PsiElement element = containingFile.findElementAt(positionToOffset - injectionOffset);
            if (element == null) {
                continue;
            }

            if (isIgnored(validationErrorType, element)) {
                continue;
            }

            final IElementType elementType = element.getNode().getElementType();
            if (elementType == GraphQLElementTypes.SPREAD) {
                // graphql-java uses the '...' as source location on fragments, so find the fragment name or type condition
                final GraphQLFragmentSelection fragmentSelection = PsiTreeUtil.getParentOfType(element, GraphQLFragmentSelection.class);
                if (fragmentSelection != null) {
                    if (fragmentSelection.getFragmentSpread() != null) {
                        element = fragmentSelection.getFragmentSpread().getNameIdentifier();
                    } else if (fragmentSelection.getInlineFragment() != null) {
                        final GraphQLTypeCondition typeCondition = fragmentSelection.getInlineFragment().getTypeCondition();
                        if (typeCondition != null) {
                            element = typeCondition.getTypeName();
                        }
                    }
                }
            } else if (elementType == GraphQLElementTypes.AT) {
                // mark the directive and not only the '@'
                element = element.getParent();
            }
            if (element != null) {
                if (isInsideTemplateElement(element)) {
                    // error due to template placeholder replacement, so we can ignore it for '___' replacement variables
                    if (validationErrorType == ValidationErrorType.UndefinedVariable) {
                        continue;
                    }
                }
                if (validationErrorType == ValidationErrorType.SubSelectionRequired) {
                    // apollo client 2.5 doesn't require sub selections for client fields
                    final GraphQLDirectivesAware directivesAware = PsiTreeUtil.getParentOfType(element, GraphQLDirectivesAware.class);
                    if (directivesAware != null) {
                        boolean ignoreError = false;
                        for (GraphQLDirective directive : directivesAware.getDirectives()) {
                            if ("client".equals(directive.getName())) {
                                ignoreError = true;
                            }
                        }
                        if (ignoreError) {
                            continue;
                        }
                    }
                }
                final String message = Optional.ofNullable(validationError.getDescription()).orElse(validationError.getMessage());
                createErrorAnnotation(annotationHolder, validationError, element, message);
            }
        }
    }

    private static boolean isIgnored(@NotNull ValidationErrorType errorType, @NotNull PsiElement element) {
        if (errorType == ValidationErrorType.MisplacedDirective) {
            // graphql-java KnownDirectives rule only recognizes executable directive locations, so ignore
            // the error if we're inside a type definition
            return PsiTreeUtil.getParentOfType(element, GraphQLTypeSystemDefinition.class) != null;
        }

        return false;
    }

    /**
     * Gets whether the specified element is inside a placeholder in a template
     */
    boolean isInsideTemplateElement(PsiElement psiElement) {
        return PsiTreeUtil.findFirstParent(
            psiElement, false,
            el -> el instanceof GraphQLTemplateDefinition || el instanceof GraphQLTemplateSelection || el instanceof GraphQLTemplateVariable
        ) != null;
    }

    /**
     * Replaces template placeholders in a GraphQL operation to produce valid GraphQL.
     * <p>
     * Positions of tokens are preserved by using replacements that fit within the ${} placeholder token.
     * <p>
     * Note that the replacement needs to be filtered for variables and selections, specifically:
     * - Variables: '$__'
     * - Selection '___'
     *
     * @param graphqlPsiFile the file to transform to valid GraphQL by replacing placeholders
     * @return the transformed valid GraphQL as a string
     */
    private String replacePlaceholdersWithValidGraphQL(PsiFile graphqlPsiFile) {
        String graphqlText = graphqlPsiFile.getText();
        if (graphqlPsiFile.getContext() instanceof PsiLanguageInjectionHost) {
            final GraphQLInjectionSearchHelper graphQLInjectionSearchHelper = ServiceManager.getService(GraphQLInjectionSearchHelper.class);
            if (graphQLInjectionSearchHelper != null) {
                graphqlText = graphQLInjectionSearchHelper.applyInjectionDelimitingQuotesEscape(graphqlText);
            }
        }
        final StringBuilder buffer = new StringBuilder(graphqlText);
        final GraphQLVisitor visitor = new GraphQLRecursiveVisitor() {
            @Override
            public void visitTemplateDefinition(@NotNull GraphQLTemplateDefinition templateDefinition) {
                // top level template, e.g. the inclusion of an external fragment below a query
                // in this case whitespace will produce a valid GraphQL document
                final TextRange textRange = templateDefinition.getTextRange();
                for (int bufferIndex = textRange.getStartOffset(); bufferIndex < textRange.getEndOffset(); bufferIndex++) {
                    buffer.setCharAt(bufferIndex, ' ');
                }

                super.visitTemplateDefinition(templateDefinition);
            }

            @Override
            public void visitTemplateSelection(@NotNull GraphQLTemplateSelection templateSelection) {
                // template is a selection, e.g. where a field or fragment would be found
                // in this case well add a '___'  selection that can fit within ${} and filter it out in the annotator
                final TextRange textRange = templateSelection.getTextRange();
                int charIndex = 0;
                for (int bufferIndex = textRange.getStartOffset(); bufferIndex < textRange.getEndOffset(); bufferIndex++) {
                    buffer.setCharAt(bufferIndex, charIndex <= 2 ? '_' : ' ');
                    charIndex++;
                }

                super.visitTemplateSelection(templateSelection);
            }

            @Override
            public void visitTemplateVariable(@NotNull GraphQLTemplateVariable templateVariable) {
                // template is a variable, so replace it with '$__' that fits within ${} and filter it out in the annotator
                final TextRange textRange = templateVariable.getTextRange();
                int charIndex = 0;
                for (int bufferIndex = textRange.getStartOffset(); bufferIndex < textRange.getEndOffset(); bufferIndex++) {
                    char currentChar;
                    switch (charIndex) {
                        case 0:
                            currentChar = '$';
                            break;
                        case 1:
                        case 2:
                            currentChar = '_';
                            break;
                        default:
                            currentChar = ' ';
                            break;
                    }
                    buffer.setCharAt(bufferIndex, currentChar);
                    charIndex++;
                }

                super.visitTemplateVariable(templateVariable);
            }
        };
        graphqlPsiFile.accept(visitor);
        return buffer.toString();
    }

    private void createErrorAnnotation(@NotNull AnnotationHolder annotationHolder,
                                       @NotNull GraphQLError error,
                                       @NotNull PsiElement element,
                                       String message) {
        if (GraphQLErrorFilter.isErrorIgnored(element.getProject(), error, element)) {
            return;
        }

        Annotation annotation = annotationHolder.createErrorAnnotation(getAnnotationAnchor(element), message);

        List<Node> references = error.getReferences();
        if (references.size() > 1) {
            annotation.setTooltip(createTooltip(error, message));
        }

        if (references.size() == 1) {
            PsiElement target = references.get(0).getElement();
            if (target != null && target.isValid()) {
                annotation.registerFix(new GraphQLNavigateToRelatedDefinition(target));
            }
        }
    }

    @NotNull
    private String createTooltip(@NotNull GraphQLError error, String message) {
        StringBuilder sb = new StringBuilder();
        sb
            .append("<html>")
            .append(message)
            .append("<br/><br/>")
            .append(GraphQLBundle.message("graphql.inspection.related.definitions"));

        for (Node reference : error.getReferences()) {
            SourceLocation sourceLocation = reference.getSourceLocation();
            if (sourceLocation == null) continue;

            String target = sourceLocation.getSourceName() + ":" + sourceLocation.getOffset();
            sb
                .append("<br/>")
                .append("<a href=\"#navigation/")
                .append(target)
                .append("\">")
                .append(target)
                .append("</a>");
        }
        sb.append("</html>");

        return sb.toString();
    }

    private @NotNull PsiElement getAnnotationAnchor(@NotNull PsiElement element) {
        if (element instanceof GraphQLTypeNameDefinitionOwnerPsiElement) {
            GraphQLTypeNameDefinition typeName = ((GraphQLTypeNameDefinitionOwnerPsiElement) element).getTypeNameDefinition();
            if (typeName != null) {
                return typeName.getNameIdentifier();
            }
        }

        if (element instanceof GraphQLTypeNameExtensionOwnerPsiElement) {
            GraphQLTypeName typeName = ((GraphQLTypeNameExtensionOwnerPsiElement) element).getTypeName();
            if (typeName != null) {
                return typeName.getNameIdentifier();
            }
        }

        LeafElement leaf = TreeUtil.findFirstLeaf(element.getNode());
        if (leaf != null) {
            return leaf.getPsi();
        }
        return element;
    }

    private LogicalPosition getLogicalPositionFromOffset(PsiFile psiFile, int offset) {
        com.intellij.openapi.editor.Document document = PsiDocumentManager.getInstance(psiFile.getProject()).getDocument(getTopLevelFile(psiFile));
        if (document != null) {
            final int lineNumber = document.getLineNumber(offset);
            final int lineStartOffset = document.getLineStartOffset(lineNumber);
            return new LogicalPosition(lineNumber, offset - lineStartOffset);
        }
        return new LogicalPosition(-1, -1);
    }

    private int getOffsetFromSourceLocation(PsiFile psiFile, SourceLocation location) {
        com.intellij.openapi.editor.Document document = PsiDocumentManager.getInstance(psiFile.getProject()).getDocument(getTopLevelFile(psiFile));
        return document != null ? document.getLineStartOffset(location.getLine() - 1) + location.getColumn() - 1 : -1;
    }

    private PsiFile getTopLevelFile(PsiFile psiFile) {
        if (psiFile.getContext() != null && psiFile.getContext().getContainingFile() != null) {
            return psiFile.getContext().getContainingFile();
        }
        return psiFile;
    }

}
