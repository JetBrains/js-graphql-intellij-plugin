package com.intellij.lang.jsgraphql.schema.library;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotifications;
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
    private final static Logger LOG = Logger.getInstance(GraphQLLibraryManager.class);
    private static final String DEFINITIONS_RESOURCE_DIR = "definitions/";

    private static final Map<GraphQLLibraryDescriptor, String> ourDefinitionResourcePaths = Map.of(
        GraphQLLibraryTypes.SPECIFICATION, "Specification.graphql",
        GraphQLLibraryTypes.RELAY, "Relay.graphql",
        GraphQLLibraryTypes.FEDERATION, "Federation.graphql"
    );

    private final Project myProject;
    private final Map<GraphQLLibraryDescriptor, GraphQLLibrary> myLibraries = new ConcurrentHashMap<>();
    private final AtomicBoolean myUpdateIsRequested = new AtomicBoolean();

    private final NotNullLazyValue<Set<VirtualFile>> myKnownLibraryRoots = NotNullLazyValue.atomicLazy(() ->
        ourDefinitionResourcePaths
            .keySet().stream()
            .map(this::getOrCreateLibrary)
            .filter(Objects::nonNull)
            .flatMap(library -> library.getSourceRoots().stream())
            .collect(Collectors.toSet())
    );

    public GraphQLLibraryManager(@NotNull Project project) {
        myProject = project;
    }

    public static GraphQLLibraryManager getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLLibraryManager.class);
    }

    @Nullable
    public GraphQLLibrary getOrCreateLibrary(@NotNull GraphQLLibraryDescriptor libraryDescriptor) {
        return myLibraries.computeIfAbsent(libraryDescriptor, __ -> {
            VirtualFile root = resolveLibraryRoot(libraryDescriptor);
            if (root == null) {
                LOG.warn("Unresolved library root: " + libraryDescriptor);
                return null;
            }
            return new GraphQLLibrary(libraryDescriptor, root);
        });
    }

    @NotNull
    public Collection<SyntheticLibrary> getAllLibraries() {
        return ourDefinitionResourcePaths
            .keySet().stream()
            .map(this::getOrCreateLibrary)
            .filter(Objects::nonNull)
            .filter(l -> l.getLibraryDescriptor().isEnabled(myProject))
            .collect(Collectors.toList());
    }

    @Nullable
    private VirtualFile resolveLibraryRoot(@NotNull GraphQLLibraryDescriptor descriptor) {
        String resourceName = ourDefinitionResourcePaths.get(descriptor);
        if (resourceName == null) {
            LOG.error("No resource files found for library: " + descriptor);
            return null;
        }
        URL resource = getClass().getClassLoader().getResource(DEFINITIONS_RESOURCE_DIR + resourceName);
        if (resource == null) {
            LOG.error("Resource not found: " + resourceName);
            return null;
        }
        VirtualFile root = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.convertFromUrl(resource));
        return root != null && root.isValid() ? root : null;
    }

    public boolean isLibraryRoot(@NotNull VirtualFile file) {
        return myKnownLibraryRoots.getValue().contains(file);
    }

    public void notifyLibrariesChanged() {
        if (myUpdateIsRequested.compareAndSet(false, true)) {
            DumbService.getInstance(myProject).smartInvokeLater(() -> {
                try {
                    WriteAction.run(() -> {
                        LOG.info("GraphQL libraries changed");

                        PsiManager.getInstance(myProject).dropPsiCaches();
                        ProjectRootManagerEx.getInstanceEx(myProject).makeRootsChange(EmptyRunnable.getInstance(), false, true);
                        DaemonCodeAnalyzer.getInstance(myProject).restart();
                        EditorNotifications.getInstance(myProject).updateAllNotifications();
                    });
                } finally {
                    myUpdateIsRequested.set(false);
                }
            }, ModalityState.NON_MODAL);
        }
    }
}
