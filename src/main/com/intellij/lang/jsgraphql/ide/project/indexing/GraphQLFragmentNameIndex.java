/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.indexing;

import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.ide.project.GraphQLInjectionSearchHelper;
import com.intellij.lang.jsgraphql.ide.references.GraphQLFindUsagesUtil;
import com.intellij.lang.jsgraphql.psi.GraphQLDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import com.intellij.util.indexing.*;
import com.intellij.util.io.BooleanDataDescriptor;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

/**
 * Index for processing files that contain one or more GraphQL fragment definitions
 */
public class GraphQLFragmentNameIndex extends FileBasedIndexExtension<String, Boolean> {

    public static final ID<String, Boolean> NAME = ID.create("GraphQLFragmentNameIndex");

    public static final String HAS_FRAGMENTS = "fragments";


    private final GraphQLInjectionSearchHelper graphQLInjectionSearchHelper;

    private final Set<FileType> includedFileTypes;

    private final DataIndexer<String, Boolean, FileContent> myDataIndexer;

    public GraphQLFragmentNameIndex() {
        myDataIndexer = inputData -> {

            final Ref<Boolean> hasFragments = Ref.create(false);

            final Ref<PsiRecursiveElementVisitor> identifierVisitor = Ref.create();
            identifierVisitor.set(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(PsiElement element) {
                    if (hasFragments.get()) {
                        // done
                        return;
                    }
                    if (element instanceof GraphQLDefinition) {
                        if (element instanceof GraphQLFragmentDefinition) {
                            hasFragments.set(true);
                        }
                        return; // no need to visit deeper than definitions since fragments are top level
                    } else if (element instanceof PsiLanguageInjectionHost && graphQLInjectionSearchHelper != null) {
                        if (graphQLInjectionSearchHelper.isJSOrKotlinGraphQLLanguageInjectionTarget(element)) {
                            final PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(element.getProject());
                            final String graphqlBuffer = StringUtils.strip(element.getText(), "` \t\n");
                            final PsiFile graphqlInjectedPsiFile = psiFileFactory.createFileFromText("", GraphQLFileType.INSTANCE, graphqlBuffer, 0, false, false);
                            graphqlInjectedPsiFile.accept(identifierVisitor.get());
                            return;
                        }
                    }
                    super.visitElement(element);
                }
            });

            inputData.getPsiFile().accept(identifierVisitor.get());

            if (hasFragments.get()) {
                return Collections.singletonMap(HAS_FRAGMENTS, true);
            } else {
                return Collections.emptyMap();
            }

        };
        includedFileTypes = GraphQLFindUsagesUtil.getService().getIncludedFileTypes();
        graphQLInjectionSearchHelper = ServiceManager.getService(GraphQLInjectionSearchHelper.class);
    }

    @NotNull
    @Override
    public ID<String, Boolean> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, Boolean, FileContent> getIndexer() {
        return myDataIndexer;
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return new EnumeratorStringDescriptor();
    }

    @NotNull
    @Override
    public DataExternalizer<Boolean> getValueExternalizer() {
        return BooleanDataDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 1;
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

}
