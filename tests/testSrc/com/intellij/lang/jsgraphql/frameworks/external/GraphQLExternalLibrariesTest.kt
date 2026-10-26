package com.intellij.lang.jsgraphql.frameworks.external

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibrary
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryDescriptor
import com.intellij.lang.jsgraphql.withExternalLibrary
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.toVirtualFileUrl
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.io.path.exists

@TestApplication
class GraphQLExternalLibrariesTest {
  private val graphQLFixture by graphQLTestFixture("/frameworks/external", enableAllInspections = true)

  @Test
  fun testSchemaValidation(): Unit = timeoutRunBlocking {
    val sourceFilePath = Paths.get(graphQLFixture.fixture.testDataPath, "ExternalSchema.graphql")
    if (!sourceFilePath.exists()) {
      throw IllegalStateException("External schema file is not found: ${sourceFilePath.toAbsolutePath()}")
    }

    val fileUrlManager = graphQLFixture.project.serviceAsync<WorkspaceModel>().getVirtualFileUrlManager()
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

  private suspend fun doTest(library: GraphQLLibrary) {
    withExternalLibrary(graphQLFixture.project, library) {
      graphQLFixture.doHighlightingTest()

      val schemaInfo = GraphQLSchemaProvider.getInstance(graphQLFixture.project).getSchemaInfo(graphQLFixture.fixture.file)
      assertNotNull(schemaInfo)
      assertThat(schemaInfo.getErrors(graphQLFixture.project)).isEmpty()
    }
  }
}
