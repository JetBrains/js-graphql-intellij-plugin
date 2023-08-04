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
import com.intellij.lang.jsgraphql.types.introspection.Introspection;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.*;
import com.intellij.util.containers.ContainerUtil;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.intellij.lang.jsgraphql.types.DirectivesUtil.nonRepeatableDirectivesOnly;
import static com.intellij.lang.jsgraphql.types.introspection.Introspection.DirectiveLocation.INPUT_FIELD_DEFINITION;
import static java.util.stream.Collectors.toList;

/**
 * This helps pre check the state of the type system to ensure it can be made into an executable schema.
 * <p>
 * It looks for missing types and ensure certain invariants are true before a schema can be made.
 */
@SuppressWarnings("rawtypes")
@Internal
public class SchemaTypeChecker {

  public List<GraphQLError> checkTypeRegistry(TypeDefinitionRegistry typeRegistry, RuntimeWiring wiring) throws SchemaProblem {
    List<GraphQLError> errors = new ArrayList<>();
    checkForMissingTypes(errors, typeRegistry);

    SchemaTypeExtensionsChecker typeExtensionsChecker = new SchemaTypeExtensionsChecker();

    typeExtensionsChecker.checkTypeExtensions(errors, typeRegistry);

    ImplementingTypesChecker implementingTypesChecker = new ImplementingTypesChecker();
    implementingTypesChecker.checkImplementingTypes(errors, typeRegistry);

    UnionTypesChecker unionTypesChecker = new UnionTypesChecker();
    unionTypesChecker.checkUnionType(errors, typeRegistry);

    SchemaExtensionsChecker.checkSchemaInvariants(errors, typeRegistry);

    checkFieldsAreSensible(errors, typeRegistry);

    //check directive definitions before checking directive usages
    checkDirectiveDefinitions(typeRegistry, errors);

    SchemaTypeDirectivesChecker directivesChecker = new SchemaTypeDirectivesChecker(typeRegistry, wiring);
    directivesChecker.checkTypeDirectives(errors);

    return errors;
  }

  private void checkForMissingTypes(List<GraphQLError> errors, TypeDefinitionRegistry typeRegistry) {
    // type extensions
    List<ObjectTypeExtensionDefinition> typeExtensions =
      typeRegistry.objectTypeExtensions().values().stream().flatMap(Collection::stream).toList();
    typeExtensions.forEach(typeExtension -> {

      List<Type> implementsTypes = typeExtension.getImplements();
      implementsTypes.forEach(checkInterfaceTypeExists(typeRegistry, errors, typeExtension));

      checkFieldTypesPresent(typeRegistry, errors, typeExtension, typeExtension.getFieldDefinitions());
    });


    Map<String, TypeDefinition> typesMap = typeRegistry.types();

    // objects
    List<ObjectTypeDefinition> objectTypes = filterTo(typesMap, ObjectTypeDefinition.class);
    objectTypes.forEach(objectType -> {

      List<Type> implementsTypes = objectType.getImplements();
      implementsTypes.forEach(checkInterfaceTypeExists(typeRegistry, errors, objectType));

      checkFieldTypesPresent(typeRegistry, errors, objectType, objectType.getFieldDefinitions());
    });

    // interfaces
    List<InterfaceTypeDefinition> interfaceTypes = filterTo(typesMap, InterfaceTypeDefinition.class);
    interfaceTypes.forEach(interfaceType -> {
      List<FieldDefinition> fields = interfaceType.getFieldDefinitions();

      checkFieldTypesPresent(typeRegistry, errors, interfaceType, fields);
    });

    // union types
    List<UnionTypeDefinition> unionTypes = filterTo(typesMap, UnionTypeDefinition.class);
    unionTypes.forEach(unionType -> {
      List<Type> memberTypes = unionType.getMemberTypes();
      memberTypes.forEach(checkTypeExists("union member", typeRegistry, errors, unionType));
    });


    // input types
    List<InputObjectTypeDefinition> inputTypes = filterTo(typesMap, InputObjectTypeDefinition.class);
    inputTypes.forEach(inputType -> {
      List<InputValueDefinition> inputValueDefinitions = inputType.getInputValueDefinitions();
      List<Type> inputValueTypes = ContainerUtil.map(inputValueDefinitions, InputValueDefinition::getType);

      inputValueTypes.forEach(checkTypeExists("input value", typeRegistry, errors, inputType));
    });
  }

