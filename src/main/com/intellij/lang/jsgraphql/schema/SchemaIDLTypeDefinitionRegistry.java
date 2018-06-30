/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.google.common.collect.Lists;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.ide.project.GraphQLPsiSearchHelper;
import com.intellij.lang.jsgraphql.psi.GraphQLDirective;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeSystemDefinition;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import graphql.GraphQLException;
import graphql.language.Document;
import graphql.parser.Parser;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;

public class SchemaIDLTypeDefinitionRegistry {

    private Project project;
    private GlobalSearchScope scope;
    private PsiManager psiManager;

    public static SchemaIDLTypeDefinitionRegistry getService(@NotNull Project project) {
        return ServiceManager.getService(project, SchemaIDLTypeDefinitionRegistry.class);
    }

    public SchemaIDLTypeDefinitionRegistry(Project project) {
        this.project = project;
        scope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(project), GraphQLFileType.INSTANCE);
        psiManager = PsiManager.getInstance(project);
    }

    public TypeDefinitionRegistry getRegistry(PsiElement scopedElement) {
        return getRegistryWithErrors(scopedElement).getRegistry();
    }

    public TypeDefinitionRegistryWithErrors getRegistryWithErrors(PsiElement scopedElement) {

        final Parser parser = new Parser();

        final TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
        final List<GraphQLException> errros = Lists.newArrayList();

        Consumer<PsiFile> processFile = psiFile -> {
            final GraphQLTypeSystemDefinition[] typeSystemDefinitions = PsiTreeUtil.getChildrenOfType(psiFile, GraphQLTypeSystemDefinition.class);
            if (typeSystemDefinitions != null) {

                // padding is a whitespace version of the file which preserves line numbers
                final StringBuilder padding = new StringBuilder(psiFile.getText());
                for(int i = 0; i < padding.length(); i++) {
                    char c = padding.charAt(i);
                    switch (c) {
                        case '\t':
                        case '\n':
                            break;
                        default:
                            padding.setCharAt(i, ' ');
                            break;
                    }
                }

                for (GraphQLTypeSystemDefinition typeSystemDefinition : typeSystemDefinitions) {
                    // parse each definition separately since graphql-java has no error recovery, and it's likely there's errors in the editor
                    try {
                        final StringBuffer schemaText = new StringBuffer(typeSystemDefinition.getText());
                        // if there are syntax errors on optional elements, replace them with whitespace
                        PsiTreeUtil.findChildrenOfType(typeSystemDefinition, PsiErrorElement.class).forEach(error -> {
                            final PsiElement parent = error.getParent();
                            if(parent instanceof GraphQLDirective) {
                                // happens when typing '@' and the name of the directive is still missing
                                final TextRange textRange = parent.getTextRange().shiftLeft(typeSystemDefinition.getTextRange().getStartOffset());
                                if(!textRange.isEmpty()) {
                                    schemaText.replace(textRange.getStartOffset(), textRange.getEndOffset(), StringUtil.repeat(" ", textRange.getLength()));
                                }
                            }
                        });

                        // finally, add whitespace up to the location of the definition to get alignment between source locations in the PSI and the graphql-java AST
                        String paddedSchemaText = schemaText.toString();
                        final int textOffset = typeSystemDefinition.getTextOffset();
                        if(textOffset > 0) {
                            paddedSchemaText = padding.substring(0, textOffset) + paddedSchemaText;
                        }

                        final Document document = parser.parseDocument(paddedSchemaText, GraphQLPsiSearchHelper.getFileName(psiFile));
                        typeRegistry.merge(new SchemaParser().buildRegistry(document));
                    } catch (GraphQLException | CancellationException e) {
                        if(e instanceof GraphQLException) {
                            errros.add((GraphQLException) e);
                        }
                        // CancellationException is a parse error, but we don't always have a valid program as the user types, so that's expected
                    }
                }
            }
        };

        final GraphQLPsiSearchHelper graphQLPsiSearchHelper = GraphQLPsiSearchHelper.getService(project);

        // Get the search scope that limits schema definition for the scoped element
        GlobalSearchScope schemaScope = graphQLPsiSearchHelper.getSchemaScope(scopedElement);

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

        return new TypeDefinitionRegistryWithErrors(typeRegistry, errros);
    }
}
