/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.documentation;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;

/**
 * Renders GraphQL documentation based on the CommonMark spec as either plain text or HTML.
 */
public class GraphQLDocumentationMarkdownRenderer {

  private static final Parser PARSER = Parser.builder().build();

  private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();
  private static final TextContentRenderer TEXT_SINGLE_LINE_RENDERER = TextContentRenderer.builder().stripNewlines(true).build();
  private static final TextContentRenderer TEXT_RENDERER = TextContentRenderer.builder().build();

  /**
   * Parses the specified markdown description and renders it as plain text where formatting is stripped
   *
   * @param description   the markdown description to render as plain text
   * @param stripNewLines whether to strip out new lines
   * @return a plain text representation of the specified markdown
   */
  public static String getDescriptionAsPlainText(String description, boolean stripNewLines) {
    return (stripNewLines ? TEXT_SINGLE_LINE_RENDERER : TEXT_RENDERER).render(PARSER.parse(description.trim()));
  }

  /**
   * Parses the specified markdown description and renders it as HTML
   *
   * @param description the markdown description to render as HTML
   * @return an HTML representation of the specified markdown
   */
  public static String getDescriptionAsHTML(String description) {
    return description != null ? HTML_RENDERER.render(PARSER.parse(description.trim())) : null;
  }
}
