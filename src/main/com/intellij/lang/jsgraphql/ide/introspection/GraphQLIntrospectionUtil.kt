@file:JvmName("GraphQLIntrospectionUtil")

package com.intellij.lang.jsgraphql.ide.introspection

import com.intellij.openapi.editor.Document


fun isJsonSchemaCandidate(document: Document?) =
    document?.text?.contains("__schema") == true
