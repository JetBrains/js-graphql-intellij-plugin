/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.search;

import com.intellij.lang.jsgraphql.ide.project.GraphQLPsiSearchHelper;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;

/**
 * Provides a list of 'type' implementations for an 'interface' definition
 */
public class GraphQLDefinitionsSearchExecutor implements QueryExecutor<PsiElement, PsiElement> {

    @Override
    public boolean execute(@NotNull PsiElement queryParameters, @NotNull Processor<? super PsiElement> consumer) {
        ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> GraphQLDefinitionsSearchExecutor.doExecute(queryParameters, consumer));
        return true;
    }

    private static boolean doExecute(PsiElement sourceElement, final Processor<? super PsiElement> consumer) {

        if (sourceElement instanceof GraphQLIdentifier && sourceElement.getParent() instanceof GraphQLTypeNameDefinition) {
            final GraphQLInterfaceTypeDefinition interfaceTypeDefinition = PsiTreeUtil.getParentOfType(sourceElement, GraphQLInterfaceTypeDefinition.class);
            if (interfaceTypeDefinition != null) {
                GraphQLPsiSearchHelper.getInstance(sourceElement.getProject()).processElementsWithWord(sourceElement, sourceElement.getText(), namedElement -> {
                    ProgressManager.checkCanceled();
                    if (namedElement instanceof GraphQLIdentifier && PsiTreeUtil.getParentOfType(namedElement, GraphQLImplementsInterfaces.class) != null) {
                        final GraphQLTypeSystemDefinition typeSystemDefinition = PsiTreeUtil.getParentOfType(namedElement, GraphQLObjectTypeDefinition.class, GraphQLObjectTypeExtensionDefinition.class);
                        if (typeSystemDefinition instanceof GraphQLObjectTypeDefinition) {
                            final GraphQLTypeNameDefinition typeNameDefinition = ((GraphQLObjectTypeDefinition) typeSystemDefinition).getTypeNameDefinition();
                            if (typeNameDefinition != null) {
                                consumer.process(typeNameDefinition.getNameIdentifier());
                            }
                        } else if (typeSystemDefinition instanceof GraphQLObjectTypeExtensionDefinition) {
                            final GraphQLTypeName typeName = ((GraphQLObjectTypeExtensionDefinition) typeSystemDefinition).getTypeName();
                            if (typeName != null) {
                                consumer.process(typeName.getNameIdentifier());
                            }
                        }
                    }
                    // continue looking for all implementing types
                    return true;
                });
            }
        }

        // execute result
        return true;
    }
}
