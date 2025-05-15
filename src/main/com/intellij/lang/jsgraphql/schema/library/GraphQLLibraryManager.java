package com.intellij.lang.jsgraphql.schema.library;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.util.ClearableLazyValue;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.EditorNotifications;
import com.intellij.util.PathUtil;
import com.intellij.util.io.URLUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public final class GraphQLLibraryManager {
  private static final Logger LOG = Logger.getInstance(GraphQLLibraryManager.class);
  private static final String DEFINITIONS_RESOURCE_DIR = "definitions";
  private static final GraphQLLibrary EMPTY_LIBRARY =
    new GraphQLLibrary(new GraphQLLibraryDescriptor("EMPTY"), new LightVirtualFile());

  private static final Map<GraphQLLibraryDescriptor, String> ourDefinitionResourcePaths = Map.of(
    GraphQLLibraryTypes.SPECIFICATION, "Specification.graphql",
    GraphQLLibraryTypes.RELAY, "Relay.graphql",
    GraphQLLibraryTypes.FEDERATION, "Federation.graphql",
    GraphQLLibraryTypes.APOLLO_KOTLIN, "ApolloKotlin.graphql"
  );

  private final Project myProject;
  private final Map<GraphQLLibraryDescriptor, GraphQLLibrary> myLibraries = new ConcurrentHashMap<>();
  private final AtomicBoolean myLibrariesChangeTriggered = new AtomicBoolean();
  private final AtomicBoolean myAsyncRefreshRequested = new AtomicBoolean();
  private volatile boolean myLibrariesEnabled = !ApplicationManager.getApplication().isUnitTestMode();

  private final ClearableLazyValue<Set<VirtualFile>> myKnownLibraryRoots = ClearableLazyValue.createAtomic(
    () -> getAllLibraries()
      .stream()
      .flatMap(library -> library.getSourceRoots().stream())
      .collect(Collectors.toSet())
  );

  public GraphQLLibraryManager(@NotNull Project project) {
    myProject = project;
  }

  public static GraphQLLibraryManager getInstance(@NotNull Project project) {
    return project.getService(GraphQLLibraryManager.class);
  }

  public @Nullable GraphQLLibrary getOrCreateLibrary(@NotNull GraphQLLibraryDescriptor libraryDescriptor) {
    if (ApplicationManager.getApplication().isUnitTestMode() && !myLibrariesEnabled) {
      return null;
    }

    GraphQLLibrary library = myLibraries.computeIfAbsent(libraryDescriptor, __ -> {
      VirtualFile root = resolveLibraryRoot(libraryDescriptor);
      if (root == null) {
        LOG.warn("Unresolved library root: " + libraryDescriptor.getIdentifier());
        // try only once during a session
        if (myAsyncRefreshRequested.compareAndSet(false, true)) {
          tryRefreshAndLoadAsync();
        }
        return EMPTY_LIBRARY;
      }
      return new GraphQLLibrary(libraryDescriptor, root);
    });

    return library == EMPTY_LIBRARY ? null : library;
  }

  private void tryRefreshAndLoadAsync() {
    ApplicationManager.getApplication().invokeLater(() -> WriteAction.run(() -> {
      URL definitionsDirUrl = getClass().getClassLoader().getResource(DEFINITIONS_RESOURCE_DIR);
      if (definitionsDirUrl == null) return;
      Pair<String, String> urlParts = URLUtil.splitJarUrl(definitionsDirUrl.getFile());
      if (urlParts == null) return;
      String jarPath = PathUtil.toSystemIndependentName(urlParts.first);
      VirtualFile jarLocalFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(jarPath);
      if (jarLocalFile == null) return;
      VirtualFile jarFile = JarFileSystem.getInstance().refreshAndFindFileByPath(jarPath + URLUtil.JAR_SEPARATOR);
      if (jarFile == null) return;
      VirtualFile definitionsDir = VfsUtil.refreshAndFindChild(jarFile, DEFINITIONS_RESOURCE_DIR);
      if (definitionsDir == null || !definitionsDir.isDirectory()) return;

      for (GraphQLLibraryDescriptor libraryDescriptor : ourDefinitionResourcePaths.keySet()) {
        VirtualFile libraryRoot = resolveLibraryRoot(libraryDescriptor);
        if (libraryRoot != null) {
          myLibraries.put(libraryDescriptor, new GraphQLLibrary(libraryDescriptor, libraryRoot));
          notifyLibrariesChanged();
        }
      }
    }), ModalityState.nonModal(), myProject.getDisposed());
  }

  public @NotNull Collection<SyntheticLibrary> getAllLibraries() {
    return ourDefinitionResourcePaths
      .keySet().stream()
      .map(this::getOrCreateLibrary)
      .filter(Objects::nonNull)
      .filter(l -> l.getLibraryDescriptor().isEnabled(myProject))
      .collect(Collectors.toList());
  }

  private @Nullable VirtualFile resolveLibraryRoot(@NotNull GraphQLLibraryDescriptor descriptor) {
    String resourceName = ourDefinitionResourcePaths.get(descriptor);
    if (resourceName == null) {
      LOG.error("No resource files found for library: " + descriptor);
      return null;
    }
    URL resource = getClass().getClassLoader().getResource(DEFINITIONS_RESOURCE_DIR + "/" + resourceName);
    if (resource == null) {
      LOG.error("Resource not found: " + resourceName);
      return null;
    }
    VirtualFile root = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.convertFromUrl(resource));
    return root != null && root.isValid() ? root : null;
  }

  public boolean isLibraryRoot(@Nullable VirtualFile file) {
    return file != null && myKnownLibraryRoots.getValue().contains(file);
  }

  public @NotNull Set<VirtualFile> getLibraryRoots() {
    return myKnownLibraryRoots.getValue();
  }

  public void notifyLibrariesChanged() {
    if (myLibrariesChangeTriggered.compareAndSet(false, true)) {
      DumbService.getInstance(myProject).smartInvokeLater(() -> {
        try {
          WriteAction.run(() -> {
            LOG.info("GraphQL libraries changed");

            myKnownLibraryRoots.drop();
            PsiManager.getInstance(myProject).dropPsiCaches();
            ProjectRootManagerEx.getInstanceEx(myProject).makeRootsChange(EmptyRunnable.getInstance(), false, true);
            DaemonCodeAnalyzer.getInstance(myProject).restart();
            EditorNotifications.getInstance(myProject).updateAllNotifications();
          });
        }
        finally {
          myLibrariesChangeTriggered.set(false);
        }
      }, ModalityState.nonModal());
    }
  }

  public void setLibrariesEnabled(boolean enabled) {
    myLibrariesEnabled = enabled;
  }
}
