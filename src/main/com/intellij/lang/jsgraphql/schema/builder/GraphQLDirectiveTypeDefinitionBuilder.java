package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.util.containers.ContainerUtil;
import graphql.language.DirectiveDefinition;
import org.jetbrains.annotations.NotNull;

public class GraphQLDirectiveTypeDefinitionBuilder extends GraphQLDefinitionBuilder<DirectiveDefinition> {

    @NotNull
    @Override
    protected DirectiveDefinition buildDefinitionImpl() {
        // ignore multiple definitions
        return ContainerUtil.getFirstItem(myDefinitions);
    }

    @Override
    protected @NotNull Class<DirectiveDefinition> getDefinitionClass() {
        return DirectiveDefinition.class;
    }

}
