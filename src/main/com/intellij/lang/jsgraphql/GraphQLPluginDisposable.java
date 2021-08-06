package com.intellij.lang.jsgraphql;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public final class GraphQLPluginDisposable implements Disposable {
    public static @NotNull Disposable getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLPluginDisposable.class);
    }

    @Override
    public void dispose() {
    }
}
