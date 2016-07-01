/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.psi;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;

public class JSGraphQLEndpointPsiUtil {

	/**
	 * Gets known definitions, ie. those defined in the specified file, or included via an import.
	 * Definitions must have a defined name to be included.
	 *
	 * @param file               the file to use as starting point
	 * @param psiDefinitionClass class of definitions to get, e.g. object type definitions
	 */
	public static <T extends JSGraphQLEndpointNamedTypeDefinition> Collection<JSGraphQLEndpointNamedTypeDefinition> getKnownDefinitions(PsiFile file, Class<T> psiDefinitionClass) {

		final Set<JSGraphQLEndpointNamedTypeDefinition> definitions = Sets.newHashSet();

		final Set<PsiFile> files = Sets.newHashSet(file);
		final JSGraphQLEndpointImportDeclaration[] importDeclarations = PsiTreeUtil.getChildrenOfType(file, JSGraphQLEndpointImportDeclaration.class);
		if (importDeclarations != null) {
			for (JSGraphQLEndpointImportDeclaration importDeclaration : importDeclarations) {
				final JSGraphQLEndpointImportFileReference[] importFileReferences = PsiTreeUtil.getChildrenOfType(importDeclaration, JSGraphQLEndpointImportFileReference.class);
				if (importFileReferences != null) {
					for (JSGraphQLEndpointImportFileReference importFileReference : importFileReferences) {
						final PsiReference reference = importFileReference.getReference();
						if (reference != null) {
							final PsiElement importedFile = reference.resolve();
							if (importedFile instanceof JSGraphQLEndpointFile) {
								files.add((PsiFile) importedFile);
							}
						}
					}
				}
			}
		}

		for (PsiFile psiFile : files) {
			final Collection<JSGraphQLEndpointNamedTypeDefinition> definitionElements = PsiTreeUtil.findChildrenOfType(psiFile, psiDefinitionClass);
			for (JSGraphQLEndpointNamedTypeDefinition definition : definitionElements) {
				final JSGraphQLEndpointNamedTypeDef namedTypeDef = definition.getNamedTypeDef();
				if (namedTypeDef != null) {
					definitions.add(definition);
				}
			}
		}

		return definitions;
	}

	/**
	 * Maps getKnownDefinitions to their corresponding names
	 */
	public static <T extends JSGraphQLEndpointNamedTypeDefinition> Collection<String> getKnownDefinitionNames(PsiFile file, Class<T> psiDefinitionClass) {
		return getKnownDefinitions(file, psiDefinitionClass).stream()
				.filter(d -> d.getNamedTypeDef() != null)
				.map(d -> d.getNamedTypeDef().getText())
				.collect(Collectors.toList());
	}

	/**
	 * Renames an identifier, e.g. during a refactoring
	 */
	public static PsiElement renameIdentifier(PsiNameIdentifierOwner owner, String name) throws IncorrectOperationException {
		final PsiElement identifier = owner.getNameIdentifier();
		if (identifier == null) {
			throw new IncorrectOperationException();
		}
		final LeafElement renamedLeaf = Factory.createSingleLeafElement(JSGraphQLEndpointTokenTypes.IDENTIFIER, name, null, identifier.getManager());
		final PsiElement renamedPsiElement = SourceTreeToPsiMap.treeElementToPsi(renamedLeaf);
		if (renamedPsiElement != null) {
			identifier.replace(renamedPsiElement);
		}
		return owner;
	}

}
