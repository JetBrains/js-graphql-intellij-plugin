package com.intellij.lang.jsgraphql.schema.library

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.runAndLogException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.*
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.toVirtualFileUrl
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.PathUtil
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import com.intellij.util.concurrency.annotations.RequiresReadLockAbsence
import com.intellij.util.io.URLUtil
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.Volatile

private const val DEFINITIONS_RESOURCE_DIR = "definitions"

private val BUNDLED_RESOURCE_PATHS = mapOf(
  GraphQLBundledLibraryTypes.SPECIFICATION to "Specification.graphql",
  GraphQLBundledLibraryTypes.RELAY to "Relay.graphql",
  GraphQLBundledLibraryTypes.FEDERATION to "Federation.graphql",
  GraphQLBundledLibraryTypes.APOLLO_KOTLIN to "ApolloKotlin.graphql"
)

private val BUNDLED_LIBRARIES_IDS = BUNDLED_RESOURCE_PATHS.keys.map { it.identifier }.toSet()

@ApiStatus.Experimental
@Service(Service.Level.PROJECT)
class GraphQLLibraryManager(private val project: Project) {
  companion object {
    private val LOG = logger<GraphQLLibraryManager>()

    @JvmStatic
    fun getInstance(project: Project): GraphQLLibraryManager = project.service()

    suspend fun getInstanceAsync(project: Project): GraphQLLibraryManager = project.serviceAsync()
  }

  private val bundledLibraries = ConcurrentHashMap<GraphQLLibraryDescriptor, GraphQLLibrary>()
  private val externalLibraries = ConcurrentHashMap<String, GraphQLLibrary>()

  @Volatile
  private var shouldInitializeLibraries = !ApplicationManager.getApplication().isUnitTestMode()

  @TestOnly
  fun enableLibraries(enabled: Boolean) {
    shouldInitializeLibraries = enabled
  }

  /**
   * Registers an external GraphQL library.
   * If a library with the same identifier is already registered,
   * it will be replaced with the new library.
   *
   * @param library The external GraphQL library to register, containing its descriptor and set of root URLs.
   */
  @RequiresBackgroundThread
  @RequiresReadLockAbsence
  suspend fun registerExternalLibrary(library: GraphQLLibrary) {
    if (!shouldInitializeLibraries) {
      return
    }

    if (isBundledLibrary(library)) {
      LOG.error("Trying to replace bundled GraphQL library with identifier: ${library.descriptor.identifier}")
      return
    }

    externalLibraries[library.descriptor.identifier] = library
    attachLibrary(library)
  }

  /**
   * If the specified library is not currently registered, a warning message is logged.
   * Otherwise, the library is removed from the list of external libraries and detached.
   *
   * @param library The external GraphQL library to unregister, containing its descriptor and set of root URLs.
   */
  @RequiresBackgroundThread
  @RequiresReadLockAbsence
  suspend fun unregisterExternalLibrary(library: GraphQLLibrary) {
    if (!shouldInitializeLibraries) {
      return
    }

    if (externalLibraries[library.descriptor.identifier] == null) {
      LOG.warn("Library with id '${library.descriptor.identifier}' is not registered")
      return
    }
    externalLibraries.remove(library.descriptor.identifier)
    detachLibrary(library)
  }

  /**
   * Synchronizes GraphQL libraries in the workspace.
   *
   * This method fetches all available libraries (both bundled and external) and filters them based
   * on their enabled state for the current project.
   * It subsequently updates the libraries in the workspace model with those that are available.
   *
   * The synchronization is skipped in unit test mode if library initialization is disabled.
   * After synchronization, the libraries are accessible through the workspace model.
   *
   * @see getOrCreateLibraries
   * @see updateLibrariesWorkspaceModel
   */
  @RequiresBackgroundThread
  @RequiresReadLockAbsence
  suspend fun syncLibraries() {
    if (!shouldInitializeLibraries) {
      return
    }

    val libraries = getOrCreateLibraries().also { LOG.debug { "GraphQL libraries to sync:\n${it.joinToString("\n")}" } }
    val availableLibraries = libraries.filter { readAction { it.descriptor.isEnabled(project) } }
    updateLibrariesWorkspaceModel(availableLibraries)
  }

  private fun isBundledLibrary(library: GraphQLLibrary): Boolean = library.descriptor.identifier in BUNDLED_LIBRARIES_IDS

  fun getLibraries(vararg attachmentScopes: GraphQLLibraryAttachmentScope): Collection<GraphQLLibrary> =
    sequenceOf(bundledLibraries.values, externalLibraries.values)
      .flatten()
      .filter { it.descriptor.attachmentScope in attachmentScopes }
      .filter { it.descriptor.isEnabled(project) }
      .toList()

  fun getLibraries(): Collection<GraphQLLibrary> =
    getLibraries(*GraphQLLibraryAttachmentScope.ALL)

  fun getLibraryRoots(vararg attachmentScopes: GraphQLLibraryAttachmentScope): Collection<VirtualFile> =
    getLibraries(*attachmentScopes).flatMap { it.sourceRoots }.toSet()

  fun getLibraryRoots(): Collection<VirtualFile> =
    getLibraryRoots(*GraphQLLibraryAttachmentScope.ALL)

  private suspend fun getOrCreateLibraries(): List<GraphQLLibrary> {
    val bundled = BUNDLED_RESOURCE_PATHS.keys.mapNotNull { getOrCreateLibrary(it) }
    val contributed = externalLibraries.values
    return bundled + contributed
  }

