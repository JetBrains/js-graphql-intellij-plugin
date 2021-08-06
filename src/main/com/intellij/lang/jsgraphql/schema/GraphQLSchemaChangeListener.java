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
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointFile;
import com.intellij.lang.jsgraphql.ide.project.GraphQLInjectionSearchHelper;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLOperationDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLTemplateDefinition;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiTreeChangeEventImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks PSI changes that can affect declared GraphQL schemas
 */
public class GraphQLSchemaChangeListener implements Disposable {

    public final static Topic<GraphQLSchemaEventListener> TOPIC = new Topic<>(
        "GraphQL Schema Change Events",
        GraphQLSchemaEventListener.class,
        Topic.BroadcastDirection.TO_PARENT
    );

    public static GraphQLSchemaChangeListener getService(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLSchemaChangeListener.class);
    }

    private final Project myProject;
    private final PsiTreeChangeAdapter myPsiTreeChangeListener;
    private final PsiManager myPsiManager;

    private final AtomicInteger schemaVersion = new AtomicInteger(0);

    public GraphQLSchemaChangeListener(Project project) {
        myProject = project;
        myPsiManager = PsiManager.getInstance(myProject);
        myPsiTreeChangeListener = new PsiTreeChangeAdapter() {

            private void checkForSchemaChange(PsiTreeChangeEvent event) {
                if (myProject.isDisposed()) {
                    myPsiManager.removePsiTreeChangeListener(myPsiTreeChangeListener);
                    return;
                }
                if (event.getFile() instanceof GraphQLFile) {
                    if (affectsGraphQLSchema(event)) {
                        signalSchemaChanged();
                    }
                }
                if (event.getFile() instanceof JSGraphQLEndpointFile) {
                    // always consider the schema changed when editing an endpoint file
                    signalSchemaChanged();
                }
                if (event.getParent() instanceof PsiLanguageInjectionHost) {
                    GraphQLInjectionSearchHelper graphQLInjectionSearchHelper = GraphQLInjectionSearchHelper.getInstance();
                    if (graphQLInjectionSearchHelper != null && graphQLInjectionSearchHelper.isGraphQLLanguageInjectionTarget(event.getParent())) {
                        // change in injection target
                        signalSchemaChanged();
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
                        signalSchemaChanged();
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
        };
        myPsiManager.addPsiTreeChangeListener(myPsiTreeChangeListener, this);

        // also consider the schema changed when the underlying schema configuration files change
        final MessageBusConnection connection = myProject.getMessageBus().connect(this);
        connection.subscribe(GraphQLConfigManager.TOPIC, this::signalSchemaChanged);
    }

    private void signalSchemaChanged() {
        final int nextVersion = this.schemaVersion.incrementAndGet();
        myProject.getMessageBus().syncPublisher(GraphQLSchemaChangeListener.TOPIC).onGraphQLSchemaChanged(nextVersion);
    }

    /**
     * Evaluates whether the change event can affect the associated GraphQL schema
     *
     * @param event the event that occurred
     * @return true if the change can affect the declared schema
     */
    private boolean affectsGraphQLSchema(PsiTreeChangeEvent event) {
        if (PsiTreeChangeEvent.PROP_FILE_NAME.equals(event.getPropertyName()) || PsiTreeChangeEvent.PROP_DIRECTORY_NAME.equals(event.getPropertyName())) {
            // renamed and moves are likely to affect schema blobs etc.
            return true;
        }
        final List<PsiElement> elements = Lists.newArrayList(event.getParent(), event.getChild(), event.getNewChild(), event.getOldChild());
        for (PsiElement element : elements) {
            if (element == null) {
                continue;
            }
            if (PsiTreeUtil.findFirstParent(element, parent -> parent instanceof GraphQLOperationDefinition || parent instanceof GraphQLFragmentDefinition || parent instanceof GraphQLTemplateDefinition) != null) {
                // edits inside query, mutation, subscription, fragment etc. don't affect the schema
                return false;
            }
        }
        // fallback to assume the schema can be affected by the edit
        return true;
    }

    @Override
    public void dispose() {
    }
}
