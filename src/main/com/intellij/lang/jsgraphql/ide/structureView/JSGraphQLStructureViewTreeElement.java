/**
 * Copyright (c) 2015, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.structureView;

import com.google.common.collect.Lists;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.lang.jsgraphql.JSGraphQLKeywords;
import com.intellij.lang.jsgraphql.JSGraphQLTokenTypes;
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.schema.psi.JSGraphQLSchemaFile;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * A node in the GraphQL/GraphQL Schema structure tree view
 */
public class JSGraphQLStructureViewTreeElement extends PsiTreeElementBase<PsiElement> {


    final PsiElement childrenBase;
    final PsiElement element;

    public JSGraphQLStructureViewTreeElement(PsiElement childrenBase, PsiElement psiElement) {
        super(psiElement);
        this.element = psiElement;
        this.childrenBase = childrenBase;
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
        final List<StructureViewTreeElement> children = Lists.newArrayList();

        if(element instanceof JSGraphQLNamedPropertyPsiElement) {
            // add attributes between the '(' and ')' tokens
            PsiElement nextSibling = PsiTreeUtil.nextVisibleLeaf(element);
            addChildrenWithElementType(children, nextSibling, JSGraphQLTokenTypes.ATTRIBUTE);
        }
        if(element instanceof JSGraphQLPsiElement) {
            if(element.getNode().getElementType() instanceof JSGraphQLElementType) {
                final JSGraphQLElementType elementType = (JSGraphQLElementType) element.getNode().getElementType();
                if(JSGraphQLElementType.QUERY_KIND.equals(elementType.getKind())) {
                    // query - add query variables between the '(' and ')' tokens if it's a named query
                    final JSGraphQLNamedTypePsiElement queryTypeName = PsiTreeUtil.getChildOfType(element, JSGraphQLNamedTypePsiElement.class);
                    if(queryTypeName != null) {
                        PsiElement nextSibling = PsiTreeUtil.nextVisibleLeaf(queryTypeName);
                        addChildrenWithElementType(children, nextSibling, JSGraphQLTokenTypes.VARIABLE);
                    }
                }
            }
        }

        if(childrenBase.getContainingFile() instanceof JSGraphQLSchemaFile) {
            // schema file is simply list of type definitions with properties
            if(childrenBase instanceof JSGraphQLSchemaFile) {
                final JSGraphQLPsiElement[] definitions = PsiTreeUtil.getChildrenOfType(childrenBase, JSGraphQLPsiElement.class);
                if(definitions != null) {
                    for (JSGraphQLPsiElement definition : definitions) {
                        final JSGraphQLNamedTypePsiElement definitionType = PsiTreeUtil.findChildOfType(definition, JSGraphQLNamedTypePsiElement.class);
                        if(definitionType != null) {
                            children.add(new JSGraphQLStructureViewTreeElement(definition, definitionType));
                        }
                    }
                }
            } else {
                // last child of the type definition parent is the '{...}' psi element which contains the property declarations
                final PsiElement lastChild = childrenBase.getLastChild();
                if (lastChild instanceof JSGraphQLPsiElement) {
                    final JSGraphQLNamedPropertyPsiElement[] properties = PsiTreeUtil.getChildrenOfType(lastChild, JSGraphQLNamedPropertyPsiElement.class);
                    if (properties != null) {
                        for (JSGraphQLNamedPropertyPsiElement property : properties) {
                            children.add(new JSGraphQLStructureViewTreeElement(property, property));
                        }
                    }
                }
                final PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(element);
                if(prevVisibleLeaf != null && JSGraphQLKeywords.ENUM.equals(prevVisibleLeaf.getText())) {
                    // enum
                    final JSGraphQLPsiElement enumValuesElement = PsiTreeUtil.getNextSiblingOfType(element, JSGraphQLPsiElement.class);
                    if(enumValuesElement != null) {
                        final JSGraphQLNamedTypePsiElement[] enumValues = PsiTreeUtil.getChildrenOfType(enumValuesElement, JSGraphQLNamedTypePsiElement.class);
                        if (enumValues != null) {
                            for (JSGraphQLNamedTypePsiElement enumValue : enumValues) {
                                children.add(new JSGraphQLStructureViewTreeElement(enumValue, enumValue));
                            }
                        }
                    }
                }
            }
        } else {

            // query file is a nested collection of selection sets (unless it's an anonymous query in the form of a selection set)
            final JSGraphQLSelectionSetPsiElement selectionSet = PsiTreeUtil.getChildOfType(childrenBase, JSGraphQLSelectionSetPsiElement.class);
            PsiElement queryChildrenBase = childrenBase;
            if (selectionSet != null && !selectionSet.isAnonymousQuery()) {
                // the children in terms of the tree are found with the '{...}' selection set
                queryChildrenBase = selectionSet;
            }
            for (PsiElement psiElement : queryChildrenBase.getChildren()) {
                if (psiElement instanceof JSGraphQLPsiElement) {
                    if(psiElement instanceof JSGraphQLFieldPsiElement) {
                        final JSGraphQLNamedPropertyPsiElement nameElement = ((JSGraphQLFieldPsiElement) psiElement).getNameElement();
                        children.add(new JSGraphQLStructureViewTreeElement(psiElement, nameElement));
                    } else if(psiElement instanceof JSGraphQLFragmentDefinitionPsiElement || psiElement instanceof JSGraphQLInlineFragmentPsiElement) {
                        final PsiElement typeElement = PsiTreeUtil.getChildOfType(psiElement, JSGraphQLNamedTypePsiElement.class);
                        if(typeElement != null) {
                            children.add(new JSGraphQLStructureViewTreeElement(psiElement, typeElement));
                        }
                    } else {
                        children.add(new JSGraphQLStructureViewTreeElement(psiElement, psiElement));
                    }
                }
            }
        }

        return children;
    }

