package com.intellij.lang.jsgraphql.schema

import com.intellij.lang.jsgraphql.GraphQLExecutionMode
import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider
import com.intellij.lang.jsgraphql.installCountingRestarter
import com.intellij.lang.jsgraphql.types.schema.idl.SchemaPrinter
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.reflect.Modifier
import java.util.Collections
import java.util.IdentityHashMap

/**
 * The schema build of the synchronous mode: a cache miss builds the schema inline under the read action of the caller,
 * with no background job, no timeout, no fallback schema and no daemon restart.
 */
@TestDataPath($$"$PROJECT_ROOT/plugins/js-graphql/tests/testData/graphql/schema/providerMode")
@TestApplication
class GraphQLSchemaProviderModeTest {
  private val graphQLFixture by graphQLTestFixture("/schema/providerMode", loadConfiguration = false)

  private val project: Project get() = graphQLFixture.project

  private val provider: GraphQLSchemaProvider get() = GraphQLSchemaProvider.getInstance(project)

  @Test
  fun testBuiltInlineIgnoringTimeout(): Unit = timeoutRunBlocking {
    assertTrue(GraphQLExecutionMode.isSynchronous)
    Registry.get("graphql.schema.build.timeout").setValue(1, graphQLFixture.testRootDisposable)
    val restarter = installCountingRestarter(project, graphQLFixture.testRootDisposable)
    copyProject()
    val query = findPsiFile("query.graphql")

    // the asynchronous mode returns the fallback schema at once here
    val cachedSchemaInfo = readAction { provider.getCachedSchemaInfo(query) }
    assertFullSchema(cachedSchemaInfo)

    val schemaInfo = readAction { provider.getSchemaInfo(query) }
    assertSame(cachedSchemaInfo, schemaInfo)
    assertEquals(0, restarter.count.get())
  }

  @Test
  fun testCacheHitByStamp(): Unit = timeoutRunBlocking {
    val restarter = installCountingRestarter(project, graphQLFixture.testRootDisposable)
    copyProject()
    val scope = getScope()
    val tracker = GraphQLSchemaContentTracker.getInstance(project)

    val first = readAction { provider.getSchemaInfo(scope) }
    val second = readAction { provider.getSchemaInfo(scope) }
    assertSame(first, second)

    val stamp = tracker.modificationCount
    tracker.update()
    assertThat(tracker.modificationCount).isGreaterThan(stamp)

    val third = readAction { provider.getSchemaInfo(scope) }
    assertNotSame(first, third)
    assertFullSchema(third)
    assertEquals(printSchema(first), printSchema(third))
    assertSame(third, readAction { provider.getSchemaInfo(scope) })
    assertEquals(0, restarter.count.get())
  }

  @Test
  fun testConcurrentBuildsAreEqual(): Unit = timeoutRunBlocking {
    val restarter = installCountingRestarter(project, graphQLFixture.testRootDisposable)
    copyProject()
    val scope = getScope()
    GraphQLSchemaContentTracker.getInstance(project).update()

    val start = CompletableDeferred<Unit>()
    val schemas = coroutineScope {
      val builds = List(2) {
        async(Dispatchers.Default) {
          start.await()
          readAction { provider.getSchemaInfo(scope) }
        }
      }
      start.complete(Unit)
      builds.awaitAll()
    }

    schemas.forEach { assertFullSchema(it) }
    assertEquals(printSchema(schemas[0]), printSchema(schemas[1]))
    val cached = readAction { provider.getSchemaInfo(scope) }
    assertTrue(schemas.any { it === cached })
    assertEquals(0, restarter.count.get())
  }

  @Test
  fun testNoDaemonRestart(): Unit = timeoutRunBlocking {
    val restarter = installCountingRestarter(project, graphQLFixture.testRootDisposable)
    copyProject()
    val scope = getScope()
    val tracker = GraphQLSchemaContentTracker.getInstance(project)

    repeat(3) {
      tracker.update()
      assertFullSchema(readAction { provider.getSchemaInfo(scope) })
    }
    assertEquals(0, restarter.count.get())
  }

