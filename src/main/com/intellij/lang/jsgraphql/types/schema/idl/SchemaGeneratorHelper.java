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
import com.intellij.lang.jsgraphql.types.schema.*;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.DirectiveNonRepeatableError;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.NotAnInputTypeError;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.NotAnOutputTypeError;
import com.intellij.lang.jsgraphql.types.util.FpKit;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.Directives.*;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.mapNotNull;
import static com.intellij.lang.jsgraphql.types.introspection.Introspection.DirectiveLocation.*;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLEnumValueDefinition.newEnumValueDefinition;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeReference.typeRef;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.*;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("rawtypes")
@Internal
public class SchemaGeneratorHelper {

  private static final Logger LOG = Logger.getInstance(SchemaGeneratorHelper.class);

  /**
   * We pass this around so we know what we have defined in a stack like manner plus
   * it gives us helper functions
   */
  static class BuildContext {
    private final TypeDefinitionRegistry typeRegistry;
    private final RuntimeWiring wiring;
    private final Deque<String> typeStack = new ArrayDeque<>();

    private final Map<String, GraphQLOutputType> outputGTypes = new LinkedHashMap<>();
    private final Map<String, GraphQLInputType> inputGTypes = new LinkedHashMap<>();
    private final Set<GraphQLDirective> directives = new LinkedHashSet<>();
    public final Map<String, OperationTypeDefinition> operationTypeDefs;
    private final List<GraphQLError> myErrors = new ArrayList<>();

    BuildContext(TypeDefinitionRegistry typeRegistry, RuntimeWiring wiring, Map<String, OperationTypeDefinition> operationTypeDefinitions) {
      this.typeRegistry = typeRegistry;
      this.wiring = wiring;
      this.operationTypeDefs = operationTypeDefinitions;
    }

    public TypeDefinitionRegistry getTypeRegistry() {
      return typeRegistry;
    }

    @Nullable TypeDefinition getTypeDefinition(@Nullable Type type) {
      return typeRegistry.getType(type).orElse(null);
    }

    boolean stackContains(TypeInfo typeInfo) {
      return typeStack.contains(typeInfo.getName());
    }

    void push(TypeInfo typeInfo) {
      typeStack.push(typeInfo.getName());
    }

    void pop() {
      typeStack.pop();
    }

    GraphQLOutputType hasOutputType(TypeDefinition typeDefinition) {
      return outputGTypes.get(typeDefinition.getName());
    }

    GraphQLInputType hasInputType(TypeDefinition typeDefinition) {
      return inputGTypes.get(typeDefinition.getName());
    }

    void putOutputType(GraphQLNamedOutputType outputType) {
      outputGTypes.put(outputType.getName(), outputType);
      // certain types can be both input and output types, for example enums and scalars
      if (outputType instanceof GraphQLInputType) {
        inputGTypes.put(outputType.getName(), (GraphQLInputType)outputType);
      }
    }

    void putInputType(GraphQLNamedInputType inputType) {
      inputGTypes.put(inputType.getName(), inputType);
      // certain types can be both input and output types, for example enums and scalars
      if (inputType instanceof GraphQLOutputType) {
        outputGTypes.put(inputType.getName(), (GraphQLOutputType)inputType);
      }
    }

    RuntimeWiring getWiring() {
      return wiring;
    }

    GraphqlTypeComparatorRegistry getComparatorRegistry() {
      return wiring.getComparatorRegistry();
    }

    public void addDirectiveDefinition(GraphQLDirective directive) {
      this.directives.add(directive);
    }

    @SuppressWarnings("unused")
    public void addDirectives(Set<GraphQLDirective> directives) {
      this.directives.addAll(directives);
    }

    public Set<GraphQLDirective> getDirectives() {
      return directives;
    }

    public List<GraphQLError> getErrors() {
      return myErrors;
    }

    public void addError(@NotNull GraphQLError error) {
      myErrors.add(error);
    }
  }

  private static Description createDescription(String s) {
    return new Description(s, null, false);
  }

  @Nullable Object buildValue(BuildContext buildCtx, Value value, GraphQLType requiredType) {
    if (value == null || value instanceof NullValue) {
      return null;
    }

    if (isNonNull(requiredType)) {
      requiredType = unwrapOne(requiredType);
    }

    try {
      Object result = null;
      if (requiredType instanceof GraphQLScalarType) {
        result = parseLiteral(value, (GraphQLScalarType)requiredType);
      }
      else if (requiredType instanceof GraphQLEnumType && value instanceof EnumValue) {
        result = ((EnumValue)value).getName();
        final EnumValuesProvider enumValuesProvider =
          buildCtx.getWiring().getEnumValuesProviders().get(((GraphQLEnumType)requiredType).getName());
        if (enumValuesProvider != null) {
          result = enumValuesProvider.getValue((String)result);
        }
      }
      else if (requiredType instanceof GraphQLEnumType && value instanceof StringValue) {
        result = ((StringValue)value).getValue();
        final EnumValuesProvider enumValuesProvider =
          buildCtx.getWiring().getEnumValuesProviders().get(((GraphQLEnumType)requiredType).getName());
        if (enumValuesProvider != null) {
          result = enumValuesProvider.getValue((String)result);
        }
      }
      else if (isList(requiredType)) {
        if (value instanceof ArrayValue) {
          result = buildArrayValue(buildCtx, requiredType, (ArrayValue)value);
        }
        else {
          result = buildArrayValue(buildCtx, requiredType, ArrayValue.newArrayValue().value(value).build());
        }
      }
      else if (value instanceof ObjectValue && requiredType instanceof GraphQLInputObjectType) {
        result = buildObjectValue(buildCtx, (ObjectValue)value, (GraphQLInputObjectType)requiredType);
      }
      else {
        LOG.warn(format(
          "cannot build value of type %s from object class %s with instance %s",
          simplePrint(requiredType), value.getClass().getSimpleName(), value));
      }
      return result;
    }
    catch (CancellationException e) {
      throw e;
    }
    catch (Exception e) {
      // we expect errors here due to potential errors in the user's code
      LOG.info(e);
      return null;
    }
  }

  private Object parseLiteral(Value value, GraphQLScalarType requiredType) {
    return requiredType.getCoercing().parseLiteral(value);
  }

