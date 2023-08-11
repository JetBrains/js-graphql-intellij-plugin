package com.intellij.lang.jsgraphql.ide.config.fileType

import com.intellij.json.JsonLanguage
import com.intellij.lang.Language
import com.intellij.lang.jsgraphql.ide.config.GRAPHQL_RC
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.LanguageSubstitutor
import java.io.FileNotFoundException
import java.io.IOException

class GraphQLConfigLanguageSubstitutor : LanguageSubstitutor() {
  companion object {
    private val LOG = logger<GraphQLConfigLanguageSubstitutor>()

    private const val FILE_NAME = GRAPHQL_RC

    private val CHANGEABLE_CONFIG_TYPE_KEY: Key<TimestampedData> = Key.create("graphql.changeable.config.type.key")
  }

  private val yamlLanguage: Language
    get() = Language.findLanguageByID("yaml") ?: PlainTextLanguage.INSTANCE

  override fun getLanguage(file: VirtualFile, project: Project): Language? {
    if (file.name != FILE_NAME || !file.isValid) {
      return null
    }

    var data = file.getUserData(CHANGEABLE_CONFIG_TYPE_KEY)
    val fileTimeStamp = file.timeStamp
    if (data?.timeStamp != fileTimeStamp) {
      data = TimestampedData(fileTimeStamp, isJsonDetected(file))
      CHANGEABLE_CONFIG_TYPE_KEY[file] = data
    }
    return if (data.isJsonDetected) JsonLanguage.INSTANCE else yamlLanguage
  }

  private fun isJsonDetected(file: VirtualFile): Boolean {
    val buffer: ByteArray = try {
      VfsUtilCore.loadNBytes(file, 64)
    }
    catch (e: FileNotFoundException) {
      return true
    }
    catch (e: IOException) {
      LOG.warn("Failed to read first 64 bytes of ${file.path}", e)
      return true
    }

    val str = String(buffer, file.charset)
    for (element in str) {
      if (element > ' ') {
        return element == '[' || element == '{' || element == '/'
      }
    }
    return true
  }

  private class TimestampedData(val timeStamp: Long, val isJsonDetected: Boolean)
}
