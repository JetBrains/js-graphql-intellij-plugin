package com.intellij.lang.jsgraphql.schema.library;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public final class GraphQLLibraryRootsProvider extends AdditionalLibraryRootsProvider {

  public static @Nullable GraphQLLibrary findLibrary(@NotNull Project project, @NotNull GraphQLLibraryDescriptor libraryDescriptor) {
    for (SyntheticLibrary library : getLibraries(project)) {
      if (library instanceof GraphQLLibrary && ((GraphQLLibrary)library).getLibraryDescriptor().equals(libraryDescriptor)) {
        return (GraphQLLibrary)library;
      }
    }
    return null;
  }

  public static @NotNull Collection<SyntheticLibrary> getLibraries(@NotNull Project project) {
    GraphQLLibraryRootsProvider provider = AdditionalLibraryRootsProvider.EP_NAME.findExtension(GraphQLLibraryRootsProvider.class);
    return provider != null ? provider.getAdditionalProjectLibraries(project) : Collections.emptyList();
  }

  public static @NotNull GlobalSearchScope createScope(@NotNull Project project) {
    Set<VirtualFile> roots = getLibraries(project).stream()
      .flatMap(library -> library.getSourceRoots().stream())
      .collect(Collectors.toSet());
    return GlobalSearchScope.filesWithLibrariesScope(project, roots);
  }

  @Override
  public @NotNull Collection<SyntheticLibrary> getAdditionalProjectLibraries(@NotNull Project project) {
    return GraphQLLibraryManager.getInstance(project).getAllLibraries();
  }

  @Override
  public @NotNull Collection<VirtualFile> getRootsToWatch(@NotNull Project project) {
    return GraphQLLibraryManager.getInstance(project).getLibraryRoots();
  }
}
