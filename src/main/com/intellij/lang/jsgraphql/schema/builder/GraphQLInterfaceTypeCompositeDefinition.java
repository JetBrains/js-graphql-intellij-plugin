package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.FieldDefinition;
import com.intellij.lang.jsgraphql.types.language.InterfaceTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.InterfaceTypeExtensionDefinition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionUtil.*;

public class GraphQLInterfaceTypeCompositeDefinition
  extends GraphQLExtendableCompositeDefinition<InterfaceTypeDefinition, InterfaceTypeExtensionDefinition> {

  @NotNull
  @Override
  protected InterfaceTypeDefinition mergeDefinitions() {
    List<Directive> directives = new ArrayList<>();
    Map<String, FieldDefinition> fieldDefinitions = new LinkedHashMap<>();

    for (InterfaceTypeDefinition definition : myDefinitions) {
      directives.addAll(definition.getDirectives());
      mergeNodes(fieldDefinitions, mapNamedNodesByKey(definition.getFieldDefinitions()));
    }

    InterfaceTypeDefinition definition = ContainerUtil.getFirstItem(myDefinitions);
    return definition.transform(builder ->
                                  builder
                                    .directives(directives)
                                    .definitions(toList(fieldDefinitions))
                                    .sourceNodes(myDefinitions)
    );
  }
}
