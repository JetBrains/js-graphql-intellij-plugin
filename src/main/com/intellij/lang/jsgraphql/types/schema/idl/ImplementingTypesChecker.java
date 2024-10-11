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
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.intellij.lang.jsgraphql.schema.builder.GraphQLRegistryBuilderUtil.mapNamedNodesByKey;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * A support class to help break up the large SchemaTypeChecker class. This handles
 * the checking of {@link com.intellij.lang.jsgraphql.types.language.ImplementingTypeDefinition}s.
 */
@Internal
class ImplementingTypesChecker {
  private static final Map<Class<? extends ImplementingTypeDefinition>, String> TYPE_OF_MAP = new HashMap<>();

  static {
    TYPE_OF_MAP.put(ObjectTypeDefinition.class, "object");
    TYPE_OF_MAP.put(ObjectTypeExtensionDefinition.class, "object extension");
    TYPE_OF_MAP.put(InterfaceTypeDefinition.class, "interface");
    TYPE_OF_MAP.put(InterfaceTypeExtensionDefinition.class, "interface extension");
  }

  /*
   * "Implementing types" (i.e.: types that might implement interfaces) have the potential to be invalid if incorrectly defined.
   *
   * The same interface might not be implemented more than once by a type and its extensions
   * The implementing type must implement all the transitive interfaces
   * An interface implementation must not result in a circular reference (i.e.: an interface implementing itself)
   * All fields declared by an interface have to be correctly declared by its implementing type, including the proper field arguments
   */
  void checkImplementingTypes(List<GraphQLError> errors, TypeDefinitionRegistry typeRegistry) {
    List<InterfaceTypeDefinition> interfaces = typeRegistry.getTypes(InterfaceTypeDefinition.class);
    List<ObjectTypeDefinition> objects = typeRegistry.getTypes(ObjectTypeDefinition.class);

    TypeDefinitionRegistry.fromSourceNodes(Stream.of(interfaces.stream(), objects.stream())
                                             .flatMap(Function.identity()), ImplementingTypeDefinition.class)
      .forEach(type -> checkImplementingType(errors, typeRegistry, type));
  }

  private void checkImplementingType(
    List<GraphQLError> errors,
    TypeDefinitionRegistry typeRegistry,
    ImplementingTypeDefinition type) {

    Map<InterfaceTypeDefinition, ImplementingTypeDefinition> implementedInterfaces =
      checkInterfacesNotImplementedMoreThanOnce(errors, type, typeRegistry);

    checkInterfaceIsImplemented(errors, typeRegistry, type, implementedInterfaces);

    checkAncestorImplementation(errors, typeRegistry, type, implementedInterfaces);
  }

  private Map<InterfaceTypeDefinition, ImplementingTypeDefinition> checkInterfacesNotImplementedMoreThanOnce(
    List<GraphQLError> errors,
    ImplementingTypeDefinition type,
    TypeDefinitionRegistry typeRegistry
  ) {
    Map<InterfaceTypeDefinition, List<ImplementingTypeDefinition>> implementedInterfaces =
      getLogicallyImplementedInterfaces(type, typeRegistry);

    Map<InterfaceTypeDefinition, ImplementingTypeDefinition> interfacesImplementedOnce = implementedInterfaces.entrySet()
      .stream()
      .filter(entry -> entry.getValue().size() == 1)
      .collect(toMap(
        Map.Entry::getKey,
        entry -> entry.getValue().get(0)
      ));

    implementedInterfaces.entrySet().stream()
      .filter(entry -> !interfacesImplementedOnce.containsKey(entry.getKey()))
      .forEach(entry -> {
        entry.getValue().forEach(offendingType -> {
          errors.add(new InterfaceImplementedMoreThanOnceError(TYPE_OF_MAP.get(offendingType.getClass()), offendingType, entry.getKey()));
        });
      });

    return interfacesImplementedOnce;
  }

