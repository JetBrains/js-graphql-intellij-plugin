package com.intellij.lang.jsgraphql.types.language;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.util.*;

import java.util.concurrent.ForkJoinPool;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.language.AstNodeAdapter.AST_NODE_ADAPTER;

/**
 * Allows for an easy way to "manipulate" the immutable Ast by changing specific nodes and getting back a new Ast
 * containing the changed nodes while everything else is the same.
 */
@PublicApi
public class AstTransformer {


    public Node transform(Node root, NodeVisitor nodeVisitor) {
        assertNotNull(root);
        assertNotNull(nodeVisitor);

        TraverserVisitor<Node> traverserVisitor = new TraverserVisitor<Node>() {
            @Override
            public TraversalControl enter(TraverserContext<Node> context) {
                return context.thisNode().accept(context, nodeVisitor);
            }

            @Override
            public TraversalControl leave(TraverserContext<Node> context) {
                return TraversalControl.CONTINUE;
            }
        };

        TreeTransformer<Node> treeTransformer = new TreeTransformer<>(AST_NODE_ADAPTER);
        return treeTransformer.transform(root, traverserVisitor);
    }

    public Node transformParallel(Node root, NodeVisitor nodeVisitor) {
        return transformParallel(root, nodeVisitor, ForkJoinPool.commonPool());
    }

    public Node transformParallel(Node root, NodeVisitor nodeVisitor, ForkJoinPool forkJoinPool) {
        assertNotNull(root);
        assertNotNull(nodeVisitor);

        TraverserVisitor<Node> traverserVisitor = new TraverserVisitorStub<Node>() {
            @Override
            public TraversalControl enter(TraverserContext<Node> context) {
                return context.thisNode().accept(context, nodeVisitor);
            }

        };

        TreeParallelTransformer<Node> treeParallelTransformer = TreeParallelTransformer.parallelTransformer(AST_NODE_ADAPTER, forkJoinPool);
        return treeParallelTransformer.transform(root, traverserVisitor);
    }

}
