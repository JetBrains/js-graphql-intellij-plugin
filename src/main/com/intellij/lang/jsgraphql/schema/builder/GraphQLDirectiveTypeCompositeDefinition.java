package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.lang.jsgraphql.types.language.DirectiveDefinition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GraphQLDirectiveTypeCompositeDefinition extends GraphQLCompositeDefinition<DirectiveDefinition> {

  @Override
  protected @NotNull DirectiveDefinition mergeDefinitions(@NotNull List<DirectiveDefinition> sourceDefinitions) {
    DirectiveDefinition definition = ContainerUtil.getFirstItem(sourceDefinitions);
    return definition.transform(builder -> builder.sourceNodes(sourceDefinitions));
  }
}
