@file:JvmName("GraphQLConfigUtils")
package com.intellij.lang.jsgraphql.ide.config

import com.intellij.lang.jsgraphql.psi.GraphQLPsiUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

fun getPhysicalVirtualFile(file: PsiFile): VirtualFile? {
    return GraphQLPsiUtil.getPhysicalVirtualFile(file)
}
