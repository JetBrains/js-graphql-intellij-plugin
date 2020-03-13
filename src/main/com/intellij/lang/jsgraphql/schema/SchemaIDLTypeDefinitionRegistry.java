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
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.json.JsonFileType;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.lang.jsgraphql.endpoint.ide.project.JSGraphQLEndpointNamedTypeRegistry;
import com.intellij.lang.jsgraphql.ide.editor.GraphQLIntrospectionHelper;
import com.intellij.lang.jsgraphql.ide.project.GraphQLInjectionSearchHelper;
import com.intellij.lang.jsgraphql.ide.project.GraphQLPsiSearchHelper;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.psi.GraphQLDirective;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeSystemDefinition;
import com.intellij.lang.jsgraphql.utils.GraphQLUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import graphql.GraphQLException;
import graphql.InvalidSyntaxError;
import graphql.language.Document;
import graphql.language.SourceLocation;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.errors.SchemaProblem;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.schema.GraphQLSchemaKeys.*;
import static graphql.schema.idl.ScalarInfo.STANDARD_SCALAR_DEFINITIONS;

public class SchemaIDLTypeDefinitionRegistry {

    private final GraphQLPsiSearchHelper graphQLPsiSearchHelper;
    private final Project project;
    private final GlobalSearchScope scope;
    private final GlobalSearchScope introspectionScope;
    private final PsiManager psiManager;
    private final JSGraphQLEndpointNamedTypeRegistry graphQLEndpointNamedTypeRegistry;
    private final GraphQLConfigManager graphQLConfigManager;
    private final GraphQLInjectionSearchHelper graphQLInjectionSearchHelper;

    private final Map<GlobalSearchScope, TypeDefinitionRegistryWithErrors> scopeToRegistry = Maps.newConcurrentMap();

    public static SchemaIDLTypeDefinitionRegistry getService(@NotNull Project project) {
        return ServiceManager.getService(project, SchemaIDLTypeDefinitionRegistry.class);
    }