  private void checkDirectiveDefinitions(TypeDefinitionRegistry typeRegistry, List<GraphQLError> errors) {

    Stream<DirectiveDefinition> directiveDefinitions = TypeDefinitionRegistry
      .fromSourceNodes(typeRegistry.getDirectiveDefinitions().values().stream(), DirectiveDefinition.class);

    directiveDefinitions.forEach(directiveDefinition -> {
      List<InputValueDefinition> arguments = directiveDefinition.getInputValueDefinitions();

      checkNamedUniqueness(errors, arguments, InputValueDefinition::getName,
                           (name, arg) -> new NonUniqueArgumentError(directiveDefinition, arg));

      List<Type> inputValueTypes = ContainerUtil.map(arguments, InputValueDefinition::getType);

      inputValueTypes.forEach(
        checkTypeExists(typeRegistry, errors, "directive definition", directiveDefinition, directiveDefinition.getName())
      );

      directiveDefinition.getDirectiveLocations().forEach(directiveLocation -> {
        String locationName = directiveLocation.getName();
        try {
          Introspection.DirectiveLocation.valueOf(locationName);
        }
        catch (IllegalArgumentException e) {
          errors.add(new DirectiveIllegalLocationError(directiveDefinition, locationName));
        }
      });
    });
  }

  private void checkFieldsAreSensible(List<GraphQLError> errors, TypeDefinitionRegistry typeRegistry) {
    Map<String, TypeDefinition> typesMap = typeRegistry.types();

    Map<String, DirectiveDefinition> directiveDefinitionMap = typeRegistry.getDirectiveDefinitions();

    // objects
    List<ObjectTypeDefinition> objectTypes = filterTo(typesMap, ObjectTypeDefinition.class);
    objectTypes.forEach(objectType -> checkObjTypeFields(errors, objectType, objectType.getFieldDefinitions(), directiveDefinitionMap));

    // interfaces
    List<InterfaceTypeDefinition> interfaceTypes = filterTo(typesMap, InterfaceTypeDefinition.class);
    interfaceTypes.forEach(
      interfaceType -> checkInterfaceFields(errors, interfaceType, interfaceType.getFieldDefinitions(), directiveDefinitionMap));

    // enum types
    List<EnumTypeDefinition> enumTypes = filterTo(typesMap, EnumTypeDefinition.class);
    enumTypes.forEach(enumType -> checkEnumValues(errors, enumType, enumType.getEnumValueDefinitions(), directiveDefinitionMap));

    // input types
    List<InputObjectTypeDefinition> inputTypes = filterTo(typesMap, InputObjectTypeDefinition.class);
    inputTypes.forEach(inputType -> checkInputValues(errors, inputType, inputType.getInputValueDefinitions(), INPUT_FIELD_DEFINITION,
                                                     directiveDefinitionMap));
  }

  private void checkObjTypeFields(List<GraphQLError> errors,
                                  ObjectTypeDefinition typeDefinition,
                                  List<FieldDefinition> fieldDefinitions,
                                  Map<String, DirectiveDefinition> directiveDefinitionMap) {
    // field unique ness
    checkNamedUniqueness(errors, fieldDefinitions, FieldDefinition::getName,
                         (name, fieldDef) -> new NonUniqueNameError(typeDefinition, fieldDef));

    // field arg unique ness
    fieldDefinitions.forEach(fld -> checkNamedUniqueness(errors, fld.getInputValueDefinitions(), InputValueDefinition::getName,
                                                         (name, inputValueDefinition) -> new NonUniqueArgumentError(typeDefinition, fld,
                                                                                                                    name)));

    // directive checks
    for (FieldDefinition fieldDefinition : fieldDefinitions) {
      List<Directive> directives = fieldDefinition.getDirectives();
      List<Directive> nonRepeatableDirectives = nonRepeatableDirectivesOnly(directiveDefinitionMap, directives);

      checkNamedUniqueness(errors, nonRepeatableDirectives, Directive::getName,
                           (directiveName, directive) -> new NonUniqueDirectiveError(typeDefinition, fieldDefinition, directiveName));
    }
    fieldDefinitions.forEach(fld -> fld.getDirectives().forEach(directive -> {

      checkNamedUniqueness(errors, directive.getArguments(), Argument::getName,
                           (argumentName, argument) -> new NonUniqueArgumentError(typeDefinition, fld, argumentName));
    }));
  }