  Object buildArrayValue(BuildContext buildCtx, GraphQLType requiredType, ArrayValue arrayValue) {
    GraphQLType wrappedType = unwrapOne(requiredType);
    return mapNotNull(arrayValue.getValues(), item -> buildValue(buildCtx, item, wrappedType));
  }

  Object buildObjectValue(BuildContext buildCtx, ObjectValue defaultValue, GraphQLInputObjectType objectType) {
    Map<String, Object> map = new LinkedHashMap<>();
    objectType.getFieldDefinitions().forEach(
      f -> {
        final Value<?> fieldValueFromDefaultObjectValue = getFieldValueFromObjectValue(defaultValue, f.getName());

        GraphQLInputType fieldType = f.getType();
        if (fieldType instanceof GraphQLTypeReference) {
          InputValueDefinition fieldDefinition = f.getDefinition();
          if (fieldDefinition != null) {
            TypeDefinition fieldTypeDefinition = buildCtx.getTypeDefinition(fieldDefinition.getType());
            if (fieldTypeDefinition != null) {
              GraphQLInputType computedFieldType = buildCtx.hasInputType(fieldTypeDefinition);
              if (computedFieldType != null) {
                fieldType = computedFieldType;
              }
            }
          }
        }

        Object value = fieldValueFromDefaultObjectValue != null
                       ? buildValue(buildCtx, fieldValueFromDefaultObjectValue, fieldType)
                       : f.getDefaultValue();
        if (value != null) {
          map.put(f.getName(), value);
        }
      }
    );
    return map;
  }

  Value<?> getFieldValueFromObjectValue(final ObjectValue objectValue, final String fieldName) {
    ObjectField objectField = ContainerUtil.find(objectValue.getObjectFields(), f -> Objects.equals(f.getName(), fieldName));
    return objectField != null ? objectField.getValue() : null;
  }

  String buildDescription(Node<?> node, Description description) {
    if (description != null) {
      return description.getContent();
    }
    List<Comment> comments = node.getComments();
    List<String> lines = new ArrayList<>();
    for (Comment comment : comments) {
      String commentLine = comment.getContent();
      if (commentLine.trim().isEmpty()) {
        lines.clear();
      }
      else {
        lines.add(commentLine);
      }
    }
    if (lines.size() == 0) {
      return null;
    }
    return String.join("\n", lines);
  }

  String buildDeprecationReason(List<Directive> directives) {
    directives = Optional.ofNullable(directives).orElse(emptyList());
    Optional<Directive> directive = directives.stream().filter(d -> "deprecated".equals(d.getName())).findFirst();
    if (directive.isEmpty()) {
      return null;
    }
    Map<String, String> args = directive.get().getArguments().stream()
      .filter(arg -> arg.getValue() instanceof StringValue)
      .collect(toMap(Argument::getName, arg -> ((StringValue)arg.getValue()).getValue()
      ));
    if (args.isEmpty()) {
      return NO_LONGER_SUPPORTED; // default value from spec
    }
    else {
      // pre flight checks have ensured its valid
      return args.get("reason");
    }
  }

  private @NotNull GraphQLDirective buildDirective(BuildContext buildCtx,
                                                   Directive directive,
                                                   DirectiveLocation directiveLocation,
                                                   Set<GraphQLDirective> runtimeDirectives,
                                                   GraphqlTypeComparatorRegistry comparatorRegistry,
                                                   Set<String> previousNames) {
    GraphQLDirective gqlDirective = buildDirective(buildCtx, directive, runtimeDirectives, directiveLocation, comparatorRegistry);
    if (previousNames.contains(directive.getName()) && gqlDirective.isNonRepeatable()) {
      buildCtx.addError(new DirectiveNonRepeatableError(directive));
    }
    previousNames.add(gqlDirective.getName());
    return gqlDirective;
  }

  // builds directives from a type and its extensions
  @NotNull GraphQLDirective buildDirective(BuildContext buildCtx,
                                           Directive directive,
                                           Set<GraphQLDirective> directiveDefinitions,
                                           DirectiveLocation directiveLocation,
                                           GraphqlTypeComparatorRegistry comparatorRegistry) {
    GraphQLDirective.Builder builder = GraphQLDirective.newDirective()
      .name(directive.getName())
      .description(buildDescription(directive, null))
      .comparatorRegistry(comparatorRegistry)
      .validLocations(directiveLocation);

    Optional<GraphQLDirective> directiveDefOpt = FpKit.findOne(directiveDefinitions, dd -> dd.getName().equals(directive.getName()));

    GraphQLDirective graphQLDirective = directiveDefOpt.orElseGet(() -> {
      Function<Type, Optional<GraphQLInputType>> inputTypeFactory = inputType -> buildInputType(buildCtx, inputType);
      Optional<DirectiveDefinition> directiveDefinition = buildCtx.getTypeRegistry().getDirectiveDefinition(directive.getName());
      return directiveDefinition.map(definition -> buildDirectiveFromDefinition(buildCtx, definition, inputTypeFactory)).orElse(null);
    });

    if (graphQLDirective != null) {
      builder.repeatable(graphQLDirective.isRepeatable());
      List<GraphQLArgument> arguments = mapNotNull(directive.getArguments(),
                                                   arg -> buildDirectiveArgument(buildCtx, arg, graphQLDirective).orElse(null));
      arguments = transferMissingArguments(arguments, graphQLDirective);
      arguments.forEach(builder::argument);
    }

    return builder.build();
  }

  private @NotNull Optional<GraphQLArgument> buildDirectiveArgument(BuildContext buildCtx,
                                                                    Argument arg,
                                                                    GraphQLDirective directiveDefinition) {
    GraphQLArgument directiveDefArgument = directiveDefinition.getArgument(arg.getName());
    if (directiveDefArgument == null) {
      return Optional.empty();
    }
    GraphQLArgument.Builder builder = GraphQLArgument.newArgument();
    builder.name(arg.getName());
    GraphQLInputType inputType;
    Object defaultValue;
    inputType = directiveDefArgument.getType();
    defaultValue = directiveDefArgument.getDefaultValue();
    builder.type(inputType);
    builder.defaultValue(defaultValue);

    Object value = buildValue(buildCtx, arg.getValue(), inputType);
    //
    // we put the default value in if the specified is null
    builder.value(value == null ? defaultValue : value);

    return Optional.of(builder.build());
  }

