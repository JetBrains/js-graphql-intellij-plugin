package com.intellij.lang.jsgraphql.ide.highlighting

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter
import com.intellij.injected.editor.DocumentWindow
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.jsgraphql.psi.GraphQLFile
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

class GraphQLInjectionHighlightInfoFilter : HighlightInfoFilter {
  override fun accept(highlightInfo: HighlightInfo, psiFile: PsiFile?): Boolean {
    if (psiFile is GraphQLFile && highlightInfo.isFromInjection && psiFile.fileDocument is DocumentWindow) {
      val infoRange = TextRange.create(highlightInfo)
      val injectedLanguageManager = InjectedLanguageManager.getInstance(psiFile.project)
      val fragments = injectedLanguageManager.intersectWithAllEditableFragments(psiFile, infoRange)
      if (fragments.isNotEmpty() && fragments.all { it.isEmpty }) {
        return false
      }
    }
    return true
  }
}