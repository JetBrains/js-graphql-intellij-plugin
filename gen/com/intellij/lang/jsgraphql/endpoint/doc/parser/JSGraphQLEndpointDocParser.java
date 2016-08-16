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

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t == TAG) {
      r = Tag(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return Document(b, l + 1);
  }

  /* ********************************************************** */
  // Rule*
  static boolean Document(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Document")) return false;
    int c = current_position_(b);
    while (true) {
      if (!Rule(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "Document", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // Tag
  //     |
  //     docText
  static boolean Rule(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Rule")) return false;
    if (!nextTokenIs(b, "", DOCNAME, DOCTEXT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Tag(b, l + 1);
    if (!r) r = consumeToken(b, DOCTEXT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // docName docValue
  public static boolean Tag(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Tag")) return false;
    if (!nextTokenIs(b, DOCNAME)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TAG, null);
    r = consumeTokens(b, 1, DOCNAME, DOCVALUE);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

}
