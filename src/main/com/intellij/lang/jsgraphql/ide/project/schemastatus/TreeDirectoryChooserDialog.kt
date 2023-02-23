/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.impl.AbstractProjectTreeStructure;
import com.intellij.ide.projectView.impl.ProjectAbstractTreeStructureBase;
import com.intellij.ide.projectView.impl.ProjectTreeBuilder;
import com.intellij.ide.util.treeView.AlphaComparator;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Directory picker for where to place a GraphQL schema configuration file.
 */
public final class TreeDirectoryChooserDialog extends DialogWrapper {

    private Tree myTree;
    private VirtualFile mySelectedFile = null;
    private final Project myProject;
    private ProjectTreeBuilder myBuilder;

    public TreeDirectoryChooserDialog(final Project project, String title) {
        super(project, true);
        setTitle(title);
        myProject = project;
        init();
        SwingUtilities.invokeLater(this::handleSelectionChanged);
    }

    @Override
    protected JComponent createCenterPanel() {
        final DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());
        myTree = new Tree(model);

        final ProjectAbstractTreeStructureBase treeStructure = new AbstractProjectTreeStructure(myProject) {
            @Override
            public boolean isFlattenPackages() {
                return false;
            }

            @Override
            public boolean isShowMembers() {
                return false;
            }

            @Override
            public boolean isHideEmptyMiddlePackages() {
                return true;
            }

            @Override
            public boolean isAbbreviatePackageNames() {
                return false;
            }

            @Override
            public boolean isShowLibraryContents() {
                return false;
            }

            @Override
            public boolean isShowModules() {
                return false;
            }

            @Override
            public List<TreeStructureProvider> getProviders() {
                return null;
            }
        };
        myBuilder = new ProjectTreeBuilder(myProject, myTree, model, AlphaComparator.INSTANCE, treeStructure);

        myTree.setRootVisible(false);
        myTree.expandRow(0);
        myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        myTree.setCellRenderer(new NodeRenderer());

        final JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(myTree);
        scrollPane.setPreferredSize(JBUI.size(500, 300));

        myTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (KeyEvent.VK_ENTER == e.getKeyCode()) {
                    doOKAction();
                }
            }
        });

        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(@NotNull MouseEvent e) {
                final TreePath path = myTree.getPathForLocation(e.getX(), e.getY());
                if (path != null && myTree.isPathSelected(path)) {
                    doOKAction();
                    return true;
                }
                return false;
            }
        }.installOn(myTree);

        myTree.addTreeSelectionListener(e -> handleSelectionChanged());

        new TreeSpeedSearch(myTree);

        return scrollPane;
    }

    private void handleSelectionChanged() {
        final VirtualFile selection = calcSelectedClass();
        setOKActionEnabled(selection != null);
    }

    @Override
    protected void doOKAction() {
        mySelectedFile = calcSelectedClass();
        if (mySelectedFile == null) return;
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        mySelectedFile = null;
        super.doCancelAction();
    }

    public VirtualFile getSelectedDirectory() {
        if (mySelectedFile != null) {
            return mySelectedFile.isDirectory() ? mySelectedFile : mySelectedFile.getParent();
        }
        return null;
    }

    private VirtualFile calcSelectedClass() {
        final TreePath path = myTree.getSelectionPath();
        if (path == null) return null;
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        final Object userObject = node.getUserObject();
        if (!(userObject instanceof ProjectViewNode)) return null;
        ProjectViewNode pvNode = (ProjectViewNode) userObject;
        return pvNode.getVirtualFile();
    }


    @Override
    public void dispose() {
        if (myBuilder != null) {
            Disposer.dispose(myBuilder);
            myBuilder = null;
        }
        super.dispose();
    }

    @Override
    protected String getDimensionServiceKey() {
        return "#com.intellij.ide.util.TreeDirectoryChooserDialog";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return myTree;
    }

}
