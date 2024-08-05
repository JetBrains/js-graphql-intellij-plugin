package com.intellij.lang.jsgraphql.ide.introspection;

import com.google.common.collect.Lists;
import com.intellij.lang.jsgraphql.psi.GraphQLElementFactory;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.lang.jsgraphql.types.Assert.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public class GraphQLIntrospectionResultToSchema {

  private static final Logger LOG = Logger.getInstance(GraphQLIntrospectionResultToSchema.class);

  private final Project myProject;

  public GraphQLIntrospectionResultToSchema(@NotNull Project project) {
    myProject = project;
  }

  /**
   * Returns a IDL Document that represents the schema as defined by the introspection result map
   *
   * @param introspectionResult the result of an introspection query on a schema
   * @return a IDL Document of the schema
   */
  @SuppressWarnings("unchecked")
  public Document createSchemaDefinition(@NotNull Map<String, Object> introspectionResult) {
    assertTrue(introspectionResult.get("__schema") != null, () -> "__schema expected");
    Map<String, Object> schema = (Map<String, Object>)introspectionResult.get("__schema");

    SchemaDefinition.Builder schemaDefinition = SchemaDefinition.newSchemaDefinition();

    Map<String, Object> queryType = (Map<String, Object>)schema.get("queryType");
    boolean nonDefaultQueryName = false;
    if (queryType != null) {
      TypeName query = TypeName.newTypeName().name((String)queryType.get("name")).build();
      nonDefaultQueryName = !"Query".equals(query.getName());
      schemaDefinition.operationTypeDefinition(
        OperationTypeDefinition.newOperationTypeDefinition().name("query").typeName(query).build());
    }

    Map<String, Object> mutationType = (Map<String, Object>)schema.get("mutationType");
    boolean nonDefaultMutationName = false;
    if (mutationType != null) {
      TypeName mutation = TypeName.newTypeName().name((String)mutationType.get("name")).build();
      nonDefaultMutationName = !"Mutation".equals(mutation.getName());
      schemaDefinition.operationTypeDefinition(
        OperationTypeDefinition.newOperationTypeDefinition().name("mutation").typeName(mutation).build());
    }

    Map<String, Object> subscriptionType = (Map<String, Object>)schema.get("subscriptionType");
    boolean nonDefaultSubscriptionName = false;
    if (subscriptionType != null) {
      TypeName subscription = TypeName.newTypeName().name(((String)subscriptionType.get("name"))).build();
      nonDefaultSubscriptionName = !"Subscription".equals(subscription.getName());
      schemaDefinition.operationTypeDefinition(
        OperationTypeDefinition.newOperationTypeDefinition().name("subscription")
          .typeName(subscription).build());
    }

    Document.Builder document = Document.newDocument();
    if (nonDefaultQueryName || nonDefaultMutationName || nonDefaultSubscriptionName) {
      document.definition(schemaDefinition.build());
    }

    List<Map<String, Object>> types = (List<Map<String, Object>>)schema.get("types");
    if (types != null) {
      for (Map<String, Object> type : types) {
        if (type == null) continue;
        TypeDefinition<?> typeDefinition = createTypeDefinition(type);
        document.definition(typeDefinition);
      }
    }

    List<Map<String, Object>> directives = (List<Map<String, Object>>)schema.get("directives");
    if (directives != null) {
      for (Map<String, Object> directive : directives) {
        if (directive == null) continue;
        DirectiveDefinition directiveDefinition = createDirectiveDefinition(directive);
        document.definition(directiveDefinition);
      }
    }

    return document.build();
  }

  private @NotNull TypeDefinition<?> createTypeDefinition(@NotNull Map<String, Object> type) {
    String kind = assertNotNull((String)type.get("kind"),
                                () -> String.format("null object kind: %s", type));

    return switch (kind) {
      case "INTERFACE" -> createInterface(type);
      case "OBJECT" -> createObject(type);
      case "UNION" -> createUnion(type);
      case "ENUM" -> createEnum(type);
      case "INPUT_OBJECT" -> createInputObject(type);
      case "SCALAR" -> createScalar(type);
      default -> assertShouldNeverHappen("unexpected kind %s", kind);
    };
  }

  private @NotNull List<FieldDefinition> createFields(@Nullable List<Map<String, Object>> fields) {
    if (fields == null) return ContainerUtil.emptyList();

    List<FieldDefinition> result = new ArrayList<>();
    for (Map<String, Object> field : fields) {
      if (field == null) continue;

      List<Map<String, Object>> args = (List<Map<String, Object>>)field.get("args");
      List<InputValueDefinition> inputValueDefinitions = createInputValueDefinitions(args);
      FieldDefinition fieldDefinition = FieldDefinition.newFieldDefinition()
        .name((String)field.get("name"))
        .description(getDescription(field))
        .type(createTypeReference((Map<String, Object>)field.get("type")))
        .inputValueDefinitions(inputValueDefinitions)
        .directives(createDeprecatedDirective(field))
        .build();
      result.add(fieldDefinition);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private @NotNull List<InputValueDefinition> createInputValueDefinitions(@Nullable List<Map<String, Object>> args) {
    if (args == null) return ContainerUtil.emptyList();

    List<InputValueDefinition> result = new ArrayList<>();
    for (Map<String, Object> arg : args) {
      if (arg == null) continue;

      Type argType = createTypeReference((Map<String, Object>)arg.get("type"));
      String valueLiteral = (String)arg.get("defaultValue");
      Value defaultValue = valueLiteral != null ? valueFromAst(valueLiteral) : null;
      InputValueDefinition inputValueDefinition = InputValueDefinition.newInputValueDefinition()
        .name((String)arg.get("name"))
        .type(argType)
        .description(getDescription(arg))
        .defaultValue(defaultValue)
        .build();
      result.add(inputValueDefinition);
    }
    return result;
  }

  private @NotNull DirectiveDefinition createDirectiveDefinition(@NotNull Map<String, Object> definition) {
    List<Map<String, Object>> args = (List<Map<String, Object>>)definition.get("args");
    List<InputValueDefinition> inputValueDefinitions = createInputValueDefinitions(args);

    return DirectiveDefinition.newDirectiveDefinition()
      .name(((String)definition.get("name")))
      .description(getDescription(definition))
      .directiveLocations(createDirectiveLocations((List<String>)definition.get("locations")))
      .inputValueDefinitions(inputValueDefinitions)
      .repeatable(isRepeatable(definition))
      .build();
  }

  @SuppressWarnings("unchecked")
  @NotNull
  InterfaceTypeDefinition createInterface(@NotNull Map<String, Object> input) {
    assertTrue(Objects.equals(input.get("kind"), "INTERFACE"), () -> "wrong input");

    final List<Map<String, Object>> fields = (List<Map<String, Object>>)input.get("fields");

    return InterfaceTypeDefinition.newInterfaceTypeDefinition()
      .name((String)input.get("name"))
      .description(getDescription(input))
      .definitions(createFields(fields))
      .build();
  }

  @SuppressWarnings("unchecked")
  @NotNull
  InputObjectTypeDefinition createInputObject(@NotNull Map<String, Object> input) {
    assertTrue(Objects.equals(input.get("kind"), "INPUT_OBJECT"), () -> "wrong input");
    final List<Map<String, Object>> fields = (List<Map<String, Object>>)input.get("inputFields");

    return InputObjectTypeDefinition.newInputObjectDefinition()
      .name((String)input.get("name"))
      .description(getDescription(input))
      .inputValueDefinitions(createInputValueDefinitions(fields))
      .build();
  }

  @SuppressWarnings("unchecked")
  @NotNull
  ObjectTypeDefinition createObject(@NotNull Map<String, Object> input) {
    assertTrue(Objects.equals(input.get("kind"), "OBJECT"), () -> "wrong input");

    ObjectTypeDefinition.Builder builder = ObjectTypeDefinition.newObjectTypeDefinition()
      .name((String)input.get("name"))
      .description(getDescription(input));
    if (input.containsKey("interfaces")) {
      builder.implementz(
        ((List<Map<String, Object>>)input.get("interfaces")).stream()
          .map(GraphQLIntrospectionResultToSchema::createTypeReference)
          .collect(Collectors.toList())
      );
    }
    List<Map<String, Object>> fields = (List<Map<String, Object>>)input.get("fields");

    builder.fieldDefinitions(createFields(fields));

    return builder.build();
  }

  private @Nullable Value<?> valueFromAst(@NotNull String literal) {
    try {
      Document doc = ReadAction.compute(() -> {
        String text = "input X { x: String = " + literal + "}";
        GraphQLFile file = GraphQLElementFactory.createFile(myProject, text);
        return file.getDocument();
      });
      InputObjectTypeDefinition inputType = (InputObjectTypeDefinition)doc.getDefinitions().get(0);
      InputValueDefinition inputValueDefinition = inputType.getInputValueDefinitions().get(0);
      return inputValueDefinition.getDefaultValue();
    }
    catch (ProcessCanceledException e) {
      throw e;
    }
    catch (Exception e) {
      LOG.error(e);
    }

    return null;
  }

  private static @NotNull TypeDefinition<?> createScalar(@NotNull Map<String, Object> input) {
    String name = (String)input.get("name");
    return ScalarTypeDefinition.newScalarTypeDefinition().name(name).description(getDescription(input)).build();
  }

  @SuppressWarnings("unchecked")
  static @NotNull UnionTypeDefinition createUnion(@NotNull Map<String, Object> input) {
    assertTrue(Objects.equals(input.get("kind"), "UNION"), () -> "wrong input");

    final List<Map<String, Object>> possibleTypes = (List<Map<String, Object>>)input.get("possibleTypes");
    final List<Type> memberTypes = Lists.newArrayList();
    if (possibleTypes != null) {
      for (Map<String, Object> possibleType : possibleTypes) {
        if (possibleType == null) continue;
        TypeName typeName = new TypeName((String)possibleType.get("name"));
        memberTypes.add(typeName);
      }
    }

    return UnionTypeDefinition.newUnionTypeDefinition()
      .name((String)input.get("name"))
      .description(getDescription(input))
      .memberTypes(memberTypes)
      .build();
  }

  @SuppressWarnings("unchecked")
  static @NotNull EnumTypeDefinition createEnum(@NotNull Map<String, Object> input) {
    assertTrue(Objects.equals(input.get("kind"), "ENUM"), () -> "wrong input");

    final List<Map<String, Object>> enumValues = (List<Map<String, Object>>)input.get("enumValues");
    final List<EnumValueDefinition> enumValueDefinitions = Lists.newArrayList();
    if (enumValues != null) {
      for (Map<String, Object> enumValue : enumValues) {
        if (enumValue == null) continue;

        EnumValueDefinition enumValueDefinition = EnumValueDefinition.newEnumValueDefinition()
          .name((String)enumValue.get("name"))
          .description(getDescription(enumValue))
          .directives(createDeprecatedDirective(enumValue))
          .build();

        enumValueDefinitions.add(enumValueDefinition);
      }
    }

    return EnumTypeDefinition.newEnumTypeDefinition()
      .name((String)input.get("name"))
      .description(getDescription(input))
      .enumValueDefinitions(enumValueDefinitions)
      .build();
  }

  private static @NotNull List<Directive> createDeprecatedDirective(@NotNull Map<String, Object> field) {
    if ((Boolean)field.get("isDeprecated")) {
      String reason = (String)field.get("deprecationReason");
      if (reason == null) {
        reason = "No longer supported"; // default according to spec
      }
      Argument reasonArg = new Argument("reason", new StringValue(reason));
      return Collections.singletonList(new Directive("deprecated", Collections.singletonList(reasonArg)));
    }
    return Collections.emptyList();
  }

  private static @Nullable Type createTypeReference(@Nullable Map<String, Object> type) {
    if (type == null) return null;

    String kind = (String)type.get("kind");
    switch (kind) {
      case "INTERFACE", "OBJECT", "UNION", "ENUM", "INPUT_OBJECT", "SCALAR" -> {
        return TypeName.newTypeName().name((String)type.get("name")).build();
      }
      case "NON_NULL" -> {
        Type ofType = createTypeReference((Map<String, Object>)type.get("ofType"));
        if (ofType == null) return null;
        return NonNullType.newNonNullType().type(ofType).build();
      }
      case "LIST" -> {
        return ListType.newListType()
          .type(createTypeReference((Map<String, Object>)type.get("ofType"))).build();
      }
      default -> {
        return assertShouldNeverHappen("Unknown kind %s", kind);
      }
    }
  }

  private boolean isRepeatable(@NotNull Map<String, Object> definition) {
    Object isRepeatable = definition.get("isRepeatable");
    return isRepeatable instanceof Boolean && (boolean)isRepeatable;
  }

  private static @Nullable Description getDescription(@Nullable Map<String, Object> descriptionAware) {
    if (descriptionAware == null) return null;

    final Object rawDescription = descriptionAware.get("description");
    if (rawDescription instanceof String description) {
      if (!description.trim().isEmpty()) {
        final boolean multiLine = description.contains("\n");
        if (multiLine) {
          // ensures the description stands on separate lines from the triple quotes
          description = "\n" + description.trim() + "\n";
        }
        return new Description(description, new SourceLocation(1, 1), multiLine);
      }
    }
    return null;
  }

  private static @NotNull List<DirectiveLocation> createDirectiveLocations(@NotNull List<String> locations) {
    return ContainerUtil.mapNotNull(
      locations,
      location -> location != null ? DirectiveLocation.newDirectiveLocation().name(location).build() : null);
  }
}
