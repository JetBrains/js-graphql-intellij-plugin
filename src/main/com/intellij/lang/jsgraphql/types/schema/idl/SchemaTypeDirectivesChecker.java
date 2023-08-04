/*
    The MIT License (MIT)

    Copyright (c) 2015 Andreas Marek and Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
    (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
    publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
    so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.introspection.Introspection.DirectiveLocation;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.intellij.lang.jsgraphql.types.introspection.Introspection.DirectiveLocation.*;
import static com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry.fromSourceNodes;
import static com.intellij.lang.jsgraphql.types.util.FpKit.getByName;
import static com.intellij.lang.jsgraphql.types.util.FpKit.mergeFirst;

/**
 * This is responsible for traversing EVERY type and field in the registry and ensuring that
 * any directives used follow the directive definition rules, for example
 * field directives can be used on object types
 */
@Internal
class SchemaTypeDirectivesChecker {

  private final TypeDefinitionRegistry typeRegistry;
  private final RuntimeWiring runtimeWiring;

  public SchemaTypeDirectivesChecker(final TypeDefinitionRegistry typeRegistry,
                                     final RuntimeWiring runtimeWiring) {
    this.typeRegistry = typeRegistry;
    this.runtimeWiring = runtimeWiring;
  }

  void checkTypeDirectives(List<GraphQLError> errors) {
    typeRegistry.objectTypeExtensions().values()
      .forEach(extDefinitions -> extDefinitions.forEach(ext -> checkDirectives(OBJECT, errors, ext)));
    typeRegistry.interfaceTypeExtensions().values()
      .forEach(extDefinitions -> extDefinitions.forEach(ext -> checkDirectives(INTERFACE, errors, ext)));
    typeRegistry.unionTypeExtensions().values()
      .forEach(extDefinitions -> extDefinitions.forEach(ext -> checkDirectives(UNION, errors, ext)));
    typeRegistry.enumTypeExtensions().values()
      .forEach(extDefinitions -> extDefinitions.forEach(ext -> checkDirectives(ENUM, errors, ext)));
    typeRegistry.scalarTypeExtensions().values()
      .forEach(extDefinitions -> extDefinitions.forEach(ext -> checkDirectives(SCALAR, errors, ext)));
    typeRegistry.inputObjectTypeExtensions().values()
      .forEach(extDefinitions -> extDefinitions.forEach(ext -> checkDirectives(INPUT_OBJECT, errors, ext)));

    typeRegistry.getTypes(ObjectTypeDefinition.class, true)
      .forEach(typeDef -> checkDirectives(OBJECT, errors, typeDef));
    typeRegistry.getTypes(InterfaceTypeDefinition.class, true)
      .forEach(typeDef -> checkDirectives(INTERFACE, errors, typeDef));
    typeRegistry.getTypes(UnionTypeDefinition.class, true)
      .forEach(typeDef -> checkDirectives(UNION, errors, typeDef));
    typeRegistry.getTypes(EnumTypeDefinition.class, true)
      .forEach(typeDef -> checkDirectives(ENUM, errors, typeDef));
    typeRegistry.getTypes(InputObjectTypeDefinition.class, true)
      .forEach(typeDef -> checkDirectives(INPUT_OBJECT, errors, typeDef));

    fromSourceNodes(typeRegistry.scalars().values().stream(), ScalarTypeDefinition.class)
      .forEach(typeDef -> checkDirectives(SCALAR, errors, typeDef));

    // we need to have a Node for error reporting so we make one in case there is not one
    Stream<SchemaDefinition> schemaDefinitions = fromSourceNodes(
      Stream.of(typeRegistry.schemaDefinition().orElse(SchemaDefinition.newSchemaDefinition().build())),
      SchemaDefinition.class
    );

    schemaDefinitions.forEach(schemaDefinition -> {
      List<Directive> directives = SchemaExtensionsChecker.gatherSchemaDirectives(schemaDefinition, typeRegistry);
      checkDirectives(DirectiveLocation.SCHEMA, errors, typeRegistry, schemaDefinition, "schema", directives);
    });

    Collection<DirectiveDefinition> directiveDefinitions = typeRegistry.getDirectiveDefinitions().values();
    commonCheck(directiveDefinitions, errors);
  }


  private void checkDirectives(DirectiveLocation expectedLocation, List<GraphQLError> errors, TypeDefinition<?> typeDef) {
    checkDirectives(expectedLocation, errors, typeRegistry, typeDef, typeDef.getName(), typeDef.getDirectives());

    if (typeDef instanceof ObjectTypeDefinition) {
      List<FieldDefinition> fieldDefinitions = ((ObjectTypeDefinition)typeDef).getFieldDefinitions();
      checkFieldsDirectives(errors, typeRegistry, fieldDefinitions);
    }
    if (typeDef instanceof InterfaceTypeDefinition) {
      List<FieldDefinition> fieldDefinitions = ((InterfaceTypeDefinition)typeDef).getFieldDefinitions();
      checkFieldsDirectives(errors, typeRegistry, fieldDefinitions);
    }
    if (typeDef instanceof EnumTypeDefinition) {
      List<EnumValueDefinition> enumValueDefinitions = ((EnumTypeDefinition)typeDef).getEnumValueDefinitions();
      enumValueDefinitions.forEach(
        definition -> checkDirectives(ENUM_VALUE, errors, typeRegistry, definition, definition.getName(), definition.getDirectives()));
    }
    if (typeDef instanceof InputObjectTypeDefinition) {
      List<InputValueDefinition> inputValueDefinitions = ((InputObjectTypeDefinition)typeDef).getInputValueDefinitions();
      inputValueDefinitions.forEach(
        definition -> checkDirectives(INPUT_FIELD_DEFINITION, errors, typeRegistry, definition, definition.getName(),
                                      definition.getDirectives()));
    }
  }

