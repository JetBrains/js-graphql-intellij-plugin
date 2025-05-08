package com.intellij.lang.jsgraphql.ide.injection

import com.intellij.openapi.util.TextRange

data class GraphQLInjectionFragment(val prefix: String?, val suffix: String?, val range: TextRange)