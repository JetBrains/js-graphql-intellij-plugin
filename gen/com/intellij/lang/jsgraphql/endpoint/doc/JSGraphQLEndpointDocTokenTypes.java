// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.endpoint.doc;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.endpoint.doc.psi.impl.*;

public interface JSGraphQLEndpointDocTokenTypes {

  IElementType TAG = new JSGraphQLEndpointDocTokenType("TAG");

  IElementType DOCNAME = new JSGraphQLEndpointDocTokenType("docName");
  IElementType DOCTEXT = new JSGraphQLEndpointDocTokenType("docText");
  IElementType DOCVALUE = new JSGraphQLEndpointDocTokenType("docValue");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == TAG) {
        return new JSGraphQLEndpointDocTagImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
