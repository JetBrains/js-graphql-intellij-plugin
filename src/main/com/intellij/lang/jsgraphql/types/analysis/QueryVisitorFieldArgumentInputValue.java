package com.intellij.lang.jsgraphql.types.analysis;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.Value;
import com.intellij.lang.jsgraphql.types.schema.GraphQLInputType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLInputValueDefinition;

/**
 * This describes the tree structure that forms from a argument input type,
 * especially with `input ComplexType { ....}` types that might in turn contain other complex
 * types and hence form a tree of values.
 */
@PublicApi
public interface QueryVisitorFieldArgumentInputValue {

    QueryVisitorFieldArgumentInputValue getParent();

    GraphQLInputValueDefinition getInputValueDefinition();

    String getName();

    GraphQLInputType getInputType();

    Value getValue();
}