  private suspend fun updateLibrariesWorkspaceModel(libraries: Collection<GraphQLLibrary>) {
    LOG.debug { "Attaching GraphQL libraries:\n${libraries.joinToString("\n")}" }
    val workspaceModel = project.serviceAsync<WorkspaceModel>()
    val newEntities = libraries.map { createLibraryEntity(it) }
    val entityStorage = MutableEntityStorage.create().apply {
      newEntities.forEach { addEntity(it) }
    }
    workspaceModel.update("Attaching GraphQL libraries") { storage ->
      storage.replaceBySource({ it is GraphQLLibraryEntitySource }, entityStorage)
    }
  }

  fun findLibrary(libraryDescriptor: GraphQLLibraryDescriptor): GraphQLLibrary? =
    getLibraries().find { it.descriptor == libraryDescriptor }

  fun isLibraryRoot(virtualFile: VirtualFile?): Boolean = virtualFile != null && virtualFile in getLibraryRoots()

  fun isLibraryRoot(virtualFile: VirtualFile?, vararg attachmentScopes: GraphQLLibraryAttachmentScope): Boolean =
    virtualFile != null && virtualFile in getLibraryRoots(*attachmentScopes)

  fun createGlobalScope(project: Project): GlobalSearchScope =
    createScope(project, GraphQLLibraryAttachmentScope.GLOBAL)

  fun createScope(project: Project, vararg attachmentScopes: GraphQLLibraryAttachmentScope): GlobalSearchScope =
    GlobalSearchScope.filesWithLibrariesScope(project, getLibraryRoots(*attachmentScopes))

  private suspend fun attachLibrary(library: GraphQLLibrary) {
    LOG.debug { "Attaching GraphQL library:\n$library" }
    val workspaceModel = project.serviceAsync<WorkspaceModel>()
    val newEntity = createLibraryEntity(library)
    workspaceModel.update("Attaching GraphQL library: ${library.descriptor.identifier}") { storage ->
      storage.addEntity(newEntity)
    }
  }

  private suspend fun detachLibrary(library: GraphQLLibrary) {
    LOG.debug { "Detaching GraphQL library:\n$library" }
    val workspaceModel = project.serviceAsync<WorkspaceModel>()
    val existingEntity = findLibraryEntityById(library) ?: return
    workspaceModel.update("Detaching GraphQL library: ${library.descriptor.identifier}") { storage ->
      storage.removeEntity(existingEntity)
    }
  }

  private suspend fun findLibraryEntityById(library: GraphQLLibrary): GraphQLLibraryEntity? {
    return project.serviceAsync<WorkspaceModel>().currentSnapshot
      .entitiesBySource { it is GraphQLLibraryEntitySource }
      .find { it is GraphQLLibraryEntity && it.identifier == library.descriptor.identifier } as? GraphQLLibraryEntity
  }

  private fun createLibraryEntity(library: GraphQLLibrary) =
    GraphQLLibraryEntity(
      library.descriptor.identifier,
      library.descriptor.displayName,
      library.rootUrls,
      GraphQLLibraryEntitySource
    ) {
      description = library.descriptor.description
      attachmentScope = library.descriptor.attachmentScope
    }

  private suspend fun getOrCreateLibrary(libraryDescriptor: GraphQLLibraryDescriptor): GraphQLLibrary? {
    bundledLibraries[libraryDescriptor]?.let { return it }

    val file = LOG.runAndLogException { resolveBundledLibraryDefinition(libraryDescriptor) }
               ?: LOG.runAndLogException { tryRefreshAndReload(libraryDescriptor) }
               ?: return null

    val fileUrlManager = project.serviceAsync<WorkspaceModel>().getVirtualFileUrlManager()
    val virtualFileUrl = file.toVirtualFileUrl(fileUrlManager)
    return GraphQLLibrary(libraryDescriptor, setOf(virtualFileUrl))
      .also { bundledLibraries[libraryDescriptor] = it }
  }

  private fun tryRefreshAndReload(libraryDescriptor: GraphQLLibraryDescriptor): VirtualFile? {
    val definitionsDirUrl = javaClass.getClassLoader().getResource(DEFINITIONS_RESOURCE_DIR) ?: return null
    val urlParts = URLUtil.splitJarUrl(definitionsDirUrl.file) ?: return null
    val jarPath = PathUtil.toSystemIndependentName(urlParts.first) ?: return null
    LocalFileSystem.getInstance().refreshAndFindFileByPath(jarPath) ?: return null
    val jarFile = JarFileSystem.getInstance().refreshAndFindFileByPath(jarPath + URLUtil.JAR_SEPARATOR) ?: return null
    VfsUtil.refreshAndFindChild(jarFile, DEFINITIONS_RESOURCE_DIR)?.takeIf { it.isDirectory && it.isValid } ?: return null

    return resolveBundledLibraryDefinition(libraryDescriptor)
  }

  private fun resolveBundledLibraryDefinition(descriptor: GraphQLLibraryDescriptor): VirtualFile? {
    val resourceName = BUNDLED_RESOURCE_PATHS[descriptor]
    if (resourceName == null) {
      LOG.error("No resource files found for library: $descriptor")
      return null
    }
    val resource = javaClass.getClassLoader().getResource("$DEFINITIONS_RESOURCE_DIR/$resourceName")
    if (resource == null) {
      LOG.error("Resource not found: $resourceName")
      return null
    }
    return VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.convertFromUrl(resource))?.takeIf { it.isValid }
  }
}