  private void checkInterfaceFields(List<GraphQLError> errors,
                                    InterfaceTypeDefinition interfaceType,
                                    List<FieldDefinition> fieldDefinitions,
                                    Map<String, DirectiveDefinition> directiveDefinitionMap) {
    // field unique ness
    checkNamedUniqueness(errors, fieldDefinitions, FieldDefinition::getName,
                         (name, fieldDef) -> new NonUniqueNameError(interfaceType, fieldDef));

    // field arg unique ness
    fieldDefinitions.forEach(fld -> checkNamedUniqueness(errors, fld.getInputValueDefinitions(), InputValueDefinition::getName,
                                                         (name, inputValueDefinition) -> new NonUniqueArgumentError(interfaceType, fld,
                                                                                                                    name)));

    // directive checks
    fieldDefinitions.forEach(fieldDefinition -> {
      List<Directive> directives = fieldDefinition.getDirectives();
      List<Directive> nonRepeatableDirectives = nonRepeatableDirectivesOnly(directiveDefinitionMap, directives);

      checkNamedUniqueness(errors, nonRepeatableDirectives, Directive::getName,
                           (directiveName, directive) -> new NonUniqueDirectiveError(interfaceType, fieldDefinition, directiveName));

      directives.forEach(directive -> checkNamedUniqueness(errors, directive.getArguments(), Argument::getName,
                                                           (argumentName, argument) -> new NonUniqueArgumentError(interfaceType,
                                                                                                                  fieldDefinition,
                                                                                                                  argumentName)));
    });
  }

  private void checkEnumValues(List<GraphQLError> errors,
                               EnumTypeDefinition enumType,
                               List<EnumValueDefinition> enumValueDefinitions,
                               Map<String, DirectiveDefinition> directiveDefinitionMap) {

    // enum unique ness
    checkNamedUniqueness(errors, enumValueDefinitions, EnumValueDefinition::getName,
                         (name, inputValueDefinition) -> new NonUniqueNameError(enumType, inputValueDefinition));


    // directive checks
    for (EnumValueDefinition enumValueDefinition : enumValueDefinitions) {
      List<Directive> directives = enumValueDefinition.getDirectives();
      List<Directive> nonRepeatableDirectives = nonRepeatableDirectivesOnly(directiveDefinitionMap, directives);

      checkNamedUniqueness(errors, nonRepeatableDirectives, Directive::getName,
                           (directiveName, directive) -> new NonUniqueDirectiveError(enumType, enumValueDefinition, directiveName));
    }

    enumValueDefinitions.forEach(enumValue -> enumValue.getDirectives().forEach(directive -> {

      BiFunction<String, Argument, NonUniqueArgumentError> errorFunction =
        (argumentName, argument) -> new NonUniqueArgumentError(enumType, enumValue, argumentName);
      checkNamedUniqueness(errors, directive.getArguments(), Argument::getName, errorFunction);
    }));
  }

  private void checkInputValues(List<GraphQLError> errors,
                                InputObjectTypeDefinition inputType,
                                List<InputValueDefinition> inputValueDefinitions,
                                Introspection.DirectiveLocation directiveLocation,
                                Map<String, DirectiveDefinition> directiveDefinitionMap) {

    // field unique ness
    checkNamedUniqueness(errors, inputValueDefinitions, InputValueDefinition::getName,
                         (name, inputValueDefinition) -> {
                           // not sure why this is needed but inlining breaks it
                           @SuppressWarnings("UnnecessaryLocalVariable")
                           InputObjectTypeDefinition as = inputType;
                           return new NonUniqueNameError(as, inputValueDefinition);
                         });


    // directive checks
    for (InputValueDefinition inputValueDefinition : inputValueDefinitions) {
      List<Directive> directives = inputValueDefinition.getDirectives();
      List<Directive> nonRepeatableDirectives = nonRepeatableDirectivesOnly(directiveDefinitionMap, directives);

      checkNamedUniqueness(errors, nonRepeatableDirectives, Directive::getName,
                           (directiveName, directive) -> new NonUniqueDirectiveError(inputType, inputValueDefinition, directiveName));
    }

    inputValueDefinitions.forEach(inputValueDef -> inputValueDef.getDirectives().forEach(directive ->
                                                                                           checkNamedUniqueness(errors,
                                                                                                                directive.getArguments(),
                                                                                                                Argument::getName,
                                                                                                                (argumentName, argument) -> new NonUniqueArgumentError(
                                                                                                                  inputType, inputValueDef,
                                                                                                                  argumentName))));
  }

