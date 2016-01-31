/**
 * Copyright (c) 2015, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema.ide.type;

import com.google.common.collect.Maps;
import com.intellij.lang.jsgraphql.languageservice.api.SchemaWithVersionResponse;
import com.intellij.lang.jsgraphql.psi.JSGraphQLNamedPropertyPsiElement;
import com.intellij.lang.jsgraphql.psi.JSGraphQLNamedTypePsiElement;
import com.intellij.lang.jsgraphql.psi.JSGraphQLPsiElement;
import com.intellij.lang.jsgraphql.schema.psi.JSGraphQLSchemaFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.Map;
import java.util.Objects;

/**
 * Contains the known types and declared fields contained in a specific GraphQL schema file
 */
public class JSGraphQLSchemaFileElements {

    private static final String QUERY = "Query";
    private static final String ANONYMOUS_QUERY = "SelectionSet";
    private static final String MUTATION = "Mutation";
    private static final String SUBSCRIPTION = "Subscription";

    private SchemaWithVersionResponse schemaWithVersion;
    private final Map<String, JSGraphQLNamedType> nameToTypes = Maps.newConcurrentMap();
    private final Map<JSGraphQLNamedPropertyPsiElement, JSGraphQLNamedType> namedTypeFromPropertyElement = Maps.newConcurrentMap();

    private final JSGraphQLSchemaFile file;

    public JSGraphQLSchemaFileElements(SchemaWithVersionResponse schemaWithVersion, JSGraphQLSchemaFile file) {
        this.schemaWithVersion = schemaWithVersion;
        this.file = file;
        collectNamedTypesAndProperties(false);
    }

    public void onPendingReloadSchema(SchemaWithVersionResponse schemaWithVersion) {
        this.schemaWithVersion = schemaWithVersion;
    }

    public void reloadSchema() {
        collectNamedTypesAndProperties(true);
    }

    public JSGraphQLNamedType getNamedType(String typeName) {
        return nameToTypes.get(typeName);
    }

    public JSGraphQLNamedType getNamedType(JSGraphQLNamedPropertyPsiElement propertyPsiElement) {
        return namedTypeFromPropertyElement.get(propertyPsiElement);
    }

    public String getSchemaUrl() {
        return schemaWithVersion != null ? schemaWithVersion.getUrl() : null;
    }

    public int getSchemaVersion() {
        return schemaWithVersion != null ? schemaWithVersion.getVersion() : -1;
    }

    public JSGraphQLSchemaFile getFile() {
        return file;
    }

    // ---- implementation ----

    private synchronized void collectNamedTypesAndProperties(boolean reload) {
        if(reload) {
            nameToTypes.clear();
            namedTypeFromPropertyElement.clear();
        }
        for (JSGraphQLPsiElement definition : PsiTreeUtil.getChildrenOfTypeAsList(file, JSGraphQLPsiElement.class)) {
            final JSGraphQLNamedTypePsiElement namedTypePsiElement = PsiTreeUtil.findChildOfType(definition, JSGraphQLNamedTypePsiElement.class);
            if(namedTypePsiElement != null) {
                final JSGraphQLNamedType namedType = new JSGraphQLNamedType(definition, namedTypePsiElement);
                final String typeName = namedTypePsiElement.getName();
                nameToTypes.put(typeName, namedType);
                if(Objects.equals(typeName, schemaWithVersion.getQueryType())) {
                    // 'query' type alias
                    nameToTypes.put(QUERY, namedType);
                    nameToTypes.put(ANONYMOUS_QUERY, namedType);
                } else if(Objects.equals(typeName, schemaWithVersion.getMutationType())) {
                    // 'mutation' type alias
                    nameToTypes.put(MUTATION, namedType);
                } else if(Objects.equals(typeName, schemaWithVersion.getSubscriptionType())) {
                    // 'subscription' type alias
                    nameToTypes.put(SUBSCRIPTION, namedType);
                }
                definition.acceptChildren(new PsiRecursiveElementVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {
                        if(element instanceof JSGraphQLNamedPropertyPsiElement) {
                            final JSGraphQLNamedPropertyPsiElement propertyElement = (JSGraphQLNamedPropertyPsiElement) element;
                            namedType.properties.put(propertyElement.getName(), new JSGraphQLPropertyType(propertyElement, namedType));
                            namedTypeFromPropertyElement.put(propertyElement, namedType);
                            // no need to visit deeper so we don't call super.visitElement
                        } else {
                            super.visitElement(element);
                        }
                    }
                });
            }
        }
    }

}
