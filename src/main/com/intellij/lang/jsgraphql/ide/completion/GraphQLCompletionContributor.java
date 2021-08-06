/*
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.completion;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import com.intellij.lang.jsgraphql.ide.documentation.GraphQLDocumentationMarkdownRenderer;
import com.intellij.lang.jsgraphql.ide.project.GraphQLPsiSearchHelper;
import com.intellij.lang.jsgraphql.psi.GraphQLArgument;
import com.intellij.lang.jsgraphql.psi.GraphQLDirective;
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValueDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLInputValueDefinition;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDirectivesAware;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLObjectValueImpl;
import com.intellij.lang.jsgraphql.schema.GraphQLExternalTypeDefinitionsProvider;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaUtil;
import com.intellij.lang.jsgraphql.types.introspection.Introspection;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import com.intellij.lang.jsgraphql.types.schema.*;
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry;
import com.intellij.lang.jsgraphql.types.validation.rules.VariablesTypesMatcher;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Consumer;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.lang.jsgraphql.GraphQLConstants.Schema.__DIRECTIVE_LOCATION_ENUM;
import static com.intellij.patterns.PlatformPatterns.psiComment;
import static com.intellij.patterns.PlatformPatterns.psiElement;

@SuppressWarnings("rawtypes")
public class GraphQLCompletionContributor extends CompletionContributor {

    // top level keywords (incomplete keywords such as "q" is inside error Psi element, hence superParent
    private static final PsiElementPattern.Capture<PsiElement> TOP_LEVEL_KEYWORD_PATTERN = psiElement(GraphQLElementTypes.NAME).withSuperParent(2, GraphQLFile.class);

    // keyword after "extend" keyword
    private static final PsiElementPattern.Capture<PsiElement> EXTEND_KEYWORD_PATTERN = psiElement().afterLeaf(psiElement(GraphQLElementTypes.EXTEND_KEYWORD));

    private static final String[] TOP_LEVEL_KEYWORDS = new String[]{
        "{", "query", "subscription", "mutation", "fragment",
        "schema", "scalar", "type", "interface", "input", "enum", "union", "directive", "extend"
    };

    private static final String[] EXTEND_KEYWORDS = new String[]{
        "scalar", "type", "interface", "input", "enum", "union", "schema"
    };


    public GraphQLCompletionContributor() {

        // top level keywords
        completeTopLevelKeywords();

        // extend <completion>
        completeExtendFollowingKeyword();

        // extend (type|interface|scalar|union|enum|input) <completion>
        completeTypeNameToExtend();


        // field names inside selection sets of operations, fields, fragments
        completeFieldNames();


        // on <completion of type name>
        completeFragmentOnTypeName();


        // ...<completion of fragment name>
        completeSpreadFragmentName();


        // completion on argument name in fields and directives
        completeArgumentName();

        // completion on object value field
        completeObjectValueField();

        // completion on directive name
        completeDirectiveName();

        // completion on directive location
        completeDirectiveLocation();

        // completion on variable definition type
        completeVariableDefinitionTypeName();

        // completion of constants (true, false, null, enums) and '{}', '[]'
        completeConstantsOrListOrInputObject();

        completeEnumNamesInList();

        // completion of "implements" in type or type extension
        completeImplementsKeyword();

        // completion of interface name inside "implements"
        completeImplementsTypeName();

        // completion of field definition to implement interface fields
        completeFieldDefinitionFromImplementedInterface();


        // completion of union type members
        completeUnionMemberTypeName();

        // completion of field definition type
        completeFieldDefinitionOutputTypeName();

        // completion of field argument definition type
        completeFieldArgumentTypeName();

        // completion of operation type names in schema definition
        completeOperationTypeNamesInSchemaDefinition();

        // completion of operation types schema definition
        completionOfOperationKeywordsInSchemaDefinition();

        // completion of variable name referenced as an argument value
        completeVariableName();

    }

    private void completionOfOperationKeywordsInSchemaDefinition() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                final GraphQLOperationTypeDefinition operationTypeDefinition = PsiTreeUtil.getParentOfType(completionElement, GraphQLOperationTypeDefinition.class);
                if (operationTypeDefinition != null && operationTypeDefinition.getOperationType() == null) {
                    final Set<String> keywords = Sets.newLinkedHashSet(Lists.newArrayList("query", "mutation", "subscription"));
                    for (GraphQLOperationTypeDefinition existingDefinition : PsiTreeUtil.findChildrenOfType(operationTypeDefinition.getParent(), GraphQLOperationTypeDefinition.class)) {
                        if (existingDefinition.getOperationType() != null) {
                            keywords.remove(existingDefinition.getOperationType().getText());
                        }
                    }
                    keywords.forEach(keyword -> result.addElement(LookupElementBuilder.create(keyword).bold().withInsertHandler(AddColonSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP)));
                }
            }
        };
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).inside(GraphQLOperationTypeDefinition.class), provider);
    }

    private void completeOperationTypeNamesInSchemaDefinition() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                final TypeDefinitionRegistry registry = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getRegistryInfo(parameters.getOriginalFile()).getTypeDefinitionRegistry();
                final Collection<GraphQLTypeName> currentTypes = PsiTreeUtil.findChildrenOfType(PsiTreeUtil.getTopmostParentOfType(completionElement, GraphQLElement.class), GraphQLTypeName.class);
                final Set<String> currentTypeNames = currentTypes.stream().map(PsiNamedElement::getName).collect(Collectors.toSet());
                registry.types().values().forEach(type -> {
                    if (type instanceof ObjectTypeDefinition && !currentTypeNames.contains(type.getName())) {
                        result.addElement(LookupElementBuilder.create(type.getName()));
                    }
                });
            }
        };
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).afterLeaf(":").inside(GraphQLOperationTypeDefinition.class), provider);
    }

    private void completeFieldArgumentTypeName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                final PsiElement completionElement = parameters.getPosition();
                final TypeDefinitionRegistry registry = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getRegistryInfo(parameters.getOriginalFile()).getTypeDefinitionRegistry();
                addInputTypeCompletions(result, registry);
            }
        };
        extend(CompletionType.BASIC,
            psiElement(GraphQLElementTypes.NAME).afterLeafSkipping(
                // skip
                PlatformPatterns.or(psiComment(), psiElement(TokenType.WHITE_SPACE), psiElement().withText("[")),
                // until argument type colon occurs
                psiElement().withText(":")
            ).inside(false, psiElement(GraphQLInputValueDefinition.class), psiElement(GraphQLDefaultValue.class)), provider);
    }

    private void completeFieldDefinitionOutputTypeName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                final PsiElement completionElement = parameters.getPosition();
                final TypeDefinitionRegistry registry = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getRegistryInfo(parameters.getOriginalFile()).getTypeDefinitionRegistry();
                final GraphQLSchema schema = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getSchemaInfo(completionElement).getSchema();
                registry.scalars().values().forEach(scalar -> result.addElement(LookupElementBuilder.create(scalar.getName())));
                final String queryName = schema.getQueryType() != null ? schema.getQueryType().getName() : "Query";
                final String mutationName = schema.getMutationType() != null ? schema.getMutationType().getName() : "Mutation";
                final String subscriptionName = schema.getSubscriptionType() != null ? schema.getSubscriptionType().getName() : "Subscription";
                final Set<String> nonOutputTypes = Sets.newLinkedHashSet(Lists.newArrayList(queryName, mutationName, subscriptionName));
                registry.types().values().forEach(type -> {
                    if (!(type instanceof InputObjectTypeDefinition) && !nonOutputTypes.contains(type.getName())) {
                        result.addElement(LookupElementBuilder.create(type.getName()));
                    }
                });
            }
        };
        extend(CompletionType.BASIC,
            PlatformPatterns.and(
                psiElement(GraphQLElementTypes.NAME).afterLeafSkipping(
                    // skip
                    PlatformPatterns.or(psiComment(), psiElement(TokenType.WHITE_SPACE), psiElement().withText("[")),
                    // until field type colon occurs
                    psiElement().withText(":")
                ).inside(GraphQLFieldDefinition.class),
                StandardPatterns.not(psiElement().inside(GraphQLArgumentsDefinition.class)), // field def arguments
                StandardPatterns.not(psiElement().inside(GraphQLArguments.class)) // directive argument
            ), provider);
    }

    private void completeUnionMemberTypeName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                final GraphQLUnionMembers unionMembers = PsiTreeUtil.getParentOfType(completionElement, GraphQLUnionMembers.class);
                if (unionMembers != null) {
                    final Set<String> currentMembers = Sets.newHashSet();
                    final GraphQLSchema schema = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                        .getSchemaInfo(completionElement).getSchema();
                    // don't want the root types in the list
                    if (schema.getQueryType() != null) {
                        currentMembers.add(schema.getQueryType().getName());
                    }
                    if (schema.getMutationType() != null) {
                        currentMembers.add(schema.getMutationType().getName());
                    }
                    if (schema.getSubscriptionType() != null) {
                        currentMembers.add(schema.getSubscriptionType().getName());
                    }
                    unionMembers.getTypeNameList().forEach(t -> currentMembers.add(t.getName()));
                    final TypeDefinitionRegistry typeDefinitionRegistry = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                        .getRegistryInfo(parameters.getOriginalFile()).getTypeDefinitionRegistry();
                    typeDefinitionRegistry.getTypes(ObjectTypeDefinition.class).forEach(schemaType -> {
                        if (currentMembers.add(schemaType.getName())) {
                            result.addElement(LookupElementBuilder.create(schemaType.getName()));
                        }
                    });
                }
            }
        };
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).inside(psiElement(GraphQLElementTypes.TYPE_NAME).inside(psiElement(GraphQLElementTypes.UNION_MEMBERS))), provider);
    }

    private void completeFieldDefinitionFromImplementedInterface() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                final GraphQLDefinition definition = PsiTreeUtil.getParentOfType(completionElement, GraphQLDefinition.class);
                if (definition != null) {
                    final GraphQLImplementsInterfaces implementsInterfaces = PsiTreeUtil.findChildOfType(definition, GraphQLImplementsInterfaces.class);
                    if (implementsInterfaces != null) {
                        final Set<String> currentFields = PsiTreeUtil.findChildrenOfType(definition, GraphQLFieldDefinition.class).stream().map(PsiNamedElement::getName).collect(Collectors.toSet());
                        implementsInterfaces.getTypeNameList().forEach(t -> {
                            final GraphQLIdentifier typeDefinitionName = GraphQLPsiSearchHelper.getResolvedReference(t);
                            if (typeDefinitionName != null) {
                                final Collection<GraphQLFieldDefinition> fieldDefinitions = PsiTreeUtil.findChildrenOfType(
                                    PsiTreeUtil.getTopmostParentOfType(typeDefinitionName, GraphQLDefinition.class),
                                    GraphQLFieldDefinition.class
                                );
                                for (GraphQLFieldDefinition fieldDefinition : fieldDefinitions) {
                                    if (!currentFields.contains(fieldDefinition.getName())) {
                                        final String implementedField = t.getName() + "." + fieldDefinition.getName();
                                        result.addElement(LookupElementBuilder.create(fieldDefinition.getText()).withTypeText(implementedField, true));
                                    }
                                }
                            }
                        });
                    }
                }

            }
        };
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).withParent(psiElement(GraphQLIdentifier.class).withParent(GraphQLFieldDefinition.class)), provider);
    }

    private void completeImplementsTypeName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                final GraphQLImplementsInterfaces implementsInterfaces = PsiTreeUtil.getParentOfType(completionElement, GraphQLImplementsInterfaces.class);
                if (implementsInterfaces != null) {
                    final Set<String> currentInterfaces = Sets.newHashSet();
                    implementsInterfaces.getTypeNameList().forEach(t -> currentInterfaces.add(t.getName()));
                    final TypeDefinitionRegistry typeDefinitionRegistry = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                        .getRegistryInfo(parameters.getOriginalFile()).getTypeDefinitionRegistry();
                    typeDefinitionRegistry.getTypes(InterfaceTypeDefinition.class).forEach(schemaInterface -> {
                        if (currentInterfaces.add(schemaInterface.getName())) {
                            result.addElement(LookupElementBuilder.create(schemaInterface.getName()));
                        }
                    });
                }
            }
        };
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).inside(psiElement(GraphQLElementTypes.TYPE_NAME).inside(psiElement(GraphQLElementTypes.IMPLEMENTS_INTERFACES))), provider);
    }

    private void completeImplementsKeyword() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                final GraphQLTypeSystemDefinition typeDefinition = PsiTreeUtil.getParentOfType(completionElement, GraphQLObjectTypeDefinition.class, GraphQLObjectTypeExtensionDefinition.class, GraphQLInterfaceTypeDefinition.class, GraphQLInterfaceTypeExtensionDefinition.class);
                if (typeDefinition instanceof GraphQLObjectTypeDefinition && ((GraphQLObjectTypeDefinition) typeDefinition).getImplementsInterfaces() != null) {
                    return;
                }
                if (typeDefinition instanceof GraphQLObjectTypeExtensionDefinition && ((GraphQLObjectTypeExtensionDefinition) typeDefinition).getImplementsInterfaces() != null) {
                    return;
                }
                if (typeDefinition instanceof GraphQLInterfaceTypeDefinition && ((GraphQLInterfaceTypeDefinition) typeDefinition).getImplementsInterfaces() != null) {
                    return;
                }
                if (typeDefinition instanceof GraphQLInterfaceTypeExtensionDefinition && ((GraphQLInterfaceTypeExtensionDefinition) typeDefinition).getImplementsInterfaces() != null) {
                    return;
                }
                result.addElement(LookupElementBuilder.create("implements").withBoldness(true).withInsertHandler(AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP));
            }
        };
        final ElementPattern<PsiElement> insideTypeDefElement = PlatformPatterns.or(
            psiElement().inside(GraphQLObjectTypeDefinition.class),
            psiElement().inside(GraphQLObjectTypeExtensionDefinition.class),
            psiElement().inside(GraphQLInterfaceTypeDefinition.class),
            psiElement().inside(GraphQLInterfaceTypeExtensionDefinition.class)
        );
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).afterLeaf(psiElement(GraphQLElementTypes.NAME).inside(insideTypeDefElement)), provider);
    }

    private void completeConstantsOrListOrInputObject() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                final PsiElement completionElement = parameters.getPosition();
                final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(completionElement, GraphQLTypeScopeProvider.class);
                if (typeScopeProvider != null) {
                    final GraphQLType typeScope = typeScopeProvider.getTypeScope();
                    if (typeScope != null) {
                        final InsertHandler<LookupElement> literalInsertHandler = (ctx, item) -> {
                            ctx.getEditor().getCaretModel().moveCaretRelatively(-1, 0, false, false, false);
                            AutoPopupController.getInstance(ctx.getProject()).autoPopupMemberLookup(ctx.getEditor(), null);
                        };
                        if (typeScope instanceof GraphQLList || (typeScope instanceof GraphQLNonNull && ((GraphQLNonNull) typeScope).getWrappedType() instanceof GraphQLList)) {
                            // list or non-null list
                            result.addElement(LookupElementBuilder.create("[]").withInsertHandler(literalInsertHandler));
                        } else {
                            // raw type is enum, boolean or object
                            final GraphQLUnmodifiedType rawType = GraphQLSchemaUtil.getUnmodifiedType(typeScope);
                            if (rawType instanceof GraphQLEnumType) {
                                ((GraphQLEnumType) rawType).getValues().forEach(value -> result.addElement(LookupElementBuilder.create(value.getName()).withTypeText(GraphQLSchemaUtil.getTypeName(typeScope))));
                            } else if (rawType instanceof GraphQLInputObjectType) {
                                if (parameters.getOriginalPosition() != null && !parameters.getOriginalPosition().getText().equals("{")) {
                                    result.addElement(LookupElementBuilder.create("{}").withInsertHandler(literalInsertHandler));
                                }
                            } else {
                                if ("Boolean".equals(rawType.getName())) {
                                    result.addElement(LookupElementBuilder.create("true").withBoldness(true));
                                    result.addElement(LookupElementBuilder.create("false").withBoldness(true));
                                }
                                // TODO JKM 'null' completion?
                            }
                        }
                    }
                }

            }
        };
        // NOTE: the PSI produces enum values when none of the keywords match, e.g. 'tru' is considered a possible enum value
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).afterLeaf(":").withSuperParent(2, GraphQLEnumValue.class), provider);
    }

    private void completeEnumNamesInList() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                final PsiElement completionElement = parameters.getPosition();
                final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(completionElement, GraphQLTypeScopeProvider.class);
                if (typeScopeProvider != null) {
                    final GraphQLType typeScope = typeScopeProvider.getTypeScope();
                    if (typeScope != null) {
                        final GraphQLUnmodifiedType rawType = GraphQLSchemaUtil.getUnmodifiedType(typeScope);
                        if (rawType instanceof GraphQLEnumType) {
                            ((GraphQLEnumType) rawType).getValues().forEach(value -> result.addElement(LookupElementBuilder.create(value.getName()).withTypeText(rawType.getName())));
                        }
                    }
                }

            }
        };
        // NOTE: the PSI produces enum values when none of the keywords match, e.g. 'tru' is considered a possible enum value
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).withSuperParent(2, psiElement(GraphQLEnumValue.class).withParent(GraphQLArrayValue.class)), provider);
    }

    private void completeVariableDefinitionTypeName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());
                final TypeDefinitionRegistry registry = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getRegistryInfo(parameters.getOriginalFile()).getTypeDefinitionRegistry();
                addInputTypeCompletions(result, registry);
            }
        };
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).inside(GraphQLTypeName.class).inside(GraphQLVariableDefinition.class), provider);
    }

    private void addInputTypeCompletions(@NotNull CompletionResultSet result, TypeDefinitionRegistry registry) {
        if (registry != null) {
            registry.scalars().values().forEach(scalar -> result.addElement(LookupElementBuilder.create(scalar.getName())));
            registry.types().values().forEach(type -> {
                if (type instanceof EnumTypeDefinition || type instanceof InputObjectTypeDefinition) {
                    result.addElement(LookupElementBuilder.create(type.getName()));
                }
            });
        }
    }

    private void completeDirectiveLocation() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                final GraphQLDirectiveLocations directiveLocations = PsiTreeUtil.getParentOfType(completionElement, GraphQLDirectiveLocations.class);
                if (directiveLocations == null) {
                    return;
                }

                PsiElement prevSibling = PsiTreeUtil.skipWhitespacesAndCommentsBackward(directiveLocations);
                if (prevSibling instanceof PsiErrorElement) {
                    List<String> keywords = new SmartList<>("on");
                    if (PsiUtilCore.getElementType(prevSibling.getPrevSibling()) != GraphQLElementTypes.REPEATABLE_KEYWORD) {
                        keywords.add("repeatable");
                    }

                    for (String keyword : keywords) {
                        result.addElement(LookupElementBuilder
                            .create(keyword).withBoldness(true).withInsertHandler(AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP));
                    }
                    return;
                }

                final Set<String> currentLocations = Sets.newHashSet();
                directiveLocations.getDirectiveLocationList().forEach(location -> currentLocations.add(location.getText()));
                final GraphQLFile builtInSchema = ObjectUtils.tryCast(
                    GraphQLExternalTypeDefinitionsProvider.getInstance(completionElement.getProject()).getBuiltInSchema(), GraphQLFile.class);
                if (builtInSchema == null) {
                    return;
                }

                for (GraphQLDefinition definition : ContainerUtil.filter(builtInSchema.getTypeDefinitions(), t -> t instanceof GraphQLEnumTypeDefinition)) {
                    final GraphQLTypeNameDefinition enumTypeName = ((GraphQLEnumTypeDefinition) definition).getTypeNameDefinition();
                    if (enumTypeName == null || !__DIRECTIVE_LOCATION_ENUM.equals(enumTypeName.getName())) {
                        continue;
                    }

                    final GraphQLEnumValueDefinitions enumValueDefinitions = ((GraphQLEnumTypeDefinition) definition).getEnumValueDefinitions();
                    if (enumValueDefinitions != null) {
                        enumValueDefinitions.getEnumValueDefinitionList().forEach(value -> {
                            final String locationName = value.getEnumValue().getText();
                            if (currentLocations.add(locationName)) {
                                result.addElement(LookupElementBuilder.create(locationName).bold());
                            }
                        });
                    }
                }
            }
        };
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).withParent(GraphQLDirectiveLocation.class), provider);
    }

    private void completeDirectiveName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());
                final TypeDefinitionRegistry registry = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getRegistryInfo(completionElement).getTypeDefinitionRegistry();

                final Set<String> addedDirectiveNames = Sets.newHashSet();

                // directives declared - available even when schema validation errors are present, as in when typing/completing a directive name
                for (DirectiveDefinition directiveDefinition : registry.getDirectiveDefinitions().values()) {
                    final EnumSet<Introspection.DirectiveLocation> validLocations = EnumSet.noneOf(Introspection.DirectiveLocation.class);
                    for (DirectiveLocation directiveLocation : directiveDefinition.getDirectiveLocations()) {
                        try {
                            validLocations.add(Introspection.DirectiveLocation.valueOf(directiveLocation.getName()));
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                    if (!isValidDirectiveLocation(validLocations, parameters.getPosition())) {
                        continue;
                    }
                    LookupElementBuilder element = LookupElementBuilder.create(directiveDefinition.getName());
                    for (InputValueDefinition directiveArgument : directiveDefinition.getInputValueDefinitions()) {
                        if (directiveArgument.getType() instanceof GraphQLNonNull) {
                            // found a required argument so insert the '()' for arguments
                            element = element.withInsertHandler((ctx, item) -> {
                                ParenthesesInsertHandler.WITH_PARAMETERS.handleInsert(ctx, item);
                                AutoPopupController.getInstance(ctx.getProject()).autoPopupMemberLookup(ctx.getEditor(), null);
                            });
                            break;
                        }
                    }
                    addedDirectiveNames.add(directiveDefinition.getName());
                    result.addElement(element);
                }

                // directive including the built-in ones from a valid and working schema
                final GraphQLSchema schema = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getSchemaInfo(completionElement).getSchema();
                for (com.intellij.lang.jsgraphql.types.schema.GraphQLDirective graphQLDirective : schema.getDirectives()) {
                    if (!addedDirectiveNames.add(graphQLDirective.getName())) {
                        continue;
                    }
                    if (!isValidDirectiveLocation(graphQLDirective.validLocations(), parameters.getPosition())) {
                        continue;
                    }
                    LookupElementBuilder element = LookupElementBuilder.create(graphQLDirective.getName());
                    for (com.intellij.lang.jsgraphql.types.schema.GraphQLArgument directiveArgument : graphQLDirective.getArguments()) {
                        if (directiveArgument.getType() instanceof GraphQLNonNull) {
                            // found a required argument so insert the '()' for arguments
                            element = element.withInsertHandler((ctx, item) -> {
                                ParenthesesInsertHandler.WITH_PARAMETERS.handleInsert(ctx, item);
                                AutoPopupController.getInstance(ctx.getProject()).autoPopupMemberLookup(ctx.getEditor(), null);
                            });
                            break;
                        }
                    }
                    result.addElement(element);
                }

            }
        };
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).withSuperParent(2, GraphQLDirective.class), provider);
    }

    private void completeObjectValueField() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                final PsiElement completionElement = parameters.getPosition();
                final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(completionElement, GraphQLObjectValueImpl.class);
                if (typeScopeProvider != null) {
                    GraphQLType typeScope = typeScopeProvider.getTypeScope();
                    if (typeScope != null) {
                        // unwrap lists, non-null etc:
                        typeScope = GraphQLSchemaUtil.getUnmodifiedType(typeScope);
                        if (typeScope instanceof GraphQLInputFieldsContainer) {
                            final List<GraphQLInputObjectField> fieldDefinitions = ((GraphQLInputFieldsContainer) typeScope).getFieldDefinitions();
                            final GraphQLObjectValue objectValue = PsiTreeUtil.getParentOfType(completionElement, GraphQLObjectValue.class);
                            if (objectValue != null) {
                                // get the existing object field names to filter them out
                                final Set<String> existingFieldNames = objectValue.getObjectFieldList().stream().map(PsiNamedElement::getName).collect(Collectors.toSet());
                                for (GraphQLInputObjectField fieldDefinition : fieldDefinitions) {
                                    if (!existingFieldNames.contains(fieldDefinition.getName())) {
                                        LookupElementBuilder element = LookupElementBuilder.create(fieldDefinition.getName()).withTypeText(GraphQLSchemaUtil.typeString(fieldDefinition.getType()));
                                        if (fieldDefinition.getDescription() != null) {
                                            final String fieldDocumentation = GraphQLDocumentationMarkdownRenderer.getDescriptionAsPlainText(fieldDefinition.getDescription(), true);
                                            element = element.withTailText(" - " + fieldDocumentation, true);
                                        }
                                        result.addElement(element.withInsertHandler(AddColonSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP));
                                    }
                                }
                            }
                        }
                    }
                }

            }
        };
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).withSuperParent(2, GraphQLObjectField.class), provider);
    }

    private void completeArgumentName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());
                final GraphQLField field = PsiTreeUtil.getParentOfType(completionElement, GraphQLField.class);
                final GraphQLDirective directive = PsiTreeUtil.getParentOfType(completionElement, GraphQLDirective.class);
                if (directive != null) {
                    // directive arguments
                    if (directive.getName() != null) {
                        final GraphQLSchema schema = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                            .getSchemaInfo(completionElement).getSchema();
                        com.intellij.lang.jsgraphql.types.schema.GraphQLDirective directiveDefinition = schema.getFirstDirective(directive.getName());
                        if (directiveDefinition != null) {

                            final Set<String> existingArgumentNames = Sets.newHashSet();
                            if (directive.getArguments() != null) {
                                for (GraphQLArgument directiveArgument : directive.getArguments().getArgumentList()) {
                                    existingArgumentNames.add(directiveArgument.getName());
                                }
                            }
                            for (com.intellij.lang.jsgraphql.types.schema.GraphQLArgument graphQLArgument : directiveDefinition.getArguments()) {
                                final String name = graphQLArgument.getName();
                                if (!existingArgumentNames.contains(name)) {
                                    result.addElement(LookupElementBuilder.create(name).withTypeText(GraphQLSchemaUtil.typeString(graphQLArgument.getType())).withInsertHandler(AddColonSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP));
                                }
                            }
                        }
                    }
                } else if (field != null && field.getName() != null) {
                    // field arguments
                    final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(field, GraphQLTypeScopeProvider.class);
                    if (typeScopeProvider != null) {
                        GraphQLType typeScope = typeScopeProvider.getTypeScope();
                        if (typeScope != null) {
                            // we need the raw type to get the fields
                            typeScope = GraphQLSchemaUtil.getUnmodifiedType(typeScope);
                        }
                        if (typeScope != null) {
                            if (typeScope instanceof GraphQLFieldsContainer) {
                                final com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition fieldDefinition = ((GraphQLFieldsContainer) typeScope).getFieldDefinition(field.getName());
                                if (fieldDefinition != null && field.getArguments() != null) {
                                    final Set<String> existingArgumentNames = field.getArguments().getArgumentList().stream().map(PsiNamedElement::getName).collect(Collectors.toSet());
                                    fieldDefinition.getArguments().forEach(argumentDefinition -> {
                                        if (!existingArgumentNames.contains(argumentDefinition.getName())) {
                                            LookupElementBuilder element = LookupElementBuilder.create(argumentDefinition.getName()).withTypeText(GraphQLSchemaUtil.typeString(argumentDefinition.getType()));
                                            if (argumentDefinition.getDescription() != null) {
                                                final String argumentDocumentation = GraphQLDocumentationMarkdownRenderer.getDescriptionAsPlainText(argumentDefinition.getDescription(), true);
                                                element = element.withTailText(" - " + argumentDocumentation, true);
                                            }
                                            result.addElement(element.withInsertHandler(AddColonSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP));
                                        }
                                    });
                                }
                            }
                        }
                    }
                }

            }
        };
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).withSuperParent(2, GraphQLArgument.class), provider);
    }

    private void completeSpreadFragmentName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());

                final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(completionElement, GraphQLTypeScopeProvider.class);

                if (typeScopeProvider != null) {
                    GraphQLType typeScope = typeScopeProvider.getTypeScope();
                    if (typeScope != null) {
                        // unwrap non-nulls, lists etc. since we want the raw type to match with the fragment type conditions
                        typeScope = GraphQLSchemaUtil.getUnmodifiedType(typeScope);

                        // fragment must be compatible with the type in scope
                        final TypeDefinitionRegistry typeDefinitionRegistry = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                            .getRegistryInfo(parameters.getOriginalFile()).getTypeDefinitionRegistry();

                        final List<GraphQLFragmentDefinition> knownFragmentDefinitions = GraphQLPsiSearchHelper.getInstance(completionElement.getProject()).getKnownFragmentDefinitions(parameters.getOriginalFile());
                        for (GraphQLFragmentDefinition fragmentDefinition : knownFragmentDefinitions) {
                            final String name = fragmentDefinition.getName();
                            if (name != null) {
                                // suggest compatible fragments based on type type conditions
                                if (isFragmentApplicableInTypeScope(typeDefinitionRegistry, fragmentDefinition, typeScope)) {
                                    result.addElement(LookupElementBuilder.create(name));
                                }
                            }
                        }

                    }

                }

                // the on keyword for inline fragments
                final PsiElement beforeCompletion = PsiTreeUtil.prevLeaf(parameters.getPosition());
                final boolean addSpaceBefore = beforeCompletion != null && beforeCompletion.getNode().getElementType() == GraphQLElementTypes.SPREAD;
                final String onCompletion = addSpaceBefore ? " on" : "on";

                result.addElement(LookupElementBuilder.create(onCompletion).withPresentableText("on").withBoldness(true).withInsertHandler(AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP));

            }
        };
        extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement(GraphQLElementTypes.SPREAD)), provider);
    }

    private void completeFragmentOnTypeName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                final PsiElement completionElement = parameters.getPosition();

                // the type condition that the 'on' keyword belongs to
                GraphQLTypeCondition typeCondition = PsiTreeUtil.getParentOfType(completionElement, GraphQLTypeCondition.class);
                if (typeCondition == null) {
                    // typeCondition is on the left if the selection set follows
                    typeCondition = PsiTreeUtil.getPrevSiblingOfType(completionElement, GraphQLTypeCondition.class);
                }
                final boolean fragmentDefinition = typeCondition != null && typeCondition.getParent() instanceof GraphQLFragmentDefinition;

                final GraphQLSchemaProvider schemaProvider = GraphQLSchemaProvider.getInstance(completionElement.getProject());
                final TypeDefinitionRegistry typeDefinitionRegistry = schemaProvider
                    .getRegistryInfo(parameters.getOriginalFile()).getTypeDefinitionRegistry();

                final List<Pair<TypeDefinition, Description>> fragmentTypes = Lists.newArrayList();

                if (fragmentDefinition) {
                    // completion in a top-level fragment definition, so add all known types, interfaces, unions
                    typeDefinitionRegistry.types().forEach((key, value) -> {
                        final boolean canFragment = value instanceof ObjectTypeDefinition || value instanceof UnionTypeDefinition || value instanceof InterfaceTypeDefinition;
                        if (canFragment) {
                            fragmentTypes.add(Pair.create(value, GraphQLSchemaUtil.getTypeDefinitionDescription(value)));
                        }
                    });
                } else {

                    // inline fragment, so get type scope
                    GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(completionElement, GraphQLTypeScopeProvider.class);

                    if (typeScopeProvider instanceof GraphQLInlineFragment && ((GraphQLInlineFragment) typeScopeProvider).getTypeCondition() == typeCondition) {
                        // if the type condition belongs to the type scope provider, we want the parent scope since that
                        // is the real source of what we can fragment on
                        typeScopeProvider = PsiTreeUtil.getParentOfType(typeScopeProvider, GraphQLTypeScopeProvider.class);
                    }

                    GraphQLType rawTypeScope = typeScopeProvider != null ? typeScopeProvider.getTypeScope() : null;
                    if (rawTypeScope != null) {
                        GraphQLUnmodifiedType typeScope = GraphQLSchemaUtil.getUnmodifiedType(rawTypeScope); // unwrap non-null and lists since fragments are about the raw type
                        final TypeDefinition fragmentType = typeDefinitionRegistry.getType(typeScope.getName()).orElse(null);
                        if (fragmentType != null) {
                            final Ref<Consumer<TypeDefinition<?>>> addTypesRecursive = new Ref<>();
                            final Consumer<TypeDefinition<?>> addTypes = (typeToFragmentOn) -> {
                                if (typeToFragmentOn instanceof ObjectTypeDefinition) {
                                    fragmentTypes.add(Pair.create(typeToFragmentOn, GraphQLSchemaUtil.getTypeDefinitionDescription(typeToFragmentOn)));
                                    final List<Type> anImplements = ((ObjectTypeDefinition) typeToFragmentOn).getImplements();
                                    if (anImplements != null) {
                                        anImplements.forEach(type -> {
                                            final TypeDefinition typeDefinition = typeDefinitionRegistry.getType(type).orElse(null);
                                            if (typeDefinition instanceof InterfaceTypeDefinition) {
                                                fragmentTypes.add(Pair.create(typeDefinition, GraphQLSchemaUtil.getTypeDefinitionDescription(typeDefinition)));
                                            }
                                        });
                                    }
                                } else if (typeToFragmentOn instanceof InterfaceTypeDefinition) {
                                    fragmentTypes.add(Pair.create(typeToFragmentOn, GraphQLSchemaUtil.getTypeDefinitionDescription(typeToFragmentOn)));
                                    final List<ObjectTypeDefinition> implementationsOf = typeDefinitionRegistry.getImplementationsOf((InterfaceTypeDefinition) typeToFragmentOn);
                                    implementationsOf.forEach(impl -> fragmentTypes.add(Pair.create(impl, GraphQLSchemaUtil.getTypeDefinitionDescription(impl))));
                                } else if (typeToFragmentOn instanceof UnionTypeDefinition) {
                                    final List<Type> memberTypes = ((UnionTypeDefinition) typeToFragmentOn).getMemberTypes();
                                    if (memberTypes != null) {
                                        memberTypes.forEach(memberType -> typeDefinitionRegistry.getType(memberType).ifPresent(memberTypeDefinition -> addTypesRecursive.get().consume(memberTypeDefinition)));
                                    }
                                }
                            };
                            addTypesRecursive.set(addTypes);
                            addTypes.consume(fragmentType);
                        }
                    }

                }

                fragmentTypes.forEach(fragmentType -> {
                    LookupElementBuilder element = LookupElementBuilder
                        .create(fragmentType.first.getName())
                        .withBoldness(true);
                    if (fragmentType.second != null) {
                        final String documentation = GraphQLDocumentationMarkdownRenderer.getDescriptionAsPlainText(fragmentType.second.getContent(), true);
                        element = element.withTailText(" - " + documentation, true);
                    }
                    result.addElement(element);
                });

            }
        };
        extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement(GraphQLElementTypes.ON_KEYWORD)), provider);
    }

    private void completeFieldNames() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                // move "__*" fields to bottom
                final CompletionResultSet orderedResult = updateResult(parameters, result);

                final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());

                GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(completionElement, GraphQLTypeScopeProvider.class);

                // check for incomplete field name, in which case we want the parent element as type scope to find this field name
                GraphQLField completionField = null;
                if (completionElement.getNode().getElementType() == GraphQLElementTypes.NAME) {
                    completionField = PsiTreeUtil.getParentOfType(completionElement, GraphQLField.class);
                }
                if (typeScopeProvider == completionField) {
                    // completed on incomplete field name, use parent
                    typeScopeProvider = PsiTreeUtil.getParentOfType(typeScopeProvider, GraphQLTypeScopeProvider.class);
                }

                if (typeScopeProvider != null) {

                    PsiElement textBeforeCompletion = PsiTreeUtil.prevLeaf(completionElement);
                    boolean isSpread = textBeforeCompletion != null && textBeforeCompletion.getText().startsWith(".");

                    if (!isSpread) {
                        GraphQLType typeScope = typeScopeProvider.getTypeScope();
                        if (typeScope != null) {
                            // we need the raw type to get the fields
                            typeScope = GraphQLSchemaUtil.getUnmodifiedType(typeScope);
                        }
                        if (typeScope instanceof GraphQLFieldsContainer) {
                            ((GraphQLFieldsContainer) typeScope).getFieldDefinitions().forEach(field -> {
                                LookupElementBuilder element = LookupElementBuilder
                                    .create(field.getName())
                                    .withBoldness(true)
                                    .withTypeText(GraphQLSchemaUtil.typeString(field.getType()));
                                if (field.getDescription() != null) {
                                    final String fieldDocumentation = GraphQLDocumentationMarkdownRenderer.getDescriptionAsPlainText(field.getDescription(), true);
                                    element = element.withTailText(" - " + fieldDocumentation, true);
                                }
                                if (field.isDeprecated()) {
                                    element = element.strikeout();
                                    if (field.getDeprecationReason() != null) {
                                        final String deprecationReason = GraphQLDocumentationMarkdownRenderer.getDescriptionAsPlainText(field.getDeprecationReason(), true);
                                        element = element.withTailText(" - Deprecated: " + deprecationReason, true);
                                    }
                                }
                                for (com.intellij.lang.jsgraphql.types.schema.GraphQLArgument fieldArgument : field.getArguments()) {
                                    if (fieldArgument.getType() instanceof GraphQLNonNull) {
                                        // on of the field arguments are required, so add the '()' for arguments
                                        element = element.withInsertHandler((ctx, item) -> {
                                            ParenthesesInsertHandler.WITH_PARAMETERS.handleInsert(ctx, item);
                                            AutoPopupController.getInstance(ctx.getProject()).autoPopupMemberLookup(ctx.getEditor(), null);
                                        });
                                        break;
                                    }
                                }
                                orderedResult.addElement(element);
                            });
                        }
                        if (!(typeScopeProvider instanceof GraphQLOperationDefinition)) {
                            // show the '...' except when top level selection in an operation
                            orderedResult.addElement(LookupElementBuilder.create("...").withInsertHandler((ctx, item) -> AutoPopupController.getInstance(ctx.getProject()).autoPopupMemberLookup(ctx.getEditor(), null)));
                            // and add the built-in __typename option
                            orderedResult.addElement(LookupElementBuilder.create("__typename"));
                        }
                    }
                }
            }
        };
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).withSuperParent(2, GraphQLField.class), provider);
    }

    private void completeTypeNameToExtend() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                final GraphQLTypeExtension typeExtension = PsiTreeUtil.getParentOfType(completionElement, GraphQLTypeExtension.class);
                final TypeDefinitionRegistry typeDefinitionRegistry = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getRegistryInfo(parameters.getOriginalFile()).getTypeDefinitionRegistry();
                if (typeExtension != null) {
                    final List<TypeDefinition<?>> types = Lists.newArrayList();
                    if (typeExtension instanceof GraphQLScalarTypeExtensionDefinition) {
                        // scalars aren't fully fledged types in the registry
                        types.addAll(typeDefinitionRegistry.scalars().values());
                    } else {
                        // "real" types
                        Class<? extends TypeDefinition<?>> applicableTypes = null;
                        if (typeExtension instanceof GraphQLObjectTypeExtensionDefinition) {
                            applicableTypes = ObjectTypeDefinition.class;
                        } else if (typeExtension instanceof GraphQLInterfaceTypeExtensionDefinition) {
                            applicableTypes = InterfaceTypeDefinition.class;
                        } else if (typeExtension instanceof GraphQLUnionTypeExtensionDefinition) {
                            applicableTypes = UnionTypeDefinition.class;
                        } else if (typeExtension instanceof GraphQLEnumTypeExtensionDefinition) {
                            applicableTypes = EnumTypeDefinition.class;
                        } else if (typeExtension instanceof GraphQLInputObjectTypeExtensionDefinition) {
                            applicableTypes = InputObjectTypeDefinition.class;
                        }
                        if (applicableTypes != null) {
                            types.addAll(typeDefinitionRegistry.getTypes(applicableTypes));
                        }
                    }
                    types.forEach(type -> {
                        LookupElementBuilder element = LookupElementBuilder.create(type.getName()).withInsertHandler(AddSpaceInsertHandler.INSTANCE);
                        result.addElement(element);
                    });
                }
            }
        };
        final ElementPattern<PsiElement> extendKeywords = PlatformPatterns.or(
            psiElement(GraphQLElementTypes.TYPE_KEYWORD),
            psiElement(GraphQLElementTypes.INTERFACE_KEYWORD),
            psiElement(GraphQLElementTypes.SCALAR_KEYWORD),
            psiElement(GraphQLElementTypes.UNION_KEYWORD),
            psiElement(GraphQLElementTypes.ENUM_KEYWORD),
            psiElement(GraphQLElementTypes.INPUT_KEYWORD)
        );
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).inside(GraphQLTypeExtension.class).afterLeaf(extendKeywords), provider);
    }

    private void completeExtendFollowingKeyword() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                for (String keyword : EXTEND_KEYWORDS) {
                    LookupElementBuilder element = LookupElementBuilder.create(keyword).withBoldness(true).withInsertHandler(AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
                    result.addElement(element);
                }
            }
        };
        extend(CompletionType.BASIC, EXTEND_KEYWORD_PATTERN, provider);
    }

    private void completeTopLevelKeywords() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final LogicalPosition completionPos = parameters.getEditor().offsetToLogicalPosition(parameters.getOffset());
                final PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(parameters.getPosition());
                if (prevVisibleLeaf != null) {
                    // NOTE: "type Foo <completion>" would grammatically allow a new definition to follow immediately
                    // but this completion at that position is likely to be unexpected and would interfere with "implements" on types
                    // so we expect top level keywords to be the first visible element on the line to complete
                    if (completionPos.line == parameters.getEditor().offsetToLogicalPosition(prevVisibleLeaf.getTextOffset()).line) {
                        return;
                    }
                }
                for (String keyword : TOP_LEVEL_KEYWORDS) {
                    // TODO filter schema if already declared
                    LookupElementBuilder element = LookupElementBuilder.create(keyword).withBoldness(true);
                    if (keyword.equals("{")) {
                        element = element.withInsertHandler((ctx, item) -> {
                            EditorModificationUtil.insertStringAtCaret(ctx.getEditor(), "}");
                            PsiDocumentManager.getInstance(ctx.getProject()).commitDocument(ctx.getEditor().getDocument());
                            ctx.getEditor().getCaretModel().moveCaretRelatively(-1, 0, false, false, false);
                        });
                    } else {
                        element = element.withInsertHandler(AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
                    }
                    result.addElement(element);
                }
            }
        };
        extend(CompletionType.BASIC, TOP_LEVEL_KEYWORD_PATTERN, provider);
    }

    private void completeVariableName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(completionElement, GraphQLTypeScopeProvider.class);
                if (typeScopeProvider == null) {
                    return;
                }

                final GraphQLType typeScope = typeScopeProvider.getTypeScope();
                if (typeScope == null) {
                    return;
                }

                final GraphQLTypedOperationDefinition query = PsiTreeUtil.getParentOfType(completionElement, GraphQLTypedOperationDefinition.class);
                if (query == null || query.getVariableDefinitions() == null) {
                    return;
                }
                final VariablesTypesMatcher variablesTypesMatcher = new VariablesTypesMatcher();
                final GraphQLSchema schema = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getSchemaInfo(parameters.getOriginalFile()).getSchema();

                for (GraphQLVariableDefinition variableDefinition : query.getVariableDefinitions().getVariableDefinitions()) {
                    if (variableDefinition.getType() == null) {
                        continue;
                    }

                    final GraphQLIdentifier variableTypeIdentifier = PsiTreeUtil.findChildOfType(variableDefinition.getType(), GraphQLIdentifier.class);
                    if (variableTypeIdentifier == null) {
                        continue;
                    }
                    GraphQLType declaredType = schema.getType(variableTypeIdentifier.getText());
                    if (declaredType == null) {
                        continue;
                    }
                    if (variableTypeIdentifier.getNextSibling() instanceof LeafPsiElement && "!".equals(variableTypeIdentifier.getNextSibling().getText())) {
                        declaredType = new GraphQLNonNull(declaredType);
                    }
                    PsiElement parent = variableTypeIdentifier.getParent();
                    while (parent != null && !(parent instanceof GraphQLVariableDefinition)) {
                        if (parent instanceof GraphQLListType) {
                            declaredType = new GraphQLList(declaredType);
                        } else if (parent instanceof GraphQLNonNullType) {
                            declaredType = new GraphQLNonNull(declaredType);
                        }
                        parent = parent.getParent();
                    }

                    final String variableNameCompletion = variableDefinition.getVariable().getText();
                    if (variablesTypesMatcher.doesVariableTypesMatch(declaredType, null, typeScope)) {
                        result.addElement(LookupElementBuilder.create(variableNameCompletion)
                            .withTypeText(GraphQLSchemaUtil.typeString(declaredType)));
                    } else {
                        PsiElement elementParent = completionElement.getParent();
                        PsiElement elementGrandParent = elementParent.getParent();
                        if ((elementParent instanceof GraphQLVariable && elementGrandParent instanceof GraphQLArrayValue) ||
                            (elementParent instanceof GraphQLIdentifier && elementGrandParent instanceof GraphQLEnumValue &&
                                elementGrandParent.getParent() instanceof GraphQLArrayValue)) {
                            // variable is used inside a list literal, e.g. [$myVar], so need to unwrap the type scope
                            GraphQLType typeScopeWithoutList = GraphQLSchemaUtil.unwrapListType(typeScope);
                            if (variablesTypesMatcher.doesVariableTypesMatch(declaredType, null, typeScopeWithoutList)) {
                                result.addElement(LookupElementBuilder.create(variableNameCompletion)
                                    .withTypeText(GraphQLSchemaUtil.typeString(declaredType)));
                            }
                        }
                    }
                }
            }
        };
        extend(CompletionType.BASIC, psiElement().andOr(
            psiElement(GraphQLElementTypes.VARIABLE_NAME),
            psiElement(GraphQLElementTypes.NAME).inside(GraphQLEnumValue.class)
        ).inside(GraphQLArgument.class), provider);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isValidDirectiveLocation(EnumSet<Introspection.DirectiveLocation> validLocations, PsiElement completionPosition) {
        final GraphQLDirectivesAware directivesAware = PsiTreeUtil.getParentOfType(completionPosition, GraphQLDirectivesAware.class);
        if (directivesAware == null) {
            return false;
        }
        for (Introspection.DirectiveLocation directiveLocation : validLocations) {
            if (isValidDirectiveLocation(directivesAware, directiveLocation)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidDirectiveLocation(GraphQLDirectivesAware directivesAware, Introspection.DirectiveLocation directiveLocation) {
        switch (directiveLocation) {
            // Executable locations
            case QUERY:
                if (directivesAware instanceof GraphQLTypedOperationDefinition) {
                    GraphQLOperationType type = ((GraphQLTypedOperationDefinition) directivesAware).getOperationType();
                    return type.getNode().findChildByType(GraphQLElementTypes.QUERY_KEYWORD) != null;
                }
                break;
            case MUTATION:
                if (directivesAware instanceof GraphQLTypedOperationDefinition) {
                    GraphQLOperationType type = ((GraphQLTypedOperationDefinition) directivesAware).getOperationType();
                    return type.getNode().findChildByType(GraphQLElementTypes.MUTATION_KEYWORD) != null;
                }
                break;
            case SUBSCRIPTION:
                if (directivesAware instanceof GraphQLTypedOperationDefinition) {
                    GraphQLOperationType type = ((GraphQLTypedOperationDefinition) directivesAware).getOperationType();
                    return type.getNode().findChildByType(GraphQLElementTypes.SUBSCRIPTION_KEYWORD) != null;
                }
                break;
            case FIELD:
                return directivesAware instanceof GraphQLField;
            case FRAGMENT_DEFINITION:
                return directivesAware instanceof GraphQLFragmentDefinition;
            case FRAGMENT_SPREAD:
                return directivesAware instanceof GraphQLFragmentSpread;
            case INLINE_FRAGMENT:
                return directivesAware instanceof GraphQLInlineFragment;

            // SDL
            case SCHEMA:
                return directivesAware instanceof GraphQLSchemaDefinition || directivesAware instanceof GraphQLSchemaExtension;
            case SCALAR:
                return directivesAware instanceof GraphQLScalarTypeDefinition || directivesAware instanceof GraphQLScalarTypeExtensionDefinition;
            case OBJECT:
                return directivesAware instanceof GraphQLObjectTypeDefinition || directivesAware instanceof GraphQLObjectTypeExtensionDefinition;
            case FIELD_DEFINITION:
                return directivesAware instanceof GraphQLFieldDefinition;
            case ARGUMENT_DEFINITION:
                return directivesAware instanceof GraphQLInputValueDefinition && directivesAware.getParent() instanceof GraphQLArgumentsDefinition;
            case INTERFACE:
                return directivesAware instanceof GraphQLInterfaceTypeDefinition || directivesAware instanceof GraphQLInterfaceTypeExtensionDefinition;
            case UNION:
                return directivesAware instanceof GraphQLUnionTypeDefinition || directivesAware instanceof GraphQLUnionTypeExtensionDefinition;
            case ENUM:
                return directivesAware instanceof GraphQLEnumTypeDefinition || directivesAware instanceof GraphQLEnumTypeExtensionDefinition;
            case ENUM_VALUE:
                return directivesAware instanceof GraphQLEnumValueDefinition;
            case INPUT_OBJECT:
                return directivesAware instanceof GraphQLInputObjectTypeDefinition || directivesAware instanceof GraphQLInputObjectTypeExtensionDefinition;
            case INPUT_FIELD_DEFINITION:
                return directivesAware instanceof GraphQLInputValueDefinition && !(directivesAware.getParent() instanceof GraphQLArgumentsDefinition);
            case VARIABLE_DEFINITION:
                return directivesAware instanceof GraphQLVariableDefinition;
        }
        return false;
    }


    /**
     * Gets whether the specified fragment candidate is valid to spread inside the specified required type scope
     *
     * @param typeDefinitionRegistry registry with available schema types, used to resolve union members and interface implementations
     * @param fragmentCandidate      the fragment to check for being able to validly spread under the required type scope
     * @param requiredTypeScope      the type scope in which the fragment is a candidate to spread
     * @return true if the fragment candidate is valid to be spread inside the type scope
     */
    private boolean isFragmentApplicableInTypeScope(TypeDefinitionRegistry typeDefinitionRegistry,
                                                    GraphQLFragmentDefinition fragmentCandidate,
                                                    GraphQLType requiredTypeScope) {

        // unwrap non-nullable and list types
        requiredTypeScope = GraphQLSchemaUtil.getUnmodifiedType(requiredTypeScope);

        final GraphQLTypeCondition typeCondition = fragmentCandidate.getTypeCondition();
        if (typeCondition == null || typeCondition.getTypeName() == null) {
            return false;
        }

        final String fragmentTypeName = Optional.ofNullable(typeCondition.getTypeName().getName()).orElse("");
        if (fragmentTypeName.equals(GraphQLSchemaUtil.getTypeName(requiredTypeScope))) {
            // direct match, e.g. User scope, fragment on User
            return true;
        }

        // check whether compatible based on interfaces and unions
        return isCompatibleFragment(typeDefinitionRegistry, requiredTypeScope, fragmentTypeName);

    }

    /**
     * Gets whether a fragment type condition name is compatible with the required type scope
     *
     * @param typeDefinitionRegistry registry with available schema types, used to resolve union members and interface implementations
     * @param rawRequiredTypeScope   the type scope in which the fragment is a candidate to spread
     * @param fragmentTypeName       the name of the type that a candidate fragment applies to
     * @return true if the candidate type condtion name is compatible inside the required type scope
     */
    private boolean isCompatibleFragment(TypeDefinitionRegistry typeDefinitionRegistry,
                                         GraphQLType rawRequiredTypeScope,
                                         String fragmentTypeName) {

        // unwrap non-nullable and list types
        GraphQLUnmodifiedType requiredTypeScope = GraphQLSchemaUtil.getUnmodifiedType(rawRequiredTypeScope);

        if (requiredTypeScope instanceof GraphQLInterfaceType) {
            // also include fragments on types implementing the interface scope
            final TypeDefinition typeScopeDefinition = typeDefinitionRegistry.types().get(requiredTypeScope.getName());
            if (typeScopeDefinition != null) {
                final List<ObjectTypeDefinition> implementations = typeDefinitionRegistry.getImplementationsOf((InterfaceTypeDefinition) typeScopeDefinition);
                for (ObjectTypeDefinition implementation : implementations) {
                    if (implementation.getName().equals(fragmentTypeName)) {
                        return true;
                    }
                }
            }
        } else if (requiredTypeScope instanceof GraphQLObjectType) {
            // include fragments on the interfaces implemented by the object type
            for (GraphQLNamedOutputType graphQLOutputType : ((GraphQLObjectType) requiredTypeScope).getInterfaces()) {
                if (graphQLOutputType.getName().equals(fragmentTypeName)) {
                    return true;
                }
            }
        } else if (requiredTypeScope instanceof GraphQLUnionType) {
            for (GraphQLNamedOutputType graphQLOutputType : ((GraphQLUnionType) requiredTypeScope).getTypes()) {
                // check each type in the union for compatibility
                if (graphQLOutputType.getName().equals(fragmentTypeName) || isCompatibleFragment(typeDefinitionRegistry, graphQLOutputType, fragmentTypeName)) {
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    private CompletionResultSet updateResult(CompletionParameters params, @NotNull CompletionResultSet result) {
        CompletionResultSet completionResultSet = result;
        CompletionSorter completionSorter =
            CompletionSorter.defaultSorter(params, completionResultSet.getPrefixMatcher())
                .weighBefore("priority", new LookupElementWeigher("GraphQLWeight") {
                    @NotNull
                    @Override
                    public Comparable weigh(@NotNull LookupElement element) {
                        return new LookupElementComparator(element);
                    }
                });
        completionResultSet = completionResultSet.withRelevanceSorter(completionSorter);
        return completionResultSet;
    }

    /**
     * Moves the built-in '__' types to the bottom of the completion list by sorting them with a '|' prefix that has a higher char code value
     */
    private static class LookupElementComparator implements Comparable<LookupElementComparator> {

        private final LookupElement lookupElement;

        public LookupElementComparator(LookupElement lookupElement) {
            this.lookupElement = lookupElement;
        }

        @Override
        public int compareTo(LookupElementComparator other) {
            return getSortText(lookupElement).compareTo(getSortText(other.lookupElement));
        }

        private String getSortText(LookupElement element) {
            if (element.getLookupString().startsWith("__") || element.getLookupString().startsWith("...")) {
                return "|" + element.getLookupString();
            }
            return element.getLookupString();
        }
    }

}
