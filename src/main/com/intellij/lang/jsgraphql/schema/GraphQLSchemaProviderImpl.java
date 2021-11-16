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
import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.idl.UnExecutableSchemaGenerator;
import com.intellij.lang.jsgraphql.types.schema.validation.InvalidSchemaException;
import com.intellij.lang.jsgraphql.types.schema.validation.SchemaValidationError;
import com.intellij.lang.jsgraphql.types.schema.validation.SchemaValidator;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.TimeoutUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GraphQLSchemaProviderImpl implements GraphQLSchemaProvider, Disposable {

    private static final Logger LOG = Logger.getInstance(GraphQLSchemaProviderImpl.class);

    public static final GraphQLSchema EMPTY_SCHEMA = GraphQLSchema.newSchema()
        .query(GraphQLObjectType.newObject().name("Query").build()).build();

    private final Map<String, GraphQLRegistryInfo> fileNameToRegistry = Maps.newConcurrentMap();
    private final Map<String, GraphQLSchemaInfo> fileNameToSchema = Maps.newConcurrentMap();
    private final GraphQLRegistryProvider myRegistryProvider;

    public GraphQLSchemaProviderImpl(@NotNull Project project) {
        myRegistryProvider = GraphQLRegistryProvider.getInstance(project);

        project.getMessageBus().connect(this).subscribe(GraphQLSchemaChangeTracker.TOPIC, () -> {
            // clear the cache on each PSI change
            fileNameToRegistry.clear();
            fileNameToSchema.clear();
        });
    }

    @NotNull
    @Override
    public GraphQLSchemaInfo getSchemaInfo(@NotNull PsiElement psiElement) {
        String containingFileName = GraphQLPsiUtil.getFileName(psiElement.getContainingFile());

        return fileNameToSchema.computeIfAbsent(containingFileName, fileName -> {
            final GraphQLRegistryInfo registryWithErrors = fileNameToRegistry.computeIfAbsent(
                containingFileName, f -> myRegistryProvider.getRegistryInfo(psiElement));

            try {
                long start = System.nanoTime();
                GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(registryWithErrors.getTypeDefinitionRegistry());
                Collection<SchemaValidationError> validationErrors = new SchemaValidator().validateSchema(schema);
                List<GraphQLException> errors = validationErrors.isEmpty()
                    ? Collections.emptyList() : Collections.singletonList(new InvalidSchemaException(validationErrors));

                if (LOG.isDebugEnabled()) {
                    long durationMillis = TimeoutUtil.getDurationMillis(start);
                    LOG.debug(String.format("Schema build completed in %d ms, requester: %s", durationMillis, containingFileName));
                }
                return new GraphQLSchemaInfo(schema, errors, registryWithErrors);
            } catch (ProcessCanceledException e) {
                throw e;
            } catch (Exception e) {
                LOG.error("Schema build error: ", e); // should never happen
                return new GraphQLSchemaInfo(
                    EMPTY_SCHEMA,
                    Lists.newArrayList(e instanceof GraphQLException ? ((GraphQLException) e) : new GraphQLException(e)),
                    registryWithErrors
                );
            }
        });
    }

    @NotNull
    @Override
    public GraphQLRegistryInfo getRegistryInfo(@NotNull PsiElement psiElement) {
        String fileName = GraphQLPsiUtil.getFileName(psiElement.getContainingFile());
        return fileNameToRegistry.computeIfAbsent(fileName, f -> myRegistryProvider.getRegistryInfo(psiElement));
    }

    @Override
    public void dispose() {
    }
}
