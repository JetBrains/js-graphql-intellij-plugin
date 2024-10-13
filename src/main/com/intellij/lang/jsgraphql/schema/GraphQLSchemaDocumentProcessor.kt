package com.intellij.lang.jsgraphql.schema

import com.intellij.lang.jsgraphql.psi.GraphQLFile
import com.intellij.lang.jsgraphql.schema.builder.GraphQLCompositeRegistry
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.util.Processor

class GraphQLSchemaDocumentProcessor : Processor<PsiFile?> {
  private val compositeRegistry = GraphQLCompositeRegistry()

  override fun process(psiFile: PsiFile?): Boolean {
    ProgressManager.checkCanceled()

    if (psiFile !is GraphQLFile) {
      return true
    }

    val document = psiFile.document
    compositeRegistry.addFromDocument(document)
    return true
  }

  fun build(): GraphQLRegistryInfo {
    return compositeRegistry.build()
  }
}
