package com.intellij.lang.jsgraphql.ide.highlighting

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter
import com.intellij.injected.editor.DocumentWindow
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.jsgraphql.psi.GraphQLFile
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

class GraphQLInjectionHighlightInfoFilter : HighlightInfoFilter {
  override fun accept(highlightInfo: HighlightInfo, file: PsiFile?): Boolean {
    if (file is GraphQLFile && highlightInfo.isFromInjection && file.fileDocument is DocumentWindow) {
      val infoRange = TextRange.create(highlightInfo)
      val injectedLanguageManager = InjectedLanguageManager.getInstance(file.project)
      val fragments = injectedLanguageManager.intersectWithAllEditableFragments(file, infoRange)
      if (fragments.isNotEmpty() && fragments.all { it.isEmpty }) {
        return false
      }
    }
    return true
  }
}