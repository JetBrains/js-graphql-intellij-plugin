package com.intellij.lang.jsgraphql.ide.resolve;

import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibrary;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryDescriptor;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryRootsProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class GraphQLResolveUtil {

    private static final Logger LOG = Logger.getInstance(GraphQLResolveUtil.class);

    private GraphQLResolveUtil() {
    }

    public static void processFilesInLibrary(@NotNull GraphQLLibraryDescriptor libraryDescriptor,
                                             @NotNull PsiElement context,
                                             @NotNull Processor<? super GraphQLFile> processor) {
        GraphQLLibrary library = GraphQLLibraryRootsProvider.findLibrary(context.getProject(), libraryDescriptor);
        if (library == null) {
            LOG.warn("Library is not found: " + libraryDescriptor);
            return;
        }

        PsiManager psiManager = context.getManager();
        for (VirtualFile root : library.getSourceRoots()) {
            PsiFile file = psiManager.findFile(root);
            if (!(file instanceof GraphQLFile) || !file.isValid()) {
                LOG.warn("Library file is invalid: " + root.getPath());
                continue;
            }

            if (!processor.process(((GraphQLFile) file))) return;
        }
    }

    @NotNull
    public static Collection<GraphQLFile> getLibraryFiles(@NotNull GraphQLLibraryDescriptor libraryDescriptor,
                                                          @NotNull PsiElement context) {
        CommonProcessors.CollectUniquesProcessor<GraphQLFile> processor = new CommonProcessors.CollectUniquesProcessor<>();
        processFilesInLibrary(libraryDescriptor, context, processor);
        return processor.getResults();
    }

    @Nullable
    public static GraphQLDefinition findContainingDefinition(@Nullable PsiElement element) {
        GraphQLDefinition definition = PsiTreeUtil.getParentOfType(element, GraphQLDefinition.class, false);
        if (definition instanceof GraphQLTemplateDefinition) {
            return null; // this is unexpected for most cases
        }
        return definition;
    }

    /**
     * This method is only a temporary workaround for current PSI implementation for symbol declarations.
     * By platform guidelines a symbol which defines a new name should implement PsiNamedElement interface.
     * But now when we resolve a reference most of the time we get a GraphQLIdentifier as a result,
     * but the real declaration we need is a node somewhere up in the PSI tree
     * like GraphQLObjectTypeDefinition, GraphQLFieldDefinition, etc.
     */
    @Nullable
    public static PsiElement findResolvedDefinition(@Nullable PsiElement resolveTarget) {
        if (resolveTarget == null) return null;

        // getParent() is used because now we resolve to GraphQLIdentifier most of the time instead of an actual symbol definition
        PsiElement definition = resolveTarget.getParent();
        if (definition instanceof GraphQLEnumValue) {
            definition = definition.getParent();
        }
        if (definition instanceof GraphQLTypeNameDefinition) {
            definition = definition.getParent();
        }
        return definition;
    }

    @Nullable
    public static PsiElement resolve(@Nullable PsiElement element) {
        if (!(element instanceof GraphQLNamedElement)) {
            return null;
        }

        final PsiElement nameIdentifier = ((GraphQLNamedElement) element).getNameIdentifier();
        if (nameIdentifier != null) {
            PsiReference reference = nameIdentifier.getReference();
            if (reference != null) {
                return reference.resolve();
            }
        }
        return null;
    }

    public static void processDirectoriesUpToContentRoot(@NotNull Project project,
                                                         @NotNull VirtualFile fileOrDir,
                                                         @NotNull Processor<? super VirtualFile> directoryProcessor) {
        if (project.isDisposed()) {
            return;
        }
        ProjectFileIndex fileIndex = ProjectFileIndex.getInstance(project);
        VirtualFile dir = fileOrDir.isDirectory() ? fileOrDir : fileOrDir.getParent();
        if (dir == null) {
            return;
        }
        VirtualFile contentRoot = fileIndex.getContentRootForFile(dir, false);
        while (dir != null && contentRoot != null) {
            if (!directoryProcessor.process(dir)) {
                return;
            }
            if (dir.equals(contentRoot)) {
                dir = dir.getParent();
                if (dir == null) {
                    return;
                }
                contentRoot = fileIndex.getContentRootForFile(dir, false);
            } else {
                dir = dir.getParent();
            }
        }
    }
}
