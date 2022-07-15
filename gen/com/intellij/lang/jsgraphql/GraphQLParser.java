// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class GraphQLParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType type, PsiBuilder builder) {
    parseLight(type, builder);
    return builder.getTreeBuilt();
  }

  public void parseLight(IElementType type, PsiBuilder builder) {
    boolean result;
    builder = adapt_builder_(type, builder, this, EXTENDS_SETS_);
    Marker marker = enter_section_(builder, 0, _COLLAPSE_, null);
    result = parse_root_(type, builder);
    exit_section_(builder, 0, marker, type, result, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType type, PsiBuilder builder) {
    return parse_root_(type, builder, 0);
  }

  static boolean parse_root_(IElementType type, PsiBuilder builder, int level) {
    return document(builder, level + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(BLOCK_STRING, QUOTED_STRING, STRING_LITERAL),
    create_token_set_(LIST_TYPE, NON_NULL_TYPE, TYPE, TYPE_NAME,
      TYPE_NAME_DEFINITION),
    create_token_set_(ARRAY_VALUE, BOOLEAN_VALUE, ENUM_VALUE, FLOAT_VALUE,
      INT_VALUE, NULL_VALUE, OBJECT_VALUE, STRING_VALUE,
      TEMPLATE_VARIABLE, VALUE, VARIABLE),
    create_token_set_(DEFINITION, DIRECTIVE_DEFINITION, ENUM_TYPE_DEFINITION, ENUM_TYPE_EXTENSION_DEFINITION,
      FRAGMENT_DEFINITION, INPUT_OBJECT_TYPE_DEFINITION, INPUT_OBJECT_TYPE_EXTENSION_DEFINITION, INTERFACE_TYPE_DEFINITION,
      INTERFACE_TYPE_EXTENSION_DEFINITION, OBJECT_TYPE_DEFINITION, OBJECT_TYPE_EXTENSION_DEFINITION, OPERATION_DEFINITION,
      SCALAR_TYPE_DEFINITION, SCALAR_TYPE_EXTENSION_DEFINITION, SCHEMA_DEFINITION, SCHEMA_EXTENSION,
      SELECTION_SET_OPERATION_DEFINITION, TEMPLATE_DEFINITION, TYPED_OPERATION_DEFINITION, TYPE_DEFINITION,
      TYPE_EXTENSION, TYPE_SYSTEM_DEFINITION, UNION_TYPE_DEFINITION, UNION_TYPE_EXTENSION_DEFINITION),
  };

  /* ********************************************************** */
  // identifier ':'
  public static boolean alias(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "alias")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, ALIAS, "<alias>");
    result = identifier(builder, level + 1);
    result = result && consumeToken(builder, COLON);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // '&'? typeName
  static boolean ampTypeName(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ampTypeName")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = ampTypeName_0(builder, level + 1);
    result = result && typeName(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // '&'?
  private static boolean ampTypeName_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "ampTypeName_0")) return false;
    consumeToken(builder, AMP);
    return true;
  }

  /* ********************************************************** */
  // identifier ':' value
  public static boolean argument(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "argument")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ARGUMENT, "<argument>");
    result = identifier(builder, level + 1);
    pinned = result; // pin = 1
    result = result && report_error_(builder, consumeToken(builder, COLON));
    result = pinned && value(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::argument_recover);
    return result || pinned;
  }

  /* ********************************************************** */
  // !(')' | argument)
  static boolean argument_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "argument_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !argument_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // ')' | argument
  private static boolean argument_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "argument_recover_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, PAREN_R);
    if (!result) result = argument(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // '(' argument+ ')'
  public static boolean arguments(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "arguments")) return false;
    if (!nextTokenIs(builder, PAREN_L)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ARGUMENTS, null);
    result = consumeToken(builder, PAREN_L);
    pinned = result; // pin = 1
    result = result && report_error_(builder, arguments_1(builder, level + 1));
    result = pinned && consumeToken(builder, PAREN_R) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // argument+
  private static boolean arguments_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "arguments_1")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = argument(builder, level + 1);
    while (result) {
      int pos = current_position_(builder);
      if (!argument(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "arguments_1", pos)) break;
    }
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // '(' inputValueDefinition+ ')'
  public static boolean argumentsDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "argumentsDefinition")) return false;
    if (!nextTokenIs(builder, PAREN_L)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ARGUMENTS_DEFINITION, null);
    result = consumeToken(builder, PAREN_L);
    pinned = result; // pin = 1
    result = result && report_error_(builder, argumentsDefinition_1(builder, level + 1));
    result = pinned && consumeToken(builder, PAREN_R) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // inputValueDefinition+
  private static boolean argumentsDefinition_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "argumentsDefinition_1")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = inputValueDefinition(builder, level + 1);
    while (result) {
      int pos = current_position_(builder);
      if (!inputValueDefinition(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "argumentsDefinition_1", pos)) break;
    }
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // '[' arrayValueElement* ']'
  public static boolean arrayValue(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "arrayValue")) return false;
    if (!nextTokenIs(builder, BRACKET_L)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ARRAY_VALUE, null);
    result = consumeToken(builder, BRACKET_L);
    pinned = result; // pin = 1
    result = result && report_error_(builder, arrayValue_1(builder, level + 1));
    result = pinned && consumeToken(builder, BRACKET_R) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // arrayValueElement*
  private static boolean arrayValue_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "arrayValue_1")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!arrayValueElement(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "arrayValue_1", pos)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // value
  static boolean arrayValueElement(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "arrayValueElement")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = value(builder, level + 1);
    exit_section_(builder, level, marker, result, false, GraphQLParser::arrayValueElement_recover);
    return result;
  }

  /* ********************************************************** */
  // !(']'| value)
  static boolean arrayValueElement_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "arrayValueElement_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !arrayValueElement_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // ']'| value
  private static boolean arrayValueElement_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "arrayValueElement_recover_0")) return false;
    boolean result;
    result = consumeToken(builder, BRACKET_R);
    if (!result) result = value(builder, level + 1);
    return result;
  }

  /* ********************************************************** */
  // OPEN_TRIPLE_QUOTE REGULAR_STRING_PART* CLOSING_TRIPLE_QUOTE
  public static boolean blockString(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "blockString")) return false;
    if (!nextTokenIs(builder, OPEN_TRIPLE_QUOTE)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, BLOCK_STRING, null);
    result = consumeToken(builder, OPEN_TRIPLE_QUOTE);
    pinned = result; // pin = 1
    result = result && report_error_(builder, blockString_1(builder, level + 1));
    result = pinned && consumeToken(builder, CLOSING_TRIPLE_QUOTE) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // REGULAR_STRING_PART*
  private static boolean blockString_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "blockString_1")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!consumeToken(builder, REGULAR_STRING_PART)) break;
      if (!empty_element_parsed_guard_(builder, "blockString_1", pos)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // 'true' | 'false'
  public static boolean booleanValue(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "booleanValue")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, BOOLEAN_VALUE, "<boolean value>");
    result = consumeToken(builder, "true");
    if (!result) result = consumeToken(builder, "false");
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // '=' value
  public static boolean defaultValue(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "defaultValue")) return false;
    if (!nextTokenIs(builder, EQUALS)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, DEFAULT_VALUE, null);
    result = consumeToken(builder, EQUALS);
    pinned = result; // pin = 1
    result = result && value(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // operationDefinition |
  //   fragmentDefinition |
  //   typeSystemDefinition |
  //   templateDefinition
  public static boolean definition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "definition")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _COLLAPSE_, DEFINITION, "<definition>");
    result = operationDefinition(builder, level + 1);
    if (!result) result = fragmentDefinition(builder, level + 1);
    if (!result) result = typeSystemDefinition(builder, level + 1);
    if (!result) result = templateDefinition(builder, level + 1);
    exit_section_(builder, level, marker, result, false, GraphQLParser::definition_recover);
    return result;
  }

  /* ********************************************************** */
  // operationType | 'fragment' | 'schema' | 'type' | 'interface' | 'input' | 'enum' | 'union' | 'scalar' | 'directive' | 'extend'
  static boolean definition_keywords(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "definition_keywords")) return false;
    boolean result;
    result = operationType(builder, level + 1);
    if (!result) result = consumeToken(builder, FRAGMENT_KEYWORD);
    if (!result) result = consumeToken(builder, SCHEMA_KEYWORD);
    if (!result) result = consumeToken(builder, TYPE_KEYWORD);
    if (!result) result = consumeToken(builder, INTERFACE_KEYWORD);
    if (!result) result = consumeToken(builder, INPUT_KEYWORD);
    if (!result) result = consumeToken(builder, ENUM_KEYWORD);
    if (!result) result = consumeToken(builder, UNION_KEYWORD);
    if (!result) result = consumeToken(builder, SCALAR_KEYWORD);
    if (!result) result = consumeToken(builder, DIRECTIVE_KEYWORD);
    if (!result) result = consumeToken(builder, EXTEND_KEYWORD);
    return result;
  }

  /* ********************************************************** */
  // !(root_tokens | NAME)
  static boolean definition_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "definition_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !definition_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // root_tokens | NAME
  private static boolean definition_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "definition_recover_0")) return false;
    boolean result;
    result = root_tokens(builder, level + 1);
    if (!result) result = consumeToken(builder, NAME);
    return result;
  }

  /* ********************************************************** */
  // stringLiteral
  public static boolean description(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "description")) return false;
    if (!nextTokenIs(builder, "<description>", OPEN_QUOTE, OPEN_TRIPLE_QUOTE)) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, DESCRIPTION, "<description>");
    result = stringLiteral(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // '@' identifier arguments?
  public static boolean directive(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "directive")) return false;
    if (!nextTokenIs(builder, AT)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, DIRECTIVE, null);
    result = consumeToken(builder, AT);
    pinned = result; // pin = 1
    result = result && report_error_(builder, identifier(builder, level + 1));
    result = pinned && directive_2(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // arguments?
  private static boolean directive_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "directive_2")) return false;
    arguments(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // description? 'directive' '@' identifier argumentsDefinition? 'repeatable'? 'on' directiveLocations
  public static boolean directiveDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "directiveDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, DIRECTIVE_DEFINITION, "<directive definition>");
    result = directiveDefinition_0(builder, level + 1);
    result = result && consumeTokens(builder, 1, DIRECTIVE_KEYWORD, AT);
    pinned = result; // pin = 2
    result = result && report_error_(builder, identifier(builder, level + 1));
    result = pinned && report_error_(builder, directiveDefinition_4(builder, level + 1)) && result;
    result = pinned && report_error_(builder, directiveDefinition_5(builder, level + 1)) && result;
    result = pinned && report_error_(builder, consumeToken(builder, ON_KEYWORD)) && result;
    result = pinned && directiveLocations(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // description?
  private static boolean directiveDefinition_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "directiveDefinition_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // argumentsDefinition?
  private static boolean directiveDefinition_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "directiveDefinition_4")) return false;
    argumentsDefinition(builder, level + 1);
    return true;
  }

  // 'repeatable'?
  private static boolean directiveDefinition_5(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "directiveDefinition_5")) return false;
    consumeToken(builder, REPEATABLE_KEYWORD);
    return true;
  }

  /* ********************************************************** */
  // 'QUERY' | 'MUTATION' | 'SUBSCRIPTION' | 'FIELD' | 'FRAGMENT_DEFINITION' | 'FRAGMENT_SPREAD' | 'INLINE_FRAGMENT' |
  //   'SCHEMA' | 'SCALAR' | 'OBJECT' | 'FIELD_DEFINITION' | 'ARGUMENT_DEFINITION' | 'INTERFACE' | 'UNION' | 'ENUM' |
  //   'ENUM_VALUE' | 'INPUT_OBJECT' | 'INPUT_FIELD_DEFINITION' | NAME
  public static boolean directiveLocation(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "directiveLocation")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, DIRECTIVE_LOCATION, "<directive location>");
    result = consumeToken(builder, "QUERY");
    if (!result) result = consumeToken(builder, "MUTATION");
    if (!result) result = consumeToken(builder, "SUBSCRIPTION");
    if (!result) result = consumeToken(builder, "FIELD");
    if (!result) result = consumeToken(builder, "FRAGMENT_DEFINITION");
    if (!result) result = consumeToken(builder, "FRAGMENT_SPREAD");
    if (!result) result = consumeToken(builder, "INLINE_FRAGMENT");
    if (!result) result = consumeToken(builder, "SCHEMA");
    if (!result) result = consumeToken(builder, "SCALAR");
    if (!result) result = consumeToken(builder, "OBJECT");
    if (!result) result = consumeToken(builder, "FIELD_DEFINITION");
    if (!result) result = consumeToken(builder, "ARGUMENT_DEFINITION");
    if (!result) result = consumeToken(builder, "INTERFACE");
    if (!result) result = consumeToken(builder, "UNION");
    if (!result) result = consumeToken(builder, "ENUM");
    if (!result) result = consumeToken(builder, "ENUM_VALUE");
    if (!result) result = consumeToken(builder, "INPUT_OBJECT");
    if (!result) result = consumeToken(builder, "INPUT_FIELD_DEFINITION");
    if (!result) result = consumeToken(builder, NAME);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // '|'? directiveLocation pipeDirectiveLocation*
  public static boolean directiveLocations(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "directiveLocations")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, DIRECTIVE_LOCATIONS, "<directive locations>");
    result = directiveLocations_0(builder, level + 1);
    result = result && directiveLocation(builder, level + 1);
    result = result && directiveLocations_2(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // '|'?
  private static boolean directiveLocations_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "directiveLocations_0")) return false;
    consumeToken(builder, PIPE);
    return true;
  }

  // pipeDirectiveLocation*
  private static boolean directiveLocations_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "directiveLocations_2")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!pipeDirectiveLocation(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "directiveLocations_2", pos)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // directive+
  static boolean directives(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "directives")) return false;
    if (!nextTokenIs(builder, AT)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = directive(builder, level + 1);
    while (result) {
      int pos = current_position_(builder);
      if (!directive(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "directives", pos)) break;
    }
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // definition*
  static boolean document(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "document")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!definition(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "document", pos)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // description? 'enum' typeNameDefinition directives? enumValueDefinitions?
  public static boolean enumTypeDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumTypeDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ENUM_TYPE_DEFINITION, "<enum type definition>");
    result = enumTypeDefinition_0(builder, level + 1);
    result = result && consumeToken(builder, ENUM_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeNameDefinition(builder, level + 1));
    result = pinned && report_error_(builder, enumTypeDefinition_3(builder, level + 1)) && result;
    result = pinned && enumTypeDefinition_4(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // description?
  private static boolean enumTypeDefinition_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumTypeDefinition_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean enumTypeDefinition_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumTypeDefinition_3")) return false;
    directives(builder, level + 1);
    return true;
  }

  // enumValueDefinitions?
  private static boolean enumTypeDefinition_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumTypeDefinition_4")) return false;
    enumValueDefinitions(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // 'extend' 'enum' typeName directives? enumValueDefinitions?
  public static boolean enumTypeExtensionDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumTypeExtensionDefinition")) return false;
    if (!nextTokenIs(builder, EXTEND_KEYWORD)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ENUM_TYPE_EXTENSION_DEFINITION, null);
    result = consumeTokens(builder, 2, EXTEND_KEYWORD, ENUM_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeName(builder, level + 1));
    result = pinned && report_error_(builder, enumTypeExtensionDefinition_3(builder, level + 1)) && result;
    result = pinned && enumTypeExtensionDefinition_4(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // directives?
  private static boolean enumTypeExtensionDefinition_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumTypeExtensionDefinition_3")) return false;
    directives(builder, level + 1);
    return true;
  }

  // enumValueDefinitions?
  private static boolean enumTypeExtensionDefinition_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumTypeExtensionDefinition_4")) return false;
    enumValueDefinitions(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // identifier
  public static boolean enumValue(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumValue")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, ENUM_VALUE, "<enum value>");
    result = identifier(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // description? enumValue directives?
  public static boolean enumValueDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumValueDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ENUM_VALUE_DEFINITION, "<enum value definition>");
    result = enumValueDefinition_0(builder, level + 1);
    result = result && enumValue(builder, level + 1);
    pinned = result; // pin = 2
    result = result && enumValueDefinition_2(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::enumValueDefinition_recover);
    return result || pinned;
  }

  // description?
  private static boolean enumValueDefinition_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumValueDefinition_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean enumValueDefinition_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumValueDefinition_2")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // !('}' | enumValueDefinition | root_tokens)
  static boolean enumValueDefinition_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumValueDefinition_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !enumValueDefinition_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // '}' | enumValueDefinition | root_tokens
  private static boolean enumValueDefinition_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumValueDefinition_recover_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, BRACE_R);
    if (!result) result = enumValueDefinition(builder, level + 1);
    if (!result) result = root_tokens(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // '{' enumValueDefinition+ '}'
  public static boolean enumValueDefinitions(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumValueDefinitions")) return false;
    if (!nextTokenIs(builder, BRACE_L)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ENUM_VALUE_DEFINITIONS, null);
    result = consumeToken(builder, BRACE_L);
    pinned = result; // pin = 1
    result = result && report_error_(builder, enumValueDefinitions_1(builder, level + 1));
    result = pinned && consumeToken(builder, BRACE_R) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // enumValueDefinition+
  private static boolean enumValueDefinitions_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumValueDefinitions_1")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = enumValueDefinition(builder, level + 1);
    while (result) {
      int pos = current_position_(builder);
      if (!enumValueDefinition(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "enumValueDefinitions_1", pos)) break;
    }
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // alias? identifier arguments? directives? selectionSet?
  public static boolean field(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "field")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, FIELD, "<field>");
    result = field_0(builder, level + 1);
    result = result && identifier(builder, level + 1);
    result = result && field_2(builder, level + 1);
    result = result && field_3(builder, level + 1);
    result = result && field_4(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // alias?
  private static boolean field_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "field_0")) return false;
    alias(builder, level + 1);
    return true;
  }

  // arguments?
  private static boolean field_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "field_2")) return false;
    arguments(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean field_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "field_3")) return false;
    directives(builder, level + 1);
    return true;
  }

  // selectionSet?
  private static boolean field_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "field_4")) return false;
    selectionSet(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // description? identifier argumentsDefinition? ':' type directives?
  public static boolean fieldDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fieldDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, FIELD_DEFINITION, "<field definition>");
    result = fieldDefinition_0(builder, level + 1);
    result = result && identifier(builder, level + 1);
    pinned = result; // pin = 2
    result = result && report_error_(builder, fieldDefinition_2(builder, level + 1));
    result = pinned && report_error_(builder, consumeToken(builder, COLON)) && result;
    result = pinned && report_error_(builder, type(builder, level + 1)) && result;
    result = pinned && fieldDefinition_5(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::fieldDefinition_recover);
    return result || pinned;
  }

  // description?
  private static boolean fieldDefinition_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fieldDefinition_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // argumentsDefinition?
  private static boolean fieldDefinition_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fieldDefinition_2")) return false;
    argumentsDefinition(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean fieldDefinition_5(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fieldDefinition_5")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // !('}' | root_tokens | fieldDefinition)
  static boolean fieldDefinition_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fieldDefinition_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !fieldDefinition_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // '}' | root_tokens | fieldDefinition
  private static boolean fieldDefinition_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fieldDefinition_recover_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, BRACE_R);
    if (!result) result = root_tokens(builder, level + 1);
    if (!result) result = fieldDefinition(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // '{' fieldDefinition* '}'
  public static boolean fieldsDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fieldsDefinition")) return false;
    if (!nextTokenIs(builder, BRACE_L)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, FIELDS_DEFINITION, null);
    result = consumeToken(builder, BRACE_L);
    pinned = result; // pin = 1
    result = result && report_error_(builder, fieldsDefinition_1(builder, level + 1));
    result = pinned && consumeToken(builder, BRACE_R) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // fieldDefinition*
  private static boolean fieldsDefinition_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fieldsDefinition_1")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!fieldDefinition(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "fieldsDefinition_1", pos)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // FLOAT
  public static boolean floatValue(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "floatValue")) return false;
    if (!nextTokenIs(builder, FLOAT)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, FLOAT);
    exit_section_(builder, marker, FLOAT_VALUE, result);
    return result;
  }

  /* ********************************************************** */
  // 'fragment' fragmentName typeCondition directives? selectionSet
  public static boolean fragmentDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fragmentDefinition")) return false;
    if (!nextTokenIs(builder, FRAGMENT_KEYWORD)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, FRAGMENT_DEFINITION, null);
    result = consumeToken(builder, FRAGMENT_KEYWORD);
    pinned = result; // pin = 1
    result = result && report_error_(builder, fragmentName(builder, level + 1));
    result = pinned && report_error_(builder, typeCondition(builder, level + 1)) && result;
    result = pinned && report_error_(builder, fragmentDefinition_3(builder, level + 1)) && result;
    result = pinned && selectionSet(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // directives?
  private static boolean fragmentDefinition_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fragmentDefinition_3")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // !'on' identifier
  static boolean fragmentName(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fragmentName")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = fragmentName_0(builder, level + 1);
    result = result && identifier(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // !'on'
  private static boolean fragmentName_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fragmentName_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !consumeToken(builder, ON_KEYWORD);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // '...' (fragmentSpread | inlineFragment)
  public static boolean fragmentSelection(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fragmentSelection")) return false;
    if (!nextTokenIs(builder, SPREAD)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, FRAGMENT_SELECTION, null);
    result = consumeToken(builder, SPREAD);
    pinned = result; // pin = 1
    result = result && fragmentSelection_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // fragmentSpread | inlineFragment
  private static boolean fragmentSelection_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fragmentSelection_1")) return false;
    boolean result;
    result = fragmentSpread(builder, level + 1);
    if (!result) result = inlineFragment(builder, level + 1);
    return result;
  }

  /* ********************************************************** */
  // fragmentName directives?
  public static boolean fragmentSpread(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fragmentSpread")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, FRAGMENT_SPREAD, "<fragment spread>");
    result = fragmentName(builder, level + 1);
    pinned = result; // pin = 1
    result = result && fragmentSpread_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // directives?
  private static boolean fragmentSpread_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fragmentSpread_1")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // NAME | 'fragment' | 'query' | 'mutation' | 'subscription' | 'schema' | 'scalar' | 'type' |
  //   'interface' | 'implements' | 'enum' | 'union' | 'input' | 'extend' | 'directive' | 'on' | 'repeatable'
  public static boolean identifier(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "identifier")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, IDENTIFIER, "<identifier>");
    result = consumeToken(builder, NAME);
    if (!result) result = consumeToken(builder, FRAGMENT_KEYWORD);
    if (!result) result = consumeToken(builder, QUERY_KEYWORD);
    if (!result) result = consumeToken(builder, MUTATION_KEYWORD);
    if (!result) result = consumeToken(builder, SUBSCRIPTION_KEYWORD);
    if (!result) result = consumeToken(builder, SCHEMA_KEYWORD);
    if (!result) result = consumeToken(builder, SCALAR_KEYWORD);
    if (!result) result = consumeToken(builder, TYPE_KEYWORD);
    if (!result) result = consumeToken(builder, INTERFACE_KEYWORD);
    if (!result) result = consumeToken(builder, IMPLEMENTS_KEYWORD);
    if (!result) result = consumeToken(builder, ENUM_KEYWORD);
    if (!result) result = consumeToken(builder, UNION_KEYWORD);
    if (!result) result = consumeToken(builder, INPUT_KEYWORD);
    if (!result) result = consumeToken(builder, EXTEND_KEYWORD);
    if (!result) result = consumeToken(builder, DIRECTIVE_KEYWORD);
    if (!result) result = consumeToken(builder, ON_KEYWORD);
    if (!result) result = consumeToken(builder, REPEATABLE_KEYWORD);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // 'implements' ampTypeName ampTypeName*
  public static boolean implementsInterfaces(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "implementsInterfaces")) return false;
    if (!nextTokenIs(builder, IMPLEMENTS_KEYWORD)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, IMPLEMENTS_INTERFACES, null);
    result = consumeToken(builder, IMPLEMENTS_KEYWORD);
    pinned = result; // pin = 1
    result = result && report_error_(builder, ampTypeName(builder, level + 1));
    result = pinned && implementsInterfaces_2(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // ampTypeName*
  private static boolean implementsInterfaces_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "implementsInterfaces_2")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!ampTypeName(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "implementsInterfaces_2", pos)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // typeCondition? directives? selectionSet
  public static boolean inlineFragment(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inlineFragment")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, INLINE_FRAGMENT, "<inline fragment>");
    result = inlineFragment_0(builder, level + 1);
    pinned = result; // pin = 1
    result = result && report_error_(builder, inlineFragment_1(builder, level + 1));
    result = pinned && selectionSet(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // typeCondition?
  private static boolean inlineFragment_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inlineFragment_0")) return false;
    typeCondition(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean inlineFragment_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inlineFragment_1")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // description? 'input' typeNameDefinition directives? inputObjectValueDefinitions?
  public static boolean inputObjectTypeDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectTypeDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, INPUT_OBJECT_TYPE_DEFINITION, "<input object type definition>");
    result = inputObjectTypeDefinition_0(builder, level + 1);
    result = result && consumeToken(builder, INPUT_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeNameDefinition(builder, level + 1));
    result = pinned && report_error_(builder, inputObjectTypeDefinition_3(builder, level + 1)) && result;
    result = pinned && inputObjectTypeDefinition_4(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // description?
  private static boolean inputObjectTypeDefinition_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectTypeDefinition_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean inputObjectTypeDefinition_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectTypeDefinition_3")) return false;
    directives(builder, level + 1);
    return true;
  }

  // inputObjectValueDefinitions?
  private static boolean inputObjectTypeDefinition_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectTypeDefinition_4")) return false;
    inputObjectValueDefinitions(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // 'extend' 'input' typeName directives? inputObjectValueDefinitions?
  public static boolean inputObjectTypeExtensionDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectTypeExtensionDefinition")) return false;
    if (!nextTokenIs(builder, EXTEND_KEYWORD)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, INPUT_OBJECT_TYPE_EXTENSION_DEFINITION, null);
    result = consumeTokens(builder, 2, EXTEND_KEYWORD, INPUT_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeName(builder, level + 1));
    result = pinned && report_error_(builder, inputObjectTypeExtensionDefinition_3(builder, level + 1)) && result;
    result = pinned && inputObjectTypeExtensionDefinition_4(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // directives?
  private static boolean inputObjectTypeExtensionDefinition_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectTypeExtensionDefinition_3")) return false;
    directives(builder, level + 1);
    return true;
  }

  // inputObjectValueDefinitions?
  private static boolean inputObjectTypeExtensionDefinition_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectTypeExtensionDefinition_4")) return false;
    inputObjectValueDefinitions(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // '{' inputValueDefinition+ '}'
  public static boolean inputObjectValueDefinitions(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectValueDefinitions")) return false;
    if (!nextTokenIs(builder, BRACE_L)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, INPUT_OBJECT_VALUE_DEFINITIONS, null);
    result = consumeToken(builder, BRACE_L);
    pinned = result; // pin = 1
    result = result && report_error_(builder, inputObjectValueDefinitions_1(builder, level + 1));
    result = pinned && consumeToken(builder, BRACE_R) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // inputValueDefinition+
  private static boolean inputObjectValueDefinitions_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectValueDefinitions_1")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = inputValueDefinition(builder, level + 1);
    while (result) {
      int pos = current_position_(builder);
      if (!inputValueDefinition(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "inputObjectValueDefinitions_1", pos)) break;
    }
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // description? identifier ':' type defaultValue? directives?
  public static boolean inputValueDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputValueDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, INPUT_VALUE_DEFINITION, "<input value definition>");
    result = inputValueDefinition_0(builder, level + 1);
    result = result && identifier(builder, level + 1);
    pinned = result; // pin = 2
    result = result && report_error_(builder, consumeToken(builder, COLON));
    result = pinned && report_error_(builder, type(builder, level + 1)) && result;
    result = pinned && report_error_(builder, inputValueDefinition_4(builder, level + 1)) && result;
    result = pinned && inputValueDefinition_5(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::inputValueDefinition_recover);
    return result || pinned;
  }

  // description?
  private static boolean inputValueDefinition_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputValueDefinition_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // defaultValue?
  private static boolean inputValueDefinition_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputValueDefinition_4")) return false;
    defaultValue(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean inputValueDefinition_5(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputValueDefinition_5")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // !(')' | '}' | inputValueDefinition)
  static boolean inputValueDefinition_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputValueDefinition_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !inputValueDefinition_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // ')' | '}' | inputValueDefinition
  private static boolean inputValueDefinition_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputValueDefinition_recover_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, PAREN_R);
    if (!result) result = consumeToken(builder, BRACE_R);
    if (!result) result = inputValueDefinition(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // NUMBER
  public static boolean intValue(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "intValue")) return false;
    if (!nextTokenIs(builder, NUMBER)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, NUMBER);
    exit_section_(builder, marker, INT_VALUE, result);
    return result;
  }

  /* ********************************************************** */
  // description? 'interface' typeNameDefinition implementsInterfaces? directives? fieldsDefinition?
  public static boolean interfaceTypeDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, INTERFACE_TYPE_DEFINITION, "<interface type definition>");
    result = interfaceTypeDefinition_0(builder, level + 1);
    result = result && consumeToken(builder, INTERFACE_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeNameDefinition(builder, level + 1));
    result = pinned && report_error_(builder, interfaceTypeDefinition_3(builder, level + 1)) && result;
    result = pinned && report_error_(builder, interfaceTypeDefinition_4(builder, level + 1)) && result;
    result = pinned && interfaceTypeDefinition_5(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // description?
  private static boolean interfaceTypeDefinition_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeDefinition_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // implementsInterfaces?
  private static boolean interfaceTypeDefinition_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeDefinition_3")) return false;
    implementsInterfaces(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean interfaceTypeDefinition_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeDefinition_4")) return false;
    directives(builder, level + 1);
    return true;
  }

  // fieldsDefinition?
  private static boolean interfaceTypeDefinition_5(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeDefinition_5")) return false;
    fieldsDefinition(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // 'extend' 'interface' typeName implementsInterfaces? directives? fieldsDefinition?
  public static boolean interfaceTypeExtensionDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeExtensionDefinition")) return false;
    if (!nextTokenIs(builder, EXTEND_KEYWORD)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, INTERFACE_TYPE_EXTENSION_DEFINITION, null);
    result = consumeTokens(builder, 2, EXTEND_KEYWORD, INTERFACE_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeName(builder, level + 1));
    result = pinned && report_error_(builder, interfaceTypeExtensionDefinition_3(builder, level + 1)) && result;
    result = pinned && report_error_(builder, interfaceTypeExtensionDefinition_4(builder, level + 1)) && result;
    result = pinned && interfaceTypeExtensionDefinition_5(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // implementsInterfaces?
  private static boolean interfaceTypeExtensionDefinition_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeExtensionDefinition_3")) return false;
    implementsInterfaces(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean interfaceTypeExtensionDefinition_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeExtensionDefinition_4")) return false;
    directives(builder, level + 1);
    return true;
  }

  // fieldsDefinition?
  private static boolean interfaceTypeExtensionDefinition_5(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeExtensionDefinition_5")) return false;
    fieldsDefinition(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // '[' type ']'
  public static boolean listType(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "listType")) return false;
    if (!nextTokenIs(builder, BRACKET_L)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, BRACKET_L);
    result = result && type(builder, level + 1);
    result = result && consumeToken(builder, BRACKET_R);
    exit_section_(builder, marker, LIST_TYPE, result);
    return result;
  }

  /* ********************************************************** */
  // typeName BANG | listType BANG
  public static boolean nonNullType(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "nonNullType")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, NON_NULL_TYPE, "<non null type>");
    result = nonNullType_0(builder, level + 1);
    if (!result) result = nonNullType_1(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // typeName BANG
  private static boolean nonNullType_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "nonNullType_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = typeName(builder, level + 1);
    result = result && consumeToken(builder, BANG);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // listType BANG
  private static boolean nonNullType_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "nonNullType_1")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = listType(builder, level + 1);
    result = result && consumeToken(builder, BANG);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // 'null'
  public static boolean nullValue(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "nullValue")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, NULL_VALUE, "<null value>");
    result = consumeToken(builder, "null");
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // identifier ':' value
  public static boolean objectField(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectField")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, OBJECT_FIELD, "<object field>");
    result = identifier(builder, level + 1);
    pinned = result; // pin = 1
    result = result && report_error_(builder, consumeToken(builder, COLON));
    result = pinned && value(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::objectField_recover);
    return result || pinned;
  }

  /* ********************************************************** */
  // !('}' | value)
  static boolean objectField_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectField_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !objectField_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // '}' | value
  private static boolean objectField_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectField_recover_0")) return false;
    boolean result;
    result = consumeToken(builder, BRACE_R);
    if (!result) result = value(builder, level + 1);
    return result;
  }

  /* ********************************************************** */
  // description? 'type' typeNameDefinition implementsInterfaces? directives? fieldsDefinition?
  public static boolean objectTypeDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, OBJECT_TYPE_DEFINITION, "<object type definition>");
    result = objectTypeDefinition_0(builder, level + 1);
    result = result && consumeToken(builder, TYPE_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeNameDefinition(builder, level + 1));
    result = pinned && report_error_(builder, objectTypeDefinition_3(builder, level + 1)) && result;
    result = pinned && report_error_(builder, objectTypeDefinition_4(builder, level + 1)) && result;
    result = pinned && objectTypeDefinition_5(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // description?
  private static boolean objectTypeDefinition_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeDefinition_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // implementsInterfaces?
  private static boolean objectTypeDefinition_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeDefinition_3")) return false;
    implementsInterfaces(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean objectTypeDefinition_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeDefinition_4")) return false;
    directives(builder, level + 1);
    return true;
  }

  // fieldsDefinition?
  private static boolean objectTypeDefinition_5(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeDefinition_5")) return false;
    fieldsDefinition(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // 'extend' 'type' typeName implementsInterfaces? directives? fieldsDefinition?
  public static boolean objectTypeExtensionDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeExtensionDefinition")) return false;
    if (!nextTokenIs(builder, EXTEND_KEYWORD)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, OBJECT_TYPE_EXTENSION_DEFINITION, null);
    result = consumeTokens(builder, 2, EXTEND_KEYWORD, TYPE_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeName(builder, level + 1));
    result = pinned && report_error_(builder, objectTypeExtensionDefinition_3(builder, level + 1)) && result;
    result = pinned && report_error_(builder, objectTypeExtensionDefinition_4(builder, level + 1)) && result;
    result = pinned && objectTypeExtensionDefinition_5(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // implementsInterfaces?
  private static boolean objectTypeExtensionDefinition_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeExtensionDefinition_3")) return false;
    implementsInterfaces(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean objectTypeExtensionDefinition_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeExtensionDefinition_4")) return false;
    directives(builder, level + 1);
    return true;
  }

  // fieldsDefinition?
  private static boolean objectTypeExtensionDefinition_5(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeExtensionDefinition_5")) return false;
    fieldsDefinition(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // '{' objectField* '}'
  public static boolean objectValue(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectValue")) return false;
    if (!nextTokenIs(builder, BRACE_L)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, OBJECT_VALUE, null);
    result = consumeToken(builder, BRACE_L);
    pinned = result; // pin = 1
    result = result && report_error_(builder, objectValue_1(builder, level + 1));
    result = pinned && consumeToken(builder, BRACE_R) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // objectField*
  private static boolean objectValue_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectValue_1")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!objectField(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "objectValue_1", pos)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // selectionSetOperationDefinition | typedOperationDefinition
  public static boolean operationDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "operationDefinition")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _COLLAPSE_, OPERATION_DEFINITION, "<operation definition>");
    result = selectionSetOperationDefinition(builder, level + 1);
    if (!result) result = typedOperationDefinition(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // 'query' | 'mutation' | 'subscription'
  public static boolean operationType(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "operationType")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, OPERATION_TYPE, "<operation type>");
    result = consumeToken(builder, QUERY_KEYWORD);
    if (!result) result = consumeToken(builder, MUTATION_KEYWORD);
    if (!result) result = consumeToken(builder, SUBSCRIPTION_KEYWORD);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // (operationType | NAME) ':' typeName
  public static boolean operationTypeDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "operationTypeDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, OPERATION_TYPE_DEFINITION, "<operation type definition>");
    result = operationTypeDefinition_0(builder, level + 1);
    pinned = result; // pin = 1
    result = result && report_error_(builder, consumeToken(builder, COLON));
    result = pinned && typeName(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::operationTypeDefinition_recover);
    return result || pinned;
  }

  // operationType | NAME
  private static boolean operationTypeDefinition_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "operationTypeDefinition_0")) return false;
    boolean result;
    result = operationType(builder, level + 1);
    if (!result) result = consumeToken(builder, NAME);
    return result;
  }

  /* ********************************************************** */
  // !('}' | root_tokens | operationTypeDefinition)
  static boolean operationTypeDefinition_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "operationTypeDefinition_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !operationTypeDefinition_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // '}' | root_tokens | operationTypeDefinition
  private static boolean operationTypeDefinition_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "operationTypeDefinition_recover_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, BRACE_R);
    if (!result) result = root_tokens(builder, level + 1);
    if (!result) result = operationTypeDefinition(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // '{' operationTypeDefinition+ '}'
  public static boolean operationTypeDefinitions(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "operationTypeDefinitions")) return false;
    if (!nextTokenIs(builder, BRACE_L)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, OPERATION_TYPE_DEFINITIONS, null);
    result = consumeToken(builder, BRACE_L);
    pinned = result; // pin = 1
    result = result && report_error_(builder, operationTypeDefinitions_1(builder, level + 1));
    result = pinned && consumeToken(builder, BRACE_R) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // operationTypeDefinition+
  private static boolean operationTypeDefinitions_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "operationTypeDefinitions_1")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = operationTypeDefinition(builder, level + 1);
    while (result) {
      int pos = current_position_(builder);
      if (!operationTypeDefinition(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "operationTypeDefinitions_1", pos)) break;
    }
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // '|' directiveLocation
  static boolean pipeDirectiveLocation(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "pipeDirectiveLocation")) return false;
    if (!nextTokenIs(builder, PIPE)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, PIPE);
    result = result && directiveLocation(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // '|' typeName
  static boolean pipeUnionMember(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "pipeUnionMember")) return false;
    if (!nextTokenIs(builder, PIPE)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = consumeToken(builder, PIPE);
    pinned = result; // pin = 1
    result = result && typeName(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // OPEN_QUOTE REGULAR_STRING_PART? CLOSING_QUOTE
  public static boolean quotedString(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "quotedString")) return false;
    if (!nextTokenIs(builder, OPEN_QUOTE)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, QUOTED_STRING, null);
    result = consumeToken(builder, OPEN_QUOTE);
    pinned = result; // pin = 1
    result = result && report_error_(builder, quotedString_1(builder, level + 1));
    result = pinned && consumeToken(builder, CLOSING_QUOTE) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // REGULAR_STRING_PART?
  private static boolean quotedString_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "quotedString_1")) return false;
    consumeToken(builder, REGULAR_STRING_PART);
    return true;
  }

  /* ********************************************************** */
  // definition_keywords | '{' /* anon query */ | OPEN_QUOTE | OPEN_TRIPLE_QUOTE /* schema description */ | (DOLLAR BRACE_L)
  static boolean root_tokens(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "root_tokens")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = definition_keywords(builder, level + 1);
    if (!result) result = consumeToken(builder, BRACE_L);
    if (!result) result = consumeToken(builder, OPEN_QUOTE);
    if (!result) result = consumeToken(builder, OPEN_TRIPLE_QUOTE);
    if (!result) result = root_tokens_4(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // DOLLAR BRACE_L
  private static boolean root_tokens_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "root_tokens_4")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeTokens(builder, 0, DOLLAR, BRACE_L);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // description? 'scalar' typeNameDefinition directives?
  public static boolean scalarTypeDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "scalarTypeDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, SCALAR_TYPE_DEFINITION, "<scalar type definition>");
    result = scalarTypeDefinition_0(builder, level + 1);
    result = result && consumeToken(builder, SCALAR_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeNameDefinition(builder, level + 1));
    result = pinned && scalarTypeDefinition_3(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // description?
  private static boolean scalarTypeDefinition_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "scalarTypeDefinition_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean scalarTypeDefinition_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "scalarTypeDefinition_3")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // 'extend' 'scalar' typeName directives?
  public static boolean scalarTypeExtensionDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "scalarTypeExtensionDefinition")) return false;
    if (!nextTokenIs(builder, EXTEND_KEYWORD)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, SCALAR_TYPE_EXTENSION_DEFINITION, null);
    result = consumeTokens(builder, 2, EXTEND_KEYWORD, SCALAR_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeName(builder, level + 1));
    result = pinned && scalarTypeExtensionDefinition_3(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // directives?
  private static boolean scalarTypeExtensionDefinition_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "scalarTypeExtensionDefinition_3")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // description? 'schema' directives? operationTypeDefinitions
  public static boolean schemaDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "schemaDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, SCHEMA_DEFINITION, "<schema definition>");
    result = schemaDefinition_0(builder, level + 1);
    result = result && consumeToken(builder, SCHEMA_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, schemaDefinition_2(builder, level + 1));
    result = pinned && operationTypeDefinitions(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // description?
  private static boolean schemaDefinition_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "schemaDefinition_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean schemaDefinition_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "schemaDefinition_2")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // 'extend' 'schema' directives? operationTypeDefinitions?
  public static boolean schemaExtension(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "schemaExtension")) return false;
    if (!nextTokenIs(builder, EXTEND_KEYWORD)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, SCHEMA_EXTENSION, null);
    result = consumeTokens(builder, 2, EXTEND_KEYWORD, SCHEMA_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, schemaExtension_2(builder, level + 1));
    result = pinned && schemaExtension_3(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // directives?
  private static boolean schemaExtension_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "schemaExtension_2")) return false;
    directives(builder, level + 1);
    return true;
  }

  // operationTypeDefinitions?
  private static boolean schemaExtension_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "schemaExtension_3")) return false;
    operationTypeDefinitions(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // field |
  //     fragmentSelection |
  //     templateSelection
  public static boolean selection(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "selection")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, SELECTION, "<selection>");
    result = field(builder, level + 1);
    if (!result) result = fragmentSelection(builder, level + 1);
    if (!result) result = templateSelection(builder, level + 1);
    exit_section_(builder, level, marker, result, false, GraphQLParser::selection_recover);
    return result;
  }

  /* ********************************************************** */
  // '{' selection+ '}'
  public static boolean selectionSet(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "selectionSet")) return false;
    if (!nextTokenIs(builder, BRACE_L)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, SELECTION_SET, null);
    result = consumeToken(builder, BRACE_L);
    pinned = result; // pin = 1
    result = result && report_error_(builder, selectionSet_1(builder, level + 1));
    result = pinned && consumeToken(builder, BRACE_R) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // selection+
  private static boolean selectionSet_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "selectionSet_1")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = selection(builder, level + 1);
    while (result) {
      int pos = current_position_(builder);
      if (!selection(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "selectionSet_1", pos)) break;
    }
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // selectionSet
  public static boolean selectionSetOperationDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "selectionSetOperationDefinition")) return false;
    if (!nextTokenIs(builder, BRACE_L)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = selectionSet(builder, level + 1);
    exit_section_(builder, marker, SELECTION_SET_OPERATION_DEFINITION, result);
    return result;
  }

  /* ********************************************************** */
  // !('}' | root_tokens | selection)
  static boolean selection_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "selection_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !selection_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // '}' | root_tokens | selection
  private static boolean selection_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "selection_recover_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, BRACE_R);
    if (!result) result = root_tokens(builder, level + 1);
    if (!result) result = selection(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // quotedString | blockString
  public static boolean stringLiteral(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "stringLiteral")) return false;
    if (!nextTokenIs(builder, "<string literal>", OPEN_QUOTE, OPEN_TRIPLE_QUOTE)) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _COLLAPSE_, STRING_LITERAL, "<string literal>");
    result = quotedString(builder, level + 1);
    if (!result) result = blockString(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // stringLiteral
  public static boolean stringValue(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "stringValue")) return false;
    if (!nextTokenIs(builder, "<string value>", OPEN_QUOTE, OPEN_TRIPLE_QUOTE)) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, STRING_VALUE, "<string value>");
    result = stringLiteral(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // templatePlaceholder
  public static boolean templateDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "templateDefinition")) return false;
    if (!nextTokenIs(builder, DOLLAR)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = templatePlaceholder(builder, level + 1);
    exit_section_(builder, marker, TEMPLATE_DEFINITION, result);
    return result;
  }

  /* ********************************************************** */
  // DOLLAR BRACE_L TEMPLATE_CHAR* BRACE_R
  static boolean templatePlaceholder(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "templatePlaceholder")) return false;
    if (!nextTokenIs(builder, DOLLAR)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeTokens(builder, 0, DOLLAR, BRACE_L);
    result = result && templatePlaceholder_2(builder, level + 1);
    result = result && consumeToken(builder, BRACE_R);
    exit_section_(builder, marker, null, result);
    return result;
  }

  // TEMPLATE_CHAR*
  private static boolean templatePlaceholder_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "templatePlaceholder_2")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!consumeToken(builder, TEMPLATE_CHAR)) break;
      if (!empty_element_parsed_guard_(builder, "templatePlaceholder_2", pos)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // templatePlaceholder
  public static boolean templateSelection(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "templateSelection")) return false;
    if (!nextTokenIs(builder, DOLLAR)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = templatePlaceholder(builder, level + 1);
    exit_section_(builder, marker, TEMPLATE_SELECTION, result);
    return result;
  }

  /* ********************************************************** */
  // templatePlaceholder
  public static boolean templateVariable(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "templateVariable")) return false;
    if (!nextTokenIs(builder, DOLLAR)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = templatePlaceholder(builder, level + 1);
    exit_section_(builder, marker, TEMPLATE_VARIABLE, result);
    return result;
  }

  /* ********************************************************** */
  // nonNullType | listType | typeName
  public static boolean type(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "type")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _COLLAPSE_, TYPE, "<type>");
    result = nonNullType(builder, level + 1);
    if (!result) result = listType(builder, level + 1);
    if (!result) result = typeName(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // 'on' typeName
  public static boolean typeCondition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typeCondition")) return false;
    if (!nextTokenIs(builder, ON_KEYWORD)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, TYPE_CONDITION, null);
    result = consumeToken(builder, ON_KEYWORD);
    pinned = result; // pin = 1
    result = result && typeName(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // scalarTypeDefinition |
  //   objectTypeDefinition |
  //   interfaceTypeDefinition |
  //   unionTypeDefinition |
  //   enumTypeDefinition |
  //   inputObjectTypeDefinition
  public static boolean typeDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typeDefinition")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _COLLAPSE_, TYPE_DEFINITION, "<type definition>");
    result = scalarTypeDefinition(builder, level + 1);
    if (!result) result = objectTypeDefinition(builder, level + 1);
    if (!result) result = interfaceTypeDefinition(builder, level + 1);
    if (!result) result = unionTypeDefinition(builder, level + 1);
    if (!result) result = enumTypeDefinition(builder, level + 1);
    if (!result) result = inputObjectTypeDefinition(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // objectTypeExtensionDefinition |
  //   interfaceTypeExtensionDefinition |
  //   unionTypeExtensionDefinition |
  //   scalarTypeExtensionDefinition |
  //   enumTypeExtensionDefinition |
  //   inputObjectTypeExtensionDefinition
  public static boolean typeExtension(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typeExtension")) return false;
    if (!nextTokenIs(builder, EXTEND_KEYWORD)) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _COLLAPSE_, TYPE_EXTENSION, null);
    result = objectTypeExtensionDefinition(builder, level + 1);
    if (!result) result = interfaceTypeExtensionDefinition(builder, level + 1);
    if (!result) result = unionTypeExtensionDefinition(builder, level + 1);
    if (!result) result = scalarTypeExtensionDefinition(builder, level + 1);
    if (!result) result = enumTypeExtensionDefinition(builder, level + 1);
    if (!result) result = inputObjectTypeExtensionDefinition(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // !(definition_keywords) identifier
  public static boolean typeName(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typeName")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, TYPE_NAME, "<type name>");
    result = typeName_0(builder, level + 1);
    result = result && identifier(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // !(definition_keywords)
  private static boolean typeName_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typeName_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !typeName_0_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // (definition_keywords)
  private static boolean typeName_0_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typeName_0_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = definition_keywords(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // !(definition_keywords) identifier
  public static boolean typeNameDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typeNameDefinition")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, TYPE_NAME_DEFINITION, "<type name definition>");
    result = typeNameDefinition_0(builder, level + 1);
    result = result && identifier(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // !(definition_keywords)
  private static boolean typeNameDefinition_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typeNameDefinition_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !typeNameDefinition_0_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // (definition_keywords)
  private static boolean typeNameDefinition_0_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typeNameDefinition_0_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = definition_keywords(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // schemaDefinition |
  //     schemaExtension |
  //     typeDefinition |
  //     typeExtension |
  //     directiveDefinition
  public static boolean typeSystemDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typeSystemDefinition")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _COLLAPSE_, TYPE_SYSTEM_DEFINITION, "<type system definition>");
    result = schemaDefinition(builder, level + 1);
    if (!result) result = schemaExtension(builder, level + 1);
    if (!result) result = typeDefinition(builder, level + 1);
    if (!result) result = typeExtension(builder, level + 1);
    if (!result) result = directiveDefinition(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // operationType identifier? variableDefinitions? directives? selectionSet
  public static boolean typedOperationDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typedOperationDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, TYPED_OPERATION_DEFINITION, "<typed operation definition>");
    result = operationType(builder, level + 1);
    pinned = result; // pin = 1
    result = result && report_error_(builder, typedOperationDefinition_1(builder, level + 1));
    result = pinned && report_error_(builder, typedOperationDefinition_2(builder, level + 1)) && result;
    result = pinned && report_error_(builder, typedOperationDefinition_3(builder, level + 1)) && result;
    result = pinned && selectionSet(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // identifier?
  private static boolean typedOperationDefinition_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typedOperationDefinition_1")) return false;
    identifier(builder, level + 1);
    return true;
  }

  // variableDefinitions?
  private static boolean typedOperationDefinition_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typedOperationDefinition_2")) return false;
    variableDefinitions(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean typedOperationDefinition_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typedOperationDefinition_3")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // '|'? typeName pipeUnionMember*
  public static boolean unionMembers(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionMembers")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, UNION_MEMBERS, "<union members>");
    result = unionMembers_0(builder, level + 1);
    result = result && typeName(builder, level + 1);
    result = result && unionMembers_2(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // '|'?
  private static boolean unionMembers_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionMembers_0")) return false;
    consumeToken(builder, PIPE);
    return true;
  }

  // pipeUnionMember*
  private static boolean unionMembers_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionMembers_2")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!pipeUnionMember(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "unionMembers_2", pos)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '=' unionMembers
  public static boolean unionMembership(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionMembership")) return false;
    if (!nextTokenIs(builder, EQUALS)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, UNION_MEMBERSHIP, null);
    result = consumeToken(builder, EQUALS);
    pinned = result; // pin = 1
    result = result && unionMembers(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // description? 'union' typeNameDefinition directives? unionMembership?
  public static boolean unionTypeDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, UNION_TYPE_DEFINITION, "<union type definition>");
    result = unionTypeDefinition_0(builder, level + 1);
    result = result && consumeToken(builder, UNION_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeNameDefinition(builder, level + 1));
    result = pinned && report_error_(builder, unionTypeDefinition_3(builder, level + 1)) && result;
    result = pinned && unionTypeDefinition_4(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // description?
  private static boolean unionTypeDefinition_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeDefinition_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean unionTypeDefinition_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeDefinition_3")) return false;
    directives(builder, level + 1);
    return true;
  }

  // unionMembership?
  private static boolean unionTypeDefinition_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeDefinition_4")) return false;
    unionMembership(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // 'extend' 'union' typeName directives? unionMembership?
  public static boolean unionTypeExtensionDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeExtensionDefinition")) return false;
    if (!nextTokenIs(builder, EXTEND_KEYWORD)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, UNION_TYPE_EXTENSION_DEFINITION, null);
    result = consumeTokens(builder, 2, EXTEND_KEYWORD, UNION_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeName(builder, level + 1));
    result = pinned && report_error_(builder, unionTypeExtensionDefinition_3(builder, level + 1)) && result;
    result = pinned && unionTypeExtensionDefinition_4(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // directives?
  private static boolean unionTypeExtensionDefinition_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeExtensionDefinition_3")) return false;
    directives(builder, level + 1);
    return true;
  }

  // unionMembership?
  private static boolean unionTypeExtensionDefinition_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeExtensionDefinition_4")) return false;
    unionMembership(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // templateVariable |
  //     variable |
  //     stringValue |
  //     intValue |
  //     floatValue |
  //     booleanValue |
  //     nullValue |
  //     enumValue |
  //     arrayValue |
  //     objectValue
  public static boolean value(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "value")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _COLLAPSE_, VALUE, "<value>");
    result = templateVariable(builder, level + 1);
    if (!result) result = variable(builder, level + 1);
    if (!result) result = stringValue(builder, level + 1);
    if (!result) result = intValue(builder, level + 1);
    if (!result) result = floatValue(builder, level + 1);
    if (!result) result = booleanValue(builder, level + 1);
    if (!result) result = nullValue(builder, level + 1);
    if (!result) result = enumValue(builder, level + 1);
    if (!result) result = arrayValue(builder, level + 1);
    if (!result) result = objectValue(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  /* ********************************************************** */
  // VARIABLE_NAME
  public static boolean variable(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "variable")) return false;
    if (!nextTokenIs(builder, VARIABLE_NAME)) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, VARIABLE_NAME);
    exit_section_(builder, marker, VARIABLE, result);
    return result;
  }

  /* ********************************************************** */
  // variable ':' type defaultValue? directives?
  public static boolean variableDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "variableDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, VARIABLE_DEFINITION, "<variable definition>");
    result = variable(builder, level + 1);
    pinned = result; // pin = 1
    result = result && report_error_(builder, consumeToken(builder, COLON));
    result = pinned && report_error_(builder, type(builder, level + 1)) && result;
    result = pinned && report_error_(builder, variableDefinition_3(builder, level + 1)) && result;
    result = pinned && variableDefinition_4(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::variableDefinition_recover);
    return result || pinned;
  }

  // defaultValue?
  private static boolean variableDefinition_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "variableDefinition_3")) return false;
    defaultValue(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean variableDefinition_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "variableDefinition_4")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // !(')' | root_tokens | variableDefinition)
  static boolean variableDefinition_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "variableDefinition_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !variableDefinition_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // ')' | root_tokens | variableDefinition
  private static boolean variableDefinition_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "variableDefinition_recover_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, PAREN_R);
    if (!result) result = root_tokens(builder, level + 1);
    if (!result) result = variableDefinition(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // '(' variableDefinition+ ')'
  public static boolean variableDefinitions(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "variableDefinitions")) return false;
    if (!nextTokenIs(builder, PAREN_L)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, VARIABLE_DEFINITIONS, null);
    result = consumeToken(builder, PAREN_L);
    pinned = result; // pin = 1
    result = result && report_error_(builder, variableDefinitions_1(builder, level + 1));
    result = pinned && consumeToken(builder, PAREN_R) && result;
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // variableDefinition+
  private static boolean variableDefinitions_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "variableDefinitions_1")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = variableDefinition(builder, level + 1);
    while (result) {
      int pos = current_position_(builder);
      if (!variableDefinition(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "variableDefinitions_1", pos)) break;
    }
    exit_section_(builder, marker, null, result);
    return result;
  }

}
