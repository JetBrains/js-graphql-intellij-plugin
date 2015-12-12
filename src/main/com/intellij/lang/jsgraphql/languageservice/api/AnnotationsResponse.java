/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.languageservice.api;

import com.google.common.collect.Lists;

import java.util.List;

public class AnnotationsResponse {

    private List<Annotation> annotations = Lists.newArrayList();

    public List<Annotation> getAnnotations() {
        return annotations;
    }
}
