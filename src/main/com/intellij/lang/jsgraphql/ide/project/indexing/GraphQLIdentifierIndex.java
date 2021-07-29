/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.indexing;

import com.google.common.collect.Maps;
import com.intellij.json.psi.*;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.ide.project.GraphQLInjectionSearchHelper;
import com.intellij.lang.jsgraphql.ide.references.GraphQLFindUsagesUtil;
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.*;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumDataDescriptor;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Indexes GraphQL identifiers in GraphQL files, GraphQL injections, and JSON GraphQL introspection query result files.
 */
public class GraphQLIdentifierIndex extends FileBasedIndexExtension<String, GraphQLIdentifierIndex.IdentifierKind> {

    public static final ID<String, IdentifierKind> NAME = ID.create("GraphQLIdentifierIndex");
    public static final int VERSION = 3;

    private final @Nullable GraphQLInjectionSearchHelper graphQLInjectionSearchHelper;

    private final Set<FileType> includedFileTypes;

    private final DataIndexer<String, IdentifierKind, FileContent> myDataIndexer;

    public enum IdentifierKind {

        IDENTIFIER_NAME,
        // FIELD_NAME,
        // FIELD_ALIAS_NAME,
        // FIELD_DEFINITION_NAME,
        // TYPE_NAME,
        // TYPE_DEFINITION_NAME,
        // FRAGMENT_SPREAD_NAME,
        // FRAGMENT_DEFINITION_NAME,
        // ARGUMENT_NAME,
        // ENUM_VALUE_NAME,
        // OBJECT_FIELD_NAME,
        // DIRECTIVE_NAME,
        // OPERATION_DEFINITION_NAME

    }

    public GraphQLIdentifierIndex() {
        myDataIndexer = inputData -> {

            final HashMap<String, IdentifierKind> identifiers = Maps.newHashMap();

            PsiRecursiveElementVisitor visitor = new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof GraphQLIdentifier) {
                        identifiers.put(element.getText(), IdentifierKind.IDENTIFIER_NAME);
                        return; // no need to visit deeper
                    } else if (element instanceof JsonElement) {
                        if (element instanceof JsonFile) {
                            if (!isIntrospectionJsonFile((JsonFile) element)) {
                                // no need to visit this JSON file as it's not an introspection file
                                return;
                            }
                        }
                        if (element instanceof JsonProperty) {
                            final JsonProperty jsonProperty = (JsonProperty) element;
                            // GraphQL identifiers in an introspection result are defined using "name" properties:
                            // https://graphql.github.io/graphql-spec/June2018/#sec-Schema-Introspection
                            if ("name".equals(jsonProperty.getName())) {
                                if (jsonProperty.getValue() instanceof JsonStringLiteral) {
                                    identifiers.put(((JsonStringLiteral) jsonProperty.getValue()).getValue(), IdentifierKind.IDENTIFIER_NAME);
                                }
                            }
                        }
                    } else if (element instanceof PsiLanguageInjectionHost && graphQLInjectionSearchHelper != null) {
                        if (graphQLInjectionSearchHelper.isGraphQLLanguageInjectionTarget(element)) {
                            final PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(element.getProject());
                            final String graphqlBuffer = StringUtils.strip(element.getText(), "` \t\n");
                            final PsiFile graphqlInjectedPsiFile = psiFileFactory.createFileFromText("", GraphQLFileType.INSTANCE, graphqlBuffer, 0, false, false);
                            graphqlInjectedPsiFile.accept(this);
                            return;
                        }
                    }
                    super.visitElement(element);
                }
            };

            inputData.getPsiFile().accept(visitor);

            return identifiers;
        };
        includedFileTypes = GraphQLFindUsagesUtil.getService().getIncludedFileTypes();
        graphQLInjectionSearchHelper = GraphQLInjectionSearchHelper.getInstance();
    }

    private boolean isIntrospectionJsonFile(JsonFile jsonFile) {
        for (PsiElement child : jsonFile.getChildren()) {
            if (child instanceof JsonObject) {
                JsonProperty dataProperty = ((JsonObject) child).findProperty("data");
                if (dataProperty != null) {
                    if (dataProperty.getValue() instanceof JsonObject) {
                        return ((JsonObject) dataProperty.getValue()).findProperty("__schema") != null;
                    }
                }
                final JsonProperty schemaProperty = ((JsonObject) child).findProperty("__schema");
                if (schemaProperty != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    @Override
    public ID<String, IdentifierKind> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, IdentifierKind, FileContent> getIndexer() {
        return myDataIndexer;
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return new EnumeratorStringDescriptor();
    }

    @NotNull
    @Override
    public DataExternalizer<IdentifierKind> getValueExternalizer() {
        return new EnumDataDescriptor<>(IdentifierKind.class);
    }

    @Override
    public int getVersion() {
        return GraphQLIndexUtil.INDEX_BASE_VERSION + VERSION;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> includedFileTypes.contains(file.getFileType());
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public @NotNull Collection<FileType> getFileTypesWithSizeLimitNotApplicable() {
        return GraphQLIndexUtil.FILE_TYPES_WITH_IGNORED_SIZE_LIMIT;
    }
}
