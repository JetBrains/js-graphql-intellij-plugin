/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus

import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.impl.AbstractProjectTreeStructure
import com.intellij.ide.projectView.impl.ProjectAbstractTreeStructureBase
import com.intellij.ide.projectView.impl.ProjectTreeBuilder
import com.intellij.ide.util.treeView.AlphaComparator
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

/**
 * Directory picker for where to place a GraphQL schema configuration file.
 */
class TreeDirectoryChooserDialog(private val project: Project, title: String?) : DialogWrapper(project, true) {
    private val disposable = Disposer.newDisposable()

    private lateinit var myTree: Tree
    private lateinit var myBuilder: ProjectTreeBuilder

    private var mySelectedFile: VirtualFile? = null

    init {
        setTitle(title)
        init()
        SwingUtilities.invokeLater { handleSelectionChanged() }
    }

    override fun createCenterPanel(): JComponent {
        val model = DefaultTreeModel(DefaultMutableTreeNode())
        myTree = Tree(model)

        val treeStructure: ProjectAbstractTreeStructureBase = object : AbstractProjectTreeStructure(project) {
            override fun isFlattenPackages(): Boolean {
                return false
            }

            override fun isShowMembers(): Boolean {
                return false
            }

            override fun isHideEmptyMiddlePackages(): Boolean {
                return true
            }

            override fun isAbbreviatePackageNames(): Boolean {
                return false
            }

            override fun isShowLibraryContents(): Boolean {
                return false
            }

            override fun isShowModules(): Boolean {
                return false
            }

            override fun getProviders(): List<TreeStructureProvider>? {
                return null
            }
        }

        myBuilder = ProjectTreeBuilder(project, myTree, model, AlphaComparator.INSTANCE, treeStructure)
        Disposer.register(disposable, myBuilder)

        myTree.isRootVisible = false
        myTree.expandRow(0)
        myTree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        myTree.cellRenderer = NodeRenderer()

        val scrollPane = ScrollPaneFactory.createScrollPane(myTree)
        scrollPane.preferredSize = JBUI.size(500, 300)

        myTree.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (KeyEvent.VK_ENTER == e.keyCode) {
                    doOKAction()
                }
            }
        })

        object : DoubleClickListener() {
            override fun onDoubleClick(e: MouseEvent): Boolean {
                val path = myTree.getPathForLocation(e.x, e.y)
                if (path != null && myTree.isPathSelected(path)) {
                    doOKAction()
                    return true
                }
                return false
            }
        }.installOn(myTree)

        myTree.addTreeSelectionListener { handleSelectionChanged() }
        TreeSpeedSearch(myTree)
        return scrollPane
    }

    private fun handleSelectionChanged() {
        val selection = calcSelectedClass()
        isOKActionEnabled = selection != null
    }

    override fun doOKAction() {
        mySelectedFile = calcSelectedClass()
        if (mySelectedFile == null) return
        super.doOKAction()
    }

    override fun doCancelAction() {
        mySelectedFile = null
        super.doCancelAction()
    }

    val selectedDirectory: VirtualFile?
        get() {
            val file = mySelectedFile
            return if (file != null) {
                if (file.isDirectory) file else file.parent
            } else null
        }

    private fun calcSelectedClass(): VirtualFile? {
        val path = myTree.selectionPath ?: return null
        val node = path.lastPathComponent as DefaultMutableTreeNode
        val userObject = node.userObject as? ProjectViewNode<*> ?: return null
        return userObject.virtualFile
    }

    public override fun dispose() {
        Disposer.dispose(disposable)

        super.dispose()
    }

    override fun getDimensionServiceKey(): String {
        return "#com.intellij.ide.util.TreeDirectoryChooserDialog"
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return myTree
    }
}