    private void addChildrenWithElementType(List<StructureViewTreeElement> children, PsiElement nextSibling, IElementType elementTypeToAdd) {
        if(nextSibling != null && nextSibling.getNode().getElementType() == JSGraphQLTokenTypes.LPAREN) {
            nextSibling = PsiTreeUtil.nextVisibleLeaf(nextSibling);
            while(nextSibling != null) {
                final IElementType elementType = nextSibling.getNode().getElementType();
                if(elementType == elementTypeToAdd) {
                    final boolean isVariableDollar = elementType == JSGraphQLTokenTypes.VARIABLE && nextSibling.getText().equals("$");
                    if(!isVariableDollar) {
                        children.add(new JSGraphQLStructureViewTreeElement(nextSibling, nextSibling));
                    }
                } else if(elementType == JSGraphQLTokenTypes.RPAREN || elementType == JSGraphQLTokenTypes.LBRACE) {
                    break;
                }
                nextSibling = PsiTreeUtil.nextVisibleLeaf(nextSibling);
            }
        }
    }

    @Override
    public Icon getIcon(boolean open) {

        // attributes and variables
        if(element instanceof LeafPsiElement) {
            final IElementType elementType = element.getNode().getElementType();
            if(elementType == JSGraphQLTokenTypes.ATTRIBUTE) {
                return JSGraphQLIcons.Schema.Attribute;
            } else if(elementType == JSGraphQLTokenTypes.VARIABLE) {
                return JSGraphQLIcons.UI.GraphQLVariables;
            }
        }

        // general purpose JSGraphQLPsiElement elements which don't provide their own icons:
        if(element.getContainingFile() instanceof JSGraphQLSchemaFile) {
            // schema icons
            if (!(element instanceof JSGraphQLNamedPropertyPsiElement)) {
                final PsiElement firstChild = childrenBase.getFirstChild();
                if (firstChild != null) {
                    if (JSGraphQLKeywords.ENUM.equals(firstChild.getText())) {
                        return JSGraphQLIcons.Schema.Enum;
                    } else if (JSGraphQLKeywords.INTERFACE.equals(firstChild.getText())) {
                        return JSGraphQLIcons.Schema.Interface;
                    }
                }
                // check if it's an enum type
                PsiElement definition = element instanceof PsiFile ? null : element.getParent();
                while(definition != null) {
                    if(definition.getParent() instanceof PsiFile) {
                        break;
                    } else {
                        definition = definition.getParent();
                    }
                }
                final PsiElement definitionKeyword = definition != null ? PsiTreeUtil.firstChild(definition) : null;
                if(definitionKeyword != null && JSGraphQLKeywords.ENUM.equals(definitionKeyword.getText())) {
                    return JSGraphQLIcons.Schema.Enum;
                }

                // otherwise considered a type in the schema ('type', 'union', 'input')
                return JSGraphQLIcons.Schema.Type;
            }
        } else {
            // query, mutation, subscription icons
            if(element instanceof JSGraphQLSelectionSetPsiElement) {
                // anonymous query
                return JSGraphQLIcons.Schema.Query;
            }
            // resolve icon based on the the first keyword
            final PsiElement firstChild = element.getFirstChild();
            if(firstChild != null && firstChild.getNode().getElementType() == JSGraphQLTokenTypes.KEYWORD) {
                final String keyword = firstChild.getText();
                if(JSGraphQLKeywords.QUERY.equals(keyword)) {
                    return JSGraphQLIcons.Schema.Query;
                }
                if(JSGraphQLKeywords.MUTATION.equals(keyword)) {
                    return JSGraphQLIcons.Schema.Mutation;
                }
                if(JSGraphQLKeywords.SUBSCRIPTION.equals(keyword)) {
                    return JSGraphQLIcons.Schema.Subscription;
                }
            }
        }

        // other more advanced psi elements such as fields and fragments provide their own icons
        return super.getIcon(open);
    }

