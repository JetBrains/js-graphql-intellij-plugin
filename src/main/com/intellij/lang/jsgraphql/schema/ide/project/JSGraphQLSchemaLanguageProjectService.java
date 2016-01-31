/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema.ide.project;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.ProjectViewPane;
import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.ide.configuration.JSGraphQLConfigurationProvider;
import com.intellij.lang.jsgraphql.ide.project.JSGraphQLLanguageServiceListener;
import com.intellij.lang.jsgraphql.languageservice.JSGraphQLNodeLanguageServiceClient;
import com.intellij.lang.jsgraphql.languageservice.api.SchemaWithVersionResponse;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.schema.JSGraphQLSchemaFileType;
import com.intellij.lang.jsgraphql.schema.JSGraphQLSchemaLanguage;
import com.intellij.lang.jsgraphql.schema.ide.type.JSGraphQLNamedType;
import com.intellij.lang.jsgraphql.schema.ide.type.JSGraphQLPropertyType;
import com.intellij.lang.jsgraphql.schema.ide.type.JSGraphQLSchemaFileElements;
import com.intellij.lang.jsgraphql.schema.psi.JSGraphQLSchemaFile;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Provides Schema information about PSI elements, such as the type name of a property in a field, and the schema declarations a GraphQL PSI element is based on.
 */
public class JSGraphQLSchemaLanguageProjectService implements FileEditorManagerListener, JSGraphQLLanguageServiceListener, Disposable {

    public static final Key<Boolean> IS_GRAPHQL_SCHEMA_VIRTUAL_FILE = Key.create("JSGraphQL.schema.file");
    public static final Key<Boolean> IS_GRAPHQL_SCHEMA_FILE_LISTENER_ADDED = Key.create("JSGraphQL.schema.file.listener.added");
    public static Set<String> SCALAR_TYPES = Sets.newHashSet("String", "String!", "Boolean", "Boolean!", "Int", "Int!", "Float", "Float!", "ID", "ID!");

    private final static Logger log = Logger.getInstance(JSGraphQLSchemaLanguageProjectService.class);

    private Project project;
    private JSGraphQLSchemaFileElements schemaFileElements;

    private final Object reloadLock = new Object();

