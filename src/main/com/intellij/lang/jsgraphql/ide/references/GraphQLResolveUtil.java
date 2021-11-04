package com.intellij.lang.jsgraphql.ide.references;

import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibrary;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryDescriptor;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryRootsProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class GraphQLResolveUtil {
    private GraphQLResolveUtil() {
    }

    public static void processFilesInLibrary(@NotNull GraphQLLibraryDescriptor libraryDescriptor,
                                             @NotNull PsiElement context,
                                             @NotNull Processor<? super GraphQLFile> processor) {
        GraphQLLibrary library = GraphQLLibraryRootsProvider.findLibrary(context.getProject(), libraryDescriptor);
        if (library == null) return;

        PsiManager psiManager = context.getManager();
        for (VirtualFile root : library.getSourceRoots()) {
            PsiFile file = psiManager.findFile(root);
            if (file instanceof GraphQLFile && file.isValid()) {
                if (!processor.process(((GraphQLFile) file))) return;
            }
        }
    }

    /**
     * Prefer {@link GraphQLResolveUtil#processFilesInLibrary(GraphQLLibraryDescriptor, PsiElement, Processor)} instead.
     *
     * @param visitor recursive visitor is used for historical reasons
     */
    public static void processFilesInLibrary(@NotNull GraphQLLibraryDescriptor libraryDescriptor,
                                             @NotNull PsiElement context,
                                             @NotNull PsiRecursiveElementVisitor visitor) {
        processFilesInLibrary(libraryDescriptor, context, CommonProcessors.processAll(file -> file.accept(visitor)));
    }

    @NotNull
    public static Collection<GraphQLFile> getLibraryFiles(@NotNull GraphQLLibraryDescriptor libraryDescriptor,
                                                          @NotNull PsiElement context) {
        CommonProcessors.CollectUniquesProcessor<GraphQLFile> processor = new CommonProcessors.CollectUniquesProcessor<>();
        processFilesInLibrary(libraryDescriptor, context, processor);
        return processor.getResults();
    }
}
