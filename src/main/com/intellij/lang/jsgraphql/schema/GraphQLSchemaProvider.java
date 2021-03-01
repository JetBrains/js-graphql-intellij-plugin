/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry;
import org.jetbrains.annotations.NotNull;

public interface GraphQLSchemaProvider {

    /**
     * Get a registry representing the known types and fields in a schema.
     * Can be based on type system definitions, an introspection result, or the more abstract endpoint language type definition.
     * Makes best efforts to create a valid registry which could be used to create a valid schema. Merges declarations with the same name.
     *
     * @param psiElement the element from which the registry is needed, serving as a scope restriction
     * @return merged GraphQL type registry
     */
    @NotNull
    TypeDefinitionRegistry getTolerantRegistry(@NotNull PsiElement psiElement);

    /**
     * @see GraphQLSchemaProvider#getTolerantRegistry(PsiElement)
     *
     * @param psiElement the element from which the schema is needed, serving as a scope restriction
     * @return merged GraphQL schema
     */
    @NotNull
    GraphQLSchema getTolerantSchema(@NotNull PsiElement psiElement);

    @NotNull
    GraphQLValidatedSchema getValidatedSchema(@NotNull PsiElement psiElement);

    @NotNull
    static GraphQLSchemaProvider getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLSchemaProvider.class);
    }
}
