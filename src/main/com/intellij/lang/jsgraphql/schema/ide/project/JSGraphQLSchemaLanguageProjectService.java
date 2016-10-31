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
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.ide.projectView.impl.ProjectViewPane;
import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.endpoint.ide.project.JSGraphQLEndpointNamedTypeRegistry;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointArgumentsDefinition;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointInputValueDefinition;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointNamedType;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointNamedTypeDefinition;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointProperty;
import com.intellij.lang.jsgraphql.ide.configuration.JSGraphQLConfigurationProvider;
import com.intellij.lang.jsgraphql.ide.project.JSGraphQLLanguageServiceListener;
import com.intellij.lang.jsgraphql.languageservice.JSGraphQLNodeLanguageServiceClient;
import com.intellij.lang.jsgraphql.languageservice.api.SchemaWithVersionResponse;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.schema.JSGraphQLSchemaFileType;
import com.intellij.lang.jsgraphql.schema.JSGraphQLSchemaLanguage;
import com.intellij.lang.jsgraphql.schema.ide.type.JSGraphQLNamedType;
import com.intellij.lang.jsgraphql.schema.ide.type.JSGraphQLNamedTypeRegistry;
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
import com.intellij.ui.LoadingNode;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides Schema information about PSI elements, such as the type name of a property in a field, and the schema declarations a GraphQL PSI element is based on.
 */
public class JSGraphQLSchemaLanguageProjectService implements FileEditorManagerListener, JSGraphQLLanguageServiceListener, Disposable {

    public static final Key<Boolean> IS_GRAPHQL_SCHEMA_VIRTUAL_FILE = Key.create("JSGraphQL.schema.file");
    public static final Key<Boolean> IS_GRAPHQL_SCHEMA_FILE_LISTENER_ADDED = Key.create("JSGraphQL.schema.file.listener.added");
    public static Set<String> SCALAR_TYPES = Sets.newHashSet("String", "String!", "Boolean", "Boolean!", "Int", "Int!", "Float", "Float!", "ID", "ID!");

    private static final Key<TreeModelListener> SCHEMA_TREE_MODEL_LISTENER = Key.create("JSGraphQL.schema.tree.model.listener");
    private static final Key<Boolean> SCHEMA_TREE_SELECT_ON_INSERT = Key.create("JSGraphQL.schema.tree.model.select");

    private final static Logger log = Logger.getInstance(JSGraphQLSchemaLanguageProjectService.class);

    private Project project;
    private JSGraphQLSchemaFileElements schemaFileElements;

    private final Object reloadLock = new Object();
    private final JSGraphQLEndpointNamedTypeRegistry endpointNamedTypeRegistry;

    public JSGraphQLSchemaLanguageProjectService(@NotNull final Project project) {
        this.project = project;
        this.endpointNamedTypeRegistry = JSGraphQLEndpointNamedTypeRegistry.getService(project);

        final MessageBusConnection connection = project.getMessageBus().connect(this);
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this);
        connection.subscribe(JSGraphQLLanguageServiceListener.TOPIC, this);

        // mark restored schema files as viewers
        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        for (VirtualFile virtualFile : fileEditorManager.getOpenFiles()) {
            markSchemaFileAsViewer(fileEditorManager, virtualFile);
        }
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
        return namedType != null ? namedType.getName() : null;
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
     * Gets whether the project uses the Endpoint Language to define the schema
     * @return true if the project is configured with a .graphqle entry file
     */
    public boolean hasEndpointEntryFile() {
        return endpointNamedTypeRegistry.hasEndpointEntryFile();
    }

