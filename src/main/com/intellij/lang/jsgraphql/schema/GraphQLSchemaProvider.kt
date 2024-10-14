/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.lang.jsgraphql.GraphQLFileType
import com.intellij.lang.jsgraphql.awaitFuture
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider
import com.intellij.lang.jsgraphql.ide.search.GraphQLPsiSearchHelper
import com.intellij.lang.jsgraphql.types.GraphQLException
import com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry
import com.intellij.lang.jsgraphql.types.schema.idl.UnExecutableSchemaGenerator
import com.intellij.lang.jsgraphql.types.schema.validation.InvalidSchemaException
import com.intellij.lang.jsgraphql.types.schema.validation.SchemaValidator
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.util.BackgroundTaskUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.containers.ContainerUtil
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.measureTimedValue

private const val BUILD_TIMEOUT_MS = 500L

/**
 * GitHub schema evaluation takes approximately 1 second in total for both type definitions and the schema combined.
 * If it exceeds the specified time limit, it indicates a serious issue with the code and not the timeout itself.
 */
private const val BUILD_TIMEOUT_TESTS_MS = 3000L

@Service(Service.Level.PROJECT)
class GraphQLSchemaProvider(private val project: Project) : Disposable {

  companion object {
    @JvmStatic
    fun getInstance(project: Project) = project.service<GraphQLSchemaProvider>()

    private val LOG = logger<GraphQLSchemaProvider>()
  }

  private val totalCallsCount = AtomicLong()
  private val startedTasksCount = AtomicLong()
  private val cacheHitsCount = AtomicLong()

  // can throw PCE, so we need to postpone initialization not to break the plugin class loading
  private val emptySchema = lazy(LazyThreadSafetyMode.PUBLICATION) {
    GraphQLSchema.newSchema().query(GraphQLObjectType.newObject().name("Query").build()).build()
  }

  private val emptySchemaInfo = lazy(LazyThreadSafetyMode.PUBLICATION) {
    GraphQLSchemaInfo(emptySchema.value, emptyList(), GraphQLRegistryInfo(TypeDefinitionRegistry(), false))
  }

  private val scopeToTask = ConcurrentHashMap<GlobalSearchScope, SchemaComputation>()
  private val scopeToSchemaCache: ConcurrentMap<GlobalSearchScope, SchemaEntry> = ContainerUtil.createConcurrentSoftKeySoftValueMap()

  @RequiresReadLock
  fun getSchemaInfo(context: PsiElement?): GraphQLSchemaInfo {
    return getSchemaInfo(GraphQLScopeProvider.getInstance(project).getResolveScope(context, true))
  }

  @RequiresReadLock
  fun getSchemaInfo(scope: GlobalSearchScope): GraphQLSchemaInfo {
    totalCallsCount.incrementAndGet()

    val currentModificationStamp = GraphQLSchemaContentTracker.getInstance(project).modificationCount

    val currentSchemaEntry = scopeToSchemaCache[scope]
    if (currentSchemaEntry?.modificationStamp == currentModificationStamp) {
      cacheHitsCount.incrementAndGet()
      if (LOG.isTraceEnabled) {
        LOG.trace("Schema from cache returned ($currentModificationStamp)")
        printStats()
      }
      return currentSchemaEntry.schemaInfo
    }

    val fallbackSchema = currentSchemaEntry?.schemaInfo ?: emptySchemaInfo.value
    var computation = scheduleComputationIfNeeded(scope, currentModificationStamp)
    computation.ensureStarted()

    val future = computation.getFuture()
    checkNotNull(future) { "Schema computation was not started" }
    try {
      awaitFuture(future, buildTimeout)
    }
    catch (e: ProcessCanceledException) {
      throw e
    }
    catch (e: Exception) {
      LOG.warn("Schema computation waiting completed with exception (${computation.startModificationStamp})", e)
    }

    printStats()
    return scopeToSchemaCache[scope]?.schemaInfo ?: fallbackSchema
  }

  private fun printStats() {
    if (LOG.isTraceEnabled) {
      val calls = totalCallsCount.get()
      val tasks = startedTasksCount.get()
      val hits = cacheHitsCount.get()
      LOG.trace("Schema build stats: total calls=$calls, tasks started=$tasks, cache hits=$hits")
    }
  }

  private val buildTimeout: Long
    get() {
      if (ApplicationManager.getApplication().isUnitTestMode) {
        return BUILD_TIMEOUT_TESTS_MS
      }

      return Registry.intValue("graphql.schema.build.timeout", BUILD_TIMEOUT_MS.toInt()).toLong()
    }

