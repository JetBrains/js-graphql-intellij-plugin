/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.indexing;

import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectionUtils;
import com.intellij.lang.jsgraphql.ide.search.GraphQLFileTypesProvider;
import com.intellij.lang.jsgraphql.psi.GraphQLDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.text.BlockSupport;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.BooleanDataDescriptor;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * Index for processing files that contain one or more GraphQL fragment definitions
 */
public final class GraphQLFragmentNameIndex extends FileBasedIndexExtension<String, Boolean> {

  public static final ID<String, Boolean> NAME = ID.create("GraphQLFragmentNameIndex");

  public static final String HAS_FRAGMENTS = "fragments";
  public static final int VERSION = 1;

  private static final String FRAGMENT_MARKER = "fragment ";

  private final DataIndexer<String, Boolean, FileContent> myDataIndexer = inputData -> {
    if (!StringUtil.contains(inputData.getContentAsText(), FRAGMENT_MARKER)) {
      return Collections.emptyMap();
    }

    PsiFile psiFile = inputData.getPsiFile();
    if (psiFile instanceof XmlFile && BlockSupport.isTooDeep(psiFile)) {
      return Collections.emptyMap();
    }

    Ref<Boolean> hasFragments = Ref.create(false);
    PsiRecursiveElementVisitor identifierVisitor = new PsiRecursiveElementVisitor() {
      @Override
      public void visitElement(@NotNull PsiElement element) {
        if (hasFragments.get()) {
          // done
          return;
        }
        if (element instanceof GraphQLDefinition) {
          if (element instanceof GraphQLFragmentDefinition) {
            hasFragments.set(true);
          }
          return; // no need to visit deeper than definitions since fragments are top level
        }
        else if (element instanceof PsiLanguageInjectionHost host) {
          if (GraphQLInjectionUtils.visitInjectionAsRawText(host, this)) {
            return;
          }
        }

        super.visitElement(element);
      }
    };

    psiFile.accept(identifierVisitor);

    if (hasFragments.get()) {
      return Collections.singletonMap(HAS_FRAGMENTS, true);
    }
    else {
      return Collections.emptyMap();
    }
  };

  @Override
  public @NotNull ID<String, Boolean> getName() {
    return NAME;
  }

  @Override
  public @NotNull DataIndexer<String, Boolean, FileContent> getIndexer() {
    return myDataIndexer;
  }

  @Override
  public @NotNull KeyDescriptor<String> getKeyDescriptor() {
    return new EnumeratorStringDescriptor();
  }

  @Override
  public @NotNull DataExternalizer<Boolean> getValueExternalizer() {
    return BooleanDataDescriptor.INSTANCE;
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
}
