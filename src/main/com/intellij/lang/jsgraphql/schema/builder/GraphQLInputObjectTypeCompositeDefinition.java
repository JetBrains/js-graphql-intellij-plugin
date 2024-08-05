package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.InputObjectTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.InputObjectTypeExtensionDefinition;
import com.intellij.lang.jsgraphql.types.language.InputValueDefinition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionUtil.*;

public class GraphQLInputObjectTypeCompositeDefinition
  extends GraphQLExtendableCompositeDefinition<InputObjectTypeDefinition, InputObjectTypeExtensionDefinition> {

  @Override
  protected @NotNull InputObjectTypeDefinition mergeDefinitions(@NotNull List<InputObjectTypeDefinition> sourceDefinitions) {
    List<Directive> directives = new ArrayList<>();
    Map<String, InputValueDefinition> inputValueDefinitions = new LinkedHashMap<>();

    for (InputObjectTypeDefinition definition : sourceDefinitions) {
      directives.addAll(definition.getDirectives());
      mergeNodes(inputValueDefinitions, mapNamedNodesByKey(definition.getInputValueDefinitions()));
    }

    InputObjectTypeDefinition definition = ContainerUtil.getFirstItem(sourceDefinitions);
    return definition.transform(builder ->
                                  builder
                                    .directives(directives)
                                    .inputValueDefinitions(toList(inputValueDefinitions))
                                    .sourceNodes(sourceDefinitions)
    );
  }
}