  private @NotNull List<GraphQLArgument> transferMissingArguments(List<GraphQLArgument> arguments, GraphQLDirective directiveDefinition) {
    Map<String, GraphQLArgument> declaredArgs = FpKit.getByName(arguments, GraphQLArgument::getName, FpKit.mergeFirst());
    List<GraphQLArgument> argumentsOut = new ArrayList<>(arguments);

    for (GraphQLArgument directiveDefArg : directiveDefinition.getArguments()) {
      if (!declaredArgs.containsKey(directiveDefArg.getName())) {
        GraphQLArgument missingArg = GraphQLArgument.newArgument()
          .name(directiveDefArg.getName())
          .description(directiveDefArg.getDescription())
          .definition(directiveDefArg.getDefinition())
          .type(directiveDefArg.getType())
          .defaultValue(directiveDefArg.getDefaultValue())
          .value(directiveDefArg.getDefaultValue())
          .build();
        argumentsOut.add(missingArg);
      }
    }
    return argumentsOut;
  }

  @NotNull GraphQLDirective buildDirectiveFromDefinition(BuildContext buildCtx,
                                                         DirectiveDefinition directiveDefinition,
                                                         Function<Type, Optional<GraphQLInputType>> inputTypeFactory) {

    GraphQLDirective.Builder builder = GraphQLDirective.newDirective()
      .name(directiveDefinition.getName())
      .definition(directiveDefinition)
      .repeatable(directiveDefinition.isRepeatable())
      .description(buildDescription(directiveDefinition, directiveDefinition.getDescription()));


    List<DirectiveLocation> locations = buildLocations(directiveDefinition);
    locations.forEach(builder::validLocations);

    List<GraphQLArgument> arguments = mapNotNull(directiveDefinition.getInputValueDefinitions(),
                                                 arg -> buildDirectiveArgumentFromDefinition(buildCtx, arg, inputTypeFactory).orElse(null));
    arguments.forEach(builder::argument);
    return builder.build();
  }

  private @NotNull List<DirectiveLocation> buildLocations(DirectiveDefinition directiveDefinition) {
    return mapNotNull(directiveDefinition.getDirectiveLocations(),
                      dl -> {
                        try {
                          String name = dl.getName();
                          if (name == null) return null;
                          return valueOf(name.toUpperCase());
                        }
                        catch (IllegalArgumentException e) {
                          return null;
                        }
                      });
  }

  private @NotNull Optional<GraphQLArgument> buildDirectiveArgumentFromDefinition(BuildContext buildCtx,
                                                                                  InputValueDefinition arg,
                                                                                  Function<Type, Optional<GraphQLInputType>> inputTypeFactory) {
    GraphQLArgument.Builder builder = GraphQLArgument.newArgument()
      .name(arg.getName())
      .definition(arg);

    GraphQLInputType inputType = inputTypeFactory.apply(arg.getType()).orElse(null);
    if (inputType == null) return Optional.empty();

    builder.type(inputType);
    builder.value(buildValue(buildCtx, arg.getDefaultValue(), inputType));
    builder.defaultValue(buildValue(buildCtx, arg.getDefaultValue(), inputType));
    builder.description(buildDescription(arg, arg.getDescription()));
    return Optional.of(builder.build());
  }

  @NotNull Optional<GraphQLInputType> buildInputType(BuildContext buildCtx, Type rawType) {

    TypeDefinition typeDefinition = buildCtx.getTypeDefinition(rawType);
    TypeInfo typeInfo = TypeInfo.typeInfo(rawType);

    if (typeDefinition == null) {
      return Optional.empty();
    }

    GraphQLInputType inputType = buildCtx.hasInputType(typeDefinition);
    if (inputType != null) {
      return Optional.ofNullable(typeInfo.decorate(inputType));
    }

    if (buildCtx.stackContains(typeInfo)) {
      // we have circled around so put in a type reference and fix it later
      return Optional.ofNullable(typeInfo.decorate(typeRef(typeInfo.getName())));
    }

    buildCtx.push(typeInfo);

    if (typeDefinition instanceof InputObjectTypeDefinition) {
      inputType = buildInputObjectType(buildCtx, (InputObjectTypeDefinition)typeDefinition);
    }
    else if (typeDefinition instanceof EnumTypeDefinition) {
      inputType = buildEnumType(buildCtx, (EnumTypeDefinition)typeDefinition);
    }
    else if (typeDefinition instanceof ScalarTypeDefinition) {
      inputType = buildScalar(buildCtx, (ScalarTypeDefinition)typeDefinition);
    }
    else {
      buildCtx.addError(new NotAnInputTypeError(rawType, typeDefinition));
    }

    if (inputType != null) {
      buildCtx.putInputType((GraphQLNamedInputType)inputType);
    }
    buildCtx.pop();
    return Optional.ofNullable(typeInfo.decorate(inputType));
  }

  @NotNull GraphQLInputObjectType buildInputObjectType(BuildContext buildCtx, InputObjectTypeDefinition typeDefinition) {
    GraphQLInputObjectType.Builder builder = GraphQLInputObjectType.newInputObject();
    builder.definition(typeDefinition);
    builder.name(typeDefinition.getName());
    builder.description(buildDescription(typeDefinition, typeDefinition.getDescription()));
    builder.comparatorRegistry(buildCtx.getComparatorRegistry());

    List<InputObjectTypeExtensionDefinition> extensions = inputObjectTypeExtensions(typeDefinition, buildCtx);
    builder.extensionDefinitions(extensions);

    builder.withDirectives(
      buildDirectives(buildCtx,
                      typeDefinition.getDirectives(),
                      directivesOf(extensions),
                      INPUT_OBJECT,
                      buildCtx.getDirectives(),
                      buildCtx.getComparatorRegistry())
    );

    typeDefinition.getInputValueDefinitions().forEach(inputValue ->
                                                        buildInputField(buildCtx, inputValue).ifPresent(builder::field));

    extensions.forEach(extension -> extension.getInputValueDefinitions().forEach(inputValueDefinition -> {
      Optional<GraphQLInputObjectField> inputField = buildInputField(buildCtx, inputValueDefinition);
      inputField.ifPresent(field -> {
        if (!builder.hasField(field.getName())) {
          builder.field(field);
        }
      });
    }));

    return builder.build();
  }

