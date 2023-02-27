/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.lang.jsgraphql.icons.GraphQLIcons
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.treeStructure.CachingSimpleNode
import com.intellij.ui.treeStructure.SimpleNode
import org.apache.commons.lang.StringUtils

/**
 * Tree node that represents a graphql-config schema
 */
class GraphQLConfigSchemaNode(
    project: Project,
    parent: SimpleNode,
    private val config: GraphQLConfig,
    projectConfig: GraphQLProjectConfig?,
) : CachingSimpleNode(project, parent) {

    private val usedProjectConfig: GraphQLProjectConfig?
    private val schemaInfo: GraphQLSchemaInfo?
    private val performSchemaDiscovery: Boolean
    private val isProjectLevelNode: Boolean

    init {
        myName = projectConfig?.name ?: StringUtils.substringAfterLast(config.dir.path, "/")
        isProjectLevelNode = projectConfig != null
        presentation.setIcon(GraphQLIcons.Files.GraphQLSchema)

        if (projectConfig == null) {
            presentation.locationString = config.dir.presentableUrl
        }

        var defaultProjectConfig: GraphQLProjectConfig? = null
        if (!isProjectLevelNode && config.hasOnlyDefaultProject()) {
            defaultProjectConfig = config.getDefault()
        }

        performSchemaDiscovery = isProjectLevelNode || defaultProjectConfig != null

        if (performSchemaDiscovery) {
            usedProjectConfig = projectConfig ?: defaultProjectConfig
            schemaInfo = runReadAction {
                val scope = usedProjectConfig!!.schemaScope
                GraphQLSchemaProvider.getInstance(myProject).getSchemaInfo(scope)
            }
        } else {
            schemaInfo = null
            usedProjectConfig = null
        }
    }

    /**
     * Gets whether this node contains a schema that includes the specified file
     */
    fun representsFile(virtualFile: VirtualFile?): Boolean {
        if (virtualFile == null) return false
        if (virtualFile == configFile && !isProjectLevelNode) {
            return true
        }
        if (performSchemaDiscovery) {
            val scope = usedProjectConfig?.scope
            if (scope != null) {
                return scope.contains(virtualFile)
            }
        }
        return false
    }

    val configFile: VirtualFile?
        get() = usedProjectConfig?.file ?: config.file

    override fun update(presentation: PresentationData) {
        super.update(presentation)
        val style = if (representsCurrentFile()) SimpleTextAttributes.STYLE_BOLD else SimpleTextAttributes.STYLE_PLAIN
        presentation.addText(name, SimpleTextAttributes(style, color))
    }

    public override fun buildChildren(): Array<SimpleNode> {
        val children: MutableList<SimpleNode> = mutableListOf()
        if (performSchemaDiscovery && schemaInfo != null) {
            children.add(GraphQLSchemaContentNode(this, schemaInfo))
            children.add(GraphQLSchemaErrorsListNode(this, schemaInfo))
        }
        if (!isProjectLevelNode && !config.hasOnlyDefaultProject()) {
            children.add(GraphQLConfigProjectsNode(this))
        }
        if (usedProjectConfig != null) {
            children.add(GraphQLSchemaEndpointsListNode(this, usedProjectConfig))
        }
        return children.toTypedArray()
    }

    override fun isAutoExpandNode(): Boolean {
        return representsCurrentFile() || !performSchemaDiscovery
    }

    override fun getEqualityObjects(): Array<Any?> {
        return arrayOf(config, schemaInfo)
    }

    private fun representsCurrentFile(): Boolean {
        return representsFile(FileEditorManager.getInstance(myProject).selectedFiles.getOrNull(0))
    }

    private class GraphQLConfigProjectsNode(private val parent: GraphQLConfigSchemaNode) :
        CachingSimpleNode(parent.myProject, parent) {

        init {
            myName = "Projects"
            icon = AllIcons.Nodes.Folder
        }

        public override fun buildChildren(): Array<SimpleNode> {
            return try {
                val config = parent.config
                return config.getProjects().values
                    .map {
                        GraphQLConfigSchemaNode(
                            myProject,
                            this,
                            config,
                            it,
                        )
                    }
                    .toTypedArray()
            } catch (ignored: IndexNotReadyException) {
                // entered "dumb" mode, so just return no children as the tree view will be rebuilt as empty shortly (GraphQLSchemasRootNode)
                NO_CHILDREN
            }
        }

        override fun isAutoExpandNode(): Boolean {
            return true
        }
    }
}
