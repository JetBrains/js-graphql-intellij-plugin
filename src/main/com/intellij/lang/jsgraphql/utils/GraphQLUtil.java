/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.utils;

import com.google.common.collect.Sets;
import com.intellij.openapi.util.Ref;
import graphql.language.AbstractNode;
import graphql.language.Document;
import graphql.language.Node;
import graphql.language.SourceLocation;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class GraphQLUtil {


    /**
     * Shifts the source locations in the specified document with the specified line detla
     * @param document a GraphQL document from graphql-java
     * @param lineDelta the delta line to apply to the document and all child nodes
     * @param firstLineColumnDelta the column delta for the first line
     */
    public static void adjustSourceLocations(Document document, int lineDelta, int firstLineColumnDelta) {
        final Ref<Consumer<Node>> adjustSourceLines = new Ref<>();
        final Set<Node> visitedNodes = Sets.newHashSet();
        adjustSourceLines.set((Node node) -> {
            if(node == null || !visitedNodes.add(node)) {
                return;
            }
            if(node instanceof AbstractNode) {
                final SourceLocation sourceLocation = node.getSourceLocation();
                if (sourceLocation != null) {
                    final int currentLine = sourceLocation.getLine();
                    final int columnDelta = currentLine == 1 ? firstLineColumnDelta : 0;
                    final SourceLocation newSourceLocation = new SourceLocation(
                            currentLine + lineDelta,
                            sourceLocation.getColumn() + columnDelta,
                            sourceLocation.getSourceName()
                    );
                    ((AbstractNode) node).setSourceLocation(newSourceLocation);
                }

            }
            //noinspection unchecked
            final List<Node> children = node.getChildren();
            if(children != null) {
                //noinspection unchecked
                children.forEach(child -> {
                    if(child != null) {
                        adjustSourceLines.get().accept(child);
                    }
                });
            }
        });
        adjustSourceLines.get().accept(document);
    }
}
