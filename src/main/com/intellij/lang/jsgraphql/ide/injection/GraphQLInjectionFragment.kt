package com.intellij.lang.jsgraphql.ide.injection

import com.intellij.openapi.util.TextRange

internal data class GraphQLInjectionFragment(val prefix: String?, val suffix: String?, val range: TextRange)