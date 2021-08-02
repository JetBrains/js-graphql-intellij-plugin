/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.psi;

import com.google.common.collect.Sets;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointFileType;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.lang.jsgraphql.v1.ide.configuration.JSGraphQLConfigurationProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class JSGraphQLEndpointPsiUtil {

	/**
	 * Creates a search scope for finding import files based on the endpoint entry file, falling back to
	 * project scope in case no entry file has been configured.
	 * @param project      project to search
	 * @param entryFile    the entry file, or null to look it up
     */
	@NotNull
	public static GlobalSearchScope getImportScopeFromEntryFile(Project project, @Nullable VirtualFile entryFile, PsiElement scopedElement) {
		if(entryFile == null) {
			entryFile = JSGraphQLConfigurationProvider.getService(project).getEndpointEntryFile(scopedElement.getContainingFile());
		}
		final GlobalSearchScope scope;
		if(entryFile != null) {
			scope = GlobalSearchScopesCore.directoriesScope(project, true, entryFile.getParent());
		} else {
			scope = GlobalSearchScope.projectScope(project);
		}
		return scope;
	}

	/**
	 * Gets known definitions, ie. those defined in the specified file, or included via an import.
	 * Definitions must have a defined name to be included.
	 *
	 * @param file               the file to use as starting point
	 * @param psiDefinitionClass class of definitions to get, e.g. object type definitions
	 * @param includeAutoImportTypes whether to include types that are not currently imported, but can be made available using an auto-import
     * @param importedFiles ref to add currently imported files to
	 */
	public static <T extends JSGraphQLEndpointNamedTypeDefinition> Collection<T> getKnownDefinitions(
            PsiFile file,
            Class<T> psiDefinitionClass,
            boolean includeAutoImportTypes,
            Ref<Collection<PsiFile>> importedFiles) {

		final Set<T> definitions = Sets.newHashSet();

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

		if(importedFiles != null) {
            importedFiles.set(Sets.newHashSet(files));
        }

		if(includeAutoImportTypes) {
			final Collection<VirtualFile> knownFiles = FileTypeIndex.getFiles(JSGraphQLEndpointFileType.INSTANCE, getImportScopeFromEntryFile(file.getProject(), null, file));
			final PsiManager psiManager = PsiManager.getInstance(file.getProject());
			knownFiles.forEach(virtualFile ->{
				final PsiFile psiFile = psiManager.findFile(virtualFile);
				if(psiFile != null) {
					files.add(psiFile);
				}
			});
		}

		for (PsiFile psiFile : files) {
			final Collection<T> definitionElements = PsiTreeUtil.findChildrenOfType(psiFile, psiDefinitionClass);
			for (T definition : definitionElements) {
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
	public static <T extends JSGraphQLEndpointNamedTypeDefinition> Collection<JSGraphQLEndpointTypeResult<T>> getKnownDefinitionNames(PsiFile file, Class<T> psiDefinitionClass, boolean autoImport) {
        final Ref<Collection<PsiFile>> importedFiles = new Ref<>();
        return getKnownDefinitions(file, psiDefinitionClass, autoImport, importedFiles).stream()
				.filter(d -> d.getNamedTypeDef() != null)
				.map(d -> {
                    PsiFile fileToImport = d.getContainingFile();
                    if(importedFiles.get().contains(fileToImport)) {
                        fileToImport = null;
                    }
                    return new JSGraphQLEndpointTypeResult<>(d.getNamedTypeDef().getText(), d, fileToImport);
                })
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
