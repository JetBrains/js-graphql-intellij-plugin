package com.intellij.lang.jsgraphql.psi

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectedLanguage
import com.intellij.lang.jsgraphql.types.parser.StringValueParsing

object GraphQLPsiImplUtil {
  @JvmStatic
  fun getValueAsString(stringValue: GraphQLStringValue): String {
    val stringLiteral = stringValue.getStringLiteral()
    val text = stringLiteral.getText()
    val multiLine = text.startsWith("\"\"\"")
    return if (multiLine) {
      StringValueParsing.parseTripleQuotedString(text)
    }
    else {
      StringValueParsing.parseSingleQuotedString(text)
    }
  }

  @JvmStatic
  fun getContent(description: GraphQLDescription): String {
    var content = description.text

    val injectionHost =
      InjectedLanguageManager.getInstance(description.project).getInjectionHost(description)
    val injectedLanguage = if (injectionHost != null) GraphQLInjectedLanguage.forElement(injectionHost) else null
    if (injectedLanguage != null) {
      val escaped = injectedLanguage.escapeHostElements(content)
      if (!escaped.isNullOrEmpty()) {
        content = escaped
      }
    }

    return if (isMultiLine(description)) {
      StringValueParsing.parseTripleQuotedString(content)
    }
    else {
      StringValueParsing.parseSingleQuotedString(content)
    }
  }

  @JvmStatic
  fun isMultiLine(description: GraphQLDescription): Boolean = description.firstChild is GraphQLBlockString
}
