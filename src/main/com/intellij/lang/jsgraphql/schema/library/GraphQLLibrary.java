package com.intellij.lang.jsgraphql.schema.library;

import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.icons.GraphQLIcons;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class GraphQLLibrary extends SyntheticLibrary implements ItemPresentation {

  private final GraphQLLibraryDescriptor myLibraryDescriptor;
  private final VirtualFile myFile;

  public GraphQLLibrary(@NotNull GraphQLLibraryDescriptor libraryDescriptor,
                        @NotNull VirtualFile file) {
    myLibraryDescriptor = libraryDescriptor;
    myFile = file;
  }

  public @NotNull GraphQLLibraryDescriptor getLibraryDescriptor() {
    return myLibraryDescriptor;
  }

  @Override
  public @Nullable String getPresentableText() {
    return String.format("%s %s", GraphQLBundle.message("graphql.library.prefix"), myLibraryDescriptor.getPresentableText());
  }

  @Override
  public @Nullable Icon getIcon(boolean unused) {
    return GraphQLIcons.Logos.GraphQL;
  }

  @Override
  public @NotNull Collection<VirtualFile> getSourceRoots() {
    return Collections.singleton(myFile);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GraphQLLibrary that = (GraphQLLibrary)o;
    return Objects.equals(myFile, that.myFile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(myFile);
  }
}
