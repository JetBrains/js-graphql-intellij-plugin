package com.intellij.lang.jsgraphql.schema.library;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class GraphQLLibraryDescriptor {

    private final String myIdentifier;

    public GraphQLLibraryDescriptor(@NotNull String identifier) {
        myIdentifier = identifier;
    }

    @NotNull
    public String getIdentifier() {
        return myIdentifier;
    }

    @NotNull
    public String getPresentableText() {
        return getIdentifier();
    }

    public boolean isEnabled(@NotNull Project project) {
        return true;
    }

    @Override
    public String toString() {
        return "GraphQLLibraryDescriptor{" +
            "myIdentifier='" + myIdentifier + '\'' +
            '}';
    }
}
