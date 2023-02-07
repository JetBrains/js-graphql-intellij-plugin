@file:JvmName("GraphQLUtil")

package com.intellij.lang.jsgraphql

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
inline fun <reified T : Any> Any?.asSafely(): @kotlin.internal.NoInfer T? = this as? T