  private fun scheduleComputationIfNeeded(scope: GlobalSearchScope, currentModificationStamp: Long): SchemaComputation {
    var computation = scopeToTask[scope]
    if (computation != null) {
      if (computation.startModificationStamp != currentModificationStamp) {
        // cancel and start a new one
        val future = computation.getFuture()
        if (future != null && !future.isDone) {
          LOG.debug("Cancelling schema computation: old=${computation.startModificationStamp}, new=${currentModificationStamp}")
          computation.indicator.cancel()
          awaitFuture(future, 50) // wait for a proper cancellation
          if (!future.isDone) {
            future.cancel(false)
          }
        }

        scopeToTask.remove(scope, computation)
        computation = null
      }
      else {
        LOG.debug("Join already in-progress schema computation (${computation.startModificationStamp})")
      }
    }

    if (computation == null) {
      val scheduledComputation = SchemaComputation(scope, currentModificationStamp, EmptyProgressIndicator())
      val currentComputation = scopeToTask.putIfAbsent(scope, scheduledComputation)
      if (currentComputation != null) {
        // concurrently started a task already
        LOG.debug("Concurrent schema computation: old=$currentModificationStamp, new=${currentComputation.startModificationStamp}")
        computation = currentComputation
      }
      else {
        startedTasksCount.incrementAndGet()
        LOG.debug("New schema computation scheduled ($currentModificationStamp)")
        computation = scheduledComputation
      }
    }
    return computation
  }

  private fun computeSchema(scope: GlobalSearchScope, modificationStamp: Long): SchemaEntry {
    ProgressManager.checkCanceled()

    val registryInfo = runReadAction { getRegistryInfo(scope, modificationStamp) }
    val schemaInfo = try {
      LOG.info("Schema build started ($modificationStamp)")
      val (schema, duration) = measureTimedValue {
        val schema =
          UnExecutableSchemaGenerator.makeUnExecutableSchema(registryInfo.typeDefinitionRegistry)
        val validationErrors = SchemaValidator().validateSchema(schema)
        val errors = if (validationErrors.isEmpty())
          emptyList()
        else
          listOf<GraphQLException>(InvalidSchemaException(validationErrors))
        GraphQLSchemaInfo(schema, errors, registryInfo)
      }
      LOG.info("Schema was built in ${duration} ($modificationStamp)")
      schema
    }
    catch (e: ProcessCanceledException) {
      LOG.info("Schema build cancelled ($modificationStamp)")
      throw e
    }
    catch (e: Exception) {
      LOG.error("Schema build error ($modificationStamp): ", e) // should never happen

      GraphQLSchemaInfo(
        emptySchema.value,
        listOfNotNull(e as? GraphQLException ?: GraphQLException(e)),
        registryInfo
      )
    }

    return SchemaEntry(schemaInfo, modificationStamp)
  }

  /**
   * @param context pass null for a global scope
   * @return registry for provided scope
   */
  @Suppress("unused")
  private fun getRegistryInfo(context: PsiElement?): GraphQLRegistryInfo {
    val currentModificationStamp = GraphQLSchemaContentTracker.getInstance(project).modificationCount
    return getRegistryInfo(GraphQLScopeProvider.getInstance(project).getResolveScope(context, true), currentModificationStamp)
  }

  private fun getRegistryInfo(schemaScope: GlobalSearchScope, modificationStamp: Long): GraphQLRegistryInfo {
    ProgressManager.checkCanceled()

    LOG.info("Registry build started ($modificationStamp)")

    val (registry, duration) = measureTimedValue {
      val processor = GraphQLSchemaDocumentProcessor()

      // GraphQL files
      FileTypeIndex.processFiles(
        GraphQLFileType.INSTANCE,
        {
          val psiFile = PsiManager.getInstance(project).findFile(it)
          if (psiFile != null) processor.process(psiFile) else true
        },
        GlobalSearchScope.getScopeRestrictedByFileTypes(schemaScope, GraphQLFileType.INSTANCE)
      )

      if (!processor.isTooComplex) {
        GraphQLPsiSearchHelper.getInstance(project).processInjectedGraphQLFiles(project, schemaScope, processor)
      }

      processor.build()
    }
    LOG.info("Registry was built in ${duration} ($modificationStamp)")
    return registry
  }

  override fun dispose() {
  }

  private inner class SchemaComputation(val scope: GlobalSearchScope, val startModificationStamp: Long, val indicator: ProgressIndicator) {
    private val lock = Any()
    private var future: CompletableFuture<Void>? = null // lock

    fun ensureStarted() {
      synchronized(lock) {
        if (future != null) {
          return
        }
        else {
          future = CompletableFuture.runAsync(
            {
              BackgroundTaskUtil.runUnderDisposeAwareIndicator(this@GraphQLSchemaProvider, {
                val schemaEntry = computeSchema(scope, startModificationStamp)

                ProgressManager.checkCanceled()
                scopeToSchemaCache.put(scope, schemaEntry)

                val application = ApplicationManager.getApplication()
                if (!application.isUnitTestMode) {
                  application.invokeLater(
                    {
                      ResolveCache.getInstance(project).clearCache(true)
                      DaemonCodeAnalyzer.getInstance(project).restart()
                    },
                    ModalityState.nonModal(),
                    project.disposed
                  )
                }
              }, indicator)
            },
            AppExecutorUtil.getAppExecutorService()
          ).whenComplete { _, _ -> scopeToTask.remove(scope, this) }
        }
      }
    }

    fun getFuture(): CompletableFuture<Void>? {
      synchronized(lock) {
        return future
      }
    }
  }

  private class SchemaEntry(val schemaInfo: GraphQLSchemaInfo, val modificationStamp: Long)
}
