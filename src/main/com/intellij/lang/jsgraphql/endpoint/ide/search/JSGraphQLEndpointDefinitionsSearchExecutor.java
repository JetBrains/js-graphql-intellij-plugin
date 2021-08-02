/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.search;

import com.intellij.lang.jsgraphql.endpoint.ide.project.JSGraphQLEndpointNamedTypeRegistry;
import com.intellij.lang.jsgraphql.endpoint.psi.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Provides a list of 'type' implementations for an 'interface' definition in a GraphQL Endpoint file.
 * Also provides implementations of interface fields that have an override in implementing types.
 */
public class JSGraphQLEndpointDefinitionsSearchExecutor implements QueryExecutor<PsiElement, PsiElement> {

    @Override
    public boolean execute(@NotNull PsiElement queryParameters, @NotNull Processor<? super PsiElement> consumer) {
        ApplicationManager.getApplication().runReadAction((Computable) () -> JSGraphQLEndpointDefinitionsSearchExecutor.doExecute(queryParameters, consumer));
        return true;
    }

    private static boolean doExecute(PsiElement sourceElement, final Processor<? super PsiElement> consumer) {

        // must be an interface definition with a named type to be applicable
        final Ref<JSGraphQLEndpointNamedTypeDef> sourceNamedTypeDef = new Ref<>();
        final Ref<JSGraphQLEndpointProperty> sourceProperty = new Ref<>();
        final Ref<JSGraphQLEndpointInterfaceTypeDefinition> sourceInterfaceDefinition = new Ref<>();

        if (sourceElement instanceof JSGraphQLEndpointNamedTypeDef) {
            sourceNamedTypeDef.set((JSGraphQLEndpointNamedTypeDef) sourceElement);
            sourceInterfaceDefinition.set(PsiTreeUtil.getParentOfType(sourceNamedTypeDef.get(), JSGraphQLEndpointInterfaceTypeDefinition.class));
        } else if (sourceElement instanceof JSGraphQLEndpointProperty) {
            sourceProperty.set((JSGraphQLEndpointProperty) sourceElement);
            sourceInterfaceDefinition.set(PsiTreeUtil.getParentOfType(sourceProperty.get(), JSGraphQLEndpointInterfaceTypeDefinition.class));
            if (sourceInterfaceDefinition.get() != null) {
                sourceNamedTypeDef.set(sourceInterfaceDefinition.get().getNamedTypeDef());
            }
        }

        if (sourceNamedTypeDef.get() != null && sourceInterfaceDefinition.get() != null) {

            final String interfaceName = sourceNamedTypeDef.get().getText();

            final JSGraphQLEndpointNamedTypeRegistry typeRegistry = JSGraphQLEndpointNamedTypeRegistry.getService(sourceElement.getProject());
            typeRegistry.enumerateTypes(sourceElement, jsGraphQLNamedType -> {
                if (jsGraphQLNamedType.definitionElement instanceof JSGraphQLEndpointObjectTypeDefinition) {
                    final JSGraphQLEndpointObjectTypeDefinition typeDefinition = (JSGraphQLEndpointObjectTypeDefinition) jsGraphQLNamedType.definitionElement;
                    final JSGraphQLEndpointImplementsInterfaces implementsInterfaces = typeDefinition.getImplementsInterfaces();
                    if (implementsInterfaces != null) {
                        for (JSGraphQLEndpointNamedType namedType : implementsInterfaces.getNamedTypeList()) {
                            if (interfaceName.equals(namedType.getName())) {
                                if (sourceProperty.get() == null) {
                                    // type implements the interface
                                    consumer.process(typeDefinition.getNamedTypeDef());
                                } else {
                                    // locate field overrides
                                    final String propertyName = sourceProperty.get().getName();
                                    jsGraphQLNamedType.definitionElement.accept(new PsiRecursiveElementVisitor() {
                                        @Override
                                        public void visitElement(PsiElement element) {
                                            if (element instanceof JSGraphQLEndpointProperty) {
                                                if ((Objects.equals(propertyName, ((JSGraphQLEndpointProperty) element).getName()))) {
                                                    consumer.process(element);
                                                }
                                                return; // don't visit deeper than properties
                                            }
                                            super.visitElement(element);
                                        }
                                    });
                                }
                                break;
                            }
                        }
                    }
                }
            });


        }
        return true;
    }
}
