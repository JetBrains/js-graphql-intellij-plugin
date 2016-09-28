/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.project;

import com.google.common.collect.Maps;
import com.intellij.lang.jsgraphql.endpoint.psi.*;
import com.intellij.lang.jsgraphql.ide.configuration.JSGraphQLConfigurationProvider;
import com.intellij.lang.jsgraphql.psi.JSGraphQLElementType;
import com.intellij.lang.jsgraphql.schema.ide.type.JSGraphQLNamedType;
import com.intellij.lang.jsgraphql.schema.ide.type.JSGraphQLNamedTypeRegistry;
import com.intellij.lang.jsgraphql.schema.ide.type.JSGraphQLPropertyType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.AnyPsiChangeListener;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Registry for resolving references to PSI Elements in the Endpoint language.
 */
public class JSGraphQLEndpointNamedTypeRegistry implements JSGraphQLNamedTypeRegistry {

    private final JSGraphQLConfigurationProvider configurationProvider;
    private final Project project;

    private final Map<Project, Map<String, JSGraphQLNamedType>> endpointTypesByName = Maps.newConcurrentMap();
    private final Map<Project, PsiFile> endpointEntryPsiFile = Maps.newConcurrentMap();

    public static JSGraphQLEndpointNamedTypeRegistry getService(@NotNull Project project) {
        return ServiceManager.getService(project, JSGraphQLEndpointNamedTypeRegistry.class);
    }

    public JSGraphQLEndpointNamedTypeRegistry(Project project) {
        this.project = project;
        this.configurationProvider = JSGraphQLConfigurationProvider.getService(project);
        project.getMessageBus().connect().subscribe(PsiManagerImpl.ANY_PSI_CHANGE_TOPIC, new AnyPsiChangeListener.Adapter() {
            @Override
            public void beforePsiChanged(boolean isPhysical) {
                // clear the cache on each PSI change
                endpointTypesByName.clear();
                endpointEntryPsiFile.clear();
            }
        });
    }

    public boolean hasEndpointEntryFile() {
        return getEndpointEntryPsiFile() != null;
    }

    private PsiFile getEndpointEntryPsiFile() {
        return endpointEntryPsiFile.computeIfAbsent(project, p -> {
            final VirtualFile endpointEntryFile = configurationProvider.getEndpointEntryFile();
            if (endpointEntryFile != null) {
                return PsiManager.getInstance(project).findFile(endpointEntryFile);
            }
            return null;
        });
    }

    @Override
    public JSGraphQLNamedType getNamedType(String typeNameToGet) {
        return computeNamedTypes().get(typeNameToGet);
    }

    public void enumerateTypes(Consumer<JSGraphQLNamedType> consumer) {
        computeNamedTypes().forEach((key, jsGraphQLNamedType) -> consumer.accept(jsGraphQLNamedType));
    }

    private Map<String, JSGraphQLNamedType> computeNamedTypes() {
        return endpointTypesByName.computeIfAbsent(project, p -> {
            final Map<String, JSGraphQLNamedType> result = Maps.newConcurrentMap();
            final PsiFile entryPsiFile = getEndpointEntryPsiFile();
            if (entryPsiFile != null) {
                Collection<JSGraphQLEndpointNamedTypeDefinition> endpointNamedTypeDefinitions = JSGraphQLEndpointPsiUtil.getKnownDefinitions(
                        entryPsiFile,
                        JSGraphQLEndpointNamedTypeDefinition.class,
                        true,
                        null
                );
                for (JSGraphQLEndpointNamedTypeDefinition typeDefinition : endpointNamedTypeDefinitions) {
                    if (typeDefinition.getNamedTypeDef() != null) {
                        final String typeName = typeDefinition.getNamedTypeDef().getText();
                        final JSGraphQLNamedType namedType = new JSGraphQLNamedType(typeDefinition, typeDefinition.getNamedTypeDef());
                        final JSGraphQLEndpointFieldDefinitionSet fieldDefinitionSet = PsiTreeUtil.findChildOfType(typeDefinition, JSGraphQLEndpointFieldDefinitionSet.class);
                        if (fieldDefinitionSet != null) {
                            final JSGraphQLEndpointFieldDefinition[] fields = PsiTreeUtil.getChildrenOfType(fieldDefinitionSet, JSGraphQLEndpointFieldDefinition.class);
                            if (fields != null) {
                                for (JSGraphQLEndpointFieldDefinition field : fields) {
                                    final JSGraphQLEndpointCompositeType propertyValueType = field.getCompositeType();
                                    if (propertyValueType != null) {
                                        String propertyValueTypeName = null;
                                        if (propertyValueType.getListType() != null) {
                                            final JSGraphQLEndpointNamedType listItemType = propertyValueType.getListType().getNamedType();
                                            if (listItemType != null) {
                                                propertyValueTypeName = listItemType.getText();
                                            }
                                        } else if (propertyValueType.getNamedType() != null) {
                                            propertyValueTypeName = propertyValueType.getNamedType().getText();
                                        }
                                        if (propertyValueTypeName != null) {
                                            namedType.properties.put(
                                                    field.getProperty().getText(),
                                                    new JSGraphQLPropertyType(field.getProperty(), namedType, propertyValueTypeName)
                                            );
                                        }
                                    }
                                }
                            }
                        }
                        result.put(typeName, namedType);
                        if (JSGraphQLElementType.QUERY_KIND.equals(typeName)) {
                            // also use Query for anonymous queries that are selection sets
                            result.put(JSGraphQLElementType.SELECTION_SET_KIND, namedType);
                        }
                    }
                }
            }

            return result;

        });
    }
}