  private @NotNull Optional<GraphQLInputObjectField> buildInputField(BuildContext buildCtx, InputValueDefinition fieldDef) {
    GraphQLInputObjectField.Builder fieldBuilder = GraphQLInputObjectField.newInputObjectField();
    fieldBuilder.definition(fieldDef);
    fieldBuilder.name(fieldDef.getName());
    fieldBuilder.description(buildDescription(fieldDef, fieldDef.getDescription()));
    fieldBuilder.deprecate(buildDeprecationReason(fieldDef.getDirectives()));
    fieldBuilder.comparatorRegistry(buildCtx.getComparatorRegistry());

    // currently the spec doesnt allow deprecations on InputValueDefinitions but it should!
    //fieldBuilder.deprecate(buildDeprecationReason(fieldDef.getDirectives()));
    GraphQLInputType inputType = buildInputType(buildCtx, fieldDef.getType()).orElse(null);
    if (inputType == null) return Optional.empty();

    fieldBuilder.type(inputType);
    Value defaultValue = fieldDef.getDefaultValue();
    if (defaultValue != null) {
      fieldBuilder.defaultValue(buildValue(buildCtx, defaultValue, inputType));
    }

    fieldBuilder.withDirectives(
      buildDirectives(buildCtx,
                      fieldDef.getDirectives(),
                      emptyList(),
                      INPUT_FIELD_DEFINITION,
                      buildCtx.getDirectives(),
                      buildCtx.getComparatorRegistry())
    );

    return Optional.of(fieldBuilder.build());
  }

  @NotNull GraphQLEnumType buildEnumType(BuildContext buildCtx, EnumTypeDefinition typeDefinition) {
    GraphQLEnumType.Builder builder = GraphQLEnumType.newEnum();
    builder.definition(typeDefinition);
    builder.name(typeDefinition.getName());
    builder.description(buildDescription(typeDefinition, typeDefinition.getDescription()));
    builder.comparatorRegistry(buildCtx.getComparatorRegistry());

    List<EnumTypeExtensionDefinition> extensions = enumTypeExtensions(typeDefinition, buildCtx);
    builder.extensionDefinitions(extensions);

    EnumValuesProvider enumValuesProvider = buildCtx.getWiring().getEnumValuesProviders().get(typeDefinition.getName());
    typeDefinition.getEnumValueDefinitions().forEach(evd -> {
      GraphQLEnumValueDefinition enumValueDefinition = buildEnumValue(buildCtx, typeDefinition, enumValuesProvider, evd);
      builder.value(enumValueDefinition);
    });

    extensions.forEach(extension -> extension.getEnumValueDefinitions().forEach(evd -> {
      GraphQLEnumValueDefinition enumValueDefinition = buildEnumValue(buildCtx, typeDefinition, enumValuesProvider, evd);
      if (!builder.hasValue(enumValueDefinition.getName())) {
        builder.value(enumValueDefinition);
      }
    }));

    builder.withDirectives(
      buildDirectives(buildCtx,
                      typeDefinition.getDirectives(),
                      directivesOf(extensions),
                      ENUM,
                      buildCtx.getDirectives(),
                      buildCtx.getComparatorRegistry())
    );

    return builder.build();
  }

  private @NotNull GraphQLEnumValueDefinition buildEnumValue(BuildContext buildCtx,
                                                             EnumTypeDefinition typeDefinition,
                                                             EnumValuesProvider enumValuesProvider,
                                                             EnumValueDefinition evd) {
    String description = buildDescription(evd, evd.getDescription());
    String deprecation = buildDeprecationReason(evd.getDirectives());

    Object value;
    if (enumValuesProvider != null) {
      value = enumValuesProvider.getValue(evd.getName());
      assertNotNull(value,
                    () -> format("EnumValuesProvider for %s returned null for %s", typeDefinition.getName(), evd.getName()));
    }
    else {
      value = evd.getName();
    }
    return newEnumValueDefinition()
      .name(evd.getName())
      .value(value)
      .description(description)
      .deprecationReason(deprecation)
      .definition(evd)
      .comparatorRegistry(buildCtx.getComparatorRegistry())
      .withDirectives(
        buildDirectives(buildCtx,
                        evd.getDirectives(),
                        emptyList(),
                        ENUM_VALUE,
                        buildCtx.getDirectives(),
                        buildCtx.getComparatorRegistry())
      )
      .build();
  }

  @NotNull GraphQLScalarType buildScalar(BuildContext buildCtx, ScalarTypeDefinition typeDefinition) {
    TypeDefinitionRegistry typeRegistry = buildCtx.getTypeRegistry();
    RuntimeWiring runtimeWiring = buildCtx.getWiring();
    WiringFactory wiringFactory = runtimeWiring.getWiringFactory();
    List<ScalarTypeExtensionDefinition> extensions = scalarTypeExtensions(typeDefinition, buildCtx);

    ScalarWiringEnvironment environment = new ScalarWiringEnvironment(typeRegistry, typeDefinition, extensions);

    GraphQLScalarType scalar;
    if (wiringFactory.providesScalar(environment)) {
      scalar = wiringFactory.getScalar(environment);
    }
    else {
      scalar = buildCtx.getWiring().getScalars().get(typeDefinition.getName());
    }

    return scalar.transform(builder -> builder
      .name(typeDefinition.getName())
      .description(buildDescription(typeDefinition, typeDefinition.getDescription()))
      .definition(typeDefinition)
      .comparatorRegistry(buildCtx.getComparatorRegistry())
      .specifiedByUrl(getSpecifiedByUrl(typeDefinition, extensions))
      .withDirectives(buildDirectives(
        buildCtx,
        typeDefinition.getDirectives(),
        directivesOf(extensions),
        SCALAR,
        buildCtx.getDirectives(),
        buildCtx.getComparatorRegistry())
      ));
  }

  @Nullable String getSpecifiedByUrl(ScalarTypeDefinition scalarTypeDefinition, List<ScalarTypeExtensionDefinition> extensions) {
    List<Directive> allDirectives = new ArrayList<>(scalarTypeDefinition.getDirectives());
    extensions.forEach(extension -> allDirectives.addAll(extension.getDirectives()));
    Optional<Directive> specifiedByDirective = FpKit.findOne(allDirectives,
                                                             directiveDefinition -> directiveDefinition.getName()
                                                               .equals(SpecifiedByDirective.getName()));
    if (specifiedByDirective.isEmpty()) {
      return null;
    }
    Argument urlArgument = specifiedByDirective.get().getArgument("url");
    if (urlArgument == null) return null;
    Value value = urlArgument.getValue();
    if (!(value instanceof StringValue)) return null;
    return ((StringValue)value).getValue();
  }

