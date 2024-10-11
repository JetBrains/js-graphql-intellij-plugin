package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInspection;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInterfaceImplementationInspection;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.FieldDefinition;
import com.intellij.lang.jsgraphql.types.language.ImplementingTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.InputValueDefinition;
import com.intellij.lang.jsgraphql.types.language.InterfaceTypeDefinition;
import org.jetbrains.annotations.Nullable;

import static java.lang.String.format;

@Internal
public class MissingInterfaceFieldArgumentError extends BaseError {
  public MissingInterfaceFieldArgumentError(String typeOfType,
                                            ImplementingTypeDefinition typeDefinition,
                                            InterfaceTypeDefinition interfaceTypeDef,
                                            FieldDefinition objectFieldDef,
                                            InputValueDefinition interfaceArgDef) {
    super(objectFieldDef, format("The %s type '%s' field '%s' is missing the argument '%s' specified via interface '%s'",
                                 typeOfType, typeDefinition.getName(), objectFieldDef.getName(), interfaceArgDef.getName(),
                                 interfaceTypeDef.getName()));
  }

  @Override
  public @Nullable Class<? extends GraphQLInspection> getInspectionClass() {
    return GraphQLInterfaceImplementationInspection.class;
  }
}
