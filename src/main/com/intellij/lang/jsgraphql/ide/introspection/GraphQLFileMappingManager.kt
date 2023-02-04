package com.intellij.lang.jsgraphql.ide.introspection;

import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.lang.jsgraphql.GraphQLSettings;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.intellij.lang.jsgraphql.schema.GraphQLSchemaKeys.*;

public final class GraphQLIntrospectionFilesManager {

    private static final Logger LOG = Logger.getInstance(GraphQLIntrospectionFilesManager.class);

    private GraphQLIntrospectionFilesManager() {
    }

    public static @NotNull GraphQLFile getOrCreateIntrospectionSDL(@NotNull VirtualFile file, @NotNull PsiFile psiFile) {
        return CachedValuesManager.getCachedValue(psiFile, GRAPHQL_INTROSPECTION_JSON_TO_SDL, () -> {
            Project project = psiFile.getProject();
            GraphQLSettings settings = GraphQLSettings.getSettings(project);

            String introspection = "";
            try {
                introspection = GraphQLIntrospectionService.getInstance(project).printIntrospectionAsGraphQL(psiFile.getText());
            } catch (ProcessCanceledException e) {
                throw e;
            } catch (Exception e) {
                LOG.warn(e);
            }

            final PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
            final String fileName = file.getPath();
            final GraphQLFile newIntrospectionFile =
                (GraphQLFile) psiFileFactory.createFileFromText(fileName, GraphQLLanguage.INSTANCE, introspection);
            newIntrospectionFile.putUserData(IS_GRAPHQL_INTROSPECTION_SDL, true);
            newIntrospectionFile.putUserData(GRAPHQL_INTROSPECTION_SDL_TO_JSON, psiFile);
            newIntrospectionFile.getVirtualFile().putUserData(IS_GRAPHQL_INTROSPECTION_SDL, true);
            newIntrospectionFile.getVirtualFile().putUserData(GRAPHQL_INTROSPECTION_SDL_TO_JSON, psiFile);
            try {
                newIntrospectionFile.getVirtualFile().setWritable(false);
            } catch (IOException e) {
                LOG.warn(e);
            }

            return CachedValueProvider.Result.create(newIntrospectionFile, psiFile, settings.getModificationTracker());
        });
    }

}