  GraphQLDirective @NotNull [] buildDirectives(BuildContext buildCtx,
                                               List<Directive> directives,
                                               List<Directive> extensionDirectives,
                                               DirectiveLocation directiveLocation,
                                               Set<GraphQLDirective> runtimeDirectives,
                                               GraphqlTypeComparatorRegistry comparatorRegistry) {
    directives = Optional.ofNullable(directives).orElse(emptyList());
    extensionDirectives = Optional.ofNullable(extensionDirectives).orElse(emptyList());
    Set<String> previousNames = new LinkedHashSet<>();

    List<GraphQLDirective> output = new ArrayList<>();
    for (Directive directive : directives) {
      GraphQLDirective gqlDirective =
        buildDirective(buildCtx, directive, directiveLocation, runtimeDirectives, comparatorRegistry, previousNames);
      output.add(gqlDirective);
    }
    for (Directive directive : extensionDirectives) {
      GraphQLDirective gqlDirective =
        buildDirective(buildCtx, directive, directiveLocation, runtimeDirectives, comparatorRegistry, previousNames);
      output.add(gqlDirective);
    }
    return output.toArray(new GraphQLDirective[0]);
  }

  private void buildInterfaceTypeInterfaces(BuildContext buildCtx,
                                            InterfaceTypeDefinition typeDefinition,
                                            GraphQLInterfaceType.Builder builder,
                                            List<InterfaceTypeExtensionDefinition> extensions) {
    Map<String, GraphQLOutputType> interfaces = new LinkedHashMap<>();
    typeDefinition.getImplements().forEach(type -> {
      Optional<GraphQLNamedOutputType> newInterfaceType = buildOutputType(buildCtx, type);
      newInterfaceType.ifPresent(outputType -> interfaces.put(outputType.getName(), outputType));
    });

    extensions.forEach(extension -> extension.getImplements().forEach(type -> {
      Optional<GraphQLNamedOutputType> newInterfaceType = buildOutputType(buildCtx, type);
      newInterfaceType.ifPresent(interfaceType -> {
        if (!interfaces.containsKey(interfaceType.getName())) {
          interfaces.put(interfaceType.getName(), interfaceType);
        }
      });
    }));

    interfaces.values().forEach(interfaze -> {
      if (interfaze instanceof GraphQLInterfaceType) {
        builder.withInterface((GraphQLInterfaceType)interfaze);
        return;
      }
      if (interfaze instanceof GraphQLTypeReference) {
        builder.withInterface((GraphQLTypeReference)interfaze);
      }
    });
  }

  private @NotNull Optional<GraphQLObjectType> buildOperation(BuildContext buildCtx, OperationTypeDefinition operation) {
    Type type = operation.getTypeName();
    GraphQLOutputType outputType = buildOutputType(buildCtx, type).orElse(null);
    return Optional.ofNullable(ObjectUtils.tryCast(outputType, GraphQLObjectType.class));
  }

  @NotNull GraphQLInterfaceType buildInterfaceType(BuildContext buildCtx, InterfaceTypeDefinition typeDefinition) {
    GraphQLInterfaceType.Builder builder = GraphQLInterfaceType.newInterface();
    builder.definition(typeDefinition);
    builder.name(typeDefinition.getName());
    builder.description(buildDescription(typeDefinition, typeDefinition.getDescription()));
    builder.comparatorRegistry(buildCtx.getComparatorRegistry());

    List<InterfaceTypeExtensionDefinition> extensions = interfaceTypeExtensions(typeDefinition, buildCtx);
    builder.extensionDefinitions(extensions);
    builder.withDirectives(
      buildDirectives(buildCtx,
                      typeDefinition.getDirectives(),
                      directivesOf(extensions),
                      OBJECT,
                      buildCtx.getDirectives(),
                      buildCtx.getComparatorRegistry())
    );

    typeDefinition.getFieldDefinitions().forEach(fieldDef ->
                                                   buildField(buildCtx, typeDefinition, fieldDef).ifPresent(builder::field));

    extensions.forEach(extension -> extension.getFieldDefinitions().forEach(fieldDef -> {
      buildField(buildCtx, typeDefinition, fieldDef).ifPresent(fieldDefinition -> {
        if (!builder.hasField(fieldDefinition.getName())) {
          builder.field(fieldDefinition);
        }
      });
    }));

    buildInterfaceTypeInterfaces(buildCtx, typeDefinition, builder, extensions);

    return builder.build();
  }

  @NotNull GraphQLObjectType buildObjectType(BuildContext buildCtx, ObjectTypeDefinition typeDefinition) {
    GraphQLObjectType.Builder builder = GraphQLObjectType.newObject();
    builder.definition(typeDefinition);
    builder.name(typeDefinition.getName());
    builder.description(buildDescription(typeDefinition, typeDefinition.getDescription()));
    builder.comparatorRegistry(buildCtx.getComparatorRegistry());

    List<ObjectTypeExtensionDefinition> extensions = objectTypeExtensions(typeDefinition, buildCtx);
    builder.extensionDefinitions(extensions);
    builder.withDirectives(
      buildDirectives(buildCtx,
                      typeDefinition.getDirectives(),
                      directivesOf(extensions),
                      OBJECT,
                      buildCtx.getDirectives(),
                      buildCtx.getComparatorRegistry())
    );

    typeDefinition.getFieldDefinitions().forEach(fieldDef -> {
      buildField(buildCtx, typeDefinition, fieldDef).ifPresent(builder::field);
    });

    extensions.forEach(extension -> extension.getFieldDefinitions().forEach(fieldDef -> {
      buildField(buildCtx, typeDefinition, fieldDef).ifPresent(fieldDefinition -> {
        if (!builder.hasField(fieldDefinition.getName())) {
          builder.field(fieldDefinition);
        }
      });
    }));

    buildObjectTypeInterfaces(buildCtx, typeDefinition, builder, extensions);

    return builder.build();
  }

