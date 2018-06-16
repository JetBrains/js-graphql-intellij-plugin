/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.v1.JSGraphQLKeywords;
import com.intellij.lang.jsgraphql.v1.JSGraphQLTokenTypes;
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.lang.jsgraphql.v1.ide.project.JSGraphQLPsiSearchHelper;
import com.intellij.lang.jsgraphql.v1.schema.psi.JSGraphQLSchemaFile;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;
import java.util.Optional;

public class JSGraphQLNamedTypePsiElement extends JSGraphQLNamedPsiElement implements JSGraphQLErrorContextAware {

    public JSGraphQLNamedTypePsiElement(@NotNull ASTNode node) {
        super(node);
    }

    /**
     * Atoms represent a schema type, e.g. Node, User etc.
     */
    public boolean isAtom() {
        final JSGraphQLElementType elementType = (JSGraphQLElementType)getNode().getElementType();
        return JSGraphQLElementType.ATOM_KIND.equals(elementType.getKind());
    }

    /**
     * Definitions represent named queries, fragments, e.g. MyQuery, MyFragment etc.
     */
    public boolean isDefinition() {
        final JSGraphQLElementType elementType = (JSGraphQLElementType)getNode().getElementType();
        return JSGraphQLElementType.DEFINITION_KIND.equals(elementType.getKind());
    }

    @Override
    public PsiReference getReference() {
        if(isDefinition()) {
            // definitions can have reference in the same file they're declared in, e.g. '...Foo' references 'fragment Foo'
            if(getContainingFile() instanceof JSGraphQLFile) {

                if(getTypeContext() != JSGraphQLNamedTypeContext.Fragment) {
                    // non-fragments are self-references
                    return new PsiReferenceBase.Immediate<>(this, TextRange.from(0, this.getTextLength()), this);
                }
                // check if this element points to a named fragment within the same psi file
                for (PsiElement definition : getContainingFile().getChildren()) {
                    if(definition instanceof JSGraphQLFragmentDefinitionPsiElement) {
                        final JSGraphQLNamedTypePsiElement definitionType = PsiTreeUtil.findChildOfType(definition, JSGraphQLNamedTypePsiElement.class);
                        if(definitionType != null && definitionType != this && Objects.equals(this.getName(), definitionType.getName())) {
                            return new PsiReferenceBase.Immediate<>(this, TextRange.from(0, definitionType.getTextLength()), definitionType);
                        }
                    }
                }

                // also search for the fragment definition in other files
                final JSGraphQLNamedTypePsiElement definitionType = JSGraphQLPsiSearchHelper.getService(getProject()).resolveFragmentReference(this); // fragmentDefinitionName.get();
                if(definitionType != null) {
                    if(this.equals(definitionType)) {
                        // this element is the fragment definition name element
                        return null;
                    }
                    return new PsiReferenceBase.Immediate<>(this, TextRange.from(0, definitionType.getTextLength()), definitionType);
                }

            }
            // null for named type in the schema, e.g. 'Node' which means that ctrl+click is find usages
            return null;
        }
        // resolve based on the schema
        return super.getReference();
    }

    public JSGraphQLNamedTypeContext getTypeContext() {
        final PsiElement prevSibling = PsiTreeUtil.prevVisibleLeaf(this);
        if(prevSibling != null && prevSibling.getNode().getElementType() == JSGraphQLTokenTypes.KEYWORD) {
            final String keyword = Optional.ofNullable(prevSibling.getText()).orElse("");
            switch (keyword) {
                case JSGraphQLKeywords.FRAGMENT:
                case JSGraphQLKeywords.FRAGMENT_DOTS:
                    return JSGraphQLNamedTypeContext.Fragment;
                case JSGraphQLKeywords.QUERY:
                    return JSGraphQLNamedTypeContext.Query;
                case JSGraphQLKeywords.SUBSCRIPTION:
                    return JSGraphQLNamedTypeContext.Subscription;
                case JSGraphQLKeywords.MUTATION:
                    return JSGraphQLNamedTypeContext.Mutation;
            }
        }
        return JSGraphQLNamedTypeContext.Unknown;
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {

            @Override
            public String getPresentableText() {
                return getName();
            }

            @Nullable
            @Override
            public String getLocationString() {
                return null;
            }

            @Override
            public Icon getIcon(boolean open) {
                return getElementIcon(0);
            }
        };
    }

    @Override
    public Icon getElementIcon(final int flags) {
        if(isAtom() || getContainingFile() instanceof JSGraphQLSchemaFile) {
            return JSGraphQLIcons.Schema.Type;
        }
        if(isDefinition()) {
            return JSGraphQLIcons.Schema.Fragment;
        }
        return super.getElementIcon(flags);
    }

    @Override
    public boolean isErrorInContext(String errorMessage) {
        if(errorMessage.startsWith("Unknown fragment")) {
            // GraphQL doesn't known this fragment, but if it's defined elsewhere, check for a valid reference
            final PsiReference reference = this.getReference();
            if(reference != null && reference.resolve() != null) {
                return false;
            }
        } else if(errorMessage.startsWith("Fragment") && errorMessage.endsWith("is never used.")) {
            // fragments are not considered unused inside an injected element (e.g. Relay graphql, Apollo gql)
            if(getContainingFile().getContext() instanceof PsiLanguageInjectionHost) {
                return false;
            }
        }
        return true;
    }
}
