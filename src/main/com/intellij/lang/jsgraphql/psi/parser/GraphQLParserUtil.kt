package com.intellij.lang.jsgraphql.psi.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilderUtil
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectionUtils
import com.intellij.lang.jsgraphql.psi.GraphQLElementTypes
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.psi.TokenType

class GraphQLParserUtil : GeneratedParserUtilBase() {
  companion object {
    @JvmStatic
    fun parseNameIfNotPlaceholder(b: PsiBuilder, level: Int): Boolean {
      if (!recursion_guard_(b, level, "parseNameIfNotPlaceholder")) return false
      if (b.tokenType != GraphQLElementTypes.NAME) return false
      if (b.tokenText?.startsWith(GraphQLInjectionUtils.GRAPHQL_EXTERNAL_FRAGMENT) == false) {
        b.advanceLexer()
        return true
      }
      return false
    }

    @JvmStatic
    fun consumePlaceholderWithError(b: PsiBuilder, level: Int): Boolean {
      if (!recursion_guard_(b, level, "consumePlaceholderWithError")) return false

      val marker = b.mark()
      var result = false
      if (b.tokenType == GraphQLElementTypes.NAME &&
          b.tokenText?.startsWith(GraphQLInjectionUtils.GRAPHQL_EXTERNAL_FRAGMENT) == true) {
        result = true
        b.advanceLexer()
        marker.error(GraphQLBundle.message("graphql.parsing.error.injection.placeholder"))
      }
      else {
        marker.rollbackTo()
      }

      return result
    }

    @JvmStatic
    fun isNewLine(b: PsiBuilder, @Suppress("UNUSED_PARAMETER") level: Int): Boolean {
      val prevTokenType = b.rawLookup(-1) ?: return true
      return prevTokenType == TokenType.WHITE_SPACE &&
             PsiBuilderUtil.rawTokenText(b, -1).contains('\n')
    }
  }
}