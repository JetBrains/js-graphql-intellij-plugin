package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.Type;
import com.intellij.lang.jsgraphql.types.language.UnionTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.UnionTypeExtensionDefinition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionUtil.*;

public class GraphQLUnionTypeCompositeDefinition
  extends GraphQLExtendableCompositeDefinition<UnionTypeDefinition, UnionTypeExtensionDefinition> {

  @NotNull
  @Override
  protected UnionTypeDefinition mergeDefinitions(@NotNull List<UnionTypeDefinition> sourceDefinitions) {
    List<Directive> directives = new ArrayList<>();

    @SuppressWarnings("rawtypes")
    Map<String, Type> memberTypes = new LinkedHashMap<>();

    for (UnionTypeDefinition definition : sourceDefinitions) {
      directives.addAll(definition.getDirectives());
      mergeNodes(memberTypes, mapTypeNodesByKey(definition.getMemberTypes()));
    }

    UnionTypeDefinition definition = ContainerUtil.getFirstItem(sourceDefinitions);
    return definition.transform(builder ->
                                  builder
                                    .directives(directives)
                                    .memberTypes(toList(memberTypes))
                                    .sourceNodes(sourceDefinitions)
    );
  }
}
