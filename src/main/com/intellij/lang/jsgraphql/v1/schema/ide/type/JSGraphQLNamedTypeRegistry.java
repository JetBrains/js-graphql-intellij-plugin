/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.schema.ide.type;

/**
 * Provides a project-wide registry to look up known type definitions based on their name
 */
public interface JSGraphQLNamedTypeRegistry {

    /**
     * Gets the name type, if any, that has the specified typeName
     */
    JSGraphQLNamedType getNamedType(String typeName);

}
