package com.intellij.lang.jsgraphql.schema.library;

import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.GraphQLSettings;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public final class GraphQLLibraryTypes {
  public static GraphQLLibraryDescriptor SPECIFICATION = new GraphQLLibraryDescriptor("SPECIFICATION") {
    @Override
    public @NotNull String getPresentableText() {
      return GraphQLBundle.message("graphql.library.built.in");
    }
  };

  public static GraphQLLibraryDescriptor RELAY = new GraphQLLibraryDescriptor("RELAY") {
    @Override
    public boolean isEnabled(@NotNull Project project) {
      return GraphQLSettings.getSettings(project).isRelaySupportEnabled();
    }

    @Override
    public @NotNull String getPresentableText() {
      return GraphQLBundle.message("graphql.library.relay");
    }
  };

  public static GraphQLLibraryDescriptor FEDERATION = new GraphQLLibraryDescriptor("FEDERATION") {
    @Override
    public boolean isEnabled(@NotNull Project project) {
      return GraphQLSettings.getSettings(project).isFederationSupportEnabled();
    }

    @Override
    public @NotNull String getPresentableText() {
      return GraphQLBundle.message("graphql.library.federation");
    }
  };

  public static GraphQLLibraryDescriptor APOLLO_KOTLIN = new GraphQLLibraryDescriptor("APOLLO_KOTLIN") {
    @Override
    public boolean isEnabled(@NotNull Project project) {
      return GraphQLSettings.getSettings(project).isApolloKotlinSupportEnabled();
    }

    @Override
    public @NotNull String getPresentableText() {
      return GraphQLBundle.message("graphql.library.apollokotlin");
    }
  };

  private GraphQLLibraryTypes() {
  }
}
