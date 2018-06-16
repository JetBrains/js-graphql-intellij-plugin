/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.project;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.lang.jsgraphql.v1.JSGraphQLTokenTypes;
import com.intellij.lang.jsgraphql.v1.ide.findUsages.JSGraphQLFindUsagesUtil;
import com.intellij.lang.jsgraphql.v1.psi.JSGraphQLFragmentDefinitionPsiElement;
import com.intellij.lang.jsgraphql.v1.psi.JSGraphQLNamedTypePsiElement;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.impl.AnyPsiChangeListener;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Enables cross-file searches for PSI references and fragment completion
 */
public class JSGraphQLPsiSearchHelper {

    private static final FileType[] FILE_TYPES = JSGraphQLFindUsagesUtil.INCLUDED_FILE_TYPES.toArray(new FileType[JSGraphQLFindUsagesUtil.INCLUDED_FILE_TYPES.size()]);

    private final Project myProject;
    private final Map<String, JSGraphQLNamedTypePsiElement> fragmentDefinitionsByName = Maps.newConcurrentMap();
    private final GlobalSearchScope searchScope;

    public static JSGraphQLPsiSearchHelper getService(@NotNull Project project) {
        return ServiceManager.getService(project, JSGraphQLPsiSearchHelper.class);
    }


    public JSGraphQLPsiSearchHelper(@NotNull final Project project) {
        myProject = project;
        searchScope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(myProject), FILE_TYPES);
        project.getMessageBus().connect().subscribe(PsiManagerImpl.ANY_PSI_CHANGE_TOPIC, new AnyPsiChangeListener.Adapter() {
            @Override
            public void beforePsiChanged(boolean isPhysical) {
                // clear the cache on each PSI change
                fragmentDefinitionsByName.clear();
            }
        });
    }

    /**
     * Gets the fragment name element that is the source of a fragment usage by searching across files
     *
     * @param fragmentUsage a specific fragment usage, e.g. '...FragmentName'
     * @return the fragment definition that the usage references, e.g. 'fragment FragmentName'
     */
    public JSGraphQLNamedTypePsiElement resolveFragmentReference(@NotNull JSGraphQLNamedTypePsiElement fragmentUsage) {
        final String fragmentName = fragmentUsage.getName();
        if (fragmentName != null) {
            final JSGraphQLNamedTypePsiElement cachedResult = fragmentDefinitionsByName.get(fragmentName);
            if (cachedResult != null) {
                return cachedResult;
            }
            final Ref<JSGraphQLNamedTypePsiElement> fragmentDefinitionRef = new Ref<>();
            try {
                PsiSearchHelper.SERVICE.getInstance(myProject).processElementsWithWord((element, offsetInElement) -> {
                    if (element instanceof JSGraphQLNamedTypePsiElement && element.getParent() instanceof JSGraphQLFragmentDefinitionPsiElement) {
                        if (!element.equals(fragmentUsage)) {
                            // only consider as a reference if the element is not the usage element
                            final JSGraphQLNamedTypePsiElement fragmentDefinition = (JSGraphQLNamedTypePsiElement) element;
                            fragmentDefinitionsByName.put(fragmentName, fragmentDefinition);
                            fragmentDefinitionRef.set(fragmentDefinition);
                        }
                        return false;
                    }
                    return true;
                }, searchScope, fragmentName, UsageSearchContext.IN_CODE, true, true);
            } catch (IndexNotReadyException e) {
                // can't search yet (e.g. during project startup)
            }
            return fragmentDefinitionRef.get();
        }
        return null;
    }

    /**
     * Finds all fragment definition across files in the project
     *
     * @return a list of known fragment definitions, or an empty list if the index is not yet ready
     */
    public List<JSGraphQLFragmentDefinitionPsiElement> getKnownFragmentDefinitions() {
        try {
            final List<JSGraphQLFragmentDefinitionPsiElement> fragmentDefinitions = Lists.newArrayList();
            PsiSearchHelper.SERVICE.getInstance(myProject).processElementsWithWord((psiElement, offsetInElement) -> {
                if (psiElement.getNode().getElementType() == JSGraphQLTokenTypes.KEYWORD && psiElement.getParent() instanceof JSGraphQLFragmentDefinitionPsiElement) {
                    final JSGraphQLFragmentDefinitionPsiElement fragmentDefinition = (JSGraphQLFragmentDefinitionPsiElement) psiElement.getParent();
                    final String fragmentName = fragmentDefinition.getName();
                    if (fragmentName != null) {
                        fragmentDefinitions.add(fragmentDefinition);
                    }
                }
                return true;
            }, searchScope, "fragment", UsageSearchContext.IN_CODE, true, true);
            return fragmentDefinitions;
        } catch (IndexNotReadyException e) {
            // can't search yet (e.g. during project startup)
        }
        return Collections.emptyList();
    }

}
