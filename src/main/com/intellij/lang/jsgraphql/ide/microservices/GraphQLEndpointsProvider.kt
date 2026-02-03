package com.intellij.lang.jsgraphql.ide.microservices

import com.intellij.ide.projectView.PresentationData
import com.intellij.lang.jsgraphql.GraphQLFileType
import com.intellij.lang.jsgraphql.GraphQLLanguage
import com.intellij.lang.jsgraphql.icons.GraphQLIcons
import com.intellij.lang.jsgraphql.psi.GraphQLElement
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLFile
import com.intellij.lang.jsgraphql.psi.GraphQLNamedTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLObjectTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLTypeNameDefinition
import com.intellij.microservices.endpoints.EndpointType
import com.intellij.microservices.endpoints.EndpointsFilter
import com.intellij.microservices.endpoints.EndpointsProvider
import com.intellij.microservices.endpoints.FrameworkPresentation
import com.intellij.microservices.endpoints.GRAPH_QL_TYPE
import com.intellij.microservices.endpoints.ModuleEndpointsFilter
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parentOfType

internal abstract class GraphQLEndpoint(val item: GraphQLElement) {
  abstract fun getEndpointPresentation(group: GraphQLFile): ItemPresentation
  fun getDocumentationElement(): PsiElement = item
  fun isValidEndpoint(): Boolean = item.isValid
}

internal class FieldDefinitionEndpoint(val field: GraphQLFieldDefinition) : GraphQLEndpoint(field) {
  override fun getEndpointPresentation(group: GraphQLFile): ItemPresentation {
    val name = field.name ?: "Unknown"
    val typeName = field.parentOfType<GraphQLNamedTypeDefinition>()?.typeNameDefinition?.name ?: "Unknown"
    return PresentationData("$typeName.$name", group.name, GraphQLIcons.FILE, null)
  }
}

internal class TypeNameDefinitionEndpoint(val type: GraphQLTypeNameDefinition) : GraphQLEndpoint(type) {
  override fun getEndpointPresentation(group: GraphQLFile): ItemPresentation {
    val typeName = type.parentOfType<GraphQLNamedTypeDefinition>()?.typeNameDefinition?.name ?: "Unknown"
    return PresentationData(typeName, group.name, GraphQLIcons.FILE, null)
  }
}

internal class GraphQLEndpointsProvider<E : GraphQLElement> : EndpointsProvider<GraphQLFile, GraphQLEndpoint> {
  override val endpointType: EndpointType = GRAPH_QL_TYPE

  override val presentation: FrameworkPresentation = FrameworkPresentation("GraphQL", "GraphQL", GraphQLIcons.FILE)

  override fun getStatus(project: Project): EndpointsProvider.Status =
    if (getGraphQLFiles(project).isNotEmpty()) EndpointsProvider.Status.HAS_ENDPOINTS
    else EndpointsProvider.Status.UNAVAILABLE

  private fun getGraphQLFiles(project: Project): MutableCollection<VirtualFile> =
    FileTypeIndex.getFiles(GraphQLFileType.INSTANCE, GlobalSearchScope.allScope(project))

  private fun getGraphQLFiles(module: Module): MutableCollection<VirtualFile> =
    FileTypeIndex.getFiles(GraphQLFileType.INSTANCE, module.moduleScope)

  override fun getEndpointGroups(project: Project, filter: EndpointsFilter): Iterable<GraphQLFile> {
    if (filter !is ModuleEndpointsFilter) return emptyList()

    val module: Module = filter.module
    val qlFiles = getGraphQLFiles(module)
    return if (qlFiles.isEmpty()) emptyList()
    else qlFiles.map {
      PsiManager.getInstance(project).findFile(it)
    }.filterIsInstance<GraphQLFile>()
  }

  override fun getEndpoints(group: GraphQLFile): Iterable<GraphQLEndpoint> {
    val definitions = getGraphQLTypeNameDefinitions(group)

    val endpoints: MutableSet<GraphQLEndpoint> = HashSet()
    endpoints.addAll(definitions
                       .filter { it.name in listOf("Query", "Mutation") }
                       .asSequence()
                       .map { it.parent }.filterIsInstance<GraphQLObjectTypeDefinition>()
                       .map { it.fieldsDefinition }.filterNotNull()
                       .flatMap { it.fieldDefinitionList }
                       .map { FieldDefinitionEndpoint(it) }
                       .toList())
    endpoints.addAll(definitions
                       .filter { it.name !in listOf("Query", "Mutation", "Subscription") }
                       .map { TypeNameDefinitionEndpoint(it) }
                       .toList()
    )

    return endpoints
  }

  private fun getGraphQLTypeNameDefinitions(group: GraphQLFile) =
    group.definitions.filterIsInstance<GraphQLObjectTypeDefinition>()
      .mapNotNull { it.getTypeNameDefinition() }

  override fun isValidEndpoint(group: GraphQLFile, endpoint: GraphQLEndpoint): Boolean = endpoint.isValidEndpoint()

  override fun getEndpointPresentation(group: GraphQLFile, endpoint: GraphQLEndpoint): ItemPresentation = endpoint.getEndpointPresentation(
    group)

  override fun getModificationTracker(project: Project): ModificationTracker {
    return PsiManager.getInstance(project).modificationTracker.forLanguage(GraphQLLanguage.INSTANCE)
  }

  override fun getDocumentationElement(group: GraphQLFile, endpoint: GraphQLEndpoint): PsiElement {
    return endpoint.getDocumentationElement()
  }
}