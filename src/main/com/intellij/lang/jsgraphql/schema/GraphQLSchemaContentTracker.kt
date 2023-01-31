/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.google.common.collect.Lists;
import com.intellij.json.psi.JsonFile;
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectionSearchHelper;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLOperationDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLTemplateDefinition;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiTreeChangeEventImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Tracks PSI changes that can affect declared GraphQL schemas
 */
public class GraphQLSchemaChangeTracker implements Disposable {

    private static final Logger LOG = Logger.getInstance(GraphQLSchemaChangeTracker.class);

    @Topic.ProjectLevel
    public final static Topic<GraphQLSchemaChangeListener> TOPIC = new Topic<>(
        "GraphQL Schema Change Events",
        GraphQLSchemaChangeListener.class,
        Topic.BroadcastDirection.NONE
    );

    public static GraphQLSchemaChangeTracker getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLSchemaChangeTracker.class);
    }

    private final Project myProject;
    private final SimpleModificationTracker myModificationTracker = new SimpleModificationTracker();

    public GraphQLSchemaChangeTracker(Project project) {
        myProject = project;

        PsiManager.getInstance(myProject).addPsiTreeChangeListener(new GraphQLSchemaPsiChangeListener(), this);

        // also consider the schema changed when the underlying schema configuration files change
        MessageBusConnection connection = myProject.getMessageBus().connect(this);
        connection.subscribe(GraphQLConfigManager.TOPIC, this::schemaChanged);
    }

    public void schemaChanged() {
        LOG.debug("GraphQL schema cache invalidated", LOG.isTraceEnabled() ? new Throwable() : null);

        ApplicationManager.getApplication().invokeLater(() -> {
            myModificationTracker.incModificationCount();
            myProject.getMessageBus().syncPublisher(GraphQLSchemaChangeTracker.TOPIC).onSchemaChanged();
        }, ModalityState.NON_MODAL, myProject.getDisposed());
    }

    @NotNull
    public ModificationTracker getSchemaModificationTracker() {
        return myModificationTracker;
    }

    @Override
    public void dispose() {
    }

    /**
     * always consider the schema changed when editing an endpoint file
     * change in injection target
     * ignore the generic event which fires for all other cases above
     * if it's not the generic case, children have been replaced, e.g. using the commenter
     */
    private class GraphQLSchemaPsiChangeListener extends PsiTreeChangeAdapter {
        private void checkForSchemaChange(@NotNull PsiTreeChangeEvent event) {
            if (myProject.isDisposed()) {
                return;
            }
            if (event.getFile() instanceof GraphQLFile) {
                if (affectsGraphQLSchema(event)) {
                    schemaChanged();
                }
            }
            if (event.getParent() instanceof PsiLanguageInjectionHost) {
                GraphQLInjectionSearchHelper graphQLInjectionSearchHelper = GraphQLInjectionSearchHelper.getInstance();
                if (graphQLInjectionSearchHelper != null && graphQLInjectionSearchHelper.isGraphQLLanguageInjectionTarget(event.getParent())) {
                    // change in injection target
                    schemaChanged();
                }
            }
            if (event.getFile() instanceof JsonFile) {
                boolean introspectionJsonUpdated = false;
                if (event.getFile().getUserData(GraphQLSchemaKeys.GRAPHQL_INTROSPECTION_JSON_TO_SDL) != null) {
                    introspectionJsonUpdated = true;
                } else {
                    final VirtualFile virtualFile = event.getFile().getVirtualFile();
                    if (virtualFile != null && Boolean.TRUE.equals(virtualFile.getUserData(GraphQLSchemaKeys.IS_GRAPHQL_INTROSPECTION_JSON))) {
                        introspectionJsonUpdated = true;
                    }
                }
                if (introspectionJsonUpdated) {
                    schemaChanged();
                }
            }
        }

        @Override
        public void propertyChanged(@NotNull PsiTreeChangeEvent event) {
            checkForSchemaChange(event);
        }

        @Override
        public void childAdded(@NotNull PsiTreeChangeEvent event) {
            checkForSchemaChange(event);
        }

        @Override
        public void childRemoved(@NotNull PsiTreeChangeEvent event) {
            checkForSchemaChange(event);
        }

        @Override
        public void childMoved(@NotNull PsiTreeChangeEvent event) {
            checkForSchemaChange(event);
        }

        @Override
        public void childReplaced(@NotNull PsiTreeChangeEvent event) {
            checkForSchemaChange(event);
        }

        @Override
        public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
            if (event instanceof PsiTreeChangeEventImpl) {
                if (!((PsiTreeChangeEventImpl) event).isGenericChange()) {
                    // ignore the generic event which fires for all other cases above
                    // if it's not the generic case, children have been replaced, e.g. using the commenter
                    checkForSchemaChange(event);
                }
            }
        }

        /**
         * Evaluates whether the change event can affect the associated GraphQL schema
         *
         * @param event the event that occurred
         * @return true if the change can affect the declared schema
         */
        private boolean affectsGraphQLSchema(@NotNull PsiTreeChangeEvent event) {
            if (PsiTreeChangeEvent.PROP_FILE_NAME.equals(event.getPropertyName()) ||
                PsiTreeChangeEvent.PROP_DIRECTORY_NAME.equals(event.getPropertyName())) {
                // renamed and moves are likely to affect schema blobs etc.
                return true;
            }
            final List<PsiElement> elements = Lists.newArrayList(
                event.getParent(), event.getChild(), event.getNewChild(), event.getOldChild());
            for (PsiElement element : elements) {
                if (element == null) {
                    continue;
                }
                if (PsiTreeUtil.findFirstParent(element,
                    parent -> parent instanceof GraphQLOperationDefinition ||
                        parent instanceof GraphQLFragmentDefinition ||
                        parent instanceof GraphQLTemplateDefinition) != null) {
                    // edits inside query, mutation, subscription, fragment etc. don't affect the schema
                    return false;
                }
            }
            // fallback to assume the schema can be affected by the edit
            return true;
        }
    }
}
