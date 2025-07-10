package com.intellij.lang.jsgraphql.schema

import com.intellij.lang.jsgraphql.psi.GraphQLFile
import com.intellij.lang.jsgraphql.types.language.Document
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiFile
import com.intellij.util.Processor

/**
 * A number chosen based on the average size estimation of GraphQL schemas.
 * For example, the GitHub schema contains approximately 1600 type definitions.
 * Can be changed via the registry option `graphql.schema.size.definitions.limit`.
 */
private const val SCHEMA_SIZE_DEFINITIONS_LIMIT_DEFAULT = 4000

internal const val SCHEMA_SIZE_DEFINITIONS_LIMIT_KEY = "graphql.schema.size.definitions.limit"

internal val SCHEMA_SIZE_DEFINITIONS_LIMIT: Int
  get() = Registry.intValue(SCHEMA_SIZE_DEFINITIONS_LIMIT_KEY, SCHEMA_SIZE_DEFINITIONS_LIMIT_DEFAULT)

private val LOG = logger<GraphQLSchemaDocumentProcessor>()

internal class GraphQLSchemaDocumentProcessor : Processor<PsiFile?> {
  val documents = HashSet<Document>()

  private val currentLimit = SCHEMA_SIZE_DEFINITIONS_LIMIT
  private var totalDefinitionsCount = 0

  override fun process(psiFile: PsiFile?): Boolean {
    ProgressManager.checkCanceled()

    if (psiFile !is GraphQLFile) {
      return true
    }

    val document = psiFile.document

    // We need to add it no matter if it's exceeding the limit to cover the case when there is only a single, but huge file.
    // Preventing limits from being exceeded is important only for cases when there are multiple files, especially when
    // there is no config defined, e.g., a whole project scope or an incorrect config for a monorepo.
    documents.add(document)

    if (!GraphQLPsiDocumentBuilder.isInLibrary(document)) {
      totalDefinitionsCount += GraphQLPsiDocumentBuilder.getTypeDefinitionsCount(document)
    }

    if (isTooComplex) {
      LOG.warn("Schema total definitions count limit exceeded: ${totalDefinitionsCount}, file: ${psiFile.virtualFile?.path.orEmpty()}")
      LOG.trace {
        documents.joinToString("\n") { document ->
          "file: ${document.filePath}, definitions: ${document.definitions.size}"
        }
      }
    }

    return !isTooComplex
  }

  private val Document.filePath: String
    get() = sourceLocation?.sourceName ?: "<unknown>"

  val isTooComplex: Boolean
    get() = totalDefinitionsCount >= currentLimit
}