  private void checkAncestorImplementation(
    List<GraphQLError> errors,
    TypeDefinitionRegistry typeRegistry,
    ImplementingTypeDefinition type,
    Map<InterfaceTypeDefinition, ImplementingTypeDefinition> implementedInterfaces) {

    if (implementedInterfaces.containsKey(type)) {
      errors.add(new InterfaceImplementingItselfError(TYPE_OF_MAP.get(type.getClass()), type));
      return;
    }

    implementedInterfaces.forEach((implementedInterface, implementingType) -> {
      Set<InterfaceTypeDefinition> transitiveInterfaces = getLogicallyImplementedInterfaces(implementedInterface, typeRegistry).keySet();

      transitiveInterfaces.forEach(transitiveInterface -> {
        if (transitiveInterface.equals(type)) {
          errors.add(new InterfaceWithCircularImplementationHierarchyError(TYPE_OF_MAP.get(type.getClass()), type, implementedInterface));
        }
        else if (!implementedInterfaces.containsKey(transitiveInterface)) {
          errors.add(
            new MissingTransitiveInterfaceError(TYPE_OF_MAP.get(implementingType.getClass()), implementingType, implementedInterface,
                                                transitiveInterface));
        }
      });
    });
  }

  private void checkInterfaceIsImplemented(
    List<GraphQLError> errors,
    TypeDefinitionRegistry typeRegistry,
    ImplementingTypeDefinition type,
    Map<InterfaceTypeDefinition, ImplementingTypeDefinition> implementedInterfaces
  ) {
    Set<FieldDefinition> fieldDefinitions = getLogicallyDeclaredFields(type, typeRegistry);

    Map<String, FieldDefinition> typeFields = fieldDefinitions.stream()
      .collect(toMap(FieldDefinition::getName, Function.identity(), mergeFirstValue()));

    implementedInterfaces.forEach((implementedInterface, implementingType) -> {
      implementedInterface.getFieldDefinitions().forEach(interfaceFieldDef -> {
        FieldDefinition typeFieldDef = typeFields.get(interfaceFieldDef.getName());
        if (typeFieldDef == null) {
          errors.add(new MissingInterfaceFieldError(TYPE_OF_MAP.get(implementingType.getClass()), implementingType, implementedInterface,
                                                    interfaceFieldDef));
        }
        else {
          if (!typeRegistry.isSubTypeOf(typeFieldDef.getType(), interfaceFieldDef.getType())) {
            String interfaceFieldType = AstPrinter.printAst(interfaceFieldDef.getType());
            String objectFieldType = AstPrinter.printAst(typeFieldDef.getType());
            errors.add(
              new InterfaceFieldRedefinitionError(TYPE_OF_MAP.get(implementingType.getClass()), implementingType, implementedInterface,
                                                  typeFieldDef, objectFieldType, interfaceFieldType));
          }

          // look at arguments
          List<InputValueDefinition> objectArgs = typeFieldDef.getInputValueDefinitions();
          List<InputValueDefinition> interfaceArgs = interfaceFieldDef.getInputValueDefinitions();
          if (objectArgs.size() < interfaceArgs.size()) {
            errors.add(
              new MissingInterfaceFieldArgumentsError(TYPE_OF_MAP.get(implementingType.getClass()), implementingType, implementedInterface,
                                                      typeFieldDef));
          }
          else {
            checkArgumentConsistency(TYPE_OF_MAP.get(implementingType.getClass()), implementingType, implementedInterface, typeFieldDef,
                                     interfaceFieldDef, errors);
          }
        }
      });
    });
  }

