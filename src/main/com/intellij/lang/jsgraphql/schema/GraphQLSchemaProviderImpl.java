/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.lang.jsgraphql.psi.GraphQLPsiUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import graphql.Directives;
import graphql.GraphQLException;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class GraphQLSchemaProviderImpl implements GraphQLSchemaProvider, Disposable {

    private static final Logger LOG = Logger.getInstance(GraphQLSchemaProviderImpl.class);

    public static final GraphQLSchema EMPTY_SCHEMA = GraphQLSchema.newSchema().query(GraphQLObjectType.newObject().name("Query").build()).build();

    private final Map<String, GraphQLValidatedTypeDefinitionRegistry> fileNameToValidatedRegistry = Maps.newConcurrentMap();
    private final Map<String, GraphQLValidatedSchema> fileNameToValidatedSchema = Maps.newConcurrentMap();

    private final Map<String, TypeDefinitionRegistry> fileNameToTolerantRegistry = Maps.newConcurrentMap();
    private final Map<String, GraphQLSchema> fileNameToTolerantSchema = Maps.newConcurrentMap();
    private final GraphQLRegistryProvider myRegistryProvider;

    public GraphQLSchemaProviderImpl(@NotNull Project project) {
        myRegistryProvider = GraphQLRegistryProvider.getInstance(project);

        project.getMessageBus().connect(this).subscribe(GraphQLSchemaChangeListener.TOPIC, schemaVersion -> {
            // clear the cache on each PSI change
            fileNameToValidatedRegistry.clear();
            fileNameToValidatedSchema.clear();

            fileNameToTolerantRegistry.clear();
            fileNameToTolerantSchema.clear();
        });
    }

    @NotNull
    @Override
    public TypeDefinitionRegistry getTolerantRegistry(@NotNull PsiElement psiElement) {
        return fileNameToTolerantRegistry.computeIfAbsent(GraphQLPsiUtil.getFileName(psiElement.getContainingFile()),
            fileName -> myRegistryProvider.getTolerantRegistry(psiElement).getRegistry());
    }

    @NotNull
    @Override
    public GraphQLValidatedSchema getValidatedSchema(@NotNull PsiElement psiElement) {
        String containingFileName = GraphQLPsiUtil.getFileName(psiElement.getContainingFile());

        return fileNameToValidatedSchema.computeIfAbsent(containingFileName, fileName -> {
            final GraphQLValidatedTypeDefinitionRegistry registryWithErrors = fileNameToValidatedRegistry.computeIfAbsent(containingFileName,
                fileName1 -> myRegistryProvider.getValidatedRegistry(psiElement));

            try {
                final GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(registryWithErrors.getRegistry());
                for (GraphQLDirective directive : schema.getDirectives()) {
                    if (Directives.DeferDirective.getName().equals(directive.getName()) && directive != Directives.DeferDirective) {
                        // more than one @defer (one was declared in addition to the built-in one from graphql-java)
                        removeBuiltInDirective(schema, Directives.DeferDirective);
                        break;
                    }
                }
                return new GraphQLValidatedSchema(schema, Collections.emptyList(), registryWithErrors);
            } catch (GraphQLException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Schema build error:", e);
                }
                return new GraphQLValidatedSchema(EMPTY_SCHEMA, Lists.newArrayList(e), registryWithErrors);
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Schema build error:", e);
                }
                return new GraphQLValidatedSchema(EMPTY_SCHEMA, Lists.newArrayList(new GraphQLException(e)), registryWithErrors);
            }
        });
    }

    @NotNull
    @Override
    public GraphQLSchema getTolerantSchema(@NotNull PsiElement psiElement) {
        return fileNameToTolerantSchema.computeIfAbsent(GraphQLPsiUtil.getFileName(psiElement.getContainingFile()), fileName -> {
            try {
                return UnExecutableSchemaGenerator.makeUnExecutableSchema(getTolerantRegistry(psiElement));
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Schema build error:", e);
                }
                return EMPTY_SCHEMA;
            }
        });
    }

    // ---- https://github.com/graphql-java/graphql-java/issues/1399 ----
    // Remove graphql-java built-in @defer since it declares valid locations that can conflict with frameworks such as Relay Modern.
    // Unfortunately no API to do this without reflection right now.
    private static Field directives = getDirectivesField();

    private static Field getDirectivesField() {
        try {
            Field directives = GraphQLSchema.class.getDeclaredField("directives");
            directives.setAccessible(true);
            return directives;
        } catch (NoSuchFieldException ignored) {
        }
        return null;
    }

    private static void removeBuiltInDirective(GraphQLSchema graphQLSchema, GraphQLDirective directiveToRemove) {
        if (directives != null) {
            try {
                final Object knownDirectives = directives.get(graphQLSchema);
                if (knownDirectives instanceof Collection) {
                    ((Collection) knownDirectives).remove(directiveToRemove);
                }
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    // ----

    @Override
    public void dispose() {
    }
}
