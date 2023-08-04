package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInspection;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLUnexpectedTypeInspection;
import com.intellij.lang.jsgraphql.types.language.TypeName;
import com.intellij.lang.jsgraphql.types.language.UnionTypeDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.String.format;

public class UnionMemberNotAnObjectTypeError extends BaseError {
  public UnionMemberNotAnObjectTypeError(@NotNull UnionTypeDefinition definition, @NotNull TypeName memberType) {
    super(definition, format(
      "The member types of a Union type must all be Object base types. Member type '%s' in Union '%s' is invalid.",
      memberType.getName(),
      definition.getName()
    ));
  }

  @Override
  public @Nullable Class<? extends GraphQLInspection> getInspectionClass() {
    return GraphQLUnexpectedTypeInspection.class;
  }
}
