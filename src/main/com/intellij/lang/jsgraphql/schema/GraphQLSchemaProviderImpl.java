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
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry;
import com.intellij.lang.jsgraphql.types.schema.idl.UnExecutableSchemaGenerator;
import com.intellij.lang.jsgraphql.types.schema.validation.InvalidSchemaException;
import com.intellij.lang.jsgraphql.types.schema.validation.SchemaValidationError;
import com.intellij.lang.jsgraphql.types.schema.validation.SchemaValidator;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GraphQLSchemaProviderImpl implements GraphQLSchemaProvider, Disposable {

    private static final Logger LOG = Logger.getInstance(GraphQLSchemaProviderImpl.class);

    public static final GraphQLSchema EMPTY_SCHEMA = GraphQLSchema.newSchema()
        .query(GraphQLObjectType.newObject().name("Query").build()).withValidation(false).build();

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
    public GraphQLValidatedSchema getValidatedSchema(@NotNull PsiElement psiElement) {
        String containingFileName = GraphQLPsiUtil.getFileName(psiElement.getContainingFile());

        return fileNameToValidatedSchema.computeIfAbsent(containingFileName, fileName -> {
            final GraphQLValidatedTypeDefinitionRegistry registryWithErrors = fileNameToValidatedRegistry.computeIfAbsent(
                containingFileName, fileName1 -> myRegistryProvider.getValidatedRegistry(psiElement));

            try {
                GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(registryWithErrors.getRegistry());
                Collection<SchemaValidationError> validationErrors = new SchemaValidator().validateSchema(schema);
                List<GraphQLException> errors = validationErrors.isEmpty()
                    ? Collections.emptyList() : Collections.singletonList(new InvalidSchemaException(validationErrors));
                return new GraphQLValidatedSchema(schema, errors, registryWithErrors);
            } catch (ProcessCanceledException e) {
                throw e;
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Schema build error: ", e);
                }
                return new GraphQLValidatedSchema(
                    EMPTY_SCHEMA,
                    Lists.newArrayList(e instanceof GraphQLException ? ((GraphQLException) e) : new GraphQLException(e)),
                    registryWithErrors
                );
            }
        });
    }

    @NotNull
    @Override
    public GraphQLSchema getTolerantSchema(@NotNull PsiElement psiElement) {
        String fileName = GraphQLPsiUtil.getFileName(psiElement.getContainingFile());
        return fileNameToTolerantSchema.computeIfAbsent(fileName, f -> {
            try {
                return UnExecutableSchemaGenerator.makeUnExecutableSchema(getTolerantRegistry(psiElement, fileName));
            } catch (ProcessCanceledException e) {
                throw e;
            } catch (Exception e) {
                LOG.warn("Schema build error: ", e);
                return EMPTY_SCHEMA;
            }
        });
    }

    @NotNull
    @Override
    public TypeDefinitionRegistry getTolerantRegistry(@NotNull PsiElement psiElement) {
        String fileName = GraphQLPsiUtil.getFileName(psiElement.getContainingFile());
        return getTolerantRegistry(psiElement, fileName);
    }

    @NotNull
    private TypeDefinitionRegistry getTolerantRegistry(@NotNull PsiElement psiElement, @NotNull String fileName) {
        return fileNameToTolerantRegistry.computeIfAbsent(fileName, f -> myRegistryProvider.getTolerantRegistry(psiElement).getRegistry());
    }

    @Override
    public void dispose() {
    }
}