  private void buildObjectTypeInterfaces(BuildContext buildCtx,
                                         ObjectTypeDefinition typeDefinition,
                                         GraphQLObjectType.Builder builder,
                                         List<ObjectTypeExtensionDefinition> extensions) {
    Map<String, GraphQLOutputType> interfaces = new LinkedHashMap<>();
    typeDefinition.getImplements().forEach(type -> {
      Optional<GraphQLNamedOutputType> newInterfaceType = buildOutputType(buildCtx, type);
      newInterfaceType.ifPresent(newInterface -> interfaces.put(newInterface.getName(), newInterface));
    });

    extensions.forEach(extension -> extension.getImplements().forEach(type -> {
      Optional<GraphQLNamedOutputType> newInterfaceType = buildOutputType(buildCtx, type);
      newInterfaceType.ifPresent(newInterface -> {
        if (!interfaces.containsKey(newInterface.getName())) {
          interfaces.put(newInterface.getName(), newInterface);
        }
      });
    }));

    interfaces.values().forEach(interfaze -> {
      if (interfaze instanceof GraphQLInterfaceType) {
        builder.withInterface((GraphQLInterfaceType)interfaze);
        return;
      }
      if (interfaze instanceof GraphQLTypeReference) {
        builder.withInterface((GraphQLTypeReference)interfaze);
      }
    });
  }

  @NotNull GraphQLUnionType buildUnionType(BuildContext buildCtx, UnionTypeDefinition typeDefinition) {
    GraphQLUnionType.Builder builder = GraphQLUnionType.newUnionType();
    builder.definition(typeDefinition);
    builder.name(typeDefinition.getName());
    builder.description(buildDescription(typeDefinition, typeDefinition.getDescription()));
    builder.comparatorRegistry(buildCtx.getComparatorRegistry());

    List<UnionTypeExtensionDefinition> extensions = unionTypeExtensions(typeDefinition, buildCtx);
    builder.extensionDefinitions(extensions);

    typeDefinition.getMemberTypes().forEach(mt -> {
      GraphQLOutputType outputType = buildOutputType(buildCtx, mt).orElse(null);
      if (outputType instanceof GraphQLTypeReference) {
        builder.possibleType((GraphQLTypeReference)outputType);
      }
      else if (outputType instanceof GraphQLObjectType) {
        builder.possibleType((GraphQLObjectType)outputType);
      }
    });

    builder.withDirectives(
      buildDirectives(buildCtx,
                      typeDefinition.getDirectives(),
                      directivesOf(extensions),
                      UNION,
                      buildCtx.getDirectives(),
                      buildCtx.getComparatorRegistry())
    );

    extensions.forEach(extension -> extension.getMemberTypes().forEach(mt -> {
                                                                         GraphQLOutputType outputType = buildOutputType(buildCtx, mt).orElse(null);
                                                                         if (outputType instanceof GraphQLNamedOutputType && !builder.containType(((GraphQLNamedOutputType)outputType).getName())) {
                                                                           if (outputType instanceof GraphQLTypeReference) {
                                                                             builder.possibleType((GraphQLTypeReference)outputType);
                                                                           }
                                                                           else if (outputType instanceof GraphQLObjectType) {
                                                                             builder.possibleType((GraphQLObjectType)outputType);
                                                                           }
                                                                         }
                                                                       }
    ));

    return builder.build();
  }

  /**
   * This is the main recursive spot that builds out the various forms of Output types
   *
   * @param buildCtx the context we need to work out what we are doing
   * @param rawType  the type to be built
   * @return an output type
   */
  @SuppressWarnings({"TypeParameterUnusedInFormals"})
  private @NotNull <T extends GraphQLOutputType> Optional<T> buildOutputType(BuildContext buildCtx, Type rawType) {

    TypeDefinition typeDefinition = buildCtx.getTypeDefinition(rawType);
    TypeInfo typeInfo = TypeInfo.typeInfo(rawType);

    if (typeDefinition == null) {
      return Optional.empty();
    }

    GraphQLOutputType outputType = buildCtx.hasOutputType(typeDefinition);
    if (outputType != null) {
      return Optional.ofNullable(typeInfo.decorate(outputType));
    }

    if (buildCtx.stackContains(typeInfo)) {
      // we have circled around so put in a type reference and fix it up later
      // otherwise we will go into an infinite loop
      return Optional.ofNullable(typeInfo.decorate(typeRef(typeInfo.getName())));
    }

    buildCtx.push(typeInfo);

    if (typeDefinition instanceof ObjectTypeDefinition) {
      outputType = buildObjectType(buildCtx, (ObjectTypeDefinition)typeDefinition);
    }
    else if (typeDefinition instanceof InterfaceTypeDefinition) {
      outputType = buildInterfaceType(buildCtx, (InterfaceTypeDefinition)typeDefinition);
    }
    else if (typeDefinition instanceof UnionTypeDefinition) {
      outputType = buildUnionType(buildCtx, (UnionTypeDefinition)typeDefinition);
    }
    else if (typeDefinition instanceof EnumTypeDefinition) {
      outputType = buildEnumType(buildCtx, (EnumTypeDefinition)typeDefinition);
    }
    else if (typeDefinition instanceof ScalarTypeDefinition) {
      outputType = buildScalar(buildCtx, (ScalarTypeDefinition)typeDefinition);
    }
    else {
      buildCtx.addError(new NotAnOutputTypeError(rawType, typeDefinition));
    }

    if (outputType != null) {
      buildCtx.putOutputType((GraphQLNamedOutputType)outputType);
    }
    buildCtx.pop();
    return Optional.ofNullable(typeInfo.decorate(outputType));
  }

  @NotNull Optional<GraphQLFieldDefinition> buildField(BuildContext buildCtx, TypeDefinition parentType, FieldDefinition fieldDef) {
    GraphQLFieldDefinition.Builder builder = GraphQLFieldDefinition.newFieldDefinition();
    builder.definition(fieldDef);
    builder.name(fieldDef.getName());
    builder.description(buildDescription(fieldDef, fieldDef.getDescription()));
    builder.deprecate(buildDeprecationReason(fieldDef.getDirectives()));
    builder.comparatorRegistry(buildCtx.getComparatorRegistry());

    GraphQLDirective[] directives = buildDirectives(buildCtx,
                                                    fieldDef.getDirectives(),
                                                    emptyList(), FIELD_DEFINITION,
                                                    buildCtx.getDirectives(),
                                                    buildCtx.getComparatorRegistry());

    builder.withDirectives(
      directives
    );

    fieldDef.getInputValueDefinitions().forEach(inputValueDefinition ->
                                                  buildArgument(buildCtx, inputValueDefinition).ifPresent(builder::argument));

    GraphQLOutputType fieldType = buildOutputType(buildCtx, fieldDef.getType()).orElse(null);
    if (fieldType == null) return Optional.empty();

    builder.type(fieldType);

    return Optional.of(builder.build());
  }

