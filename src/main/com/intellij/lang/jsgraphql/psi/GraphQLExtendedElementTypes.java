package com.intellij.lang.jsgraphql.psi;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.TokenSet;

import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.BLOCK_STRING;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.CLOSING_QUOTE;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.CLOSING_TRIPLE_QUOTE;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.DIRECTIVE_KEYWORD;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.ENUM_KEYWORD;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.EOL_COMMENT;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.EXTEND_KEYWORD;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.FLOAT;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.FRAGMENT_KEYWORD;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.IMPLEMENTS_KEYWORD;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.INPUT_KEYWORD;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.INTERFACE_KEYWORD;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.MUTATION_KEYWORD;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.NUMBER;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.ON_KEYWORD;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.OPEN_QUOTE;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.OPEN_TRIPLE_QUOTE;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.QUERY_KEYWORD;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.QUOTED_STRING;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.REGULAR_STRING_PART;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.REPEATABLE_KEYWORD;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.SCALAR_KEYWORD;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.SCHEMA_KEYWORD;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.SUBSCRIPTION_KEYWORD;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.TYPE_KEYWORD;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.UNION_KEYWORD;

public interface GraphQLExtendedElementTypes {

  TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
  TokenSet COMMENTS = TokenSet.create(EOL_COMMENT);

  TokenSet KEYWORDS = TokenSet.create(
    QUERY_KEYWORD, MUTATION_KEYWORD, SUBSCRIPTION_KEYWORD, FRAGMENT_KEYWORD, ON_KEYWORD, SCHEMA_KEYWORD, TYPE_KEYWORD, SCALAR_KEYWORD,
    INTERFACE_KEYWORD, IMPLEMENTS_KEYWORD, ENUM_KEYWORD, UNION_KEYWORD, EXTEND_KEYWORD, INPUT_KEYWORD, DIRECTIVE_KEYWORD,
    REPEATABLE_KEYWORD);

  TokenSet SINGLE_QUOTES = TokenSet.create(OPEN_QUOTE, CLOSING_QUOTE);
  TokenSet TRIPLE_QUOTES = TokenSet.create(OPEN_TRIPLE_QUOTE, CLOSING_TRIPLE_QUOTE);
  TokenSet QUOTES = TokenSet.orSet(SINGLE_QUOTES, TRIPLE_QUOTES);
  TokenSet STRING_TOKENS = TokenSet.orSet(QUOTES, TokenSet.create(REGULAR_STRING_PART));
  TokenSet STRING_LITERALS = TokenSet.create(QUOTED_STRING, BLOCK_STRING);

  TokenSet NUMBER_LITERALS = TokenSet.create(NUMBER, FLOAT);
}
