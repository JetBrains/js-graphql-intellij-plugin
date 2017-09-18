/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;


/**
 * Implemented by PSI elements that are able to "mute" traditional GraphQL errors based on the context, e.g. Relay fragments
 */
public interface JSGraphQLErrorContextAware {

    /**
     * Gets whether the errors should be reported in this element context
     * @param errorMessage the error message from the traditional GraphQL validation
     * @return whether the error should be shown in this context
     */
    boolean isErrorInContext(String errorMessage);

}
