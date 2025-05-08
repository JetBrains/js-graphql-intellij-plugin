package com.intellij.graphql.javascript.workspace

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigListener
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLSchemaPointer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.storage.MutableEntityStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service(Service.Level.PROJECT)
class GraphQLNodeModulesLibraryUpdater(private val project: Project, @VisibleForTesting val cs: CoroutineScope) {

  private val lock = ReentrantLock()
  private val roots = mutableSetOf<String>() // lock

  private val configProvider = GraphQLConfigProvider.getInstance(project)

  fun updateNodeModulesEntity() {
    cs.launch {
      val prevRoots = lock.withLock { roots.toSet() }
      val newRoots = configProvider.getAllConfigs(true)
        .asSequence()
        .flatMap { it.getProjects().values }
        .flatMap { config ->
          config.schema.asSequence().map { schemaPointer -> schemaPointer.pattern }
            .plus(config.documents)
            .plus(config.include)
            .filter { it.contains("node_modules") }
            .mapNotNull { GraphQLSchemaPointer.createPathForLocal(it, config.dir) }
            .map {
              if (ApplicationManager.getApplication().isUnitTestMode)
                VirtualFileManager.constructUrl(config.dir.fileSystem.protocol, it)
              else
                VfsUtil.pathToUrl(it)
            }
        }
        .toSet()

      if (newRoots != prevRoots) {
        lock.withLock {
          roots.clear()
          roots.addAll(newRoots)
        }

        attachEntity(newRoots)
      }
    }
  }

  private suspend fun attachEntity(roots: Collection<String>) {
    val virtualFileUrlManager = WorkspaceModel.getInstance(project).getVirtualFileUrlManager()
    val rootUrls = roots.mapTo(mutableSetOf()) { virtualFileUrlManager.getOrCreateFromUrl(it) }
    val newModulesEntity = GraphQLNodeModulesEntity(rootUrls, GraphQLNodeModulesEntitySource)
    val entityStorage = MutableEntityStorage.create().apply {
      addEntity(newModulesEntity)
    }
    WorkspaceModel.getInstance(project).update("Attach GraphQL node modules") { storage ->
      storage.replaceBySource({ it is GraphQLNodeModulesEntitySource }, entityStorage)
    }
  }

  class ConfigListener(private val project: Project) : GraphQLConfigListener {
    override fun onConfigurationChanged() {
      getInstance(project).updateNodeModulesEntity()
    }
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): GraphQLNodeModulesLibraryUpdater = project.service()
  }
}