package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.ScalarTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.ScalarTypeExtensionDefinition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GraphQLScalarTypeCompositeDefinition
  extends GraphQLExtendableCompositeDefinition<ScalarTypeDefinition, ScalarTypeExtensionDefinition> {

  @NotNull
  @Override
  protected ScalarTypeDefinition mergeDefinitions(@NotNull List<ScalarTypeDefinition> sourceDefinitions) {
    List<Directive> directives = new ArrayList<>();

    for (ScalarTypeDefinition definition : sourceDefinitions) {
      directives.addAll(definition.getDirectives());
    }

    ScalarTypeDefinition definition = ContainerUtil.getFirstItem(sourceDefinitions);
    return definition.transform(builder ->
                                  builder
                                    .directives(directives)
                                    .sourceNodes(sourceDefinitions)
    );
  }
}
