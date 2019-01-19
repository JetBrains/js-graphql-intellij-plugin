/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.endpoint.ide.project.JSGraphQLEndpointNamedTypeRegistry;
import com.intellij.lang.jsgraphql.ide.project.GraphQLPsiSearchHelper;
import com.intellij.lang.jsgraphql.psi.GraphQLDirective;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeSystemDefinition;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import graphql.GraphQLException;
import graphql.language.AbstractNode;
import graphql.language.Document;
import graphql.language.Node;
import graphql.language.SourceLocation;
import graphql.parser.Parser;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;

import static graphql.schema.idl.ScalarInfo.STANDARD_SCALAR_DEFINITIONS;

public class SchemaIDLTypeDefinitionRegistry {

    private final GraphQLPsiSearchHelper graphQLPsiSearchHelper;
    private final Project project;
    private final GlobalSearchScope scope;
    private final PsiManager psiManager;
    private final JSGraphQLEndpointNamedTypeRegistry graphQLEndpointNamedTypeRegistry;

    private final Map<GlobalSearchScope, TypeDefinitionRegistryWithErrors> scopeToRegistry = Maps.newConcurrentMap();

    public static SchemaIDLTypeDefinitionRegistry getService(@NotNull Project project) {
        return ServiceManager.getService(project, SchemaIDLTypeDefinitionRegistry.class);
    }

    public SchemaIDLTypeDefinitionRegistry(Project project) {
        this.project = project;
        scope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(project), GraphQLFileType.INSTANCE);
        psiManager = PsiManager.getInstance(project);
        graphQLEndpointNamedTypeRegistry = JSGraphQLEndpointNamedTypeRegistry.getService(project);
        graphQLPsiSearchHelper = GraphQLPsiSearchHelper.getService(project);
        project.getMessageBus().connect().subscribe(GraphQLSchemaChangeListener.TOPIC, new GraphQLSchemaEventListener() {
            @Override
            public void onGraphQLSchemaChanged() {
                scopeToRegistry.clear();
            }
        });

