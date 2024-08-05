package com.intellij.lang.jsgraphql.ide.injection

import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.json.JsonFileType
import com.intellij.lang.jsgraphql.GraphQLFileType
import com.intellij.openapi.fileTypes.FileType

private class GraphQLDefaultFileTypeContributor : GraphQLFileTypeContributor {
  override fun getFileTypes(): Collection<FileType> {
    return setOf(
      GraphQLFileType.INSTANCE,
      JsonFileType.INSTANCE,
      HtmlFileType.INSTANCE,
    )
  }
}
