// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.*;
import static com.intellij.lang.jsgraphql.psi.parser.GraphQLParserUtil.*;
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
      VALUE, VARIABLE),
    create_token_set_(DEFINITION, DIRECTIVE_DEFINITION, ENUM_TYPE_DEFINITION, ENUM_TYPE_EXTENSION_DEFINITION,
      FRAGMENT_DEFINITION, INPUT_OBJECT_TYPE_DEFINITION, INPUT_OBJECT_TYPE_EXTENSION_DEFINITION, INTERFACE_TYPE_DEFINITION,
      INTERFACE_TYPE_EXTENSION_DEFINITION, OBJECT_TYPE_DEFINITION, OBJECT_TYPE_EXTENSION_DEFINITION, OPERATION_DEFINITION,
      SCALAR_TYPE_DEFINITION, SCALAR_TYPE_EXTENSION_DEFINITION, SCHEMA_DEFINITION, SCHEMA_EXTENSION,
      SELECTION_SET_OPERATION_DEFINITION, TYPED_OPERATION_DEFINITION, TYPE_DEFINITION, TYPE_EXTENSION,
      TYPE_SYSTEM_DEFINITION, UNION_TYPE_DEFINITION, UNION_TYPE_EXTENSION_DEFINITION),
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
  // ':' <<param>>
  static boolean colon(PsiBuilder builder, int level, Parser aParam) {
    if (!recursion_guard_(builder, level, "colon")) return false;
    if (!nextTokenIs(builder, COLON)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = consumeToken(builder, COLON);
    pinned = result; // pin = 1
    result = result && aParam.parse(builder, level);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
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
  //   typeSystemDefinition
  public static boolean definition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "definition")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _COLLAPSE_, DEFINITION, "<definition>");
    result = operationDefinition(builder, level + 1);
    if (!result) result = fragmentDefinition(builder, level + 1);
    if (!result) result = typeSystemDefinition(builder, level + 1);
    exit_section_(builder, level, marker, result, false, GraphQLParser::definition_recover);
    return result;
  }

  /* ********************************************************** */
  // operationType | 'fragment' | 'schema' | 'type' | 'interface' | 'input' | 'enum' | 'union' | 'scalar' | 'directive' | 'extend'
  static boolean definitionKeywords(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "definitionKeywords")) return false;
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
  // !(rootTokens | NAME)
  static boolean definition_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "definition_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !definition_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // rootTokens | NAME
  private static boolean definition_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "definition_recover_0")) return false;
    boolean result;
    result = rootTokens(builder, level + 1);
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
  // (directive | placeholder)+
  static boolean directives(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "directives")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = directives_0(builder, level + 1);
    while (result) {
      int pos = current_position_(builder);
      if (!directives_0(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "directives", pos)) break;
    }
    exit_section_(builder, marker, null, result);
    return result;
  }

  // directive | placeholder
  private static boolean directives_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "directives_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = directive(builder, level + 1);
    if (!result) result = consumePlaceholderWithError(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // (definition | placeholder)*
  static boolean document(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "document")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!document_0(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "document", pos)) break;
    }
    return true;
  }

  // definition | placeholder
  private static boolean document_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "document_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = definition(builder, level + 1);
    if (!result) result = consumePlaceholderWithError(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // enumTypeDefinitionHeader enumValueDefinitions?
  public static boolean enumTypeDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumTypeDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ENUM_TYPE_DEFINITION, "<enum type definition>");
    result = enumTypeDefinitionHeader(builder, level + 1);
    pinned = result; // pin = 1
    result = result && enumTypeDefinition_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // enumValueDefinitions?
  private static boolean enumTypeDefinition_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumTypeDefinition_1")) return false;
    enumValueDefinitions(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // description? 'enum' typeNameDefinition directives?
  static boolean enumTypeDefinitionHeader(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumTypeDefinitionHeader")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = enumTypeDefinitionHeader_0(builder, level + 1);
    result = result && consumeToken(builder, ENUM_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeNameDefinition(builder, level + 1));
    result = pinned && enumTypeDefinitionHeader_3(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::rootTokens_recover);
    return result || pinned;
  }

  // description?
  private static boolean enumTypeDefinitionHeader_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumTypeDefinitionHeader_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean enumTypeDefinitionHeader_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumTypeDefinitionHeader_3")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // enumTypeExtensionDefinitionHeader enumValueDefinitions?
  public static boolean enumTypeExtensionDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumTypeExtensionDefinition")) return false;
    if (!nextTokenIs(builder, EXTEND_KEYWORD)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, ENUM_TYPE_EXTENSION_DEFINITION, null);
    result = enumTypeExtensionDefinitionHeader(builder, level + 1);
    pinned = result; // pin = 1
    result = result && enumTypeExtensionDefinition_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // enumValueDefinitions?
  private static boolean enumTypeExtensionDefinition_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumTypeExtensionDefinition_1")) return false;
    enumValueDefinitions(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // 'extend' 'enum' typeName directives?
  static boolean enumTypeExtensionDefinitionHeader(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumTypeExtensionDefinitionHeader")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = consumeTokens(builder, 2, EXTEND_KEYWORD, ENUM_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeName(builder, level + 1));
    result = pinned && enumTypeExtensionDefinitionHeader_3(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::rootTokens_recover);
    return result || pinned;
  }

  // directives?
  private static boolean enumTypeExtensionDefinitionHeader_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumTypeExtensionDefinitionHeader_3")) return false;
    directives(builder, level + 1);
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
  // !('}' | enumValueDefinition | rootTokens)
  static boolean enumValueDefinition_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumValueDefinition_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !enumValueDefinition_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // '}' | enumValueDefinition | rootTokens
  private static boolean enumValueDefinition_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "enumValueDefinition_recover_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, BRACE_R);
    if (!result) result = enumValueDefinition(builder, level + 1);
    if (!result) result = rootTokens(builder, level + 1);
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
  // description? identifier argumentsDefinition? <<colon type>> directives?
  public static boolean fieldDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fieldDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, FIELD_DEFINITION, "<field definition>");
    result = fieldDefinition_0(builder, level + 1);
    result = result && identifier(builder, level + 1);
    pinned = result; // pin = 2
    result = result && report_error_(builder, fieldDefinition_2(builder, level + 1));
    result = pinned && report_error_(builder, colon(builder, level + 1, GraphQLParser::type)) && result;
    result = pinned && fieldDefinition_4(builder, level + 1) && result;
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
  private static boolean fieldDefinition_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fieldDefinition_4")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // !('}' | rootTokens | identifier)
  static boolean fieldDefinition_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fieldDefinition_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !fieldDefinition_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // '}' | rootTokens | identifier
  private static boolean fieldDefinition_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fieldDefinition_recover_0")) return false;
    boolean result;
    result = consumeToken(builder, BRACE_R);
    if (!result) result = rootTokens(builder, level + 1);
    if (!result) result = identifier(builder, level + 1);
    return result;
  }

  /* ********************************************************** */
  // '{' (fieldDefinition)* '}'
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

  // (fieldDefinition)*
  private static boolean fieldsDefinition_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fieldsDefinition_1")) return false;
    while (true) {
      int pos = current_position_(builder);
      if (!fieldsDefinition_1_0(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "fieldsDefinition_1", pos)) break;
    }
    return true;
  }

  // (fieldDefinition)
  private static boolean fieldsDefinition_1_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fieldsDefinition_1_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = fieldDefinition(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
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
  // fragmentDefinitionHeader selectionSet
  public static boolean fragmentDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fragmentDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, FRAGMENT_DEFINITION, "<fragment definition>");
    result = fragmentDefinitionHeader(builder, level + 1);
    pinned = result; // pin = 1
    result = result && selectionSet(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // description? 'fragment' fragmentName typeCondition directives?
  static boolean fragmentDefinitionHeader(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fragmentDefinitionHeader")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = fragmentDefinitionHeader_0(builder, level + 1);
    result = result && consumeToken(builder, FRAGMENT_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, fragmentName(builder, level + 1));
    result = pinned && report_error_(builder, typeCondition(builder, level + 1)) && result;
    result = pinned && fragmentDefinitionHeader_4(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::rootTokens_recover);
    return result || pinned;
  }

  // description?
  private static boolean fragmentDefinitionHeader_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fragmentDefinitionHeader_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean fragmentDefinitionHeader_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "fragmentDefinitionHeader_4")) return false;
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
  // inputObjectTypeDefinitionHeader inputObjectValueDefinitions?
  public static boolean inputObjectTypeDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectTypeDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, INPUT_OBJECT_TYPE_DEFINITION, "<input object type definition>");
    result = inputObjectTypeDefinitionHeader(builder, level + 1);
    pinned = result; // pin = 1
    result = result && inputObjectTypeDefinition_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // inputObjectValueDefinitions?
  private static boolean inputObjectTypeDefinition_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectTypeDefinition_1")) return false;
    inputObjectValueDefinitions(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // description? 'input' typeNameDefinition directives?
  static boolean inputObjectTypeDefinitionHeader(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectTypeDefinitionHeader")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = inputObjectTypeDefinitionHeader_0(builder, level + 1);
    result = result && consumeToken(builder, INPUT_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeNameDefinition(builder, level + 1));
    result = pinned && inputObjectTypeDefinitionHeader_3(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::rootTokens_recover);
    return result || pinned;
  }

  // description?
  private static boolean inputObjectTypeDefinitionHeader_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectTypeDefinitionHeader_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean inputObjectTypeDefinitionHeader_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectTypeDefinitionHeader_3")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // inputObjectTypeExtensionDefinitionHeader inputObjectValueDefinitions?
  public static boolean inputObjectTypeExtensionDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectTypeExtensionDefinition")) return false;
    if (!nextTokenIs(builder, EXTEND_KEYWORD)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, INPUT_OBJECT_TYPE_EXTENSION_DEFINITION, null);
    result = inputObjectTypeExtensionDefinitionHeader(builder, level + 1);
    pinned = result; // pin = 1
    result = result && inputObjectTypeExtensionDefinition_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // inputObjectValueDefinitions?
  private static boolean inputObjectTypeExtensionDefinition_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectTypeExtensionDefinition_1")) return false;
    inputObjectValueDefinitions(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // 'extend' 'input' typeName directives?
  static boolean inputObjectTypeExtensionDefinitionHeader(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectTypeExtensionDefinitionHeader")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = consumeTokens(builder, 2, EXTEND_KEYWORD, INPUT_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeName(builder, level + 1));
    result = pinned && inputObjectTypeExtensionDefinitionHeader_3(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::rootTokens_recover);
    return result || pinned;
  }

  // directives?
  private static boolean inputObjectTypeExtensionDefinitionHeader_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputObjectTypeExtensionDefinitionHeader_3")) return false;
    directives(builder, level + 1);
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
  // description? identifier <<colon type>> defaultValue? directives?
  public static boolean inputValueDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputValueDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, INPUT_VALUE_DEFINITION, "<input value definition>");
    result = inputValueDefinition_0(builder, level + 1);
    result = result && identifier(builder, level + 1);
    pinned = result; // pin = 2
    result = result && report_error_(builder, colon(builder, level + 1, GraphQLParser::type));
    result = pinned && report_error_(builder, inputValueDefinition_3(builder, level + 1)) && result;
    result = pinned && inputValueDefinition_4(builder, level + 1) && result;
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
  private static boolean inputValueDefinition_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputValueDefinition_3")) return false;
    defaultValue(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean inputValueDefinition_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "inputValueDefinition_4")) return false;
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
  // interfaceTypeDefinitionHeader fieldsDefinition?
  public static boolean interfaceTypeDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, INTERFACE_TYPE_DEFINITION, "<interface type definition>");
    result = interfaceTypeDefinitionHeader(builder, level + 1);
    pinned = result; // pin = 1
    result = result && interfaceTypeDefinition_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // fieldsDefinition?
  private static boolean interfaceTypeDefinition_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeDefinition_1")) return false;
    fieldsDefinition(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // description? 'interface' typeNameDefinition implementsInterfaces? directives?
  static boolean interfaceTypeDefinitionHeader(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeDefinitionHeader")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = interfaceTypeDefinitionHeader_0(builder, level + 1);
    result = result && consumeToken(builder, INTERFACE_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeNameDefinition(builder, level + 1));
    result = pinned && report_error_(builder, interfaceTypeDefinitionHeader_3(builder, level + 1)) && result;
    result = pinned && interfaceTypeDefinitionHeader_4(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::rootTokens_recover);
    return result || pinned;
  }

  // description?
  private static boolean interfaceTypeDefinitionHeader_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeDefinitionHeader_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // implementsInterfaces?
  private static boolean interfaceTypeDefinitionHeader_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeDefinitionHeader_3")) return false;
    implementsInterfaces(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean interfaceTypeDefinitionHeader_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeDefinitionHeader_4")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // interfaceTypeExtensionDefinitionHeader fieldsDefinition?
  public static boolean interfaceTypeExtensionDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeExtensionDefinition")) return false;
    if (!nextTokenIs(builder, EXTEND_KEYWORD)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, INTERFACE_TYPE_EXTENSION_DEFINITION, null);
    result = interfaceTypeExtensionDefinitionHeader(builder, level + 1);
    pinned = result; // pin = 1
    result = result && interfaceTypeExtensionDefinition_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // fieldsDefinition?
  private static boolean interfaceTypeExtensionDefinition_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeExtensionDefinition_1")) return false;
    fieldsDefinition(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // 'extend' 'interface' typeName implementsInterfaces? directives?
  static boolean interfaceTypeExtensionDefinitionHeader(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeExtensionDefinitionHeader")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = consumeTokens(builder, 2, EXTEND_KEYWORD, INTERFACE_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeName(builder, level + 1));
    result = pinned && report_error_(builder, interfaceTypeExtensionDefinitionHeader_3(builder, level + 1)) && result;
    result = pinned && interfaceTypeExtensionDefinitionHeader_4(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::rootTokens_recover);
    return result || pinned;
  }

  // implementsInterfaces?
  private static boolean interfaceTypeExtensionDefinitionHeader_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeExtensionDefinitionHeader_3")) return false;
    implementsInterfaces(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean interfaceTypeExtensionDefinitionHeader_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "interfaceTypeExtensionDefinitionHeader_4")) return false;
    directives(builder, level + 1);
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
  // objectTypeDefinitionHeader fieldsDefinition?
  public static boolean objectTypeDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, OBJECT_TYPE_DEFINITION, "<object type definition>");
    result = objectTypeDefinitionHeader(builder, level + 1);
    pinned = result; // pin = 1
    result = result && objectTypeDefinition_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // fieldsDefinition?
  private static boolean objectTypeDefinition_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeDefinition_1")) return false;
    fieldsDefinition(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // description? 'type' typeNameDefinition implementsInterfaces? directives?
  static boolean objectTypeDefinitionHeader(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeDefinitionHeader")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = objectTypeDefinitionHeader_0(builder, level + 1);
    result = result && consumeToken(builder, TYPE_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeNameDefinition(builder, level + 1));
    result = pinned && report_error_(builder, objectTypeDefinitionHeader_3(builder, level + 1)) && result;
    result = pinned && objectTypeDefinitionHeader_4(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::rootTokens_recover);
    return result || pinned;
  }

  // description?
  private static boolean objectTypeDefinitionHeader_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeDefinitionHeader_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // implementsInterfaces?
  private static boolean objectTypeDefinitionHeader_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeDefinitionHeader_3")) return false;
    implementsInterfaces(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean objectTypeDefinitionHeader_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeDefinitionHeader_4")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // objectTypeExtensionDefinitionHeader fieldsDefinition?
  public static boolean objectTypeExtensionDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeExtensionDefinition")) return false;
    if (!nextTokenIs(builder, EXTEND_KEYWORD)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, OBJECT_TYPE_EXTENSION_DEFINITION, null);
    result = objectTypeExtensionDefinitionHeader(builder, level + 1);
    pinned = result; // pin = 1
    result = result && objectTypeExtensionDefinition_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // fieldsDefinition?
  private static boolean objectTypeExtensionDefinition_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeExtensionDefinition_1")) return false;
    fieldsDefinition(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // 'extend' 'type' typeName implementsInterfaces? directives?
  static boolean objectTypeExtensionDefinitionHeader(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeExtensionDefinitionHeader")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = consumeTokens(builder, 2, EXTEND_KEYWORD, TYPE_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeName(builder, level + 1));
    result = pinned && report_error_(builder, objectTypeExtensionDefinitionHeader_3(builder, level + 1)) && result;
    result = pinned && objectTypeExtensionDefinitionHeader_4(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::rootTokens_recover);
    return result || pinned;
  }

  // implementsInterfaces?
  private static boolean objectTypeExtensionDefinitionHeader_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeExtensionDefinitionHeader_3")) return false;
    implementsInterfaces(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean objectTypeExtensionDefinitionHeader_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "objectTypeExtensionDefinitionHeader_4")) return false;
    directives(builder, level + 1);
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
  // !('}' | identifier | rootTokens)
  static boolean operationTypeDefinition_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "operationTypeDefinition_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !operationTypeDefinition_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // '}' | identifier | rootTokens
  private static boolean operationTypeDefinition_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "operationTypeDefinition_recover_0")) return false;
    boolean result;
    result = consumeToken(builder, BRACE_R);
    if (!result) result = identifier(builder, level + 1);
    if (!result) result = rootTokens(builder, level + 1);
    return result;
  }

  /* ********************************************************** */
  // '{' (operationTypeDefinition | placeholder)+ '}'
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

  // (operationTypeDefinition | placeholder)+
  private static boolean operationTypeDefinitions_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "operationTypeDefinitions_1")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = operationTypeDefinitions_1_0(builder, level + 1);
    while (result) {
      int pos = current_position_(builder);
      if (!operationTypeDefinitions_1_0(builder, level + 1)) break;
      if (!empty_element_parsed_guard_(builder, "operationTypeDefinitions_1", pos)) break;
    }
    exit_section_(builder, marker, null, result);
    return result;
  }

  // operationTypeDefinition | placeholder
  private static boolean operationTypeDefinitions_1_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "operationTypeDefinitions_1_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = operationTypeDefinition(builder, level + 1);
    if (!result) result = consumePlaceholderWithError(builder, level + 1);
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
  // definitionKeywords | '{' /* anon query */ | OPEN_QUOTE | OPEN_TRIPLE_QUOTE
  static boolean rootTokens(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "rootTokens")) return false;
    boolean result;
    result = definitionKeywords(builder, level + 1);
    if (!result) result = consumeToken(builder, BRACE_L);
    if (!result) result = consumeToken(builder, OPEN_QUOTE);
    if (!result) result = consumeToken(builder, OPEN_TRIPLE_QUOTE);
    return result;
  }

  /* ********************************************************** */
  // !(rootTokens)
  static boolean rootTokens_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "rootTokens_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !rootTokens_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // (rootTokens)
  private static boolean rootTokens_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "rootTokens_recover_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = rootTokens(builder, level + 1);
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
  // schemaDefinitionHeader operationTypeDefinitions
  public static boolean schemaDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "schemaDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, SCHEMA_DEFINITION, "<schema definition>");
    result = schemaDefinitionHeader(builder, level + 1);
    pinned = result; // pin = 1
    result = result && operationTypeDefinitions(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // description? 'schema' directives?
  static boolean schemaDefinitionHeader(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "schemaDefinitionHeader")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = schemaDefinitionHeader_0(builder, level + 1);
    result = result && consumeToken(builder, SCHEMA_KEYWORD);
    pinned = result; // pin = 2
    result = result && schemaDefinitionHeader_2(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::rootTokens_recover);
    return result || pinned;
  }

  // description?
  private static boolean schemaDefinitionHeader_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "schemaDefinitionHeader_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean schemaDefinitionHeader_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "schemaDefinitionHeader_2")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // schemaExtensionHeader operationTypeDefinitions?
  public static boolean schemaExtension(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "schemaExtension")) return false;
    if (!nextTokenIs(builder, EXTEND_KEYWORD)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, SCHEMA_EXTENSION, null);
    result = schemaExtensionHeader(builder, level + 1);
    pinned = result; // pin = 1
    result = result && schemaExtension_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // operationTypeDefinitions?
  private static boolean schemaExtension_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "schemaExtension_1")) return false;
    operationTypeDefinitions(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // 'extend' 'schema' directives?
  static boolean schemaExtensionHeader(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "schemaExtensionHeader")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = consumeTokens(builder, 2, EXTEND_KEYWORD, SCHEMA_KEYWORD);
    pinned = result; // pin = 2
    result = result && schemaExtensionHeader_2(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::rootTokens_recover);
    return result || pinned;
  }

  // directives?
  private static boolean schemaExtensionHeader_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "schemaExtensionHeader_2")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // field |
  //     fragmentSelection
  public static boolean selection(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "selection")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, SELECTION, "<selection>");
    result = field(builder, level + 1);
    if (!result) result = fragmentSelection(builder, level + 1);
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
  // !('}' | rootTokens | selection)
  static boolean selection_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "selection_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !selection_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // '}' | rootTokens | selection
  private static boolean selection_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "selection_recover_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = consumeToken(builder, BRACE_R);
    if (!result) result = rootTokens(builder, level + 1);
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
  // !(definitionKeywords) identifier
  public static boolean typeName(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typeName")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, TYPE_NAME, "<type name>");
    result = typeName_0(builder, level + 1);
    result = result && identifier(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // !(definitionKeywords)
  private static boolean typeName_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typeName_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !typeName_0_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // (definitionKeywords)
  private static boolean typeName_0_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typeName_0_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = definitionKeywords(builder, level + 1);
    exit_section_(builder, marker, null, result);
    return result;
  }

  /* ********************************************************** */
  // !(definitionKeywords) identifier
  public static boolean typeNameDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typeNameDefinition")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NONE_, TYPE_NAME_DEFINITION, "<type name definition>");
    result = typeNameDefinition_0(builder, level + 1);
    result = result && identifier(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // !(definitionKeywords)
  private static boolean typeNameDefinition_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typeNameDefinition_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !typeNameDefinition_0_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // (definitionKeywords)
  private static boolean typeNameDefinition_0_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typeNameDefinition_0_0")) return false;
    boolean result;
    Marker marker = enter_section_(builder);
    result = definitionKeywords(builder, level + 1);
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
  // typedOperationDefinitionHeader selectionSet
  public static boolean typedOperationDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typedOperationDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, TYPED_OPERATION_DEFINITION, "<typed operation definition>");
    result = typedOperationDefinitionHeader(builder, level + 1);
    pinned = result; // pin = 1
    result = result && selectionSet(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  /* ********************************************************** */
  // description? operationType identifier? variableDefinitions? directives?
  static boolean typedOperationDefinitionHeader(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typedOperationDefinitionHeader")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = typedOperationDefinitionHeader_0(builder, level + 1);
    result = result && operationType(builder, level + 1);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typedOperationDefinitionHeader_2(builder, level + 1));
    result = pinned && report_error_(builder, typedOperationDefinitionHeader_3(builder, level + 1)) && result;
    result = pinned && typedOperationDefinitionHeader_4(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::rootTokens_recover);
    return result || pinned;
  }

  // description?
  private static boolean typedOperationDefinitionHeader_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typedOperationDefinitionHeader_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // identifier?
  private static boolean typedOperationDefinitionHeader_2(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typedOperationDefinitionHeader_2")) return false;
    identifier(builder, level + 1);
    return true;
  }

  // variableDefinitions?
  private static boolean typedOperationDefinitionHeader_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typedOperationDefinitionHeader_3")) return false;
    variableDefinitions(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean typedOperationDefinitionHeader_4(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "typedOperationDefinitionHeader_4")) return false;
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
  // unionTypeDefinitionHeader unionMembership?
  public static boolean unionTypeDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, UNION_TYPE_DEFINITION, "<union type definition>");
    result = unionTypeDefinitionHeader(builder, level + 1);
    pinned = result; // pin = 1
    result = result && unionTypeDefinition_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // unionMembership?
  private static boolean unionTypeDefinition_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeDefinition_1")) return false;
    unionMembership(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // description? 'union' typeNameDefinition directives?
  static boolean unionTypeDefinitionHeader(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeDefinitionHeader")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = unionTypeDefinitionHeader_0(builder, level + 1);
    result = result && consumeToken(builder, UNION_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeNameDefinition(builder, level + 1));
    result = pinned && unionTypeDefinitionHeader_3(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::unionTypeDefinitionHeader_recover);
    return result || pinned;
  }

  // description?
  private static boolean unionTypeDefinitionHeader_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeDefinitionHeader_0")) return false;
    description(builder, level + 1);
    return true;
  }

  // directives?
  private static boolean unionTypeDefinitionHeader_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeDefinitionHeader_3")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // !(rootTokens | '=')
  static boolean unionTypeDefinitionHeader_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeDefinitionHeader_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !unionTypeDefinitionHeader_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // rootTokens | '='
  private static boolean unionTypeDefinitionHeader_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeDefinitionHeader_recover_0")) return false;
    boolean result;
    result = rootTokens(builder, level + 1);
    if (!result) result = consumeToken(builder, EQUALS);
    return result;
  }

  /* ********************************************************** */
  // unionTypeExtensionDefinitionHeader unionMembership?
  public static boolean unionTypeExtensionDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeExtensionDefinition")) return false;
    if (!nextTokenIs(builder, EXTEND_KEYWORD)) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, UNION_TYPE_EXTENSION_DEFINITION, null);
    result = unionTypeExtensionDefinitionHeader(builder, level + 1);
    pinned = result; // pin = 1
    result = result && unionTypeExtensionDefinition_1(builder, level + 1);
    exit_section_(builder, level, marker, result, pinned, null);
    return result || pinned;
  }

  // unionMembership?
  private static boolean unionTypeExtensionDefinition_1(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeExtensionDefinition_1")) return false;
    unionMembership(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // 'extend' 'union' typeName directives?
  static boolean unionTypeExtensionDefinitionHeader(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeExtensionDefinitionHeader")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_);
    result = consumeTokens(builder, 2, EXTEND_KEYWORD, UNION_KEYWORD);
    pinned = result; // pin = 2
    result = result && report_error_(builder, typeName(builder, level + 1));
    result = pinned && unionTypeExtensionDefinitionHeader_3(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::unionTypeDefinitionHeader_recover);
    return result || pinned;
  }

  // directives?
  private static boolean unionTypeExtensionDefinitionHeader_3(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "unionTypeExtensionDefinitionHeader_3")) return false;
    directives(builder, level + 1);
    return true;
  }

  /* ********************************************************** */
  // variable |
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
    result = variable(builder, level + 1);
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
  // description? variable <<colon type>> defaultValue? directives?
  public static boolean variableDefinition(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "variableDefinition")) return false;
    boolean result, pinned;
    Marker marker = enter_section_(builder, level, _NONE_, VARIABLE_DEFINITION, "<variable definition>");
    result = variableDefinition_0(builder, level + 1);
    result = result && variable(builder, level + 1);
    pinned = result; // pin = 2
    result = result && report_error_(builder, colon(builder, level + 1, GraphQLParser::type));
    result = pinned && report_error_(builder, variableDefinition_3(builder, level + 1)) && result;
    result = pinned && variableDefinition_4(builder, level + 1) && result;
    exit_section_(builder, level, marker, result, pinned, GraphQLParser::variableDefinition_recover);
    return result || pinned;
  }

  // description?
  private static boolean variableDefinition_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "variableDefinition_0")) return false;
    description(builder, level + 1);
    return true;
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
  // !(')' | VARIABLE_NAME | rootTokens)
  static boolean variableDefinition_recover(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "variableDefinition_recover")) return false;
    boolean result;
    Marker marker = enter_section_(builder, level, _NOT_);
    result = !variableDefinition_recover_0(builder, level + 1);
    exit_section_(builder, level, marker, result, false, null);
    return result;
  }

  // ')' | VARIABLE_NAME | rootTokens
  private static boolean variableDefinition_recover_0(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "variableDefinition_recover_0")) return false;
    boolean result;
    result = consumeToken(builder, PAREN_R);
    if (!result) result = consumeToken(builder, VARIABLE_NAME);
    if (!result) result = rootTokens(builder, level + 1);
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
