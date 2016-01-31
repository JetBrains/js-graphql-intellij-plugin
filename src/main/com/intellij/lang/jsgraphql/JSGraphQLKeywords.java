/**
 * Copyright (c) 2015, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql;

public interface JSGraphQLKeywords {

    // graphql
    String QUERY = "query";
    String FRAGMENT = "fragment";
    String FRAGMENT_DOTS = "...";    
    String FRAGMENT_ON = "on";
    String MUTATION = "mutation";
    String SUBSCRIPTION = "subscription";
    
    // graphql schema
    String TYPE = "type";
    String INTERFACE = "interface";
    String UNION = "union";
    String SCALAR = "scalar";
    String ENUM = "enum";
    String INPUT = "input";
    String EXTEND = "extend";
    
    
}