    /**
     * Gets the GraphQL Schema PSI element that the specified element references as it declaration
     * @param element the GraphQL PSI element to get the declaring GraphQL schema PSI element for
     * @return the GraphQL Schema PSI element that declares the specified element, or <code>null</code> if no matching declaration is found
     */
    @Nullable
    public PsiElement getReference(PsiElement element) {

        final boolean hasEndpointFile = endpointNamedTypeRegistry.hasEndpointEntryFile();

        final JSGraphQLSchemaFileElements schemaFileElements;
        final PsiFile containingFile = element.getContainingFile();
        if(containingFile instanceof JSGraphQLSchemaFile && !isProjectSchemaFile(containingFile.getVirtualFile())) {
            // schema file that's not the in-memory project schema
            final SchemaWithVersionResponse schemaWithVersion = new SchemaWithVersionResponse();
            schemaFileElements = new JSGraphQLSchemaFileElements(schemaWithVersion, (JSGraphQLSchemaFile) containingFile);
        } else {
            // use project schema
            schemaFileElements = getOrCreateSchemaFileElements();
        }
        if(element instanceof JSGraphQLNamedPsiElement) {
            JSGraphQLNamedPsiElement namedElement = (JSGraphQLNamedPsiElement)element;
            if(namedElement instanceof JSGraphQLNamedTypePsiElement) {
                if(hasEndpointFile) {
                    final String nameToFind = namedElement.getName();
                    if(nameToFind != null) {
                        final JSGraphQLNamedType namedType = endpointNamedTypeRegistry.getNamedType(nameToFind);
                        if(namedType != null && namedType.definitionElement instanceof JSGraphQLEndpointNamedTypeDefinition) {
                            final JSGraphQLEndpointNamedTypeDefinition namedTypeDefinition = (JSGraphQLEndpointNamedTypeDefinition) namedType.definitionElement;
                            return namedTypeDefinition.getNamedTypeDef();
                        } else {
                            return null;
                        }
                    }
                }
                // not using endpoint, so element references a JSGraphQLNamedType in the GraphQL schema file
                for (PsiElement definition : schemaFileElements.getFile().getChildren()) {
                    final JSGraphQLNamedPsiElement definitionName = PsiTreeUtil.findChildOfType(definition, JSGraphQLNamedPsiElement.class);
                    if(definitionName != null) {
                        if(Objects.equals(namedElement.getName(), definitionName.getName())) {
                            if(definitionName == element) {
                                // potential self reference, but make sure it's a valid one
                                if(definitionName instanceof JSGraphQLNamedTypePsiElement) {
                                    if(((JSGraphQLNamedTypePsiElement) definitionName).getTypeContext() == JSGraphQLNamedTypeContext.Unknown) {
                                        // ignore self-references inside schema { query: Query } etc.
                                        continue;
                                    }
                                }
                            }
                            return definitionName;
                        }
                    }
                }
            } else if(element instanceof JSGraphQLNamedPropertyPsiElement) {
                // get the name of the field, e.g. foo > bar > bas, stopping at fragments that give away the type,
                // or ultimately when we're at a top level definition
                final JSGraphQLNamedTypeRegistry typeRegistry;
                if(hasEndpointFile) {
                    typeRegistry = endpointNamedTypeRegistry;
                } else {
                    typeRegistry = schemaFileElements;
                }
                final PsiElement schemaPropertyReferenceElement = resolveSchemaPropertyReferenceElement((JSGraphQLNamedPropertyPsiElement) element, typeRegistry);
                if(schemaPropertyReferenceElement != null) {
                    return schemaPropertyReferenceElement;
                }
            } else if(element instanceof JSGraphQLAttributePsiElement) {
	            return resolveSchemaAttributeReferenceElement((JSGraphQLAttributePsiElement)element, hasEndpointFile ? endpointNamedTypeRegistry: schemaFileElements);
            }
        }

        if(hasEndpointFile) {
            return null;
        }

        // fallback is the schema file to make sure references are updated on schema changes
        if(containingFile instanceof JSGraphQLFile) {
            return schemaFileElements.getFile();
        }

        // null for non-resolvable elements in the schema file itself
        return null;
    }

	public static boolean isProjectSchemaFile(VirtualFile file) {
        return file != null && Boolean.TRUE.equals(file.getUserData(JSGraphQLSchemaLanguageProjectService.IS_GRAPHQL_SCHEMA_VIRTUAL_FILE));
    }

