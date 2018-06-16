/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.annotator;

import com.intellij.lang.jsgraphql.v1.languageservice.api.AnnotationsResponse;
import com.intellij.openapi.editor.Editor;

public class JSGraphQLAnnotationResult {

    private AnnotationsResponse annotationsReponse;
    private Editor editor;

    public JSGraphQLAnnotationResult(AnnotationsResponse annotationsReponse, Editor editor) {
        this.annotationsReponse = annotationsReponse;
        this.editor = editor;
    }

    public Editor getEditor() {
        return editor;
    }

    public AnnotationsResponse getAnnotationsReponse() {
        return annotationsReponse;
    }

    public void releaseEditor() {
        editor = null;
    }
}
