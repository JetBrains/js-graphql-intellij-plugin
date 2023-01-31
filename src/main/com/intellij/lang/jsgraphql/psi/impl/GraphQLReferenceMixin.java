/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLReferenceService;
import com.intellij.lang.jsgraphql.ide.search.GraphQLScopeProvider;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier;
import com.intellij.lang.jsgraphql.psi.GraphQLReferenceElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GraphQLReferenceMixin extends GraphQLNamedElementImpl implements GraphQLReferenceElement {

    public GraphQLReferenceMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    public GraphQLIdentifier getNameIdentifier() {
        if (this instanceof GraphQLIdentifier) {
            return (GraphQLIdentifier) this;
        }
        return null;
    }

    @Override
    public @Nullable String getReferenceName() {
        // TODO: [vepanimas] a temporary solution, identifiers and referencing elements shouldn't implement PsiNamedElement
        return getName();
    }

    @Override
    public PsiReference getReference() {
        return GraphQLReferenceService.getService(getProject()).resolveReference(this);
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        SearchScope useScope = super.getUseScope();
        final GraphQLFile psiFile = ObjectUtils.tryCast(getContainingFile(), GraphQLFile.class);
        if (psiFile != null && InjectedLanguageManager.getInstance(getProject()).isInjectedFragment(psiFile)) {
            // this PSI element is part of injected GraphQL, so we have to expand the use scope which defaults to the current file only
            useScope = useScope.union(GraphQLScopeProvider.getInstance(getProject()).getResolveScope(this));
        }
        return useScope;
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
        boolean equivalentTo = super.isEquivalentTo(another);
        if (!equivalentTo && another instanceof GraphQLReferenceElement && getParent() instanceof GraphQLFieldDefinition) {
            // field may be an implementation from an interface

            // TODO: [vepanimas] resolve() call is not expected here.
            //  Also a field definition shouldn't have a reference to an interface field,
            //  because it means that this field should become unresolved, when you remove an interface field,
            //  but it doesn't work that way, it's an independent declaration on its own.
            PsiReference reference = getReference();
            if (reference != null) {
                equivalentTo = reference.resolve() == another;
            }
        }
        return equivalentTo;
    }
}
