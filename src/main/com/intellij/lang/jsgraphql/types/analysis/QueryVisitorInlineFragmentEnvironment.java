package com.intellij.lang.jsgraphql.types.analysis;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.InlineFragment;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

@PublicApi
public interface QueryVisitorInlineFragmentEnvironment {

    /**
     * @return the graphql schema in play
     */
    GraphQLSchema getSchema();

    InlineFragment getInlineFragment();

    TraverserContext<Node> getTraverserContext();
}
