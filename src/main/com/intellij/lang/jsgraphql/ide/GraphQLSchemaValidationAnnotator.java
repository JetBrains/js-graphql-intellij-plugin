/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.jsgraphql.ide.project.GraphQLInjectionSearchHelper;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDescriptionAware;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDirectivesAware;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.lang.jsgraphql.schema.GraphQLValidatedSchema;
import com.intellij.lang.jsgraphql.utils.GraphQLUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import graphql.AssertException;
import graphql.GraphQLError;
import graphql.language.Document;
import graphql.language.SourceLocation;
import graphql.schema.idl.errors.SchemaProblem;
import graphql.schema.validation.InvalidSchemaException;
import graphql.validation.ValidationError;
import graphql.validation.ValidationErrorType;
import graphql.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;

public class GraphQLSchemaValidationAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (!(psiElement instanceof GraphQLFile)) return;

        final PsiFile containingFile = psiElement.getContainingFile();
        final Project project = psiElement.getProject();

        List<? extends GraphQLError> userData;
        try {
            userData = runExternalValidator(psiElement, annotationHolder, containingFile, project);
        } catch (SchemaProblem | CancellationException | InvalidSchemaException | AssertException e) {
            return;
        }

        for (GraphQLError userDatum : userData) {
            if (!(userDatum instanceof ValidationError)) {
                continue;
            }

            final ValidationError validationError = (ValidationError) userDatum;
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
                    processValidationError(annotationHolder, containingFile, validationError, validationErrorType);
                    break;
                default:
                    // remaining rules are handled above using psi references
                    break;
            }
        }

    }

    private @NotNull List<? extends GraphQLError> runExternalValidator(@NotNull PsiElement psiElement,
                                                                       @NotNull AnnotationHolder annotationHolder,
                                                                       @NotNull PsiFile containingFile,
                                                                       @NotNull Project project) {
        List<? extends GraphQLError> userData;

        final GraphQLValidatedSchema schema = GraphQLSchemaProvider.getInstance(project).getValidatedSchema(psiElement);
        if (!schema.isErrorsPresent()) {
            // adjust source locations for injected GraphQL since the annotator works on the entire editor buffer (e.g. tsx with graphql tagged templates)
            int lineDelta = 0;
            int firstLineColumnDelta = 0;
            if (containingFile.getContext() != null) {
                final LogicalPosition logicalPosition = getLogicalPositionFromOffset(containingFile, containingFile.getContext().getTextOffset());
                if (logicalPosition.line > 0 || logicalPosition.column > 0) {
                    // logical positions can be used as deltas between graphql-java and intellij since graphql-java is 1-based and intellij is 0-based
                    lineDelta = logicalPosition.line;
                    firstLineColumnDelta = logicalPosition.column;
                }
            }
            final Document document = GraphQLUtil.parseDocument(replacePlaceholdersWithValidGraphQL(containingFile), lineDelta, firstLineColumnDelta);
            userData = new Validator().validateDocument(schema.getSchema(), document);
        } else {
            final String currentFileName = GraphQLPsiUtil.getFileName(containingFile);
            final Ref<SourceLocation> firstSchemaError = new Ref<>();
            for (GraphQLError error : schema.getErrors()) {
                List<SourceLocation> errorLocations = error.getLocations();
                SourceLocation firstSourceLocation = errorLocations != null
                    ? errorLocations.stream().findFirst().orElse(null) : null;
                if (firstSourceLocation != null && firstSchemaError.isNull()) {
                    firstSchemaError.set(firstSourceLocation);
                }
                if (firstSourceLocation != null && currentFileName.equals(firstSourceLocation.getSourceName())) {
                    int positionToOffset = getOffsetFromSourceLocation(containingFile, firstSourceLocation);
                    if (positionToOffset == -1) {
                        continue;
                    }
                    if (containingFile.getContext() != null) {
                        // injected file, so adjust the position
                        positionToOffset = positionToOffset - containingFile.getContext().getTextOffset();
                    }
                    PsiElement errorPsiElement = containingFile.findElementAt(positionToOffset);
                    if (errorPsiElement != null) {
                        PsiElement nextLeaf = PsiTreeUtil.nextVisibleLeaf(errorPsiElement);
                        if (nextLeaf != null && nextLeaf.getParent() instanceof GraphQLIdentifier) {
                            // graphql-errors typically point to the keywords of definitions, so
                            // use the definition identifier in that case
                            errorPsiElement = nextLeaf.getParent();
                        }
                        createErrorAnnotation(annotationHolder, errorPsiElement, error.getMessage());
                    }
                }
            }

            userData = Collections.emptyList();
        }
        return userData;
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
            PsiElement errorPsiElement = containingFile.findElementAt(positionToOffset - injectionOffset);
            if (errorPsiElement == null) {
                continue;
            }

            if (isIgnored(validationErrorType, errorPsiElement)) {
                continue;
            }

            final IElementType elementType = errorPsiElement.getNode().getElementType();
            if (elementType == GraphQLElementTypes.SPREAD) {
                // graphql-java uses the '...' as source location on fragments, so find the fragment name or type condition
                final GraphQLFragmentSelection fragmentSelection = PsiTreeUtil.getParentOfType(errorPsiElement, GraphQLFragmentSelection.class);
                if (fragmentSelection != null) {
                    if (fragmentSelection.getFragmentSpread() != null) {
                        errorPsiElement = fragmentSelection.getFragmentSpread().getNameIdentifier();
                    } else if (fragmentSelection.getInlineFragment() != null) {
                        final GraphQLTypeCondition typeCondition = fragmentSelection.getInlineFragment().getTypeCondition();
                        if (typeCondition != null) {
                            errorPsiElement = typeCondition.getTypeName();
                        }
                    }
                }
            } else if (elementType == GraphQLElementTypes.AT) {
                // mark the directive and not only the '@'
                errorPsiElement = errorPsiElement.getParent();
            }
            if (errorPsiElement != null) {
                if (isInsideTemplateElement(errorPsiElement)) {
                    // error due to template placeholder replacement, so we can ignore it for '___' replacement variables
                    if (validationErrorType == ValidationErrorType.UndefinedVariable) {
                        continue;
                    }
                }
                if (validationErrorType == ValidationErrorType.SubSelectionRequired) {
                    // apollo client 2.5 doesn't require sub selections for client fields
                    final GraphQLDirectivesAware directivesAware = PsiTreeUtil.getParentOfType(errorPsiElement, GraphQLDirectivesAware.class);
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
                createErrorAnnotation(annotationHolder, errorPsiElement, message);
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
                                       PsiElement errorPsiElement,
                                       String message) {
        if (GraphQLRelayModernAnnotationFilter.getService(errorPsiElement.getProject()).errorIsIgnored(errorPsiElement)) {
            return;
        }
        // error locations from graphql-java will give us the beginning of a type definition including the description
        final GraphQLQuotedString quotedString = PsiTreeUtil.getParentOfType(errorPsiElement, GraphQLQuotedString.class);
        if (quotedString != null) {
            // check if this is the description
            final GraphQLDescriptionAware descriptionAware = PsiTreeUtil.getParentOfType(quotedString, GraphQLDescriptionAware.class);
            if (descriptionAware != null && descriptionAware.getDescription() == quotedString) {
                final GraphQLIdentifier describedName = PsiTreeUtil.findChildOfType(descriptionAware, GraphQLIdentifier.class);
                if (describedName != null) {
                    // highlight the identifier (e.g. type name) that has the error instead
                    errorPsiElement = describedName;
                }
            }
        }
        annotationHolder.createErrorAnnotation(errorPsiElement, message);
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
