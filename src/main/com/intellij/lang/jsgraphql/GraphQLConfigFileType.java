package com.intellij.lang.jsgraphql;

import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class GraphQLConfigFileType extends JsonFileType {
    public static final GraphQLConfigFileType INSTANCE = new GraphQLConfigFileType();

    public GraphQLConfigFileType() {
        super(JsonLanguage.INSTANCE, true);
    }

    @Override
    public @NotNull String getName() {
        return "GraphQL Config";
    }

    @Override
    public @NotNull String getDescription() {
        return GraphQLBundle.message("graphql.config.file.type.description");
    }

    @Override
    public @Nls @NotNull String getDisplayName() {
        return GraphQLBundle.message("graphql.config.file.type.description");
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "json";
    }
}
