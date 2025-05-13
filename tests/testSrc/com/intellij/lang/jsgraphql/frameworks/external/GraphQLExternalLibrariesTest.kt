package com.intellij.lang.jsgraphql.frameworks.external

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibrary
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryDescriptor
import com.intellij.lang.jsgraphql.withExternalLibrary
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.toVirtualFileUrl
import java.nio.file.Paths
import kotlin.io.path.exists

class GraphQLExternalLibrariesTest : GraphQLTestCaseBase() {

  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    enableAllInspections()
  }

  override fun getBasePath() = "/frameworks/external"

  fun testSchemaValidation() {
    val sourceFilePath = Paths.get(testDataPath, "ExternalSchema.graphql")
    if (!sourceFilePath.exists()) {
      throw IllegalStateException("External schema file is not found: ${sourceFilePath.toAbsolutePath()}")
    }

    val fileUrlManager = WorkspaceModel.getInstance(project).getVirtualFileUrlManager()
    val rootUrl = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(sourceFilePath)
                    ?.toVirtualFileUrl(fileUrlManager)
                  ?: throw IllegalStateException("External schema virtual file is not found: ${sourceFilePath.toAbsolutePath()}")

    val descriptor = object : GraphQLLibraryDescriptor("EXTERNAL") {
      override fun isEnabled(project: Project): Boolean {
        return true
      }
    }
    val library = GraphQLLibrary(descriptor, setOf(rootUrl))

    doTest(library)
  }

  private fun doTest(library: GraphQLLibrary) {
    withExternalLibrary(project, library, {
      doHighlightingTest()

      val schemaInfo = GraphQLSchemaProvider.getInstance(project).getSchemaInfo(myFixture.file)
      assertNotNull(schemaInfo)
      assertEmpty(schemaInfo.getErrors(project))
    }, testRootDisposable)
  }
}