  private void checkArgumentConsistency(
    String typeOfType,
    ImplementingTypeDefinition objectTypeDef,
    InterfaceTypeDefinition interfaceTypeDef,
    FieldDefinition objectFieldDef,
    FieldDefinition interfaceFieldDef,
    List<GraphQLError> errors
  ) {
    List<InputValueDefinition> objectArgs = objectFieldDef.getInputValueDefinitions();
    List<InputValueDefinition> interfaceArgs = interfaceFieldDef.getInputValueDefinitions();
    Map<String, InputValueDefinition> objectArgsByName = mapNamedNodesByKey(objectArgs);

    for (int i = 0; i < interfaceArgs.size(); i++) {
      InputValueDefinition interfaceArgDef = interfaceArgs.get(i);
      String argName = interfaceArgDef.getName();
      InputValueDefinition objectArgDef = objectArgsByName.get(argName);
      if (objectArgDef == null) {
        errors.add(new MissingInterfaceFieldArgumentError(typeOfType, objectTypeDef, interfaceTypeDef, objectFieldDef, interfaceArgDef));
        continue;
      }

      String interfaceArgStr = AstPrinter.printAstCompact(interfaceArgDef);
      String objectArgStr = AstPrinter.printAstCompact(objectArgDef);
      if (!interfaceArgStr.equals(objectArgStr)) {
        errors.add(new InterfaceFieldArgumentRedefinitionError(
          typeOfType, objectTypeDef, interfaceTypeDef, objectFieldDef, objectArgStr, interfaceArgStr, interfaceArgDef, objectArgDef));
      }
    }

    if (objectArgs.size() > interfaceArgs.size()) {
      Map<String, InputValueDefinition> interfaceArgsByName = mapNamedNodesByKey(interfaceArgs);
      for (InputValueDefinition objectArg : objectArgs) {
        if (interfaceArgsByName.containsKey(objectArg.getName())) {
          continue;
        }

        if (objectArg.getType() instanceof NonNullType) {
          errors.add(new InterfaceFieldArgumentNotOptionalError(
            typeOfType, objectTypeDef, interfaceTypeDef, objectFieldDef, objectArg));
        }
      }
    }
  }

  private Map<InterfaceTypeDefinition, List<ImplementingTypeDefinition>> getLogicallyImplementedInterfaces(
    ImplementingTypeDefinition type,
    TypeDefinitionRegistry typeRegistry
  ) {

    Stream<ImplementingTypeDefinition> extensions = Stream.concat(
      typeRegistry.interfaceTypeExtensions().getOrDefault(type.getName(), emptyList()).stream(),
      typeRegistry.objectTypeExtensions().getOrDefault(type.getName(), emptyList()).stream()
    );

    return Stream.concat(Stream.of(type), extensions)
      .collect(HashMap::new, (map, implementingType) -> {
        List<Type> implementedInterfaces = implementingType.getImplements();

        toInterfaceTypeDefinitions(typeRegistry, implementedInterfaces).forEach(implemented -> {
          List<ImplementingTypeDefinition> implementingTypes = map.getOrDefault(implemented, new ArrayList<>());
          implementingTypes.add(implementingType);
          map.put(implemented, implementingTypes);
        });
      }, HashMap::putAll);
  }

  private Set<FieldDefinition> getLogicallyDeclaredFields(
    ImplementingTypeDefinition type,
    TypeDefinitionRegistry typeRegistry
  ) {

    Stream<ImplementingTypeDefinition> extensions = Stream.concat(
      typeRegistry.interfaceTypeExtensions().getOrDefault(type.getName(), emptyList()).stream(),
      typeRegistry.objectTypeExtensions().getOrDefault(type.getName(), emptyList()).stream()
    );

    return Stream.concat(Stream.of(type), extensions)
      .flatMap(implementingType -> {
        List<FieldDefinition> fieldDefinitions = implementingType.getFieldDefinitions();
        return fieldDefinitions.stream();
      })
      .collect(toSet());
  }

  private <T> BinaryOperator<T> mergeFirstValue() {
    return (v1, v2) -> v1;
  }

  private @NotNull Optional<InterfaceTypeDefinition> toInterfaceTypeDefinition(Type type, TypeDefinitionRegistry typeRegistry) {
    TypeInfo typeInfo = TypeInfo.typeInfo(type);
    TypeName unwrapped = typeInfo.getTypeName();

    return typeRegistry.getType(unwrapped, InterfaceTypeDefinition.class);
  }

  private Set<InterfaceTypeDefinition> toInterfaceTypeDefinitions(TypeDefinitionRegistry typeRegistry, Collection<Type> implementsTypes) {
    return TypeDefinitionRegistry.fromSourceNodes(
      implementsTypes.stream()
        .map(t -> toInterfaceTypeDefinition(t, typeRegistry))
        .filter(Optional::isPresent)
        .map(Optional::get),
      InterfaceTypeDefinition.class
    ).collect(toSet());
  }
}
