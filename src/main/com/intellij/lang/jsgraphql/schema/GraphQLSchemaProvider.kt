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
import com.intellij.lang.jsgraphql.schema.builder.GraphQLCompositeRegistry
import com.intellij.lang.jsgraphql.types.GraphQLException
import com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry
import com.intellij.lang.jsgraphql.types.schema.idl.UnExecutableSchemaGenerator
import com.intellij.lang.jsgraphql.types.schema.validation.InvalidSchemaException
import com.intellij.lang.jsgraphql.types.schema.validation.SchemaValidator
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.blockingContext
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.containers.ContainerUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.launch
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
class GraphQLSchemaProvider(private val project: Project, private val coroutineScope: CoroutineScope) : Disposable {

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

  fun getSchemaInfo(context: PsiElement?): GraphQLSchemaInfo {
    val scope = runReadAction { GraphQLScopeProvider.getInstance(project).getResolveScope(context, true) }
    return getSchemaInfo(scope)
  }

  fun getSchemaInfo(scope: GlobalSearchScope): GraphQLSchemaInfo {
    return getFromCacheOrSchedule(scope)
  }

  fun getCachedSchemaInfo(context: PsiElement?): GraphQLSchemaInfo {
    val scope = runReadAction { GraphQLScopeProvider.getInstance(project).getResolveScope(context, true) }
    return getCachedSchemaInfo(scope)
  }

  fun getCachedSchemaInfo(scope: GlobalSearchScope): GraphQLSchemaInfo {
    return getFromCacheOrSchedule(scope, wait = false)
  }

  private fun getFromCacheOrSchedule(scope: GlobalSearchScope, wait: Boolean = true): GraphQLSchemaInfo {
    totalCallsCount.incrementAndGet()

    val currentModificationStamp = GraphQLSchemaContentTracker.getInstance(project).modificationCount

    val currentSchemaEntry = scopeToSchemaCache[scope]
    if (currentSchemaEntry?.modificationStamp == currentModificationStamp) {
      cacheHitsCount.incrementAndGet()
      if (LOG.isTraceEnabled) {
        LOG.trace { "Schema from cache returned (scope=${scope.scopeId}, stamp=$currentModificationStamp)" }
        printStats()
      }
      return currentSchemaEntry.schemaInfo
    }

    val fallbackSchema = currentSchemaEntry?.schemaInfo ?: emptySchemaInfo.value
    var computation = scheduleComputationIfNeeded(scope, currentModificationStamp).apply { ensureStarted() }

    val job = computation.getJob()
    checkNotNull(job) { "Schema computation was not started (scope=${scope.scopeId}, stamp=${computation.startModificationStamp})" }
    try {
      awaitFuture(job.asCompletableFuture(), if (wait) buildTimeout else 0)
    }
    catch (e: ProcessCanceledException) {
      throw e
    }
    catch (e: Exception) {
      LOG.warn("Schema computation waiting completed with exception (scope=${scope.scopeId}, stamp=${computation.startModificationStamp})", e)
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
      val job = computation.getJob()
      if (computation.startModificationStamp != currentModificationStamp || (job != null && job.isCancelled)) {
        // cancel and start a new one
        if (job != null) {
          if (!job.isCompleted) {
            LOG.debug { "Cancelling schema computation (scope=${scope.scopeId}, old=${computation.startModificationStamp}, new=${currentModificationStamp})" }
            job.cancel()
          }
          else if (job.isCancelled) {
            LOG.debug { "Restarting already cancelled job (scope=${scope.scopeId}, stamp=${currentModificationStamp})" }
          }
        }

        scopeToTask.remove(scope, computation)
        computation = null
      }
      else {
        LOG.debug { "Join already in-progress schema computation (scope=${scope.scopeId}, stamp=${computation.startModificationStamp})" }
      }
    }

    if (computation == null) {
      val scheduledComputation = SchemaComputation(scope, currentModificationStamp)
      val currentComputation = scopeToTask.putIfAbsent(scope, scheduledComputation)
      if (currentComputation != null) {
        // concurrently started a task already
        LOG.debug { "Concurrent schema computation (scope=${scope.scopeId}, old=$currentModificationStamp, new=${currentComputation.startModificationStamp})" }
        computation = currentComputation
      }
      else {
        startedTasksCount.incrementAndGet()
        LOG.debug { "New schema computation scheduled (scope=${scope.scopeId}, stamp=$currentModificationStamp)" }
        computation = scheduledComputation
      }
    }
    return computation
  }

