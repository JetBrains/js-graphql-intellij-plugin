package com.intellij.lang.jsgraphql.schema.library;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GraphQLLibraryDescriptor {

  private final String myIdentifier;

  public GraphQLLibraryDescriptor(@NotNull String identifier) {
    myIdentifier = identifier;
  }

  public @NotNull String getIdentifier() {
    return myIdentifier;
  }

  public @NotNull String getPresentableText() {
    return getIdentifier();
  }

  public boolean isEnabled(@NotNull Project project) {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GraphQLLibraryDescriptor that = (GraphQLLibraryDescriptor)o;
    return Objects.equals(myIdentifier, that.myIdentifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(myIdentifier);
  }

  @Override
  public String toString() {
    return "GraphQLLibraryDescriptor{" +
           "myIdentifier='" + myIdentifier + '\'' +
           '}';
  }
}
