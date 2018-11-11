/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project;

import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Recognize .graphqls as a GraphQL files (v1 had separate extensions given that GraphQL didn't include SDL at that time)
 */
public class GraphQLSLegacyFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        consumer.consume(GraphQLFileType.INSTANCE, "graphqls");
    }
}