  private suspend fun computeSchema(scope: GlobalSearchScope, modificationStamp: Long): SchemaEntry {
    checkCanceled()

    val registryInfo = getRegistryInfo(scope, modificationStamp)
    val schemaInfo = try {
      LOG.debug { "Schema build started (scope=${scope.scopeId}, stamp=$modificationStamp)" }
      val (schema, duration) = blockingContext {
        measureTimedValue {
          val schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(registryInfo.typeDefinitionRegistry)
          val validationErrors = SchemaValidator().validateSchema(schema)
          val errors = if (validationErrors.isEmpty())
            emptyList()
          else
            listOf<GraphQLException>(InvalidSchemaException(validationErrors))
          GraphQLSchemaInfo(schema, errors, registryInfo)
        }
      }
      LOG.info("Schema was built in ${duration} (scope=${scope.scopeId}, stamp=$modificationStamp)")
      schema
    }
    catch (e: CancellationException) {
      LOG.info("Schema build cancelled (scope=${scope.scopeId}, stamp=$modificationStamp)")
      throw e
    }
    catch (e: Exception) {
      LOG.error("Schema build error (scope=${scope.scopeId}, stamp=$modificationStamp): ", e) // should never happen

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
  private suspend fun getRegistryInfo(context: PsiElement?): GraphQLRegistryInfo {
    val currentModificationStamp = GraphQLSchemaContentTracker.getInstance(project).modificationCount
    return getRegistryInfo(GraphQLScopeProvider.getInstance(project).getResolveScope(context, true), currentModificationStamp)
  }

  private suspend fun getRegistryInfo(scope: GlobalSearchScope, modificationStamp: Long): GraphQLRegistryInfo {
    checkCanceled()

    LOG.debug { "Registry build started (scope=${scope.scopeId}, stamp=$modificationStamp)" }
    val (registry, duration) = measureTimedValue {
      val documentsProcessor = smartReadAction(project) { processSchemaDocuments(scope) }

      blockingContext {
        val compositeRegistry = GraphQLCompositeRegistry()
        documentsProcessor.documents.forEach {
          compositeRegistry.addFromDocument(it)
        }
        GraphQLRegistryInfo(compositeRegistry.build(), documentsProcessor.isTooComplex)
      }
    }
    LOG.info("Registry was built in ${duration} (scope=${scope.scopeId}, stamp=$modificationStamp)")
    return registry
  }

  private fun processSchemaDocuments(scope: GlobalSearchScope): GraphQLSchemaDocumentProcessor {
    val processor = GraphQLSchemaDocumentProcessor()

    FileTypeIndex.processFiles(
      GraphQLFileType.INSTANCE,
      {
        val psiFile = PsiManager.getInstance(project).findFile(it)
        if (psiFile != null) processor.process(psiFile) else true
      },
      GlobalSearchScope.getScopeRestrictedByFileTypes(scope, GraphQLFileType.INSTANCE)
    )

    if (!processor.isTooComplex) {
      GraphQLPsiSearchHelper.getInstance(project).processInjectedGraphQLFiles(project, scope, processor)
    }

    return processor
  }

  override fun dispose() {
  }

  private inner class SchemaComputation(val scope: GlobalSearchScope, val startModificationStamp: Long) {
    private val lock = Any()
    private var job: Job? = null // lock

    fun ensureStarted() {
      synchronized(lock) {
        if (job != null) {
          return
        }
        else {
          job = coroutineScope.launch {
            val schemaEntry = computeSchema(scope, startModificationStamp)

            checkCanceled()
            scopeToSchemaCache.put(scope, schemaEntry)
            scopeToTask.remove(scope, this@SchemaComputation)

            ResolveCache.getInstance(project).clearCache(true)
            if (!ApplicationManager.getApplication().isUnitTestMode) {
              DaemonCodeAnalyzer.getInstance(project).restart()
            }
            project.messageBus.syncPublisher(GraphQLSchemaCacheChangeListener.TOPIC).onSchemaCacheChanged()
          }
        }
      }
    }

    fun getJob(): Job? {
      synchronized(lock) {
        return job
      }
    }
  }

  private class SchemaEntry(val schemaInfo: GraphQLSchemaInfo, val modificationStamp: Long)

  private val GlobalSearchScope.scopeId: String
    get() = hashCode().toString()
}