  @NotNull Optional<GraphQLArgument> buildArgument(BuildContext buildCtx, InputValueDefinition valueDefinition) {
    GraphQLArgument.Builder builder = GraphQLArgument.newArgument();
    builder.definition(valueDefinition);
    builder.name(valueDefinition.getName());
    builder.description(buildDescription(valueDefinition, valueDefinition.getDescription()));
    builder.deprecate(buildDeprecationReason(valueDefinition.getDirectives()));
    builder.comparatorRegistry(buildCtx.getComparatorRegistry());

    GraphQLInputType inputType = buildInputType(buildCtx, valueDefinition.getType()).orElse(null);
    if (inputType == null) return Optional.empty();
    builder.type(inputType);
    Value defaultValue = valueDefinition.getDefaultValue();
    if (defaultValue != null) {
      builder.defaultValue(buildValue(buildCtx, defaultValue, inputType));
    }

    builder.withDirectives(
      buildDirectives(buildCtx,
                      valueDefinition.getDirectives(),
                      emptyList(),
                      ARGUMENT_DEFINITION,
                      buildCtx.getDirectives(),
                      buildCtx.getComparatorRegistry())
    );

    return Optional.of(builder.build());
  }

  void buildOperations(BuildContext buildCtx, GraphQLSchema.Builder schemaBuilder) {
    //
    // Schema can be missing if the type is called 'Query'.  Pre flight checks have checked that!
    //
    TypeDefinitionRegistry typeRegistry = buildCtx.getTypeRegistry();
    Map<String, OperationTypeDefinition> operationTypeDefs = buildCtx.operationTypeDefs;

    GraphQLOutputType query = null;
    GraphQLOutputType mutation;
    GraphQLOutputType subscription;

    Optional<OperationTypeDefinition> queryOperation = getOperationNamed("query", operationTypeDefs);
    if (queryOperation.isEmpty()) {
      Optional<TypeDefinition> queryTypeDef = typeRegistry.getType("Query");
      if (queryTypeDef.isPresent()) {
        query = buildOutputType(buildCtx, TypeName.newTypeName().name(queryTypeDef.get().getName()).build()).orElse(null);
      }
    }
    else {
      query = buildOperation(buildCtx, queryOperation.get()).orElse(null);
    }
    schemaBuilder.query(ObjectUtils.tryCast(query, GraphQLObjectType.class));

    Optional<OperationTypeDefinition> mutationOperation = getOperationNamed("mutation", operationTypeDefs);
    if (mutationOperation.isEmpty()) {
      Optional<TypeDefinition> mutationTypeDef = typeRegistry.getType("Mutation");
      if (mutationTypeDef.isPresent()) {
        mutation = buildOutputType(buildCtx, TypeName.newTypeName().name(mutationTypeDef.get().getName()).build()).orElse(null);
        schemaBuilder.mutation(ObjectUtils.tryCast(mutation, GraphQLObjectType.class));
      }
    }
    else {
      mutation = buildOperation(buildCtx, mutationOperation.get()).orElse(null);
      schemaBuilder.mutation(ObjectUtils.tryCast(mutation, GraphQLObjectType.class));
    }

    Optional<OperationTypeDefinition> subscriptionOperation = getOperationNamed("subscription", operationTypeDefs);
    if (subscriptionOperation.isEmpty()) {
      Optional<TypeDefinition> subscriptionTypeDef = typeRegistry.getType("Subscription");
      if (subscriptionTypeDef.isPresent()) {
        subscription = buildOutputType(buildCtx, TypeName.newTypeName().name(subscriptionTypeDef.get().getName()).build()).orElse(null);
        schemaBuilder.subscription(ObjectUtils.tryCast(subscription, GraphQLObjectType.class));
      }
    }
    else {
      subscription = buildOperation(buildCtx, subscriptionOperation.get()).orElse(null);
      schemaBuilder.subscription(ObjectUtils.tryCast(subscription, GraphQLObjectType.class));
    }
  }

  void buildSchemaDirectivesAndExtensions(BuildContext buildCtx, GraphQLSchema.Builder schemaBuilder) {
    TypeDefinitionRegistry typeRegistry = buildCtx.getTypeRegistry();
    List<Directive> schemaDirectiveList = SchemaExtensionsChecker.gatherSchemaDirectives(typeRegistry);
    Set<GraphQLDirective> runtimeDirectives = buildCtx.getDirectives();
    schemaBuilder.withSchemaDirectives(
      buildDirectives(buildCtx, schemaDirectiveList, emptyList(), DirectiveLocation.SCHEMA, runtimeDirectives,
                      buildCtx.getComparatorRegistry())
    );

    schemaBuilder.definition(typeRegistry.schemaDefinition().orElse(null));
    schemaBuilder.extensionDefinitions(typeRegistry.getSchemaExtensionDefinitions());
  }

  @NotNull List<InputObjectTypeExtensionDefinition> inputObjectTypeExtensions(InputObjectTypeDefinition typeDefinition,
                                                                              BuildContext buildCtx) {
    return buildCtx.getTypeRegistry().inputObjectTypeExtensions().getOrDefault(typeDefinition.getName(), emptyList());
  }

  @NotNull List<EnumTypeExtensionDefinition> enumTypeExtensions(EnumTypeDefinition typeDefinition, BuildContext buildCtx) {
    return buildCtx.getTypeRegistry().enumTypeExtensions().getOrDefault(typeDefinition.getName(), emptyList());
  }

  @NotNull List<ScalarTypeExtensionDefinition> scalarTypeExtensions(ScalarTypeDefinition typeDefinition, BuildContext buildCtx) {
    return buildCtx.getTypeRegistry().scalarTypeExtensions().getOrDefault(typeDefinition.getName(), emptyList());
  }

  @NotNull List<InterfaceTypeExtensionDefinition> interfaceTypeExtensions(InterfaceTypeDefinition typeDefinition, BuildContext buildCtx) {
    return buildCtx.getTypeRegistry().interfaceTypeExtensions().getOrDefault(typeDefinition.getName(), emptyList());
  }