    public SchemaIDLTypeDefinitionRegistry(Project project) {
        this.project = project;
        scope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(project), GraphQLFileType.INSTANCE);
        introspectionScope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(project), JsonFileType.INSTANCE);
        psiManager = PsiManager.getInstance(project);
        graphQLEndpointNamedTypeRegistry = JSGraphQLEndpointNamedTypeRegistry.getService(project);
        graphQLPsiSearchHelper = GraphQLPsiSearchHelper.getService(project);
        graphQLConfigManager = GraphQLConfigManager.getService(project);
        graphQLInjectionSearchHelper = ServiceManager.getService(GraphQLInjectionSearchHelper.class);
        project.getMessageBus().connect().subscribe(GraphQLSchemaChangeListener.TOPIC, new GraphQLSchemaEventListener() {
            @Override
            public void onGraphQLSchemaChanged(Integer schemaVersion) {
                scopeToRegistry.clear();
            }
        });

        // don't want the "Java" scalars to be available without being declared explicitly
        STANDARD_SCALAR_DEFINITIONS.remove("Long");
        STANDARD_SCALAR_DEFINITIONS.remove("BigInteger");
        STANDARD_SCALAR_DEFINITIONS.remove("BigDecimal");
        STANDARD_SCALAR_DEFINITIONS.remove("Short");
        STANDARD_SCALAR_DEFINITIONS.remove("Char");
        STANDARD_SCALAR_DEFINITIONS.remove("Byte");

    }

    public TypeDefinitionRegistryWithErrors getRegistryWithErrors(PsiElement scopedElement) {

        // Get the search scope that limits schema definition for the scoped element
        final GlobalSearchScope schemaScope = graphQLPsiSearchHelper.getSchemaScope(scopedElement);

        return scopeToRegistry.computeIfAbsent(schemaScope, s -> {

            final TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
            final List<GraphQLException> errors = Lists.newArrayList();

            final Ref<Boolean> processedGraphQL = Ref.create(false);

            Consumer<PsiFile> processFile = psiFile -> {
                if (!(psiFile instanceof GraphQLFile)) {
                    return;
                }
                processedGraphQL.set(true);
                final GraphQLTypeSystemDefinition[] typeSystemDefinitions = PsiTreeUtil.getChildrenOfType(psiFile, GraphQLTypeSystemDefinition.class);
                if (typeSystemDefinitions != null) {

                    // for injected GraphQL we need to take the location of the injection into account, so count the lines plus first-line column delta
                    int injectedFirstLineColumnDelta = 0;
                    int injectionLineDelta = 0;
                    if (psiFile.getContext() != null) {
                        int endOffset = psiFile.getContext().getTextOffset();
                        final PsiFile fileWithInjection = psiFile.getContext().getContainingFile();
                        final CharSequence injectionBuffer = fileWithInjection.getViewProvider().getContents();
                        for (int i = 0; i < endOffset; i++) {
                            if (injectionBuffer.charAt(i) == '\n') {
                                injectedFirstLineColumnDelta = 0;
                                injectionLineDelta++;
                            } else {
                                injectedFirstLineColumnDelta++;
                            }
                        }
                    }

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
                            String definitionSourceText = typeSystemDefinition.getText();
                            if (graphQLInjectionSearchHelper != null && psiFile.getContext() instanceof PsiLanguageInjectionHost) {
                                definitionSourceText = graphQLInjectionSearchHelper.applyInjectionDelimitingQuotesEscape(definitionSourceText);
                            }
                            final StringBuffer typeSystemDefinitionBuffer = new StringBuffer(definitionPrefix.length() + definitionSourceText.length());
                            typeSystemDefinitionBuffer.append(definitionPrefix).append(definitionSourceText);
                            // if there are syntax errors on optional elements, replace them with whitespace
                            PsiTreeUtil.findChildrenOfType(typeSystemDefinition, PsiErrorElement.class).forEach(error -> {
                                final PsiElement parent = error.getParent();
                                if(parent instanceof GraphQLDirective) {
                                    // happens when typing '@' and the name of the directive is still missing
                                    final int delta = typeSystemDefinition.getTextRange().getStartOffset();
                                    final TextRange parentRange = parent.getTextRange();
                                    final TextRange textRange = new TextRange(parentRange.getStartOffset() - delta, parentRange.getEndOffset() - delta);
                                    if(!textRange.isEmpty()) {
                                        typeSystemDefinitionBuffer.replace(textRange.getStartOffset(), textRange.getEndOffset(), StringUtil.repeat(" ", textRange.getLength()));
                                    }
                                }
                            });

                            // adjust line numbers in source locations if there's a line delta compared to the original file buffer
                            final Document document = GraphQLUtil.parseDocument(typeSystemDefinitionBuffer.toString(), GraphQLPsiSearchHelper.getFileName(psiFile), lineDelta.get() + injectionLineDelta, injectedFirstLineColumnDelta);

                            typeRegistry.merge(new SchemaParser().buildRegistry(document));
                        } catch (GraphQLException | CancellationException e) {
                            if(e instanceof GraphQLException) {
                                errors.add((GraphQLException) e);
                            } else if (e instanceof CancellationException) {
                                // CancellationException is a parse error, but we don't always have a valid program as the user types, so that's expected
                                if(e.getCause() instanceof RecognitionException) {
                                    final Token offendingToken = ((RecognitionException) e.getCause()).getOffendingToken();
                                    if(offendingToken != null) {
                                        final List<SourceLocation> sourceLocation = Collections.singletonList(
                                                GraphQLUtil.createSourceLocationFromDelta(offendingToken, lineDelta.get() + injectionLineDelta, injectedFirstLineColumnDelta)
                                        );
                                        errors.add(new SchemaProblem(Collections.singletonList(new InvalidSyntaxError(sourceLocation, "Unexpected token: \"" + offendingToken.getText() + "\""))));
                                    }
                                }
                            }
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

            // JSON GraphQL introspection result files
            if(!graphQLConfigManager.getConfigurationsByPath().isEmpty()) {
                // need one or more configurations to be able to point "schemaPath" to relevant JSON files
                // otherwise all JSON files would be in scope
                FileTypeIndex.processFiles(JsonFileType.INSTANCE, file -> {
                    // only JSON files that are directly referenced as "schemaPath" from the .graphqlconfig will be
                    // considered within scope, so we can just go ahead and try to turn the JSON into GraphQL
                    final PsiFile psiFile = psiManager.findFile(file);
                    if (psiFile != null) {
                        try {
                            synchronized (GRAPHQL_INTROSPECTION_JSON_TO_SDL) {
                                final String introspectionJsonAsGraphQL = GraphQLIntrospectionHelper.getService(project).printIntrospectionJsonAsGraphQL(psiFile.getText());
                                final GraphQLFile currentSDLPsiFile = psiFile.getUserData(GRAPHQL_INTROSPECTION_JSON_TO_SDL);
                                if (currentSDLPsiFile != null && currentSDLPsiFile.getText().equals(introspectionJsonAsGraphQL)) {
                                    // already have a PSI file that matches the introspection SDL
                                    processFile.accept(currentSDLPsiFile);
                                } else {
                                    final PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
                                    final String fileName = file.getPath();
                                    final GraphQLFile newIntrospectionFile = (GraphQLFile) psiFileFactory.createFileFromText(fileName, GraphQLLanguage.INSTANCE, introspectionJsonAsGraphQL);
                                    newIntrospectionFile.putUserData(IS_GRAPHQL_INTROSPECTION_SDL, true);
                                    newIntrospectionFile.putUserData(GRAPHQL_INTROSPECTION_SDL_TO_JSON, psiFile);
                                    newIntrospectionFile.getVirtualFile().putUserData(IS_GRAPHQL_INTROSPECTION_SDL, true);
                                    newIntrospectionFile.getVirtualFile().putUserData(GRAPHQL_INTROSPECTION_SDL_TO_JSON, psiFile);
                                    newIntrospectionFile.getVirtualFile().setWritable(false);
                                    psiFile.putUserData(GRAPHQL_INTROSPECTION_JSON_TO_SDL, newIntrospectionFile);
                                    file.putUserData(GRAPHQL_INTROSPECTION_JSON_TO_SDL, newIntrospectionFile);
                                    processFile.accept(newIntrospectionFile);
                                }
                            }
                        } catch (Exception e) {
                            final List<SourceLocation> sourceLocation = Collections.singletonList(new SourceLocation(1, 1, GraphQLPsiSearchHelper.getFileName(psiFile)));
                            errors.add(new SchemaProblem(Collections.singletonList(new InvalidSyntaxError(sourceLocation, e.getMessage()))));
                        }
                    }
                    return true;
                }, introspectionScope.intersectWith(schemaScope));
            }

            // Injected GraphQL
            graphQLPsiSearchHelper.processInjectedGraphQLPsiFiles(scopedElement, schemaScope, processFile);

            // Built-in that are additions to a default registry which already has the GraphQL spec directives
            graphQLPsiSearchHelper.processAdditionalBuiltInPsiFiles(schemaScope, processFile);

            // Types defined using GraphQL Endpoint Language
            VirtualFile virtualFile = scopedElement.getContainingFile().getOriginalFile().getVirtualFile();
            while (virtualFile instanceof VirtualFileWindow) {
                virtualFile = ((VirtualFileWindow) virtualFile).getDelegate();
            }
            if (graphQLConfigManager.getEndpointLanguageConfiguration(virtualFile, null) != null) {
                final TypeDefinitionRegistryWithErrors endpointTypesAsRegistry = graphQLEndpointNamedTypeRegistry.getTypesAsRegistry(scopedElement);
                try {
                    typeRegistry.merge(endpointTypesAsRegistry.getRegistry());
                    errors.addAll(endpointTypesAsRegistry.getErrors());
                } catch (GraphQLException e) {
                    errors.add(e);
                }
            }

            return new TypeDefinitionRegistryWithErrors(typeRegistry, errors, processedGraphQL.get());

        });

    }
}
