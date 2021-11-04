package com.intellij.lang.jsgraphql.schema.library;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GraphQLLibraryRootsProvider extends AdditionalLibraryRootsProvider {

    @Nullable
    public static GraphQLLibrary findLibrary(@NotNull Project project, @NotNull GraphQLLibraryDescriptor libraryDescriptor) {
        for (SyntheticLibrary library : getLibraries(project)) {
            if (library instanceof GraphQLLibrary && ((GraphQLLibrary) library).getLibraryDescriptor().equals(libraryDescriptor)) {
                return (GraphQLLibrary) library;
            }
        }
        return null;
    }

    @NotNull
    public static Collection<SyntheticLibrary> getLibraries(@NotNull Project project) {
        GraphQLLibraryRootsProvider provider = AdditionalLibraryRootsProvider.EP_NAME.findExtension(GraphQLLibraryRootsProvider.class);
        return provider != null ? provider.getAdditionalProjectLibraries(project) : Collections.emptyList();
    }

    @NotNull
    @Override
    public Collection<SyntheticLibrary> getAdditionalProjectLibraries(@NotNull Project project) {
        return GraphQLLibraryManager.getInstance(project).getAllLibraries();
    }

    @NotNull
    @Override
    public Collection<VirtualFile> getRootsToWatch(@NotNull Project project) {
        Set<VirtualFile> roots = new HashSet<>();
        for (SyntheticLibrary library : GraphQLLibraryManager.getInstance(project).getAllLibraries()) {
            roots.addAll(library.getSourceRoots());
        }
        return roots;
    }
}
