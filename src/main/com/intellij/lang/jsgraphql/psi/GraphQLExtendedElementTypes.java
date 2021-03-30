package com.intellij.lang.jsgraphql.psi;

import com.intellij.psi.tree.TokenSet;

import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.*;

public interface GraphQLExtendedElementTypes {
    TokenSet KEYWORDS = TokenSet.create(
        QUERY_KEYWORD, MUTATION_KEYWORD, SUBSCRIPTION_KEYWORD, FRAGMENT_KEYWORD, ON_KEYWORD, SCHEMA_KEYWORD, TYPE_KEYWORD, SCALAR_KEYWORD,
        INTERFACE_KEYWORD, IMPLEMENTS_KEYWORD, ENUM_KEYWORD, UNION_KEYWORD, EXTEND_KEYWORD, INPUT_KEYWORD, DIRECTIVE_KEYWORD, REPEATABLE_KEYWORD);
}