  @NotNull List<ObjectTypeExtensionDefinition> objectTypeExtensions(ObjectTypeDefinition typeDefinition, BuildContext buildCtx) {
    return buildCtx.getTypeRegistry().objectTypeExtensions().getOrDefault(typeDefinition.getName(), emptyList());
  }

  @NotNull List<UnionTypeExtensionDefinition> unionTypeExtensions(UnionTypeDefinition typeDefinition, BuildContext buildCtx) {
    return buildCtx.getTypeRegistry().unionTypeExtensions().getOrDefault(typeDefinition.getName(), emptyList());
  }

  /**
   * We build the query / mutation / subscription path as a tree of referenced types
   * but then we build the rest of the types specified and put them in as additional types
   *
   * @param buildCtx the context we need to work out what we are doing
   * @return the additional types not referenced from the top level operations
   */
  Set<GraphQLType> buildAdditionalTypes(BuildContext buildCtx) {
    TypeDefinitionRegistry typeRegistry = buildCtx.getTypeRegistry();

    Set<String> detachedTypeNames = getDetachedTypeNames(buildCtx);

    Set<GraphQLType> additionalTypes = new LinkedHashSet<>();
    // recursively record detached types on the ctx and add them to the additionalTypes set
    typeRegistry.types().values().stream()
      .filter(typeDefinition -> detachedTypeNames.contains(typeDefinition.getName()))
      .forEach(typeDefinition -> {
        TypeName typeName = TypeName.newTypeName().name(typeDefinition.getName()).build();

        if (typeDefinition instanceof InputObjectTypeDefinition) {
          if (buildCtx.hasInputType(typeDefinition) == null) {
            buildInputType(buildCtx, typeName).ifPresent(
              inputType -> buildCtx.putInputType((GraphQLNamedInputType)inputType));
          }
          additionalTypes.add(buildCtx.inputGTypes.get(typeDefinition.getName()));
        }
        else {
          if (buildCtx.hasOutputType(typeDefinition) == null) {
            buildOutputType(buildCtx, typeName).ifPresent(
              outputType -> buildCtx.putOutputType((GraphQLNamedOutputType)outputType));
          }
          additionalTypes.add(buildCtx.outputGTypes.get(typeDefinition.getName()));
        }
      });

    typeRegistry.scalars().values().stream()
      .filter(typeDefinition -> detachedTypeNames.contains(typeDefinition.getName()))
      .forEach(scalarTypeDefinition -> {
        if (ScalarInfo.isGraphqlSpecifiedScalar(scalarTypeDefinition.getName())) {
          return;
        }

        if (buildCtx.hasInputType(scalarTypeDefinition) == null && buildCtx.hasOutputType(scalarTypeDefinition) == null) {
          buildCtx.putOutputType(buildScalar(buildCtx, scalarTypeDefinition));
        }
        if (buildCtx.hasInputType(scalarTypeDefinition) != null) {
          additionalTypes.add(buildCtx.inputGTypes.get(scalarTypeDefinition.getName()));
        }
        else if (buildCtx.hasOutputType(scalarTypeDefinition) != null) {
          additionalTypes.add(buildCtx.outputGTypes.get(scalarTypeDefinition.getName()));
        }
      });

    return additionalTypes;
  }

  /**
   * Detached types (or additional types) are all types that
   * are not connected to the root operations types.
   *
   * @param buildCtx buildCtx
   * @return detached type names
   */
  private Set<String> getDetachedTypeNames(BuildContext buildCtx) {
    TypeDefinitionRegistry typeRegistry = buildCtx.getTypeRegistry();
    // connected types are all types that have a path that connects them back to the root operation types.
    Set<String> connectedTypes = new HashSet<>(buildCtx.inputGTypes.keySet());
    connectedTypes.addAll(buildCtx.outputGTypes.keySet());

    Set<String> allTypeNames = new HashSet<>(typeRegistry.types().keySet());
    Set<String> scalars = new HashSet<>(typeRegistry.scalars().keySet());
    allTypeNames.addAll(scalars);

    // detached types are all types minus the connected types.
    Set<String> detachedTypeNames = new HashSet<>(allTypeNames);
    detachedTypeNames.removeAll(connectedTypes);
    return detachedTypeNames;
  }

  Set<GraphQLDirective> buildAdditionalDirectives(BuildContext buildCtx) {
    Set<GraphQLDirective> additionalDirectives = new LinkedHashSet<>();
    TypeDefinitionRegistry typeRegistry = buildCtx.getTypeRegistry();

    for (DirectiveDefinition directiveDefinition : typeRegistry.getDirectiveDefinitions().values()) {
      Function<Type, Optional<GraphQLInputType>> inputTypeFactory = inputType -> buildInputType(buildCtx, inputType);
      GraphQLDirective directive = buildDirectiveFromDefinition(buildCtx, directiveDefinition, inputTypeFactory);
      buildCtx.addDirectiveDefinition(directive);
      additionalDirectives.add(directive);
    }
    return additionalDirectives;
  }

  void addDirectivesIncludedByDefault(TypeDefinitionRegistry typeRegistry) {
    if (typeRegistry.getDirectiveDefinition(DEPRECATED_DIRECTIVE_DEFINITION.getName()).isEmpty()) {
      typeRegistry.add(DEPRECATED_DIRECTIVE_DEFINITION);
    }
    if (typeRegistry.getDirectiveDefinition(SPECIFIED_BY_DIRECTIVE_DEFINITION.getName()).isEmpty()) {
      typeRegistry.add(SPECIFIED_BY_DIRECTIVE_DEFINITION);
    }
  }

  private @NotNull Optional<OperationTypeDefinition> getOperationNamed(String name,
                                                                       Map<String, OperationTypeDefinition> operationTypeDefs) {
    return Optional.ofNullable(operationTypeDefs.get(name));
  }

  private @NotNull List<Directive> directivesOf(List<? extends TypeDefinition> typeDefinitions) {
    //noinspection RedundantTypeArguments
    Stream<Directive> stream = typeDefinitions.stream()
      .map(TypeDefinition::getDirectives).filter(Objects::nonNull)
      .<Directive>flatMap(List::stream);
    return stream.collect(Collectors.toList());
  }
}
