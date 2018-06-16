/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.intellij.psi.PsiElement;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeDefinitionRegistry;

public interface GraphQLTypeDefinitionRegistryService {

    /**
     * Get a registry representing the known types and fields in a schema.
     * Can be based on type system definitions, an introspection result, or the more abstract endpoint language type definition
     * @param psiElement the element from which the registry is needed, serving as a scope restriction
     */
    TypeDefinitionRegistry getRegistry(PsiElement psiElement);

    GraphQLSchema getSchema(PsiElement psiElement);

    GraphQLSchemaWithErrors getSchemaWithErrors(PsiElement psiElement);

}
