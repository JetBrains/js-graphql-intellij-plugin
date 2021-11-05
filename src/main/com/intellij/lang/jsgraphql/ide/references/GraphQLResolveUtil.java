package com.intellij.lang.jsgraphql.ide.references;

import com.intellij.lang.jsgraphql.psi.GraphQLDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValue;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeNameDefinition;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibrary;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryDescriptor;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryRootsProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    public static GraphQLDefinition findContainingDefinition(@Nullable PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, GraphQLDefinition.class, false);
    }

    @Nullable
    public static PsiElement findDeclaringDefinition(@Nullable PsiElement element) {
        if (element == null) return null;

        // getParent() is used because now we resolve to GraphQLIdentifier most of the time instead of an actual symbol definition
        PsiElement definition = element.getParent();
        if (definition instanceof GraphQLEnumValue) {
            definition = definition.getParent();
        }
        if (definition instanceof GraphQLTypeNameDefinition) {
            definition = definition.getParent();
        }
        return definition;
    }
}
