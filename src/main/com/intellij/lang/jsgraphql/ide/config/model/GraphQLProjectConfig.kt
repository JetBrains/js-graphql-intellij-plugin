package com.intellij.lang.jsgraphql.ide.config.model

import com.intellij.lang.jsgraphql.asSafely
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLEnvironmentSnapshot
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLExpandVariableContext
import com.intellij.lang.jsgraphql.ide.config.env.expandVariables
import com.intellij.lang.jsgraphql.ide.config.isLegacyConfig
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLConfigKeys
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawEndpoint
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawProjectConfig
import com.intellij.lang.jsgraphql.ide.config.parseMap
import com.intellij.lang.jsgraphql.ide.config.scope.GraphQLConfigGlobMatcher
import com.intellij.lang.jsgraphql.ide.config.scope.GraphQLConfigSchemaScope
import com.intellij.lang.jsgraphql.ide.config.scope.GraphQLConfigScope
import com.intellij.lang.jsgraphql.ide.config.scope.GraphQLFileMatcherCache
import com.intellij.lang.jsgraphql.ide.introspection.remote.GraphQLRemoteSchemasRegistry
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeDependency
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider
import com.intellij.lang.jsgraphql.psi.getPhysicalVirtualFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager


// NOTE: update hashCode and equals when changed
class GraphQLProjectConfig(
  private val project: Project,
  val name: String,
  val rawData: GraphQLRawProjectConfig,
  val defaultData: GraphQLRawProjectConfig?,
  val environment: GraphQLEnvironmentSnapshot,
  val rootConfig: GraphQLConfig,
) {
  private val generatedSourcesManager = GraphQLGeneratedSourcesManager.getInstance(project)
  private val remoteSchemasRegistry = GraphQLRemoteSchemasRegistry.getInstance(project)

  val dir: VirtualFile = rootConfig.dir

  val file: VirtualFile? = rootConfig.file

  val isRootEmpty: Boolean = rootConfig.isEmpty

  val isLegacy = isLegacyConfig(file)

  val schema: List<GraphQLSchemaPointer> = (rawData.schema ?: defaultData?.schema)?.map {
    GraphQLSchemaPointer(project, dir, it, isLegacy, environment).also { pointer ->
      if (pointer.isRemote && !pointer.outputPath.isNullOrEmpty()) {
        remoteSchemasRegistry.associate(pointer.outputPath, file?.path ?: dir.path)
      }
    }
  } ?: emptyList()

  val documents: List<String> =
    (rawData.documents ?: defaultData?.documents)?.let { expandVariables(it, expandContext) } ?: emptyList()

  val extensions: Map<String, Any?> = buildMap {
    defaultData?.extensions?.let { putAll(it) }
    rawData.extensions?.let { putAll(it) }
  }

  val include: List<String> =
    (rawData.include ?: defaultData?.include)?.let { expandVariables(it, expandContext) } ?: emptyList()

  val exclude: List<String> =
    (rawData.exclude ?: defaultData?.exclude)?.let { expandVariables(it, expandContext) } ?: emptyList()

  val endpoints: List<GraphQLConfigEndpoint> by lazy { buildEndpoints() }

  val isDefault = name == GraphQLConfig.DEFAULT_PROJECT

  // need to create a new one for each invocation, because it has its own state
  private val expandContext
    get() = GraphQLExpandVariableContext(project, dir, isLegacy, environment)

  private val outOfScopePaths: List<String> by lazy {
    schema.asSequence()
      .mapNotNull { it.filePath }
      .filter { it.startsWith("..") }
      .map { FileUtil.toCanonicalPath(FileUtil.join(dir.path, FileUtil.toSystemIndependentName(it))) }
      .toList()
  }

  private val matchingCache = GraphQLFileMatcherCache.newInstance(project)

  private val matchingSchemaCache = GraphQLFileMatcherCache.newInstance(project)

  private val baseScope
    get() = GlobalSearchScope
      .allScope(project)
      .union(generatedSourcesManager.createGeneratedSourcesScope())
      .union(remoteSchemasRegistry.createRemoteIntrospectionScope())

  private val scopeCached: CachedValue<GlobalSearchScope> =
    CachedValuesManager.getManager(project).createCachedValue {
      CachedValueProvider.Result.create(
        GraphQLScopeProvider.createScope(project, GraphQLConfigScope(project, baseScope, this), dir),
        GraphQLScopeDependency.getInstance(project),
      )
    }

  val scope: GlobalSearchScope
    get() = scopeCached.value

  private val schemaScopeCached: CachedValue<GlobalSearchScope> =
    CachedValuesManager.getManager(project).createCachedValue {
      CachedValueProvider.Result.create(
        GraphQLScopeProvider.createScope(project, GraphQLConfigSchemaScope(project, baseScope, this), dir),
        GraphQLScopeDependency.getInstance(project),
      )
    }

  val schemaScope: GlobalSearchScope
    get() = schemaScopeCached.value

  fun matches(context: PsiFile): Boolean {
    return getPhysicalVirtualFile(context)?.let { matches(it) } ?: false
  }

  fun matches(virtualFile: VirtualFile): Boolean {
    return matchingCache.value.match(virtualFile, ::matchesImpl)
  }

  private fun matchesImpl(virtualFile: VirtualFile): Boolean {
    if (generatedSourcesManager.isGeneratedFile(virtualFile)) {
      return generatedSourcesManager.getSourceFile(virtualFile)?.let { matches(it) } ?: false
    }

    val isSchemaOrDocument = sequenceOf(schema, documents).any { matchPattern(virtualFile, it) }
    if (isSchemaOrDocument) {
      return true
    }

    return isIncluded(virtualFile)
  }

  fun matchesSchema(context: PsiFile): Boolean {
    return getPhysicalVirtualFile(context)?.let { matchesSchema(it) } ?: false
  }

  fun matchesSchema(virtualFile: VirtualFile): Boolean {
    return matchingSchemaCache.value.match(virtualFile, ::matchesSchemaImpl)
  }

  private fun matchesSchemaImpl(virtualFile: VirtualFile): Boolean {
    if (generatedSourcesManager.isGeneratedFile(virtualFile)) {
      return generatedSourcesManager.getSourceFile(virtualFile)?.let { matchesSchema(it) } ?: false
    }

    val isSchema = schema.any { matchPattern(virtualFile, it) }
    if (isSchema) {
      return true
    }

    return isIncluded(virtualFile)
  }

  private fun isIncluded(virtualFile: VirtualFile): Boolean {
    if (isIncludedOutOfScopeFile(virtualFile)) {
      return true
    }

    val isExcluded = if (exclude.isNotEmpty()) matchPattern(virtualFile, exclude) else false
    if (isExcluded) {
      return false
    }

    return if (include.isNotEmpty()) matchPattern(virtualFile, include) else false
  }

  fun isIncludedOutOfScopeFile(virtualFile: VirtualFile): Boolean {
    return outOfScopePaths.any { FileUtil.pathsEqual(it, virtualFile.path) }
  }

  private fun matchPattern(candidate: VirtualFile, pointer: Any?): Boolean {
    return when (pointer) {
      is List<*> -> pointer.any { matchPattern(candidate, it) }
      is String -> GraphQLConfigGlobMatcher.getInstance(project).matches(candidate, pointer, dir)
      is GraphQLSchemaPointer -> if (pointer.isRemote) {
        FileUtil.pathsEqual(candidate.path, pointer.outputPath)
      }
      else {
        matchPattern(candidate, pointer.globPath)
      }

      else -> false
    }
  }

  private fun buildEndpoints(): List<GraphQLConfigEndpoint> {
    val schemaEndpoints = schema
      .filter { it.isRemote }
      .map {
        GraphQLConfigEndpoint(
          project,
          GraphQLRawEndpoint(url = it.rawData.pattern, headers = it.rawData.headers),
          dir,
          isLegacy,
          environment,
          it.rawData,
          this,
        )
      }

    val localIntrospectionPath = schema.firstOrNull()?.takeUnless { it.isRemote }?.rawData
    val extensionEndpoints =
      extensions[GraphQLConfigKeys.EXTENSION_ENDPOINTS].asSafely<Map<*, *>>()
        ?.mapNotNull { (key: Any?, value: Any?) ->
          val endpointName = key as? String ?: return@mapNotNull null

          when (value) {
            is String -> {
              GraphQLRawEndpoint(
                endpointName,
                value as String?,
                emptyMap(),
                false,
              )
            }

            is Map<*, *> -> {
              val url = value[GraphQLConfigKeys.EXTENSION_ENDPOINT_URL]
              if (url is String) {
                GraphQLRawEndpoint(
                  endpointName,
                  url,
                  parseMap(value[GraphQLConfigKeys.HEADERS]) ?: emptyMap(),
                  value[GraphQLConfigKeys.INTROSPECT] as Boolean?
                )
              }
              else {
                null
              }
            }

            else -> null
          }
        }
        ?.map {
          GraphQLConfigEndpoint(project, it, dir, isLegacy, environment, localIntrospectionPath, this)
        }
      ?: emptyList()

    return schemaEndpoints + extensionEndpoints
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as GraphQLProjectConfig

    if (project != other.project) return false
    if (name != other.name) return false
    if (rawData != other.rawData) return false
    if (defaultData != other.defaultData) return false
    if (environment != other.environment) return false
    if (dir != other.dir) return false
    if (file != other.file) return false
    return isRootEmpty == other.isRootEmpty
  }

  override fun hashCode(): Int {
    var result = project.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + rawData.hashCode()
    result = 31 * result + (defaultData?.hashCode() ?: 0)
    result = 31 * result + environment.hashCode()
    result = 31 * result + dir.hashCode()
    result = 31 * result + (file?.hashCode() ?: 0)
    result = 31 * result + isRootEmpty.hashCode()
    return result
  }
}
