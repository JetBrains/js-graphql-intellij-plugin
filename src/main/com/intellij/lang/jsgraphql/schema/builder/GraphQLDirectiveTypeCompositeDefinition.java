package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.lang.jsgraphql.types.language.DirectiveDefinition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

public class GraphQLDirectiveTypeCompositeDefinition extends GraphQLCompositeDefinition<DirectiveDefinition> {

    @NotNull
    @Override
    protected DirectiveDefinition mergeDefinitions() {
        DirectiveDefinition definition = ContainerUtil.getFirstItem(myDefinitions);
        return definition.transform(builder -> builder.sourceNodes(myDefinitions));
    }

}
