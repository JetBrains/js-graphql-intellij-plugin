package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.FieldDefinition;
import com.intellij.lang.jsgraphql.types.language.ObjectTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.ObjectTypeExtensionDefinition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionUtil.*;

public class GraphQLObjectTypeCompositeDefinition
  extends GraphQLExtendableCompositeDefinition<ObjectTypeDefinition, ObjectTypeExtensionDefinition> {
  @NotNull
  @Override
  protected ObjectTypeDefinition mergeDefinitions(@NotNull List<ObjectTypeDefinition> sourceDefinitions) {
    List<Directive> directives = new ArrayList<>();
    Map<String, FieldDefinition> fieldDefinitions = new LinkedHashMap<>();

    for (ObjectTypeDefinition definition : sourceDefinitions) {
      directives.addAll(definition.getDirectives());
      mergeNodes(fieldDefinitions, mapNamedNodesByKey(definition.getFieldDefinitions()));
    }

    ObjectTypeDefinition definition = ContainerUtil.getFirstItem(sourceDefinitions);
    return definition.transform(builder ->
                                  builder
                                    .directives(directives)
                                    .fieldDefinitions(toList(fieldDefinitions))
                                    .sourceNodes(sourceDefinitions)
    );
  }
}
