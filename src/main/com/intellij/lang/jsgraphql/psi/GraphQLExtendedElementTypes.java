package com.intellij.lang.jsgraphql.psi;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.TokenSet;

import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.*;

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
