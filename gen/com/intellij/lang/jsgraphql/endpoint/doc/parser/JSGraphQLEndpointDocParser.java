// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.endpoint.doc.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.intellij.lang.jsgraphql.endpoint.doc.JSGraphQLEndpointDocTokenTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class JSGraphQLEndpointDocParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType root_, PsiBuilder builder_) {
    parseLight(root_, builder_);
    return builder_.getTreeBuilt();
  }

  public void parseLight(IElementType root_, PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, null);
    Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    result_ = parse_root_(root_, builder_);
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType root_, PsiBuilder builder_) {
    return parse_root_(root_, builder_, 0);
  }

  static boolean parse_root_(IElementType root_, PsiBuilder builder_, int level_) {
    return Document(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // Rule*
  static boolean Document(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Document")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!Rule(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "Document", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // Tag | docText
  static boolean Rule(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Rule")) return false;
    if (!nextTokenIs(builder_, "", DOCNAME, DOCTEXT)) return false;
    boolean result_;
    result_ = Tag(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, DOCTEXT);
    return result_;
  }

  /* ********************************************************** */
  // docName docValue
  public static boolean Tag(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Tag")) return false;
    if (!nextTokenIs(builder_, DOCNAME)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TAG, null);
    result_ = consumeTokens(builder_, 1, DOCNAME, DOCVALUE);
    pinned_ = result_; // pin = 1
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

}
