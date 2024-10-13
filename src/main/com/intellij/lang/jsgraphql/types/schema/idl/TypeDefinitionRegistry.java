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

import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.DirectiveRedefinitionError;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.SchemaProblem;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.SchemaRedefinitionError;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.TypeRedefinitionError;
import com.intellij.lang.jsgraphql.types.util.FpKit;
import com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.lang.jsgraphql.types.schema.idl.SchemaExtensionsChecker.defineOperationDefs;
import static com.intellij.lang.jsgraphql.types.schema.idl.SchemaExtensionsChecker.gatherOperationDefs;
import static java.util.Optional.ofNullable;

@SuppressWarnings({"rawtypes", "UnusedReturnValue"})
@PublicApi
public class TypeDefinitionRegistry {
  private final Map<String, List<ObjectTypeExtensionDefinition>> objectTypeExtensions = new LinkedHashMap<>();
  private final Map<String, List<InterfaceTypeExtensionDefinition>> interfaceTypeExtensions = new LinkedHashMap<>();
  private final Map<String, List<UnionTypeExtensionDefinition>> unionTypeExtensions = new LinkedHashMap<>();
  private final Map<String, List<EnumTypeExtensionDefinition>> enumTypeExtensions = new LinkedHashMap<>();
  private final Map<String, List<ScalarTypeExtensionDefinition>> scalarTypeExtensions = new LinkedHashMap<>();
  private final Map<String, List<InputObjectTypeExtensionDefinition>> inputObjectTypeExtensions = new LinkedHashMap<>();

  private final Map<String, TypeDefinition> types = new LinkedHashMap<>();
  private final Map<String, ScalarTypeDefinition> scalarTypes = new LinkedHashMap<>();
  private final Map<String, DirectiveDefinition> directiveDefinitions = new LinkedHashMap<>();
  private SchemaDefinition schema;
  private final List<SchemaExtensionDefinition> schemaExtensionDefinitions = new ArrayList<>();

  private final List<GraphQLException> myErrors = new ArrayList<>();

  public static <T extends Node> Stream<T> fromSourceNodes(@NotNull Stream<T> definitions, @NotNull Class<T> targetClass) {
    //noinspection unchecked
    return definitions
      .flatMap(def -> def.isComposite() ? (Stream<? extends Node>)def.getSourceNodes().stream() : Stream.of(def))
      .filter(targetClass::isInstance)
      .map(targetClass::cast);
  }

  public static <T extends Node> List<T> fromSourceNodes(@NotNull List<T> nodes, @NotNull Class<T> targetClass) {
    return fromSourceNodes(nodes.stream(), targetClass).collect(Collectors.toList());
  }

  public @NotNull List<GraphQLException> getErrors() {
    return Collections.unmodifiableList(myErrors);
  }

  public void addError(@NotNull GraphQLException error) {
    myErrors.add(error);
  }

  /**
   * This will merge these type registries together and return this one
   *
   * @param typeRegistry the registry to be merged into this one
   * @return this registry
   * @throws SchemaProblem if there are problems merging the types such as redefinitions
   */
  public TypeDefinitionRegistry merge(TypeDefinitionRegistry typeRegistry) {
    Map<String, TypeDefinition> tempTypes = new LinkedHashMap<>();
    typeRegistry.types.values().forEach(newEntry -> define(this.types, tempTypes, newEntry));

    Map<String, DirectiveDefinition> tempDirectiveDefs = new LinkedHashMap<>();
    typeRegistry.directiveDefinitions.values().forEach(newEntry -> define(this.directiveDefinitions, tempDirectiveDefs, newEntry));

    Map<String, ScalarTypeDefinition> tempScalarTypes = new LinkedHashMap<>();
    typeRegistry.scalarTypes.values().forEach(newEntry -> define(this.scalarTypes, tempScalarTypes, newEntry));

    checkMergeSchemaDefs(typeRegistry);

    if (this.schema == null) {
      // ensure schema is not overwritten by merge
      this.schema = typeRegistry.schema;
    }
    this.schemaExtensionDefinitions.addAll(typeRegistry.schemaExtensionDefinitions);

    // ok commit to the merge
    this.types.putAll(tempTypes);
    this.scalarTypes.putAll(tempScalarTypes);
    this.directiveDefinitions.putAll(tempDirectiveDefs);
    //
    // merge type extensions since they can be redefined by design
    typeRegistry.objectTypeExtensions.forEach((key, value) -> {
      List<ObjectTypeExtensionDefinition> currentList = this.objectTypeExtensions
        .computeIfAbsent(key, k -> new ArrayList<>());
      currentList.addAll(value);
    });
    typeRegistry.interfaceTypeExtensions.forEach((key, value) -> {
      List<InterfaceTypeExtensionDefinition> currentList = this.interfaceTypeExtensions
        .computeIfAbsent(key, k -> new ArrayList<>());
      currentList.addAll(value);
    });
    typeRegistry.unionTypeExtensions.forEach((key, value) -> {
      List<UnionTypeExtensionDefinition> currentList = this.unionTypeExtensions
        .computeIfAbsent(key, k -> new ArrayList<>());
      currentList.addAll(value);
    });
    typeRegistry.enumTypeExtensions.forEach((key, value) -> {
      List<EnumTypeExtensionDefinition> currentList = this.enumTypeExtensions
        .computeIfAbsent(key, k -> new ArrayList<>());
      currentList.addAll(value);
    });
    typeRegistry.scalarTypeExtensions.forEach((key, value) -> {
      List<ScalarTypeExtensionDefinition> currentList = this.scalarTypeExtensions
        .computeIfAbsent(key, k -> new ArrayList<>());
      currentList.addAll(value);
    });
    typeRegistry.inputObjectTypeExtensions.forEach((key, value) -> {
      List<InputObjectTypeExtensionDefinition> currentList = this.inputObjectTypeExtensions
        .computeIfAbsent(key, k -> new ArrayList<>());
      currentList.addAll(value);
    });

    return this;
  }

