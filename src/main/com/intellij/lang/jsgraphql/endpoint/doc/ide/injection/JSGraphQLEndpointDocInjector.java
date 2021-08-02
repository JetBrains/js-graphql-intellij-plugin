/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.doc.ide.injection;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointLanguage;
import com.intellij.lang.jsgraphql.endpoint.doc.JSGraphQLEndpointDocLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.InjectedLanguagePlaces;
import com.intellij.psi.LanguageInjector;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiLanguageInjectionHost;

/**
 * Injects GraphQL Endpoint Doc into the documentation comments
 */
public class JSGraphQLEndpointDocInjector implements LanguageInjector {


	@Override
	public void getLanguagesToInject(@NotNull PsiLanguageInjectionHost host, @NotNull InjectedLanguagePlaces injectionPlacesRegistrar) {
		if(ApplicationManager.getApplication().isUnitTestMode()) {
			// intellij unit test env doesn't properly support language injection in combination with formatter tests, so skip injection in that case
			return;
		}
		if (host instanceof PsiComment && host.getLanguage() == JSGraphQLEndpointLanguage.INSTANCE) {
			injectionPlacesRegistrar.addPlace(JSGraphQLEndpointDocLanguage.INSTANCE, TextRange.create(0, host.getTextLength()), "", "");
		}
	}
}
