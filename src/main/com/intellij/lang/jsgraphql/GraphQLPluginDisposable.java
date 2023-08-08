package com.intellij.lang.jsgraphql;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Service.Level;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

@Service(Level.PROJECT)
public final class GraphQLPluginDisposable implements Disposable {
  public static @NotNull Disposable getInstance(@NotNull Project project) {
    return project.getService(GraphQLPluginDisposable.class);
  }

  @Override
  public void dispose() {
  }
}