  private Map<String, OperationTypeDefinition> checkMergeSchemaDefs(TypeDefinitionRegistry toBeMergedTypeRegistry) {
    List<GraphQLError> errors = new ArrayList<>();
    if (toBeMergedTypeRegistry.schema != null && this.schema != null) {
      errors.add(new SchemaRedefinitionError(this.schema));
    }

    Map<String, OperationTypeDefinition> tempOperationDefs = gatherOperationDefs(errors, this.schema, this.schemaExtensionDefinitions);
    Map<String, OperationTypeDefinition> mergedOperationDefs =
      gatherOperationDefs(errors, toBeMergedTypeRegistry.schema, toBeMergedTypeRegistry.schemaExtensionDefinitions);

    defineOperationDefs(errors, mergedOperationDefs.values(), tempOperationDefs);

    if (!errors.isEmpty()) {
      myErrors.add(new SchemaProblem(errors));
    }

    return tempOperationDefs;
  }

  /**
   * Adds a a collections of definitions to the registry
   *
   * @param definitions the definitions to add
   */
  public void addAll(Collection<SDLDefinition> definitions) {
    for (SDLDefinition definition : definitions) {
      add(definition);
    }
  }

  /**
   * Adds a definition to the registry
   *
   * @param definition the definition to add
   */
  public void add(SDLDefinition definition) {
    ProgressManager.checkCanceled();

    // extensions
    if (definition instanceof ObjectTypeExtensionDefinition newEntry) {
      defineExt(objectTypeExtensions, newEntry, ObjectTypeExtensionDefinition::getName);
    }
    else if (definition instanceof InterfaceTypeExtensionDefinition newEntry) {
      defineExt(interfaceTypeExtensions, newEntry, InterfaceTypeExtensionDefinition::getName);
    }
    else if (definition instanceof UnionTypeExtensionDefinition newEntry) {
      defineExt(unionTypeExtensions, newEntry, UnionTypeExtensionDefinition::getName);
    }
    else if (definition instanceof EnumTypeExtensionDefinition newEntry) {
      defineExt(enumTypeExtensions, newEntry, EnumTypeExtensionDefinition::getName);
    }
    else if (definition instanceof ScalarTypeExtensionDefinition newEntry) {
      defineExt(scalarTypeExtensions, newEntry, ScalarTypeExtensionDefinition::getName);
    }
    else if (definition instanceof InputObjectTypeExtensionDefinition newEntry) {
      defineExt(inputObjectTypeExtensions, newEntry, InputObjectTypeExtensionDefinition::getName);
    }
    else if (definition instanceof SchemaExtensionDefinition) {
      schemaExtensionDefinitions.add((SchemaExtensionDefinition)definition);
    }
    else if (definition instanceof ScalarTypeDefinition newEntry) {
      define(scalarTypes, scalarTypes, newEntry);
    }
    else if (definition instanceof TypeDefinition newEntry) {
      define(types, types, newEntry);
    }
    else if (definition instanceof DirectiveDefinition newEntry) {
      define(directiveDefinitions, directiveDefinitions, newEntry);
    }
    else if (definition instanceof SchemaDefinition newSchema) {
      if (schema != null) {
        myErrors.add(new SchemaRedefinitionError(this.schema));
      }
      else {
        schema = newSchema;
      }
    }
    else {
      Assert.assertShouldNeverHappen();
    }
  }

