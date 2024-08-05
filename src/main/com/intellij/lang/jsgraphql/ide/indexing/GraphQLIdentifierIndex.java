/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.indexing;

import com.intellij.json.psi.*;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectedLanguage;
import com.intellij.lang.jsgraphql.ide.search.GraphQLFileTypesProvider;
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.*;
import com.intellij.psi.text.BlockSupport;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumDataDescriptor;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Indexes GraphQL identifiers in GraphQL files, GraphQL injections, and JSON GraphQL introspection query result files.
 */
public final class GraphQLIdentifierIndex extends FileBasedIndexExtension<String, GraphQLIdentifierIndex.IdentifierKind> {

  public static final ID<String, IdentifierKind> NAME = ID.create("GraphQLIdentifierIndex");
  public static final int VERSION = 3;

  public enum IdentifierKind {
    IDENTIFIER_NAME
  }

  private final DataIndexer<String, IdentifierKind, FileContent> myDataIndexer = inputData -> {
    PsiFile psiFile = inputData.getPsiFile();
    if (psiFile instanceof XmlFile && BlockSupport.isTooDeep(psiFile)) {
      return Collections.emptyMap();
    }

    final Map<String, IdentifierKind> identifiers = new HashMap<>();
    PsiRecursiveElementVisitor visitor = new PsiRecursiveElementVisitor() {
      @Override
      public void visitElement(@NotNull PsiElement element) {
        if (element instanceof GraphQLIdentifier) {
          identifiers.put(element.getText(), IdentifierKind.IDENTIFIER_NAME);
          return; // no need to visit deeper
        }
        else if (element instanceof JsonElement) {
          if (element instanceof JsonFile) {
            if (!isIntrospectionJsonFile((JsonFile)element)) {
              // no need to visit this JSON file as it's not an introspection file
              return;
            }
          }
          if (element instanceof JsonProperty jsonProperty) {
            // GraphQL identifiers in an introspection result are defined using "name" properties:
            // https://graphql.github.io/graphql-spec/June2018/#sec-Schema-Introspection
            if ("name".equals(jsonProperty.getName())) {
              if (jsonProperty.getValue() instanceof JsonStringLiteral) {
                identifiers.put(
                  ((JsonStringLiteral)jsonProperty.getValue()).getValue(),
                  IdentifierKind.IDENTIFIER_NAME
                );
              }
            }
          }
        }
        else if (element instanceof PsiLanguageInjectionHost) {
          GraphQLInjectedLanguage injectedLanguage = GraphQLInjectedLanguage.forElement(element);
          if (injectedLanguage != null && injectedLanguage.isLanguageInjectionTarget(element)) {
            final String injectedText = injectedLanguage.getInjectedTextForIndexing(element);
            if (injectedText != null) {
              final PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(element.getProject());
              final PsiFile graphqlInjectedPsiFile = psiFileFactory
                .createFileFromText("", GraphQLFileType.INSTANCE, injectedText, 0, false, false);
              graphqlInjectedPsiFile.accept(this);
              return;
            }
          }
        }
        super.visitElement(element);
      }
    };

    psiFile.accept(visitor);

    return identifiers;
  };

  private static boolean isIntrospectionJsonFile(@NotNull JsonFile jsonFile) {
    for (PsiElement child : jsonFile.getChildren()) {
      if (child instanceof JsonObject) {
        JsonProperty dataProperty = ((JsonObject)child).findProperty("data");
        if (dataProperty != null) {
          if (dataProperty.getValue() instanceof JsonObject) {
            return ((JsonObject)dataProperty.getValue()).findProperty("__schema") != null;
          }
        }
        final JsonProperty schemaProperty = ((JsonObject)child).findProperty("__schema");
        if (schemaProperty != null) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public @NotNull ID<String, IdentifierKind> getName() {
    return NAME;
  }

  @Override
  public @NotNull DataIndexer<String, IdentifierKind, FileContent> getIndexer() {
    return myDataIndexer;
  }

  @Override
  public @NotNull KeyDescriptor<String> getKeyDescriptor() {
    return new EnumeratorStringDescriptor();
  }

  @Override
  public @NotNull DataExternalizer<IdentifierKind> getValueExternalizer() {
    return new EnumDataDescriptor<>(IdentifierKind.class);
  }

  @Override
  public int getVersion() {
    return GraphQLIndexUtil.INDEX_BASE_VERSION + VERSION;
  }

  @Override
  public @NotNull FileBasedIndex.InputFilter getInputFilter() {
    return file -> GraphQLFileTypesProvider.getService().isAcceptedFile(file);
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
