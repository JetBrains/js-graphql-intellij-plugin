/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus

import com.intellij.icons.AllIcons
import com.intellij.lang.jsgraphql.createScratchFromEndpoint
import com.intellij.lang.jsgraphql.icons.GraphQLIcons
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.treeStructure.CachingSimpleNode
import com.intellij.ui.treeStructure.SimpleNode
import com.intellij.ui.treeStructure.SimpleTree
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent

/**
 * Tree node which provides a schema endpoints list
 */
class GraphQLSchemaEndpointsListNode(
    parent: SimpleNode?,
    projectConfig: GraphQLProjectConfig?,
) : CachingSimpleNode(parent) {

    private val endpoints: List<GraphQLConfigEndpoint>

    init {
        endpoints = projectConfig?.endpoints ?: emptyList()
        myName = "Endpoints"
        icon = AllIcons.Nodes.WebFolder
    }

    public override fun buildChildren(): Array<SimpleNode> {
        return if (endpoints.isEmpty()) {
            arrayOf(DefaultEndpointNode(myProject))
        } else {
            endpoints
                .map { ConfigurableEndpointNode(this, it) }
                .toTypedArray()
        }
    }

    override fun isAutoExpandNode(): Boolean {
        return true
    }

    private class ConfigurableEndpointNode(parent: SimpleNode, private val endpoint: GraphQLConfigEndpoint) :
        SimpleNode(parent) {
        init {
            myName = endpoint.displayName
            templatePresentation.tooltip = "Endpoints allow you to perform GraphQL introspection, queries and mutations"
            templatePresentation.locationString = endpoint.url
            icon = GraphQLIcons.UI.GraphQLNode
        }

        override fun handleDoubleClickOrEnter(tree: SimpleTree, inputEvent: InputEvent) {
            val introspect = "Get GraphQL Schema from Endpoint (introspection)"
            val createScratch = "New GraphQL Scratch File (for query, mutation testing)"
            val listPopup = JBPopupFactory.getInstance().createListPopup(
                object : BaseListPopupStep<String>("Choose Endpoint Action", introspect, createScratch) {
                    override fun onChosen(selectedValue: String, finalChoice: Boolean): PopupStep<*> {
                        return doFinalStep {
                            if (introspect == selectedValue) {
                                GraphQLIntrospectionService.getInstance(myProject).performIntrospectionQuery(endpoint)
                            } else if (createScratch == selectedValue) {
                                createScratchFromEndpoint(myProject, endpoint, true)
                            }
                        }
                    }
                })

            if (inputEvent is KeyEvent) {
                listPopup.showInFocusCenter()
            } else if (inputEvent is MouseEvent) {
                listPopup.show(RelativePoint(inputEvent))
            }
        }

        override fun getChildren(): Array<SimpleNode> {
            return NO_CHILDREN
        }

        override fun isAlwaysLeaf(): Boolean {
            return true
        }
    }

    private class DefaultEndpointNode(project: Project) : SimpleNode(project) {
        init {
            myName = "No endpoints available in the default schema"
            templatePresentation.tooltip = "Endpoints allow you to perform GraphQL introspection, queries and mutations"
            icon = AllIcons.General.Information
        }

        override fun getChildren(): Array<SimpleNode> {
            return NO_CHILDREN
        }

        override fun isAlwaysLeaf(): Boolean {
            return true
        }
    }
}
