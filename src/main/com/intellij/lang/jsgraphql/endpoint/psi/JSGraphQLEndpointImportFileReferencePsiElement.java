/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.psi;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointFileType;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.lang.jsgraphql.v1.ide.configuration.JSGraphQLConfigurationProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceOwner;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PsiFileReference;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.refactoring.rename.BindablePsiReference;
import com.intellij.util.IncorrectOperationException;

/**
 * Represents the file name aspect of an import declaration. It references the corresponding PSI File.
 */
public class JSGraphQLEndpointImportFileReferencePsiElement extends JSGraphQLEndpointPsiElement implements PsiNameIdentifierOwner {

	public static final Object[] NO_VARIANTS = new Object[0];

	public JSGraphQLEndpointImportFileReferencePsiElement(@NotNull ASTNode node) {
		super(node);
	}

	@Override
	public String getName() {
		// used for find usages
		final PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier != null ? nameIdentifier.getText() : getText();
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier() {
		final PsiElement stringElement = this.findChildByType(JSGraphQLEndpointTokenTypes.QUOTED_STRING);
		if(stringElement != null) {
			final ASTNode string = stringElement.getNode().findChildByType(JSGraphQLEndpointTokenTypes.STRING);
			if(string != null) {
				final ASTNode stringBody = string.findChildByType(JSGraphQLEndpointTokenTypes.STRING_BODY);
				if (stringBody != null) {
					return stringBody.getPsi();
				}
			}
		}
		return null;
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
		final PsiElement nameIdentifier = getNameIdentifier();
		if(nameIdentifier != null) {
			final LeafElement renamedLeaf = Factory.createSingleLeafElement(JSGraphQLEndpointTokenTypes.STRING_BODY, name, null, getManager());
			final PsiElement renamedPsiElement = SourceTreeToPsiMap.treeElementToPsi(renamedLeaf);
			if (renamedPsiElement != null) {
				nameIdentifier.replace(renamedPsiElement);
			}
		}
		return this;
	}

	@Override
	public PsiReference getReference() {
		final PsiElement nameIdentifier = getNameIdentifier();
		if(nameIdentifier != null) {
			return new JSGraphQLEndpointFilePsiReference(this, TextRange.from(1, nameIdentifier.getTextLength()), nameIdentifier);
		}
		return null;
	}

	/**
	 * Reference to a GraphQL Endpoint file. Supports file renames.
	 */
	private static class JSGraphQLEndpointFilePsiReference extends PsiReferenceBase<PsiNamedElement> implements BindablePsiReference, PsiFileReference, FileReferenceOwner {

		private final PsiElement nameIdentifier;

		public JSGraphQLEndpointFilePsiReference(PsiNamedElement element, TextRange rangeInElement, PsiElement nameIdentifier) {
			super(element, rangeInElement);
			this.nameIdentifier = nameIdentifier;
		}

		@Nullable
		@Override
		public PsiElement resolve() {
			final Project project = this.getElement().getProject();
			final VirtualFile entryFile = JSGraphQLConfigurationProvider.getService(project).getEndpointEntryFile(this.getElement().getContainingFile());
			if(entryFile != null && entryFile.getParent() != null) {
				final String fileName = nameIdentifier.getText();
				if(fileName.startsWith(".") || fileName.startsWith("/")) {
					// we're always relative to the entry file, so return null in case a relative-to-current-file is attempted
					return null;
				}
				final VirtualFile importedFile = entryFile.getParent().findFileByRelativePath(fileName + "." + JSGraphQLEndpointFileType.INSTANCE.getDefaultExtension());
				if(importedFile != null) {
					return PsiManager.getInstance(project).findFile(importedFile);
				}
			}
			return null;
		}

		@NotNull
		@Override
		public Object[] getVariants() {
			return new Object[0];
		}

		@Override
		public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
			// handles file renames
			if(element instanceof JSGraphQLEndpointFile) {
				String newFileName = ((JSGraphQLEndpointFile) element).getName();
				newFileName = StringUtils.removeEnd(newFileName, "." + JSGraphQLEndpointFileType.INSTANCE.getDefaultExtension());
				if(nameIdentifier.getText().contains("/")) {
					// add the path
					newFileName = StringUtils.substringBeforeLast(nameIdentifier.getText(), "/") + "/" + newFileName;
				}
				myElement.setName(newFileName);
			}
			return myElement;
		}

		@NotNull
		@Override
		public ResolveResult[] multiResolve(boolean incompleteCode) {
			final PsiElement resolve = resolve();
			if(resolve != null) {
				return new ResolveResult[] { new PsiElementResolveResult(resolve) };
			}
			return ResolveResult.EMPTY_ARRAY;
		}

		@Nullable
		@Override
		public PsiFileReference getLastFileReference() {
			return this;
		}
	}

}
