/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.JSGraphQLTokenTypes;
import com.intellij.lang.jsgraphql.ide.findUsages.JSGraphQLFindUsagesUtil;
import com.intellij.lang.jsgraphql.schema.ide.project.JSGraphQLSchemaLanguageProjectService;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UserDataHolderEx;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JSGraphQLNamedPsiElement extends JSGraphQLPsiElement implements PsiNameIdentifierOwner {

    private static final TokenSet NAME_TOKEN_SET = TokenSet.create(JSGraphQLTokenTypes.PROPERTY, JSGraphQLTokenTypes.DEF);
    private static final Object[] NO_VARIANTS = new Object[0];

    private static final Key<GlobalSearchScope> USE_SCOPE = Key.create("jsgraphql.usages.scope");

    public JSGraphQLNamedPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    private PsiReference reference;


    // --- Name ---

    @Override
    public String getName() {
        final PsiElement nameIdentifier = getNameIdentifier();
        return nameIdentifier != null ? nameIdentifier.getText() : getText();
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }



    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        final ASTNode[] children = this.getNode().getChildren(NAME_TOKEN_SET);
        return children.length > 0 ? children[0].getPsi() : this;
    }


    // ---- References and find usages ----


    @Override
    public boolean isEquivalentTo(PsiElement another) {
        if(this == another) {
            return true;
        }

        // the structure view relies on this method to find usages from the nodes
        if(another instanceof JSGraphQLNamedPsiElement && getText().equals(another.getText())) {
            // same token text
            // check if we're a reference to 'another', e.g. the type definition in the schema file
            if(getContainingFile() != another.getContainingFile()) {
                final PsiReference reference = getReference();
                if(reference != null) {
                    return reference.resolve() == another;
                }
            }
            if(this instanceof JSGraphQLNamedTypePsiElement && another instanceof JSGraphQLNamedTypePsiElement) {
                // GraphQL type names are typically global and equivalent if their text matches and they're
                // the same type, e.g. query types, however fragment types are local to the file they're declared in
                JSGraphQLNamedTypePsiElement self = (JSGraphQLNamedTypePsiElement) this;
                JSGraphQLNamedTypePsiElement anotherType = (JSGraphQLNamedTypePsiElement) another;
                final boolean sameTypeContext = self.getTypeContext() == anotherType.getTypeContext();
                if(sameTypeContext && self.getTypeContext() == JSGraphQLNamedTypeContext.Fragment) {
                    // fragments are local type, so the files need to be the same as well
                    return self.getContainingFile().equals(another.getContainingFile());
                }
                return sameTypeContext;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        // For find usages, we want GraphQL PSI elements to be considered project-wide.
        // This enables find usages in modules that live outside the project base dir
        // but we only want to find usages in relevant file types, e.g. GraphQL, JS and TypeScript
        final Project project = getProject();
        final GlobalSearchScope cached = project.getUserData(USE_SCOPE);
        return cached != null ? cached : ((UserDataHolderEx)project).putUserDataIfAbsent(USE_SCOPE, createFileTypeRestrictedUsageScope(project));
    }

    @NotNull
    private GlobalSearchScope createFileTypeRestrictedUsageScope(Project project) {
        FileType[] fileTypes = JSGraphQLFindUsagesUtil.INCLUDED_FILE_TYPES.toArray(new FileType[JSGraphQLFindUsagesUtil.INCLUDED_FILE_TYPES.size()]);
        return GlobalSearchScope.getScopeRestrictedByFileTypes(ProjectScope.getAllScope(project), fileTypes);
    }

    @Override
    public void subtreeChanged() {
        // make sure we clear the cached reference when a child changes, e.g. when editing the type or property name
        reference = null;
        super.subtreeChanged();
    }

    @Override
    public PsiReference getReference() {

        if(reference == null) {

            final PsiElement self = this;
            reference = new PsiReferenceBase<JSGraphQLPsiElement>(this, TextRange.from(0, self.getTextLength())) {

                private JSGraphQLSchemaLanguageProjectService service;
                private PsiElement cachedResolvedReference;
                private int cachedSchemaVersion = -1;

                @Nullable
                @Override
                public PsiElement resolve() {
                    if(!self.isValid()) {
                        return null;
                    }
                    if(service == null) {
                        service = JSGraphQLSchemaLanguageProjectService.getService(self.getProject());
                    }
                    final int currentSchemaVersion = service.getSchemaVersion();
                    if(currentSchemaVersion != cachedSchemaVersion) {
                        cachedSchemaVersion = currentSchemaVersion;
                        cachedResolvedReference = service.getReference(self);
                    }
                    return cachedResolvedReference;
                }

                @NotNull
                @Override
                public Object[] getVariants() {
                    // variants appears to filter the shown completions -- but should be okay for now inside a single property/type name
                    return NO_VARIANTS;
                }
            };
        }
        return reference;
    }

}