  /**
   * A simple function that takes a list of things, asks for their names and checks that the
   * names are unique within that list.  If not it calls the error handler function
   *
   * @param errors            the error list
   * @param listOfNamedThings the list of named things
   * @param namer             the function naming a thing
   * @param errorFunction     the function producing an error
   */
  static <T, E extends GraphQLError> void checkNamedUniqueness(List<GraphQLError> errors,
                                                               List<T> listOfNamedThings,
                                                               Function<T, String> namer,
                                                               BiFunction<String, T, E> errorFunction) {
    Set<String> names = new LinkedHashSet<>();
    listOfNamedThings.forEach(thing -> {
      String name = namer.apply(thing);
      if (names.contains(name)) {
        errors.add(errorFunction.apply(name, thing));
      }
      else {
        names.add(name);
      }
    });
  }

  private void checkFieldTypesPresent(TypeDefinitionRegistry typeRegistry,
                                      List<GraphQLError> errors,
                                      TypeDefinition typeDefinition,
                                      List<FieldDefinition> fields) {
    List<Type> fieldTypes = ContainerUtil.map(fields, FieldDefinition::getType);
    fieldTypes.forEach(checkTypeExists("field", typeRegistry, errors, typeDefinition));

    List<Type> fieldInputValues = fields.stream()
      .map(f -> ContainerUtil.map(f.getInputValueDefinitions(), InputValueDefinition::getType))
      .flatMap(Collection::stream)
      .toList();

    fieldInputValues.forEach(checkTypeExists("field input", typeRegistry, errors, typeDefinition));
  }


  private Consumer<Type> checkTypeExists(String typeOfType,
                                         TypeDefinitionRegistry typeRegistry,
                                         List<GraphQLError> errors,
                                         TypeDefinition typeDefinition) {
    return t -> {
      TypeName unwrapped = TypeInfo.typeInfo(t).getTypeName();
      if (!typeRegistry.hasType(unwrapped)) {
        errors.add(new MissingTypeError(typeOfType, typeDefinition, unwrapped));
      }
    };
  }

  private Consumer<Type> checkTypeExists(TypeDefinitionRegistry typeRegistry,
                                         List<GraphQLError> errors,
                                         String typeOfType,
                                         Node element,
                                         String elementName) {
    return ivType -> {
      TypeName unwrapped = TypeInfo.typeInfo(ivType).getTypeName();
      if (!typeRegistry.hasType(unwrapped)) {
        errors.add(new MissingTypeError(typeOfType, element, elementName, unwrapped));
      }
    };
  }

  private Consumer<? super Type> checkInterfaceTypeExists(TypeDefinitionRegistry typeRegistry,
                                                          List<GraphQLError> errors,
                                                          TypeDefinition typeDefinition) {
    return t -> {
      TypeInfo typeInfo = TypeInfo.typeInfo(t);
      TypeName unwrapped = typeInfo.getTypeName();
      Optional<TypeDefinition> type = typeRegistry.getType(unwrapped);
      if (type.isEmpty()) {
        errors.add(new MissingInterfaceTypeError("interface", typeDefinition, unwrapped));
      }
      else if (!(type.get() instanceof InterfaceTypeDefinition)) {
        errors.add(new MissingInterfaceTypeError("interface", typeDefinition, unwrapped));
      }
    };
  }

  private <T extends TypeDefinition> List<T> filterTo(Map<String, TypeDefinition> types, Class<? extends T> clazz) {
    return types.values().stream()
      .filter(t -> clazz.equals(t.getClass()))
      .map(clazz::cast)
      .collect(toList());
  }
}