    @Nullable
    @Override
    public String getPresentableText() {

        // types and properties
        if(element instanceof PsiNamedElement) {
            String text = ((PsiNamedElement) element).getName();

            if(childrenBase instanceof JSGraphQLFieldPsiElement) {
                // this is a field, so append the qualifier if any, e.g. 'myQualifier: myField {...}'
                PsiElement prevSibling = PsiTreeUtil.prevVisibleLeaf(childrenBase);
                if(prevSibling != null && prevSibling.getNode().getElementType() == JSGraphQLTokenTypes.PUNCTUATION) {
                    final PsiElement couldBeQualifier = PsiTreeUtil.prevVisibleLeaf(prevSibling);
                    if(couldBeQualifier != null && couldBeQualifier.getNode().getElementType() == JSGraphQLTokenTypes.QUALIFIER) {
                        text = couldBeQualifier.getText() + ": " + text;
                    }
                }
            }
            return text;
        }

        // anonymous query
        if(element instanceof JSGraphQLSelectionSetPsiElement) {
            if(((JSGraphQLSelectionSetPsiElement) element).isAnonymousQuery()) {
                return "Query";
            }
        }

        // definitions (query, mutation, subscription)
        final JSGraphQLNamedTypePsiElement namedTypePsiElement = PsiTreeUtil.getChildOfType(element, JSGraphQLNamedTypePsiElement.class);
        if(namedTypePsiElement != null) {
            return namedTypePsiElement.getText();
        }

        // variables
        if(element instanceof LeafPsiElement) {
            final IElementType elementType = element.getNode().getElementType();
            if(elementType == JSGraphQLTokenTypes.VARIABLE) {
                return "$" + element.getText();
            }
        }

        // fallback is to return the first visible token from the text
        final String text = element.getText();
        return Optional.ofNullable(StringUtil.substringBefore(text, " ")).orElse(text);
    }
}
