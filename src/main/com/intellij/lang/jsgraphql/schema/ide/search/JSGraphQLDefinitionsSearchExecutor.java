/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema.ide.search;

import com.google.common.collect.Lists;
import com.intellij.lang.jsgraphql.JSGraphQLTokenTypes;
import com.intellij.lang.jsgraphql.psi.JSGraphQLNamedTypePsiElement;
import com.intellij.lang.jsgraphql.psi.JSGraphQLPsiElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Provides a list of 'type' implementations for an 'interface' definition in a GraphQL Schema file.
 */
public class JSGraphQLDefinitionsSearchExecutor implements QueryExecutor<PsiElement, PsiElement> {

    private static final String IMPLEMENTS_KEYWORD = "implements";

    @Override
    public boolean execute(@NotNull PsiElement queryParameters, @NotNull Processor<PsiElement> consumer) {
        ApplicationManager.getApplication().runReadAction((Computable) () -> JSGraphQLDefinitionsSearchExecutor.doExecute(queryParameters, consumer));
        return true;
    }

    private static boolean doExecute(PsiElement sourceElement, final Processor<PsiElement> consumer) {
        if (sourceElement instanceof JSGraphQLNamedTypePsiElement) {
            final String typeName = ((JSGraphQLNamedTypePsiElement) sourceElement).getName();
            if(typeName != null) {
                for (JSGraphQLPsiElement definition : PsiTreeUtil.getChildrenOfTypeAsList(sourceElement.getContainingFile(), JSGraphQLPsiElement.class)) {
                    final JSGraphQLNamedTypePsiElement namedTypePsiElement = PsiTreeUtil.findChildOfType(definition, JSGraphQLNamedTypePsiElement.class);
                    if (namedTypePsiElement != null) {
                        getImplements(namedTypePsiElement).stream().filter(typeImplementsElement -> typeName.equals(typeImplementsElement.getName())).forEach(typeImplementsElement -> {
                            consumer.process(namedTypePsiElement);
                        });
                    }
                }
            }
        }
        return true;
    }

    /**
     * Looks for a list of named types between the 'implements' token and the '{' token of a schema type declaration
     * @param namedTypePsiElement the named type element to get 'implements' for
     * @return a list of types implemented by namedTypePsiElement
     */
    private static List<JSGraphQLNamedTypePsiElement> getImplements(JSGraphQLNamedTypePsiElement namedTypePsiElement) {
        PsiElement sibling = PsiTreeUtil.nextVisibleLeaf(namedTypePsiElement);
        if(sibling != null && sibling.getNode().getElementType() == JSGraphQLTokenTypes.KEYWORD && IMPLEMENTS_KEYWORD.equals(sibling.getText())) {
            final List<JSGraphQLNamedTypePsiElement> implementsLists = Lists.newArrayList();
            sibling = sibling.getNextSibling();
            while(sibling != null) {
                if(sibling instanceof JSGraphQLNamedTypePsiElement) {
                    implementsLists.add((JSGraphQLNamedTypePsiElement) sibling);
                } else if(sibling.getNode().getElementType() == JSGraphQLTokenTypes.LBRACE) {
                    // encountered a '{' which indicates the list of implements types are done
                    break;
                }
                sibling = sibling.getNextSibling();

            }
            return implementsLists;
        }
        return Collections.emptyList();
    }
}
