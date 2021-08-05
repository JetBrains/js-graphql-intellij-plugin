/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.jsgraphql.GraphQLConstants;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.lang.jsgraphql.schema.GraphQLPsiToLanguage;
import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;

public class GraphQLFile extends PsiFileBase {
    public GraphQLFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, GraphQLLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return GraphQLFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return GraphQLConstants.GraphQL;
    }

    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }

    @NotNull
    public Collection<GraphQLDefinition> getDefinitions() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, GraphQLDefinition.class);
    }

    @NotNull
    public Collection<GraphQLDefinition> getTypeDefinitions() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, GraphQLTypeSystemDefinition.class);
    }

    public Document getDocument() {
        return CachedValuesManager.getCachedValue(this, () -> {
            Document document = GraphQLPsiToLanguage.INSTANCE.createDocument(this);
            return CachedValueProvider.Result.createSingleDependency(document, this);
        });
    }
}