    public JSGraphQLSchemaLanguageProjectService(@NotNull final Project project) {
        this.project = project;

        final MessageBusConnection connection = project.getMessageBus().connect(this);
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this);
        connection.subscribe(JSGraphQLLanguageServiceListener.TOPIC, this);
    }


    // ---- Service methods  ----

    public static JSGraphQLSchemaLanguageProjectService getService(@NotNull Project project) {
        return ServiceManager.getService(project, JSGraphQLSchemaLanguageProjectService.class);
    }

    /**
     * @return the GraphQL schema associated with the project
     */
    public JSGraphQLSchemaFile getSchemaFile() {
        return getOrCreateSchemaFileElements().getFile();
    }

    public String getSchemaUrl() {
        return getOrCreateSchemaFileElements().getSchemaUrl();
    }

    public int getSchemaVersion() {
        return getOrCreateSchemaFileElements().getSchemaVersion();
    }

    /**
     * Gets the declaring type name of a property psi element, if the field that the property is part of is known by the GraphQL schema
     * currently associated with the project
     * @param propertyPsiElement the property element to get the type name for. This element can be from a GraphQL PSI file or from the GraphQL Schema PSI file itself
     * @return the type name as defined by the schema, or <code>null</code> if the element has no associated schema type
     */
    @Nullable
    public String getTypeName(JSGraphQLNamedPropertyPsiElement propertyPsiElement) {
        if(propertyPsiElement.getContainingFile() instanceof JSGraphQLFile) {
            // resolve the schema reference that has the type info
            final PsiReference reference = propertyPsiElement.getReference();
            if(reference != null) {
                final PsiElement psiReference = reference.getElement();
                if (psiReference instanceof JSGraphQLNamedPropertyPsiElement) {
                    propertyPsiElement = (JSGraphQLNamedPropertyPsiElement) psiReference;
                } else {
                    return null;
                }
            }
        }
        final JSGraphQLNamedType namedType = getOrCreateSchemaFileElements().getNamedType(propertyPsiElement);
        return namedType != null ? namedType.nameElement.getName() : null;
    }


    /**
     * Gets the named type that has the specified type name
     * @param typeName the name of the type to get
     * @return the corresponding named type, or <code>null</code> if the type is unknown
     */
    @Nullable
    public JSGraphQLNamedType getNamedType(@NotNull  String typeName) {
        return getOrCreateSchemaFileElements().getNamedType(typeName);
    }

    /**
     * Gets the GraphQL Schema PSI element that the specified element references as it declaration
     * @param element the GraphQL PSI element to get the declaring GraphQL schema PSI element for
     * @return the GraphQL Schema PSI element that declares the specified element, or <code>null</code> if no matching declaration is found
     */
    @Nullable
    public PsiElement getReference(PsiElement element) {
        JSGraphQLSchemaFileElements schemaFileElements = getOrCreateSchemaFileElements();
        if(element instanceof JSGraphQLNamedPsiElement) {
            JSGraphQLNamedPsiElement namedElement = (JSGraphQLNamedPsiElement)element;
            if(namedElement instanceof JSGraphQLNamedTypePsiElement) {
                // element references a JSGraphQLNamedType in the GraphQL schema file
                for (PsiElement definition : schemaFileElements.getFile().getChildren()) {
                    final JSGraphQLNamedPsiElement definitionName = PsiTreeUtil.findChildOfType(definition, JSGraphQLNamedPsiElement.class);
                    if(definitionName != null) {
                        if(Objects.equals(namedElement.getName(), definitionName.getName())) {
                            return definitionName;
                        }
                    }
                }
            } else if(element instanceof JSGraphQLNamedPropertyPsiElement) {
                // get the name of the field, e.g. foo > bar > bas, stopping at fragments that give away the type,
                // or ultimately when we're at a top level definition
                final JSGraphQLNamedPropertyPsiElement schemaPropertyReferenceElement = resolveSchemaPropertyReferenceElement((JSGraphQLNamedPropertyPsiElement) element, schemaFileElements);
                if(schemaPropertyReferenceElement != null) {
                    return schemaPropertyReferenceElement;
                }
            }
        }

        // fallback is the schema file to make sure references are updated on schema changes
        if(element.getContainingFile() instanceof JSGraphQLFile) {
            return schemaFileElements.getFile();
        }

        // null for non-resolvable elements in the schema file itself
        return null;
    }


    // ---- FileEditorManagerListener ----

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        if(file.getFileType() == JSGraphQLSchemaFileType.INSTANCE) {
            // mark the schema editor as viewer only
            final FileEditor fileEditor = source.getSelectedEditor(file);
            if (fileEditor instanceof TextEditor) {
                final Editor editor = ((TextEditor) fileEditor).getEditor();
                if (editor instanceof EditorEx) {
                    ((EditorEx) editor).setViewer(true);
                }
            }
        }
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {}

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {}

    @Override
    public void dispose() {}


    // ---- JSGraphQLLanguageServiceListener ----

    @Override
    public void onProcessHandlerTextAvailable(String text) {
        if(text != null && text.startsWith("Loaded schema from")) {
            // console.log from js-graphql-language-service languageService.js which signals a schema was loaded
            synchronized (reloadLock) {
                if(project.isDisposed()) {
                    return;
                }
                UIUtil.invokeLaterIfNeeded(() -> {
                    if(schemaFileElements != null) {
                        // reload
                        reloadSchemaFileElements(schemaFileElements);
                    } else {
                        // first load
                        getOrCreateSchemaFileElements();
                    }
                });
            }
        }
    }


    // ---- implementation ----

    @Nullable
    private JSGraphQLNamedPropertyPsiElement resolveSchemaPropertyReferenceElement(@NotNull JSGraphQLNamedPropertyPsiElement propertyPsiElement, JSGraphQLSchemaFileElements schemaFileElements) {
        final JSGraphQLSchemaPropertyPath propertyPath = getPropertyPath(propertyPsiElement, schemaFileElements);
        if(propertyPath != null) {
            JSGraphQLNamedType currentType = propertyPath.declaringType;
            JSGraphQLPropertyType currentPropertyType = null;
            for (String property : propertyPath.properties) {
                if(currentType != null) {
                    currentPropertyType = currentType.properties.get(property);
                    if (currentPropertyType == null) {
                        // unknown property in the schema
                        return null;
                    }
                    if (currentPropertyType.propertyValueTypeElement == null) {
                        return null;
                    }
                    currentType = schemaFileElements.getNamedType(currentPropertyType.propertyValueTypeElement.getName());
                }
            }
            if(currentPropertyType != null) {
                if(log.isDebugEnabled() && !Objects.equals(currentPropertyType.propertyElement.getName(), propertyPsiElement.getName())) {
                    // wrong property resolved
                    log.debug("Wrong property resolved", propertyPsiElement, currentPropertyType.propertyElement);
                }
                return currentPropertyType.propertyElement;
            }
        }
        return null;
    }

    private static class JSGraphQLSchemaPropertyPath {
        final List<String> properties = Lists.newArrayList();
        final JSGraphQLNamedType declaringType;
        public JSGraphQLSchemaPropertyPath(JSGraphQLNamedType declaringType) {
            this.declaringType = declaringType;
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private JSGraphQLSchemaPropertyPath getPropertyPath(@NotNull JSGraphQLNamedPropertyPsiElement propertyPsiElement, JSGraphQLSchemaFileElements schemaFileElements) {

        if(propertyPsiElement.getContainingFile() instanceof JSGraphQLSchemaFile) {
            // no need to resolve property paths for psi elements inside schema file
            // this is only needed in regular GraphQL files
            return null;
        }

        String declaringTypeName = null;
        PsiElement declaringTypeElement = null;

        // get the type from a fragment definition or inline fragment
        final PsiElement declaringFragmentElement = PsiTreeUtil.getParentOfType(propertyPsiElement, JSGraphQLFragmentDefinitionPsiElement.class, JSGraphQLInlineFragmentPsiElement.class);
        if(declaringFragmentElement != null) {
            final JSGraphQLNamedTypePsiElement[] fragmentTypes = PsiTreeUtil.getChildrenOfType(declaringFragmentElement, JSGraphQLNamedTypePsiElement.class);
            if(fragmentTypes != null) {
                for (JSGraphQLNamedTypePsiElement fragmentType : fragmentTypes) {
                    if (fragmentType.isAtom()) {
                        // we're looking for the schema type and not the name of the fragment definition
                        declaringTypeName = fragmentType.getName();
                        declaringTypeElement = declaringFragmentElement;
                        break;
                    }
                }
            }
        }

        // get the type from a top-level operation definition, e.g. query, mutation, subscription
        if(declaringTypeElement == null) {
            final PsiFile file = propertyPsiElement.getContainingFile();
            PsiElement parent = propertyPsiElement.getParent();
            while(parent != null) {
                if(parent.getParent() != file) {
                    parent = parent.getParent();
                } else {
                    // we're at a top level declaration, so we need to use a type alias such as 'Query', 'Mutation', 'Subscription' to determine the type
                    final ASTNode node = parent.getNode();
                    if(node.getElementType() instanceof JSGraphQLElementType) {
                        final JSGraphQLElementType elementType = (JSGraphQLElementType) node.getElementType();
                        declaringTypeName = elementType.getKind();
                        declaringTypeElement = parent;
                    }
                    break;
                }
            }
        }

        // finally, if we found the type name, return the property path that the psi element belongs to
        if(declaringTypeName != null) {
            final JSGraphQLNamedType namedType = schemaFileElements.getNamedType(declaringTypeName);
            if(namedType != null) {
                final JSGraphQLSchemaPropertyPath ret = new JSGraphQLSchemaPropertyPath(namedType);
                final Set<JSGraphQLNamedPropertyPsiElement> addedPropertyNameElements = Sets.newHashSet();
                PsiElement parent = propertyPsiElement.getParent();
                while(parent != null && parent != declaringTypeElement) {
                    if(parent instanceof JSGraphQLFieldPsiElement) {
                        final JSGraphQLFieldPsiElement fieldPsiElement = (JSGraphQLFieldPsiElement) parent;
                        final JSGraphQLNamedPropertyPsiElement nameElement = fieldPsiElement.getNameElement();
                        if(nameElement != null && addedPropertyNameElements.add(nameElement)) {
                            ret.properties.add(nameElement.getName());
                        }
                    }
                    parent = parent.getParent();
                }
                // we want the properties in top-down order, starting at the declaring type
                Collections.reverse(ret.properties);
                if(addedPropertyNameElements.add(propertyPsiElement)) {
                    ret.properties.add(propertyPsiElement.getName());
                }
                return ret;
            }
        }

        return null;
    }

    private JSGraphQLSchemaFileElements getOrCreateSchemaFileElements() {
        if(schemaFileElements == null) {
            final SchemaWithVersionResponse schemaWithVersion = JSGraphQLNodeLanguageServiceClient.getSchemaWithVersion(project);
            final PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
            final String schemaFileName = getSchemaFileName();
            final String schemaText = schemaWithVersion != null ? schemaWithVersion.getSchema() : "";
            final JSGraphQLSchemaFile file = (JSGraphQLSchemaFile) psiFileFactory.createFileFromText(
                    schemaFileName,
                    JSGraphQLSchemaLanguage.INSTANCE,
                    schemaText
            );
            file.getVirtualFile().putUserData(IS_GRAPHQL_SCHEMA_VIRTUAL_FILE, true);
            schemaFileElements = new JSGraphQLSchemaFileElements(schemaWithVersion, file);
        }
        return schemaFileElements;
    }

    @SuppressWarnings("CodeBlock2Expr")
    private void reloadSchemaFileElements(JSGraphQLSchemaFileElements schemaFileElements) {
        final SchemaWithVersionResponse schemaWithVersion = JSGraphQLNodeLanguageServiceClient.getSchemaWithVersion(project);
        if(schemaWithVersion != null) {
            // update the psi schema file
            UIUtil.invokeLaterIfNeeded(() -> {
                ApplicationManager.getApplication().runWriteAction(() -> {
                    if(project.isDisposed()) {
                        return;
                    }
                    final Document document = PsiDocumentManager.getInstance(project).getDocument(schemaFileElements.getFile());
                    if(document != null) {

                        // only change the PSI by re-parsing when the schema url or text is different
                        if(isNewSchemaUrl(schemaFileElements, schemaWithVersion) || !Objects.equals(document.getText(), schemaWithVersion.getSchema())) {

                            // reload the schema via a listener once the schema file psi has been updated
                            schemaFileElements.onPendingReloadSchema(schemaWithVersion);
                            if (!Boolean.TRUE.equals(project.getUserData(IS_GRAPHQL_SCHEMA_FILE_LISTENER_ADDED))) {
                                PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {
                                    @Override
                                    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
                                        if (event.getParent() == schemaFileElements.getFile()) {
                                            schemaFileElements.reloadSchema();
                                            schemaFileElements.getFile().setName(getSchemaFileName());
                                            project.getMessageBus().syncPublisher(JSGraphQLSchemaLanguageServiceListener.TOPIC).onSchemaReloaded();
                                            final ProjectView projectView = ProjectView.getInstance(project);
                                            if (projectView != null && projectView.getCurrentProjectViewPane() instanceof ProjectViewPane) {
                                                projectView.refresh();
                                            }
                                        }
                                    }
                                }, project);
                                project.putUserData(IS_GRAPHQL_SCHEMA_FILE_LISTENER_ADDED, true);
                            }

                            // finally, update the document to cause the re-parse and schema file psi update
                            document.setText(schemaWithVersion.getSchema());

                        }

                    }
                });
            });
        }
    }

    private String getSchemaFileName() {
        final VirtualFile baseDir = JSGraphQLConfigurationProvider.getService(project).getConfigurationBaseDir();
        if(baseDir != null) {
            final Module moduleByName = ModuleManager.getInstance(project).findModuleByName(baseDir.getName());
            if(moduleByName != null) {
                return getSchemaFileNameFromModule(moduleByName);
            }
            for (Module module : ModuleManager.getInstance(project).getModules()) {
                for (VirtualFile contentRoot : ModuleRootManager.getInstance(module).getContentRoots()) {
                    if(baseDir.equals(contentRoot)) {
                        return getSchemaFileNameFromModule(module);
                    }
                }
            }
        }
        return project.getName() + "." + JSGraphQLSchemaFileType.INSTANCE.getDefaultExtension();
    }

    private String getSchemaFileNameFromModule(Module module) {
        return module.getName() + "." + JSGraphQLSchemaFileType.INSTANCE.getDefaultExtension();
    }

    private boolean isNewSchemaUrl(JSGraphQLSchemaFileElements schemaFileElements, SchemaWithVersionResponse schemaWithVersion) {
        return !Objects.equals(schemaFileElements.getSchemaUrl(), schemaWithVersion.getUrl());
    }

}
