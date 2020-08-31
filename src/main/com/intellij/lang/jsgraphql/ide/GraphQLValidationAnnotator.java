/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.daemon.impl.quickfix.RenameElementFix;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.AnnotationSession;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.jsgraphql.ide.project.GraphQLInjectionSearchHelper;
import com.intellij.lang.jsgraphql.ide.project.GraphQLPsiSearchHelper;
import com.intellij.lang.jsgraphql.psi.GraphQLArgument;
import com.intellij.lang.jsgraphql.psi.GraphQLDirective;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDescriptionAware;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDirectivesAware;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.lang.jsgraphql.schema.GraphQLTypeScopeProvider;
import com.intellij.lang.jsgraphql.schema.GraphQLValidatedSchema;
import com.intellij.lang.jsgraphql.utils.GraphQLUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.text.EditDistance;
import graphql.AssertException;
import graphql.GraphQLError;
import graphql.language.Document;
import graphql.language.SourceLocation;
import graphql.schema.*;
import graphql.schema.idl.errors.SchemaProblem;
import graphql.schema.validation.InvalidSchemaException;
import graphql.validation.ValidationError;
import graphql.validation.ValidationErrorType;
import graphql.validation.Validator;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GraphQLValidationAnnotator implements Annotator {

    private static final Key<List<? extends GraphQLError>> ERRORS = Key.create(GraphQLValidationAnnotator.class.getName() + ".errors");
    private static final Key<Editor> EDITOR = Key.create(GraphQLValidationAnnotator.class.getName() + ".editor");

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {

        if (psiElement instanceof PsiWhiteSpace || psiElement instanceof PsiFile) {
            return;
        }

        // identifiers - fields, fragment spreads, field arguments, directives, type names, input object fields
        if (psiElement instanceof GraphQLIdentifier) {
            final PsiReference reference = psiElement.getReference();
            if (reference == null || reference.resolve() == null) {
                final PsiElement parent = psiElement.getParent();
                final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(parent, GraphQLTypeScopeProvider.class);
                graphql.schema.GraphQLType typeScope = null;
                if (typeScopeProvider != null) {
                    typeScope = typeScopeProvider.getTypeScope();
                    if (typeScope != null) {
                        // unwrap non-nulls and lists for type and field hints
                        typeScope = GraphQLUtil.getUnmodifiedType(typeScope);
                    }
                }

                String message = null;

                // fixes to automatically rename misspelled identifiers
                final List<LocalQuickFix> fixes = Lists.newArrayList();
                Consumer<List<String>> createFixes = (List<String> suggestions) -> {
                    suggestions.forEach(suggestion -> fixes.add(new RenameElementFix((PsiNamedElement) psiElement, suggestion)));
                };

                if (parent instanceof GraphQLField) {
                    message = "Unknown field \"" + psiElement.getText() + "\"";
                    if (typeScope != null) {
                        String definitionType = "";
                        if (typeScope instanceof GraphQLObjectType) {
                            definitionType = "object ";
                        } else if (typeScope instanceof GraphQLInterfaceType) {
                            definitionType = "interface ";
                        }
                        message += " on " + definitionType + "type \"" + GraphQLUtil.getName(typeScope) + "\"";
                        final List<String> suggestions = getFieldNameSuggestions(psiElement.getText(), typeScope);
                        if (suggestions != null && !suggestions.isEmpty()) {
                            message += ". Did you mean " + formatSuggestions(suggestions) + "?";
                            createFixes.accept(suggestions);
                        }
                    } else {
                        // no type info available from the parent
                        message += ": The parent selection or operation does not resolve to a valid schema type";
                    }
                } else if (parent instanceof GraphQLFragmentSpread) {
                    message = "Unknown fragment spread \"" + psiElement.getText() + "\"";
                } else if (parent instanceof GraphQLArgument) {
                    message = "Unknown argument \"" + psiElement.getText() + "\"";
                    if (typeScope != null) {
                        final List<String> suggestions = getArgumentNameSuggestions(psiElement, typeScope);
                        if (!suggestions.isEmpty()) {
                            message += ". Did you mean " + formatSuggestions(suggestions) + "?";
                            createFixes.accept(suggestions);
                        }
                    }
                } else if (parent instanceof GraphQLDirective) {
                    message = "Unknown directive \"" + psiElement.getText() + "\"";
                } else if (parent instanceof GraphQLObjectField) {
                    message = "Unknown field \"" + psiElement.getText() + "\"";
                    if (typeScope != null) {
                        message += " on input type \"" + GraphQLUtil.getName(typeScope) + "\"";
                        final List<String> suggestions = getFieldNameSuggestions(psiElement.getText(), typeScope);
                        if (suggestions != null && !suggestions.isEmpty()) {
                            message += ". Did you mean " + formatSuggestions(suggestions) + "?";
                            createFixes.accept(suggestions);
                        }
                    }
                } else if (parent instanceof GraphQLTypeName) {
                    message = "Unknown type \"" + psiElement.getText() + "\"";
                    fixes.addAll(GraphQLMissingTypeFix.getApplicableFixes((GraphQLIdentifier) psiElement));
                }
                if (message != null) {
                    final Optional<Annotation> annotation = createErrorAnnotation(annotationHolder, psiElement, message);
                    if (annotation.isPresent()) {
                        annotation.get().setTextAttributes(CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES);
                        if (!fixes.isEmpty()) {
                            final InspectionManager inspectionManager = InspectionManager.getInstance(psiElement.getProject());
                            final ProblemDescriptor problemDescriptor = inspectionManager.createProblemDescriptor(
                                psiElement,
                                psiElement,
                                message,
                                ProblemHighlightType.ERROR,
                                true,
                                LocalQuickFix.EMPTY_ARRAY
                            );
                            fixes.forEach(fix -> annotation.get().registerFix(fix, null, null, problemDescriptor));
                        }
                    }
                }
            }
        }

        // valid directive location names
        if (psiElement instanceof GraphQLDirectiveLocation) {
            final PsiReference reference = psiElement.getReference();
            if (reference == null || reference.resolve() == null) {
                Optional<Annotation> errorAnnotation = createErrorAnnotation(annotationHolder, psiElement, "Unknown directive location '" + psiElement.getText() + "'.");
                errorAnnotation.ifPresent(annotation -> annotation.setTextAttributes(CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES));
            }
        }

        // valid enum value names according to spec
        if (psiElement instanceof GraphQLEnumValue) {
            final GraphQLIdentifier nameIdentifier = ((GraphQLEnumValue) psiElement).getNameIdentifier();
            final String enumValueName = nameIdentifier.getText();
            if ("true".equals(enumValueName) || "false".equals(enumValueName) || "null".equals(enumValueName)) {
                createErrorAnnotation(annotationHolder, nameIdentifier, "Enum values can not be named \"" + enumValueName + "\"");
            }
        }

        // validation using graphql-java
        final AnnotationSession session = annotationHolder.getCurrentAnnotationSession();
        final PsiFile containingFile = psiElement.getContainingFile();
        final Project project = psiElement.getProject();

        List<? extends GraphQLError> userData = session.getUserData(ERRORS);
        if (userData == null) {

            try {
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
                        SourceLocation firstSourceLocation = error.getLocations().stream().findFirst().orElse(null);
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
                session.putUserData(ERRORS, userData);
            } catch (SchemaProblem | CancellationException | InvalidSchemaException | AssertException e) {
                // error in graphql-java, so no validation available at this time
                session.putUserData(ERRORS, Collections.emptyList());
            }

            if (userData != null) {
                for (GraphQLError userDatum : userData) {
                    if (userDatum instanceof ValidationError) {
                        final ValidationError validationError = (ValidationError) userDatum;
                        final ValidationErrorType validationErrorType = validationError.getValidationErrorType();
                        if (validationErrorType != null) {
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
                                        if (errorPsiElement != null) {
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
                                                if (validationErrorType == ValidationErrorType.MisplacedDirective) {
                                                    // graphql-java KnownDirectives rule only recognizes executable directive locations, so ignore
                                                    // the error if we're inside a type definition
                                                    if (PsiTreeUtil.getTopmostParentOfType(errorPsiElement, GraphQLTypeSystemDefinition.class) != null) {
                                                        continue;
                                                    }
                                                }
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
                                    break;
                                default:
                                    // remaining rules are handled above using psi references
                                    break;
                            }
                        }
                    }
                }
            }

        }
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
        final StringBuffer buffer = new StringBuffer(graphqlText);
        final GraphQLVisitor visitor = new GraphQLVisitor() {
            @Override
            public void visitTemplateDefinition(@NotNull GraphQLTemplateDefinition templateDefinition) {
                // top level template, e.g. the inclusion of an external fragment below a query
                // in this case whitespace will produce a valid GraphQL document
                final TextRange textRange = templateDefinition.getTextRange();
                for (int bufferIndex = textRange.getStartOffset(); bufferIndex < textRange.getEndOffset(); bufferIndex++) {
                    buffer.setCharAt(bufferIndex, ' ');
                }
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
            }
        };
        graphqlPsiFile.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                element.accept(visitor);
                super.visitElement(element);
            }
        });
        return buffer.toString();
    }

    private Optional<Annotation> createErrorAnnotation(@NotNull AnnotationHolder annotationHolder,
                                                       PsiElement errorPsiElement,
                                                       String message) {
        if (GraphQLRelayModernAnnotationFilter.getService(errorPsiElement.getProject()).errorIsIgnored(errorPsiElement)) {
            return Optional.empty();
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
        return Optional.of(annotationHolder.createErrorAnnotation(errorPsiElement, message));
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

    private List<String> getArgumentNameSuggestions(PsiElement argument, graphql.schema.GraphQLType typeScope) {
        final GraphQLField field = PsiTreeUtil.getParentOfType(argument, GraphQLField.class);
        final GraphQLIdentifier fieldDefinitionIdentifier = GraphQLPsiSearchHelper.getResolvedReference(field);
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

    private List<String> getFieldNameSuggestions(String fieldName, graphql.schema.GraphQLType typeScope) {
        List<String> fieldNames = null;
        if (typeScope instanceof GraphQLFieldsContainer) {
            fieldNames = ((GraphQLFieldsContainer) typeScope).getFieldDefinitions().stream().map(graphql.schema.GraphQLFieldDefinition::getName).collect(Collectors.toList());
        } else if (typeScope instanceof GraphQLInputFieldsContainer) {
            fieldNames = ((GraphQLInputFieldsContainer) typeScope).getFieldDefinitions().stream().map(GraphQLInputObjectField::getName).collect(Collectors.toList());
        }
        if (fieldNames != null) {
            return getSuggestions(fieldName, fieldNames);
        }
        return Collections.emptyList();
    }

    @NotNull
    private List<String> getSuggestions(String text, List<String> candidates) {
        return candidates.stream()
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