        // don't want the "Java" scalars to be available without being declared explicitly
        STANDARD_SCALAR_DEFINITIONS.remove("Long");
        STANDARD_SCALAR_DEFINITIONS.remove("BigInteger");
        STANDARD_SCALAR_DEFINITIONS.remove("BigDecimal");
        STANDARD_SCALAR_DEFINITIONS.remove("Short");
        STANDARD_SCALAR_DEFINITIONS.remove("Char");

    }

    public TypeDefinitionRegistryWithErrors getRegistryWithErrors(PsiElement scopedElement) {

        // Get the search scope that limits schema definition for the scoped element
        final GlobalSearchScope schemaScope = graphQLPsiSearchHelper.getSchemaScope(scopedElement);

        return scopeToRegistry.computeIfAbsent(schemaScope, s -> {

            final Parser parser = new Parser();

            final TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
            final List<GraphQLException> errors = Lists.newArrayList();

            Consumer<PsiFile> processFile = psiFile -> {
                final GraphQLTypeSystemDefinition[] typeSystemDefinitions = PsiTreeUtil.getChildrenOfType(psiFile, GraphQLTypeSystemDefinition.class);
                if (typeSystemDefinitions != null) {

                    // count out the new lines to be able to map from text offset in the buffer to line number (1-based

                    final String fileBuffer = psiFile.getText();
                    final Map<Integer, Integer> offsetToLine = Maps.newHashMap();
                    int currentLine = 1; // GraphQL antlr parser is 1-based for line numbers
                    for(int i = 0; i < fileBuffer.length(); i++) {
                        if(fileBuffer.charAt(i) == '\n') {
                            currentLine++;
                            offsetToLine.put(i + 1, currentLine);
                        }
                    }

                    for (GraphQLTypeSystemDefinition typeSystemDefinition : typeSystemDefinitions) {

                        // parse each definition separately since graphql-java has no error recovery, and it's likely there's errors in the editor

                        // track where the definition starts, but also include white space and comments leading up to the definition
                        // since it can contain docs and affects line/col
                        int bufferStart = typeSystemDefinition.getTextOffset();
                        int prefixStart = bufferStart;

                        PsiElement prevSibling = typeSystemDefinition.getPrevSibling();
                        while(prevSibling instanceof PsiComment || prevSibling instanceof PsiWhiteSpace) {
                            prefixStart = prevSibling.getTextOffset();
                            prevSibling = prevSibling.getPrevSibling();
                        }

                        String definitionPrefix;
                        if(prefixStart < bufferStart) {
                            definitionPrefix = fileBuffer.substring(prefixStart, bufferStart);
                        } else {
                            definitionPrefix = "";
                        }

                        Ref<Integer> lineDelta = new Ref<>(0);
                        for (int i = prefixStart; i > 0; i--) {
                            Integer lineAtOffset = offsetToLine.get(i);
                            if(lineAtOffset != null) {
                                lineDelta.set(lineAtOffset - 1);
                                break;
                            }
                        }

                        try {
                            final String definitionSourceText = typeSystemDefinition.getText();
                            final StringBuffer typeSystemDefinitionBuffer = new StringBuffer(definitionPrefix.length() + definitionSourceText.length());
                            typeSystemDefinitionBuffer.append(definitionPrefix).append(definitionSourceText);
                            // if there are syntax errors on optional elements, replace them with whitespace
                            PsiTreeUtil.findChildrenOfType(typeSystemDefinition, PsiErrorElement.class).forEach(error -> {
                                final PsiElement parent = error.getParent();
                                if(parent instanceof GraphQLDirective) {
                                    // happens when typing '@' and the name of the directive is still missing
                                    final TextRange textRange = parent.getTextRange().shiftLeft(typeSystemDefinition.getTextRange().getStartOffset());
                                    if(!textRange.isEmpty()) {
                                        typeSystemDefinitionBuffer.replace(textRange.getStartOffset(), textRange.getEndOffset(), StringUtil.repeat(" ", textRange.getLength()));
                                    }
                                }
                            });

                            final Document document = parser.parseDocument(typeSystemDefinitionBuffer.toString(), GraphQLPsiSearchHelper.getFileName(psiFile));

                            // adjust line numbers in source locations if there's a line delta compared to the original file buffer
                            if (lineDelta.get() > 0) {
                                final Ref<Consumer<Node>> adjustSourceLines = new Ref<>();
                                final Set<Node> visitedNodes = Sets.newHashSet();
                                adjustSourceLines.set((Node node) -> {
                                    if(node == null || !visitedNodes.add(node)) {
                                        return;
                                    }
                                    if(node instanceof AbstractNode) {
                                        final SourceLocation sourceLocation = node.getSourceLocation();
                                        if (sourceLocation != null) {
                                            final SourceLocation newSourceLocation = new SourceLocation(
                                                    sourceLocation.getLine() + lineDelta.get(),
                                                    sourceLocation.getColumn(),
                                                    sourceLocation.getSourceName()
                                            );
                                            ((AbstractNode) node).setSourceLocation(newSourceLocation);
                                        }

                                    }
                                    //noinspection unchecked
                                    final List<Node> children = node.getChildren();
                                    if(children != null) {
                                        //noinspection unchecked
                                        children.forEach(child -> {
                                            if(child != null) {
                                                adjustSourceLines.get().accept(child);
                                            }
                                        });
                                    }
                                });
                                adjustSourceLines.get().accept(document);
                            }

                            typeRegistry.merge(new SchemaParser().buildRegistry(document));
                        } catch (GraphQLException | CancellationException e) {
                            if(e instanceof GraphQLException) {
                                errors.add((GraphQLException) e);
                            }
                            // CancellationException is a parse error, but we don't always have a valid program as the user types, so that's expected
                        }
                    }
                }
            };

            // GraphQL files
            FileTypeIndex.processFiles(GraphQLFileType.INSTANCE, file -> {
                final PsiFile psiFile = psiManager.findFile(file);
                if (psiFile != null) {
                    processFile.accept(psiFile);
                }
                return true;
            }, scope.intersectWith(schemaScope));

            // Injected GraphQL
            graphQLPsiSearchHelper.processInjectedGraphQLPsiFiles(scopedElement, schemaScope, processFile);

            // Built-in that are additions to a default registry which already has the GraphQL spec directives
            graphQLPsiSearchHelper.processAdditionalBuiltInPsiFiles(schemaScope, processFile);

            // Types defined using GraphQL Endpoint Language
            if(graphQLEndpointNamedTypeRegistry.hasEndpointEntryFile()) {
                final TypeDefinitionRegistryWithErrors endpointTypesAsRegistry = graphQLEndpointNamedTypeRegistry.getTypesAsRegistry();
                typeRegistry.merge(endpointTypesAsRegistry.getRegistry());
                errors.addAll(endpointTypesAsRegistry.getErrors());
            }

            return new TypeDefinitionRegistryWithErrors(typeRegistry, errors);

        });

    }
}