  private <T extends TypeDefinition> void define(Map<String, T> source, Map<String, T> target, T newEntry) {
    String name = newEntry.getName();

    T olderEntry = source.get(name);
    if (olderEntry != null) {
      handleReDefinition(olderEntry, newEntry);
    }
    else {
      target.put(name, newEntry);
    }
  }

  private <T extends DirectiveDefinition> void define(Map<String, T> source, Map<String, T> target, T newEntry) {
    String name = newEntry.getName();

    T olderEntry = source.get(name);
    if (olderEntry != null) {
      handleReDefinition(olderEntry, newEntry);
    }
    else {
      target.put(name, newEntry);
    }
  }

  private <T> void defineExt(Map<String, List<T>> typeExtensions, T newEntry, Function<T, String> namerFunc) {
    List<T> currentList = typeExtensions.computeIfAbsent(namerFunc.apply(newEntry), k -> new ArrayList<>());
    currentList.add(newEntry);
  }

  public Map<String, TypeDefinition> types() {
    ProgressManager.checkCanceled();
    return new LinkedHashMap<>(types);
  }

  public Map<String, ScalarTypeDefinition> scalars() {
    ProgressManager.checkCanceled();
    LinkedHashMap<String, ScalarTypeDefinition> scalars = new LinkedHashMap<>(ScalarInfo.GRAPHQL_SPECIFICATION_SCALARS_DEFINITIONS);
    scalars.putAll(scalarTypes);
    return scalars;
  }

  public Map<String, List<ObjectTypeExtensionDefinition>> objectTypeExtensions() {
    ProgressManager.checkCanceled();
    return new LinkedHashMap<>(objectTypeExtensions);
  }

  public Map<String, List<InterfaceTypeExtensionDefinition>> interfaceTypeExtensions() {
    ProgressManager.checkCanceled();
    return new LinkedHashMap<>(interfaceTypeExtensions);
  }

  public Map<String, List<UnionTypeExtensionDefinition>> unionTypeExtensions() {
    ProgressManager.checkCanceled();
    return new LinkedHashMap<>(unionTypeExtensions);
  }

  public Map<String, List<EnumTypeExtensionDefinition>> enumTypeExtensions() {
    ProgressManager.checkCanceled();
    return new LinkedHashMap<>(enumTypeExtensions);
  }

  public Map<String, List<ScalarTypeExtensionDefinition>> scalarTypeExtensions() {
    ProgressManager.checkCanceled();
    return new LinkedHashMap<>(scalarTypeExtensions);
  }

  public Map<String, List<InputObjectTypeExtensionDefinition>> inputObjectTypeExtensions() {
    ProgressManager.checkCanceled();
    return new LinkedHashMap<>(inputObjectTypeExtensions);
  }

  public Optional<SchemaDefinition> schemaDefinition() {
    ProgressManager.checkCanceled();
    return ofNullable(schema);
  }

  public List<SchemaExtensionDefinition> getSchemaExtensionDefinitions() {
    ProgressManager.checkCanceled();
    return new ArrayList<>(schemaExtensionDefinitions);
  }

  private void handleReDefinition(TypeDefinition oldEntry, TypeDefinition newEntry) {
    myErrors.add(new TypeRedefinitionError(newEntry, oldEntry));
  }

  private void handleReDefinition(DirectiveDefinition oldEntry, DirectiveDefinition newEntry) {
    myErrors.add(new DirectiveRedefinitionError(newEntry, oldEntry));
  }

  public Optional<DirectiveDefinition> getDirectiveDefinition(String directiveName) {
    ProgressManager.checkCanceled();
    return Optional.ofNullable(directiveDefinitions.get(directiveName));
  }