    // ---- FileEditorManagerListener ----

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        markSchemaFileAsViewer(source, file);
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {}

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        if(event.getNewFile() != null) {
            selectProjectSchemaInTree(event);
            markSchemaFileAsViewer(event.getManager(), event.getNewFile());
        }
    }

    private void selectProjectSchemaInTree(@NotNull FileEditorManagerEvent event) {
        if(!isProjectSchemaFile(event.getNewFile())) {
            return;
        }
        try {
            JSGraphQLSchemaFileNode node = project.getUserData(JSGraphQLSchemaFileNode.GRAPHQL_SCHEMA_TREE_NODE);
            if(node != null) {
                final ProjectView projectView = ProjectView.getInstance(project);
                if(projectView.isAutoscrollFromSource(projectView.getCurrentViewId())) {
                    final AbstractProjectViewPane projectViewPane = projectView.getCurrentProjectViewPane();
                    if(projectViewPane != null) {
                        if(projectViewPane.getSelectedDescriptor() != node) {
                            final JTree tree = projectViewPane.getTree();
                            final TreeNode root = (TreeNode) tree.getModel().getRoot();
                            if(root != null) {
                                for(int c = 0; c < root.getChildCount(); c++) {
                                    final TreeNode schemaDirectory = root.getChildAt(c);
                                    if(schemaDirectory instanceof DefaultMutableTreeNode) {
                                        final Object userObject = ((DefaultMutableTreeNode) schemaDirectory).getUserObject();
                                        if(userObject instanceof JSGraphQLSchemaDirectoryNode) {
                                            if(schemaDirectory.getChildCount() == 1) {
                                                final TreeNode schemaNode = schemaDirectory.getChildAt(0);
                                                final TreePath treePath = new TreePath(
                                                        new Object[]{root, schemaDirectory, schemaNode}
                                                );
                                                TreeModelListener treeModelListener = project.getUserData(SCHEMA_TREE_MODEL_LISTENER);
                                                if(treeModelListener == null) {
                                                    treeModelListener = new TreeModelListener() {
                                                        @Override
                                                        public void treeNodesInserted(TreeModelEvent treeModelEvent) {
                                                            // idea uses "Loading..." nodes, so listen for inserts
                                                            if(Boolean.TRUE.equals(project.getUserData(SCHEMA_TREE_SELECT_ON_INSERT))) {
                                                                if(treeModelEvent.getChildren() != null) {
                                                                    Object child = treeModelEvent.getChildren()[0];
                                                                    if(child instanceof DefaultMutableTreeNode) {
                                                                        final DefaultMutableTreeNode insertedNode = (DefaultMutableTreeNode) child;
                                                                        if(insertedNode.getUserObject() instanceof JSGraphQLSchemaFileNode) {
                                                                            final TreePath insertTreePath = new TreePath(insertedNode.getPath());
                                                                            tree.setSelectionPath(insertTreePath);
                                                                            tree.scrollPathToVisible(insertTreePath);
                                                                            project.putUserData(SCHEMA_TREE_SELECT_ON_INSERT, false);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        @Override
                                                        public void treeNodesChanged(TreeModelEvent treeModelEvent) {}
                                                        @Override
                                                        public void treeNodesRemoved(TreeModelEvent treeModelEvent) {}
                                                        @Override
                                                        public void treeStructureChanged(TreeModelEvent treeModelEvent) {}
                                                    };
                                                    tree.getModel().addTreeModelListener(treeModelListener);
                                                    project.putUserData(SCHEMA_TREE_MODEL_LISTENER, treeModelListener);
                                                }
                                                tree.expandPath(treePath);
                                                tree.setSelectionPath(treePath);
                                                tree.scrollPathToVisible(treePath);
                                                project.putUserData(SCHEMA_TREE_SELECT_ON_INSERT, schemaNode instanceof LoadingNode);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Unable to select GraphQL schema node", e);
        }
    }

    @Override
    public void dispose() {}

    private void markSchemaFileAsViewer(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        if(!isProjectSchemaFile(file)) {
            // only the project schema should be read only
            return;
        }
        if(file.getFileType() == JSGraphQLSchemaFileType.INSTANCE || JSGraphQLSchemaFileType.isGraphQLScratchFile(project, file)) {
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
    private PsiElement resolveSchemaPropertyReferenceElement(@NotNull JSGraphQLNamedPropertyPsiElement propertyPsiElement, JSGraphQLNamedTypeRegistry namedTypeRegistry) {
        final JSGraphQLSchemaPropertyPath propertyPath = getPropertyPath(propertyPsiElement, namedTypeRegistry);
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
                    if (currentPropertyType.propertyValueTypeName == null) {
                        return null;
                    }
                    currentType = namedTypeRegistry.getNamedType(currentPropertyType.propertyValueTypeName);
                }
            }
            if(currentPropertyType != null) {
                if(log.isDebugEnabled() && !Objects.equals(currentPropertyType.getPropertyName(), propertyPsiElement.getName())) {
                    // wrong property resolved
                    log.debug("Wrong property resolved", propertyPsiElement, currentPropertyType.propertyElement);
                }
                return currentPropertyType.propertyElement;
            }
        }
        return null;
    }

	private PsiElement resolveSchemaAttributeReferenceElement(JSGraphQLAttributePsiElement element, JSGraphQLNamedTypeRegistry namedTypeRegistry) {

		if(element.getContainingFile() instanceof JSGraphQLSchemaFile) {
			// attributes are self references in schema files
			return null;
		}

		// get the field argument that the attribute belongs to
		final JSGraphQLArgumentPsiElement argumentPsiElement = PsiTreeUtil.getParentOfType(element, JSGraphQLArgumentPsiElement.class);
		final JSGraphQLAttributePsiElement argumentAttribute;
		if(argumentPsiElement != null) {
			// attribute is wrapped in argument (list/object attribute values)
			argumentAttribute = argumentPsiElement.getAttribute();
		} else {
			// attribute is the arguments itself (literal attribute value)
			argumentAttribute = element;
		}

		// and collect any fields that we have to traverse from the argument input type
		final List<JSGraphQLObjectFieldPsiElement> parentObjectFields = Lists.newArrayList();
		JSGraphQLObjectFieldPsiElement parent = PsiTreeUtil.getParentOfType(element, JSGraphQLObjectFieldPsiElement.class);
		while (parent != null) {
			parentObjectFields.add(0, parent);
			parent = PsiTreeUtil.getParentOfType(parent, JSGraphQLObjectFieldPsiElement.class);
		}

		final List<String> propertyNames = parentObjectFields.stream().map(f -> f.getAttribute().getName()).collect(Collectors.toList());
		if(element.getParent() instanceof JSGraphQLObjectValuePsiElement) {
			// the attribute is part of an "{ }" object value, so add the name of the attribute to the property names that need to be resolved
			propertyNames.add(element.getName());
		}

		// locate the argument declaration in the schema using the field that the argument belongs to
		final JSGraphQLFieldPsiElement field = PsiTreeUtil.getParentOfType(argumentAttribute, JSGraphQLFieldPsiElement.class);
		if (field != null && field.getNameElement() != null) {
			final PsiReference reference = field.getNameElement().getReference();
			if (reference != null) {
				final PsiElement schemaFieldDeclaration = reference.resolve();
				if(schemaFieldDeclaration != null) {
					// locate a matching argument in the field, and get the type name after the colon
					final String argumentName = argumentAttribute.getText();

					PsiNamedElement argumentType = null;
					if(schemaFieldDeclaration instanceof JSGraphQLEndpointProperty) {
						final JSGraphQLEndpointArgumentsDefinition argumentsDefinition = PsiTreeUtil.getNextSiblingOfType(schemaFieldDeclaration, JSGraphQLEndpointArgumentsDefinition.class);
						if(argumentsDefinition != null) {
							final List<JSGraphQLEndpointInputValueDefinition> arguments = PsiTreeUtil.getChildrenOfTypeAsList(argumentsDefinition.getInputValueDefinitions(), JSGraphQLEndpointInputValueDefinition.class);
							for (JSGraphQLEndpointInputValueDefinition argument : arguments) {
								if(argument.getInputValueDefinitionIdentifier().getText().equals(argumentName)) {
									if(propertyNames.isEmpty()) {
										return argument.getInputValueDefinitionIdentifier();
									}
									argumentType = PsiTreeUtil.findChildOfType(argument, JSGraphQLEndpointNamedType.class);
									break;
								}
							}
						}
					} else {
						PsiElement nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(schemaFieldDeclaration);
						boolean foundArgument = false;
						while (nextVisibleLeaf != null) {
							if (nextVisibleLeaf.getParent() instanceof PsiNamedElement) {
								final PsiNamedElement psiNamedElement = (PsiNamedElement) nextVisibleLeaf.getParent();
								if (foundArgument) {
									// the identifier following the argument and colon is the argument type
									argumentType = psiNamedElement;
									break;
								} else if (Optional.ofNullable(psiNamedElement.getName()).orElse("").equals(argumentName)) {
									if (propertyNames.isEmpty()) {
										// the attribute is a top level attribute that references an argument
										return psiNamedElement;
									}
									foundArgument = true;
								}
							}
							nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(nextVisibleLeaf);
						}
					}
					if(argumentType != null) {

						JSGraphQLNamedType currentType = namedTypeRegistry.getNamedType(argumentType.getName());
						JSGraphQLPropertyType currentPropertyType = null;

						for (String property : propertyNames) {
							if(currentType != null) {
								currentPropertyType = currentType.properties.get(property);
								if (currentPropertyType == null) {
									// unknown property in the schema
									return null;
								}
								if (currentPropertyType.propertyValueTypeName == null) {
									return null;
								}
								currentType = namedTypeRegistry.getNamedType(currentPropertyType.propertyValueTypeName);
							}
						}

						if(currentPropertyType != null) {
							return currentPropertyType.propertyElement;
						}


					}
				}

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
    private JSGraphQLSchemaPropertyPath getPropertyPath(@NotNull JSGraphQLNamedPropertyPsiElement propertyPsiElement, JSGraphQLNamedTypeRegistry namedTypeRegistry) {

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
            final JSGraphQLNamedType namedType = namedTypeRegistry.getNamedType(declaringTypeName);
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
