package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInspection;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLMemberRedefinitionInspection;
import com.intellij.lang.jsgraphql.types.language.TypeName;
import com.intellij.lang.jsgraphql.types.language.UnionTypeDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.String.format;

public class UnionMemberNotUniqueError extends BaseError {
  public UnionMemberNotUniqueError(@NotNull UnionTypeDefinition definition, @NotNull TypeName memberType) {
    super(definition,
          format(
            "The member type '%s' in Union '%s' is not unique. The member types of a Union type must be unique.",
            memberType.getName(),
            definition.getName()
          )
    );
  }

  @Override
  public @Nullable Class<? extends GraphQLInspection> getInspectionClass() {
    return GraphQLMemberRedefinitionInspection.class;
  }
}