  private void checkFieldsDirectives(List<GraphQLError> errors,
                                     TypeDefinitionRegistry typeRegistry,
                                     List<FieldDefinition> fieldDefinitions) {
    fieldDefinitions.forEach(definition -> {
      checkDirectives(FIELD_DEFINITION, errors, typeRegistry, definition, definition.getName(), definition.getDirectives());
      //
      // and check its arguments
      definition.getInputValueDefinitions()
        .forEach(arg -> checkDirectives(ARGUMENT_DEFINITION, errors, typeRegistry, arg, arg.getName(), arg.getDirectives()));
    });
  }

  private void checkDirectives(DirectiveLocation expectedLocation,
                               List<GraphQLError> errors,
                               TypeDefinitionRegistry typeRegistry,
                               Node<?> element,
                               String elementName,
                               List<Directive> directives) {
    directives.forEach(directive -> {
      Optional<DirectiveDefinition> directiveDefinition = typeRegistry.getDirectiveDefinition(directive.getName());
      if (directiveDefinition.isEmpty()) {
        errors.add(new DirectiveUndeclaredError(element, elementName, directive.getName()));
      }
      else {
        if (!inRightLocation(expectedLocation, directiveDefinition.get())) {
          errors.add(new DirectiveIllegalLocationError(element, elementName, directive.getName(), expectedLocation.name()));
        }
        checkDirectiveArguments(errors, typeRegistry, element, elementName, directive, directiveDefinition.get());
      }
    });
  }

  private boolean inRightLocation(DirectiveLocation expectedLocation, DirectiveDefinition directiveDefinition) {
    List<String> names = directiveDefinition.getDirectiveLocations()
      .stream().map(com.intellij.lang.jsgraphql.types.language.DirectiveLocation::getName)
      .map(String::toUpperCase)
      .toList();

    return names.contains(expectedLocation.name().toUpperCase());
  }

  private void checkDirectiveArguments(List<GraphQLError> errors,
                                       TypeDefinitionRegistry typeRegistry,
                                       Node element,
                                       String elementName,
                                       Directive directive,
                                       DirectiveDefinition directiveDefinition) {
    Map<String, InputValueDefinition> allowedArgs =
      getByName(directiveDefinition.getInputValueDefinitions(), (InputValueDefinition::getName), mergeFirst());
    Map<String, Argument> providedArgs = getByName(directive.getArguments(), (Argument::getName), mergeFirst());
    directive.getArguments().forEach(argument -> {
      InputValueDefinition allowedArg = allowedArgs.get(argument.getName());
      if (allowedArg == null) {
        errors.add(new DirectiveUnknownArgumentError(element, elementName, directive.getName(), argument.getName()));
      }
      else {
        ArgValueOfAllowedTypeChecker argValueOfAllowedTypeChecker =
          new ArgValueOfAllowedTypeChecker(directive, element, elementName, argument, typeRegistry, runtimeWiring);
        argValueOfAllowedTypeChecker.checkArgValueMatchesAllowedType(errors, argument.getValue(), allowedArg.getType());
      }
    });
    allowedArgs.forEach((argName, definitionArgument) -> {
      if (isNoNullArgWithoutDefaultValue(definitionArgument)) {
        if (!providedArgs.containsKey(argName)) {
          errors.add(new DirectiveMissingNonNullArgumentError(element, elementName, directive.getName(), argName));
        }
      }
    });
  }

  private boolean isNoNullArgWithoutDefaultValue(InputValueDefinition definitionArgument) {
    return definitionArgument.getType() instanceof NonNullType && definitionArgument.getDefaultValue() == null;
  }

  private void commonCheck(Collection<DirectiveDefinition> directiveDefinitions, List<GraphQLError> errors) {
    directiveDefinitions.forEach(directiveDefinition -> {
      assertTypeName(directiveDefinition, errors);
      directiveDefinition.getInputValueDefinitions().forEach(inputValueDefinition -> {
        assertTypeName(inputValueDefinition, errors);
        assertExistAndIsInputType(inputValueDefinition, errors);
        if (inputValueDefinition.hasDirective(directiveDefinition.getName())) {
          errors.add(new DirectiveIllegalReferenceError(directiveDefinition, inputValueDefinition));
        }
      });
    });
  }

  private void assertTypeName(NamedNode node, List<GraphQLError> errors) {
    if (node.getName().length() >= 2 && node.getName().startsWith("__")) {
      errors.add((new IllegalNameError(node)));
    }
  }

  public void assertExistAndIsInputType(InputValueDefinition definition, List<GraphQLError> errors) {
    TypeName namedType = TypeUtil.unwrapAll(definition.getType());

    TypeDefinition unwrappedType = findTypeDefFromRegistry(namedType.getName(), typeRegistry);

    if (unwrappedType == null) {
      errors.add(new MissingTypeError(namedType.getName(), definition, definition.getName()));
    }
  }

  private TypeDefinition findTypeDefFromRegistry(String typeName, TypeDefinitionRegistry typeRegistry) {
    return typeRegistry.getType(typeName).orElseGet(() -> typeRegistry.scalars().get(typeName));
  }
}