  /**
   * A schema with no PSI can be shared by several projects or analyzer contexts.
   */
  @Test
  fun testSchemaHoldsNoPsi(): Unit = timeoutRunBlocking {
    copyProject()
    val scope = getScope()
    val schemaInfo = readAction { provider.getSchemaInfo(scope) }
    assertFullSchema(schemaInfo)

    assertNull(findReachablePsiElement(schemaInfo))
    // the walk finds PSI behind a collection and a field
    assertNotNull(findReachablePsiElement(listOf(Pair("query", findPsiFile("query.graphql")))))
  }

  private fun assertFullSchema(schemaInfo: GraphQLSchemaInfo) {
    val schema = schemaInfo.schema
    val queryType = schema.queryType
    assertNotNull(queryType)
    assertNotNull(queryType!!.getFieldDefinition("user"))
    assertNotNull(queryType.getFieldDefinition("users"))
    assertNotNull(schema.getObjectType("User"))
    assertNotNull(schema.getType("Node"))
    assertNotNull(schema.getType("Role"))
    assertNotNull(schema.getType("UserFilter"))
  }

  private suspend fun printSchema(schemaInfo: GraphQLSchemaInfo): String =
    readAction { SchemaPrinter(project, SchemaPrinter.Options.defaultOptions()).print(schemaInfo.schema) }

  private suspend fun copyProject() {
    edtWriteAction { graphQLFixture.fixture.copyDirectoryToProject("project", "") }
    IndexingTestUtil.waitUntilIndexesAreReady(project)
  }

  private suspend fun getScope(): GlobalSearchScope {
    val query = findPsiFile("query.graphql")
    val scope = readAction { GraphQLScopeProvider.getInstance(project).getResolveScope(query, true) }
    assertThat(scope).isNotSameAs(GlobalSearchScope.EMPTY_SCOPE)
    return scope
  }

  private suspend fun findPsiFile(path: String): PsiFile {
    val file = graphQLFixture.fixture.findFileInTempDir(path)
    return readAction { PsiManager.getInstance(project).findFile(file) }!!
  }
}

private const val MAX_VISITED_OBJECTS = 1_000_000

/**
 * Walks the object graph of [root] by reflection and returns the path to the first reachable [PsiElement], or `null`.
 * The walk reads the fields of the non-JDK classes only; it visits the elements of arrays, collections and maps of any class.
 */
private fun findReachablePsiElement(root: Any): String? {
  val visited = Collections.newSetFromMap(IdentityHashMap<Any, Boolean>())
  val queue = ArrayDeque<Pair<Any, String>>()
  queue.add(root to root.javaClass.simpleName)
  while (queue.isNotEmpty()) {
    val (obj, path) = queue.removeFirst()
    if (!visited.add(obj)) {
      continue
    }
    check(visited.size <= MAX_VISITED_OBJECTS) { "The schema graph is too large: more than $MAX_VISITED_OBJECTS objects, last at $path" }
    if (obj is PsiElement) {
      return "$path: ${obj.javaClass.name}"
    }

    val objClass = obj.javaClass
    when {
      objClass.isArray -> {
        if (!objClass.componentType.isPrimitive) {
          (obj as Array<*>).forEachIndexed { index, element -> element?.let { queue.add(it to "$path[$index]") } }
        }
      }
      obj is Map<*, *> -> obj.entries.forEach { (key, value) ->
        key?.let { queue.add(it to "$path{key}") }
        value?.let { queue.add(it to "$path{$key}") }
      }
      obj is Collection<*> -> obj.forEachIndexed { index, element -> element?.let { queue.add(it to "$path[$index]") } }
    }

    var fieldsClass: Class<*>? = objClass
    while (fieldsClass != null && !isJdkClass(fieldsClass)) {
      for (field in fieldsClass.declaredFields) {
        if (Modifier.isStatic(field.modifiers) || field.type.isPrimitive) {
          continue
        }
        field.isAccessible = true
        field.get(obj)?.let { queue.add(it to "$path.${field.name}") }
      }
      fieldsClass = fieldsClass.superclass
    }
  }
  return null
}

private fun isJdkClass(aClass: Class<*>): Boolean {
  val name = aClass.name
  return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jdk.") ||
         name.startsWith("sun.") || name.startsWith("com.sun.")
}