  public Map<String, DirectiveDefinition> getDirectiveDefinitions() {
    ProgressManager.checkCanceled();
    return new LinkedHashMap<>(directiveDefinitions);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean hasType(@Nullable TypeName typeName) {
    ProgressManager.checkCanceled();
    if (typeName == null) return false;

    String name = typeName.getName();
    return types.containsKey(name) ||
           ScalarInfo.GRAPHQL_SPECIFICATION_SCALARS_DEFINITIONS.containsKey(name) ||
           scalarTypes.containsKey(name) ||
           objectTypeExtensions.containsKey(name);
  }

  public @NotNull Optional<TypeDefinition> getType(@Nullable Type type) {
    if (type == null) return Optional.empty();
    String typeName = TypeInfo.typeInfo(type).getName();
    return getType(typeName);
  }

  public @NotNull <T extends TypeDefinition> Optional<T> getType(@Nullable Type type, Class<T> ofType) {
    if (type == null) return Optional.empty();
    String typeName = TypeInfo.typeInfo(type).getName();
    return getType(typeName, ofType);
  }

  public @NotNull Optional<TypeDefinition> getType(@Nullable String typeName) {
    ProgressManager.checkCanceled();
    TypeDefinition<?> typeDefinition = types.get(typeName);
    if (typeDefinition != null) {
      return Optional.of(typeDefinition);
    }
    typeDefinition = scalars().get(typeName);
    if (typeDefinition != null) {
      return Optional.of(typeDefinition);
    }
    return Optional.empty();
  }

  public <T extends TypeDefinition> @NotNull Optional<T> getType(@Nullable String typeName, Class<T> ofType) {
    Optional<TypeDefinition> type = getType(typeName);
    if (type.isPresent()) {
      TypeDefinition typeDefinition = type.get();
      if (typeDefinition.getClass().equals(ofType)) {
        //noinspection unchecked
        return Optional.of((T)typeDefinition);
      }
    }
    return Optional.empty();
  }

  /**
   * Returns true if the specified type exists in the registry and is an abstract (Interface or Union) type
   *
   * @param type the type to check
   * @return true if its abstract
   */
  public boolean isInterfaceOrUnion(Type type) {
    Optional<TypeDefinition> typeDefinition = getType(type);
    if (typeDefinition.isPresent()) {
      TypeDefinition definition = typeDefinition.get();
      return definition instanceof UnionTypeDefinition || definition instanceof InterfaceTypeDefinition;
    }
    return false;
  }

  /**
   * Returns true if the specified type exists in the registry and is an object type or interface
   *
   * @param type the type to check
   * @return true if its an object type or interface
   */
  public boolean isObjectTypeOrInterface(Type type) {
    Optional<TypeDefinition> typeDefinition = getType(type);
    if (typeDefinition.isPresent()) {
      TypeDefinition definition = typeDefinition.get();
      return definition instanceof ObjectTypeDefinition || definition instanceof InterfaceTypeDefinition;
    }
    return false;
  }

  /**
   * Returns true if the specified type exists in the registry and is an object type
   *
   * @param type the type to check
   * @return true if its an object type
   */
  public boolean isObjectType(Type type) {
    return getType(type, ObjectTypeDefinition.class).isPresent();
  }

  /**
   * Returns a list of types in the registry of that specified class
   *
   * @param targetClass the class to search for
   * @param <T>         must extend TypeDefinition
   * @return a list of types of the target class
   */
  public <T extends TypeDefinition> List<T> getTypes(Class<T> targetClass) {
    ProgressManager.checkCanceled();
    return types.values().stream()
      .filter(targetClass::isInstance)
      .map(targetClass::cast)
      .collect(Collectors.toList());
  }

  public <T extends TypeDefinition> List<T> getTypes(Class<T> targetClass, boolean fromSourceNodes) {
    ProgressManager.checkCanceled();
    Stream<T> nodeStream = types.values().stream()
      .filter(targetClass::isInstance)
      .map(targetClass::cast);

    if (fromSourceNodes) {
      nodeStream = fromSourceNodes(nodeStream, targetClass);
    }

    return nodeStream
      .collect(Collectors.toList());
  }

  /**
   * Returns a map of types in the registry of that specified class keyed by name
   *
   * @param targetClass the class to search for
   * @param <T>         must extend TypeDefinition
   * @return a map of types
   */
  public <T extends TypeDefinition> Map<String, T> getTypesMap(Class<T> targetClass) {
    List<T> list = getTypes(targetClass);
    return FpKit.getByName(list, TypeDefinition::getName, FpKit.mergeFirst());
  }

  /**
   * Returns the list of object and interface types that implement the given interface type
   *
   * @param targetInterface the target to search for
   * @return the list of object types that implement the given interface type
   * @see TypeDefinitionRegistry#getImplementationsOf(InterfaceTypeDefinition)
   */
  public List<ImplementingTypeDefinition> getAllImplementationsOf(InterfaceTypeDefinition targetInterface) {
    List<ImplementingTypeDefinition> typeDefinitions = getTypes(ImplementingTypeDefinition.class);
    return typeDefinitions.stream().filter(typeDefinition -> {
      List<Type> implementsList = typeDefinition.getImplements();
      for (Type iFace : implementsList) {
        Optional<InterfaceTypeDefinition> interfaceTypeDef = getType(iFace, InterfaceTypeDefinition.class);
        if (interfaceTypeDef.isPresent()) {
          boolean equals = interfaceTypeDef.get().getName().equals(targetInterface.getName());
          if (equals) {
            return true;
          }
        }
      }
      return false;
    }).collect(Collectors.toList());
  }

  /**
   * Returns the list of object interface types that implement the given interface type
   *
   * @param targetInterface the target to search for
   * @return the list of object types that implement the given interface type
   * @see TypeDefinitionRegistry#getAllImplementationsOf(InterfaceTypeDefinition)
   */
  public List<ObjectTypeDefinition> getImplementationsOf(InterfaceTypeDefinition targetInterface) {
    return this.getAllImplementationsOf(targetInterface)
      .stream()
      .filter(typeDefinition -> typeDefinition instanceof ObjectTypeDefinition)
      .map(typeDefinition -> (ObjectTypeDefinition)typeDefinition)
      .collect(Collectors.toList());
  }

  /**
   * Returns true of the abstract type is in implemented by the object type or interface
   *
   * @param abstractType the abstract type to check (interface or union)
   * @param possibleType the object type or interface to check
   * @return true if the object type or interface implements the abstract type
   */
  public boolean isPossibleType(Type abstractType, Type possibleType) {
    if (!isInterfaceOrUnion(abstractType)) {
      return false;
    }
    if (!isObjectTypeOrInterface(possibleType)) {
      return false;
    }
    TypeDefinition targetObjectTypeDef = getType(possibleType).get();
    TypeDefinition abstractTypeDef = getType(abstractType).get();
    if (abstractTypeDef instanceof UnionTypeDefinition) {
      List<Type> memberTypes = ((UnionTypeDefinition)abstractTypeDef).getMemberTypes();
      for (Type memberType : memberTypes) {
        Optional<ObjectTypeDefinition> checkType = getType(memberType, ObjectTypeDefinition.class);
        if (checkType.isPresent()) {
          if (checkType.get().getName().equals(targetObjectTypeDef.getName())) {
            return true;
          }
        }
      }
      return false;
    }
    else {
      InterfaceTypeDefinition iFace = (InterfaceTypeDefinition)abstractTypeDef;
      List<ImplementingTypeDefinition> implementingTypeDefinitions = getAllImplementationsOf(iFace);
      return implementingTypeDefinitions.stream()
        .anyMatch(od -> Objects.equals(od.getName(), targetObjectTypeDef.getName()));
    }
  }

  /**
   * Returns true if the maybe type is either equal or a subset of the second super type (covariant).
   *
   * @param maybeSubType the type to check
   * @param superType    the equality checked type
   * @return true if maybeSubType is covariant or equal to superType
   */
  @SuppressWarnings({"SimplifiableIfStatement", "RedundantIfStatement"})
  public boolean isSubTypeOf(Type maybeSubType, Type superType) {
    TypeInfo maybeSubTypeInfo = TypeInfo.typeInfo(maybeSubType);
    TypeInfo superTypeInfo = TypeInfo.typeInfo(superType);
    // Equivalent type is a valid subtype
    if (maybeSubTypeInfo.equals(superTypeInfo)) {
      return true;
    }


    // If superType is non-null, maybeSubType must also be non-null.
    if (superTypeInfo.isNonNull()) {
      if (maybeSubTypeInfo.isNonNull()) {
        return isSubTypeOf(maybeSubTypeInfo.unwrapOneType(), superTypeInfo.unwrapOneType());
      }
      return false;
    }
    if (maybeSubTypeInfo.isNonNull()) {
      // If superType is nullable, maybeSubType may be non-null or nullable.
      return isSubTypeOf(maybeSubTypeInfo.unwrapOneType(), superType);
    }

    // If superType type is a list, maybeSubType type must also be a list.
    if (superTypeInfo.isList()) {
      if (maybeSubTypeInfo.isList()) {
        return isSubTypeOf(maybeSubTypeInfo.unwrapOneType(), superTypeInfo.unwrapOneType());
      }
      return false;
    }
    if (maybeSubTypeInfo.isList()) {
      // If superType is not a list, maybeSubType must also be not a list.
      return false;
    }

    // If superType type is an abstract type, maybeSubType type may be a currently
    // possible object type.
    if (isInterfaceOrUnion(superType) &&
        isObjectTypeOrInterface(maybeSubType) &&
        isPossibleType(superType, maybeSubType)) {
      return true;
    }

    // Otherwise, the child type is not a valid subtype of the parent type.
    return false;
  }
}
