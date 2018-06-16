// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.endpoint.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class JSGraphQLEndpointParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType root_, PsiBuilder builder_) {
    parseLight(root_, builder_);
    return builder_.getTreeBuilt();
  }

  public void parseLight(IElementType root_, PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, EXTENDS_SETS_);
    Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    if (root_ == ANNOTATION) {
      result_ = Annotation(builder_, 0);
    }
    else if (root_ == ANNOTATION_ARGUMENT_LIST_VALUE) {
      result_ = AnnotationArgumentListValue(builder_, 0);
    }
    else if (root_ == ANNOTATION_ARGUMENT_OBJECT_FIELD) {
      result_ = AnnotationArgumentObjectField(builder_, 0);
    }
    else if (root_ == ANNOTATION_ARGUMENT_OBJECT_VALUE) {
      result_ = AnnotationArgumentObjectValue(builder_, 0);
    }
    else if (root_ == ANNOTATION_ARGUMENT_VALUE) {
      result_ = AnnotationArgumentValue(builder_, 0);
    }
    else if (root_ == ANNOTATION_ARGUMENTS) {
      result_ = AnnotationArguments(builder_, 0);
    }
    else if (root_ == ANNOTATION_DEFINITION) {
      result_ = AnnotationDefinition(builder_, 0);
    }
    else if (root_ == ARGUMENTS_DEFINITION) {
      result_ = ArgumentsDefinition(builder_, 0);
    }
    else if (root_ == COMPOSITE_TYPE) {
      result_ = CompositeType(builder_, 0);
    }
    else if (root_ == ENUM_TYPE_DEFINITION) {
      result_ = EnumTypeDefinition(builder_, 0);
    }
    else if (root_ == ENUM_VALUE_DEFINITION) {
      result_ = EnumValueDefinition(builder_, 0);
    }
    else if (root_ == ENUM_VALUE_DEFINITION_SET) {
      result_ = EnumValueDefinitionSet(builder_, 0);
    }
    else if (root_ == FIELD_DEFINITION) {
      result_ = FieldDefinition(builder_, 0);
    }
    else if (root_ == FIELD_DEFINITION_SET) {
      result_ = FieldDefinitionSet(builder_, 0);
    }
    else if (root_ == IMPLEMENTS_INTERFACES) {
      result_ = ImplementsInterfaces(builder_, 0);
    }
    else if (root_ == IMPORT_DECLARATION) {
      result_ = ImportDeclaration(builder_, 0);
    }
    else if (root_ == IMPORT_FILE_REFERENCE) {
      result_ = ImportFileReference(builder_, 0);
    }
    else if (root_ == INPUT_OBJECT_TYPE_DEFINITION) {
      result_ = InputObjectTypeDefinition(builder_, 0);
    }
    else if (root_ == INPUT_VALUE_DEFINITION) {
      result_ = InputValueDefinition(builder_, 0);
    }
    else if (root_ == INPUT_VALUE_DEFINITION_IDENTIFIER) {
      result_ = InputValueDefinitionIdentifier(builder_, 0);
    }
    else if (root_ == INPUT_VALUE_DEFINITIONS) {
      result_ = InputValueDefinitions(builder_, 0);
    }
    else if (root_ == INTERFACE_TYPE_DEFINITION) {
      result_ = InterfaceTypeDefinition(builder_, 0);
    }
    else if (root_ == LIST_TYPE) {
      result_ = ListType(builder_, 0);
    }
    else if (root_ == NAMED_ANNOTATION_ARGUMENT) {
      result_ = NamedAnnotationArgument(builder_, 0);
    }
    else if (root_ == NAMED_ANNOTATION_ARGUMENTS) {
      result_ = NamedAnnotationArguments(builder_, 0);
    }
    else if (root_ == NAMED_TYPE) {
      result_ = NamedType(builder_, 0);
    }
    else if (root_ == NAMED_TYPE_DEF) {
      result_ = NamedTypeDef(builder_, 0);
    }
    else if (root_ == OBJECT_TYPE_DEFINITION) {
      result_ = ObjectTypeDefinition(builder_, 0);
    }
    else if (root_ == OPERATION_TYPE_DEFINITION) {
      result_ = OperationTypeDefinition(builder_, 0);
    }
    else if (root_ == OPERATION_TYPE_DEFINITION_SET) {
      result_ = OperationTypeDefinitionSet(builder_, 0);
    }
    else if (root_ == PROPERTY) {
      result_ = Property(builder_, 0);
    }
    else if (root_ == QUOTED_STRING) {
      result_ = QuotedString(builder_, 0);
    }
    else if (root_ == SCALAR_TYPE_DEFINITION) {
      result_ = ScalarTypeDefinition(builder_, 0);
    }
    else if (root_ == SCHEMA_DEFINITION) {
      result_ = SchemaDefinition(builder_, 0);
    }
    else if (root_ == STRING) {
      result_ = String(builder_, 0);
    }
    else if (root_ == UNION_MEMBER) {
      result_ = UnionMember(builder_, 0);
    }
    else if (root_ == UNION_MEMBER_SET) {
      result_ = UnionMemberSet(builder_, 0);
    }
    else if (root_ == UNION_TYPE_DEFINITION) {
      result_ = UnionTypeDefinition(builder_, 0);
    }
    else {
      result_ = parse_root_(root_, builder_, 0);
    }
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType root_, PsiBuilder builder_, int level_) {
    return Document(builder_, level_ + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(ANNOTATION_ARGUMENT_LIST_VALUE, ANNOTATION_ARGUMENT_OBJECT_VALUE, ANNOTATION_ARGUMENT_VALUE),
  };

  /* ********************************************************** */
  // AT_ANNOTATION AnnotationArguments?
  public static boolean Annotation(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Annotation")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ANNOTATION, "<annotation>");
    result_ = consumeToken(builder_, AT_ANNOTATION);
    pinned_ = result_; // pin = 1
    result_ = result_ && Annotation_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, RecoverAnnotation_parser_);
    return result_ || pinned_;
  }

  // AnnotationArguments?
  private static boolean Annotation_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Annotation_1")) return false;
    AnnotationArguments(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // LBRACKET (AnnotationArgumentValue (COMMA AnnotationArgumentValue)*)? RBRACKET
  public static boolean AnnotationArgumentListValue(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnnotationArgumentListValue")) return false;
    if (!nextTokenIs(builder_, LBRACKET)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ANNOTATION_ARGUMENT_LIST_VALUE, null);
    result_ = consumeToken(builder_, LBRACKET);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, AnnotationArgumentListValue_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACKET) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (AnnotationArgumentValue (COMMA AnnotationArgumentValue)*)?
  private static boolean AnnotationArgumentListValue_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnnotationArgumentListValue_1")) return false;
    AnnotationArgumentListValue_1_0(builder_, level_ + 1);
    return true;
  }

  // AnnotationArgumentValue (COMMA AnnotationArgumentValue)*
  private static boolean AnnotationArgumentListValue_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnnotationArgumentListValue_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = AnnotationArgumentValue(builder_, level_ + 1);
    result_ = result_ && AnnotationArgumentListValue_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA AnnotationArgumentValue)*
  private static boolean AnnotationArgumentListValue_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnnotationArgumentListValue_1_0_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!AnnotationArgumentListValue_1_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "AnnotationArgumentListValue_1_0_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // COMMA AnnotationArgumentValue
  private static boolean AnnotationArgumentListValue_1_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnnotationArgumentListValue_1_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && AnnotationArgumentValue(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier (EQUALS | COLON) AnnotationArgumentValue
  public static boolean AnnotationArgumentObjectField(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnnotationArgumentObjectField")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    result_ = result_ && AnnotationArgumentObjectField_1(builder_, level_ + 1);
    result_ = result_ && AnnotationArgumentValue(builder_, level_ + 1);
    exit_section_(builder_, marker_, ANNOTATION_ARGUMENT_OBJECT_FIELD, result_);
    return result_;
  }

  // EQUALS | COLON
  private static boolean AnnotationArgumentObjectField_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnnotationArgumentObjectField_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EQUALS);
    if (!result_) result_ = consumeToken(builder_, COLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LBRACE (AnnotationArgumentObjectField (COMMA AnnotationArgumentObjectField)*)? RBRACE
  public static boolean AnnotationArgumentObjectValue(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnnotationArgumentObjectValue")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ANNOTATION_ARGUMENT_OBJECT_VALUE, null);
    result_ = consumeToken(builder_, LBRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, AnnotationArgumentObjectValue_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (AnnotationArgumentObjectField (COMMA AnnotationArgumentObjectField)*)?
  private static boolean AnnotationArgumentObjectValue_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnnotationArgumentObjectValue_1")) return false;
    AnnotationArgumentObjectValue_1_0(builder_, level_ + 1);
    return true;
  }

  // AnnotationArgumentObjectField (COMMA AnnotationArgumentObjectField)*
  private static boolean AnnotationArgumentObjectValue_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnnotationArgumentObjectValue_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = AnnotationArgumentObjectField(builder_, level_ + 1);
    result_ = result_ && AnnotationArgumentObjectValue_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA AnnotationArgumentObjectField)*
  private static boolean AnnotationArgumentObjectValue_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnnotationArgumentObjectValue_1_0_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!AnnotationArgumentObjectValue_1_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "AnnotationArgumentObjectValue_1_0_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // COMMA AnnotationArgumentObjectField
  private static boolean AnnotationArgumentObjectValue_1_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnnotationArgumentObjectValue_1_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && AnnotationArgumentObjectField(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // TRUE | FALSE | number | QuotedString | AnnotationArgumentListValue | AnnotationArgumentObjectValue
  public static boolean AnnotationArgumentValue(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnnotationArgumentValue")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, ANNOTATION_ARGUMENT_VALUE, "<annotation argument value>");
    result_ = consumeToken(builder_, TRUE);
    if (!result_) result_ = consumeToken(builder_, FALSE);
    if (!result_) result_ = consumeToken(builder_, NUMBER);
    if (!result_) result_ = QuotedString(builder_, level_ + 1);
    if (!result_) result_ = AnnotationArgumentListValue(builder_, level_ + 1);
    if (!result_) result_ = AnnotationArgumentObjectValue(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // LPAREN (AnnotationArgumentValue | NamedAnnotationArguments) RPAREN
  public static boolean AnnotationArguments(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnnotationArguments")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ANNOTATION_ARGUMENTS, null);
    result_ = consumeToken(builder_, LPAREN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, AnnotationArguments_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RPAREN) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // AnnotationArgumentValue | NamedAnnotationArguments
  private static boolean AnnotationArguments_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnnotationArguments_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = AnnotationArgumentValue(builder_, level_ + 1);
    if (!result_) result_ = NamedAnnotationArguments(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ANNOTATION_DEF NamedTypeDef ArgumentsDefinition?
  public static boolean AnnotationDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnnotationDefinition")) return false;
    if (!nextTokenIs(builder_, ANNOTATION_DEF)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ANNOTATION_DEFINITION, null);
    result_ = consumeToken(builder_, ANNOTATION_DEF);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, NamedTypeDef(builder_, level_ + 1));
    result_ = pinned_ && AnnotationDefinition_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ArgumentsDefinition?
  private static boolean AnnotationDefinition_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnnotationDefinition_2")) return false;
    ArgumentsDefinition(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // Annotation*
  static boolean Annotations(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Annotations")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!Annotation(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "Annotations", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // LPAREN InputValueDefinitions RPAREN
  public static boolean ArgumentsDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ArgumentsDefinition")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ARGUMENTS_DEFINITION, "<arguments definition>");
    result_ = consumeToken(builder_, LPAREN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, InputValueDefinitions(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RPAREN) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, RecoverArgumentsDefinition_parser_);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // COMMA EnumValueDefinition
  static boolean CommaEnumValueDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CommaEnumValueDefinition")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, COMMA);
    pinned_ = result_; // pin = 1
    result_ = result_ && EnumValueDefinition(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, RecoverCommaEnumValueDefinition_parser_);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // COMMA NamedAnnotationArgument
  static boolean CommaNamedAnnotationArgument(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CommaNamedAnnotationArgument")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, COMMA);
    pinned_ = result_; // pin = 1
    result_ = result_ && NamedAnnotationArgument(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, RecoverNamedAnnotationArgument_parser_);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // COMMA NamedType
  static boolean CommaNamedType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CommaNamedType")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, COMMA);
    pinned_ = result_; // pin = 1
    result_ = result_ && NamedType(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, RecoverCommaNamedType_parser_);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // ListType | (NamedType REQUIRED?)
  public static boolean CompositeType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CompositeType")) return false;
    if (!nextTokenIs(builder_, "<composite type>", LBRACKET, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COMPOSITE_TYPE, "<composite type>");
    result_ = ListType(builder_, level_ + 1);
    if (!result_) result_ = CompositeType_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // NamedType REQUIRED?
  private static boolean CompositeType_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CompositeType_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = NamedType(builder_, level_ + 1);
    result_ = result_ && CompositeType_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // REQUIRED?
  private static boolean CompositeType_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CompositeType_1_1")) return false;
    consumeToken(builder_, REQUIRED);
    return true;
  }

  /* ********************************************************** */
  // ObjectTypeDefinition
  //     |
  //     InterfaceTypeDefinition
  //     |
  //     InputObjectTypeDefinition
  //     |
  //     EnumTypeDefinition
  //     |
  //     UnionTypeDefinition
  //     |
  //     ScalarTypeDefinition
  //     |
  //     ImportDeclaration
  //     |
  //     SchemaDefinition
  //     |
  //     AnnotationDefinition
  static boolean Definition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Definition")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = ObjectTypeDefinition(builder_, level_ + 1);
    if (!result_) result_ = InterfaceTypeDefinition(builder_, level_ + 1);
    if (!result_) result_ = InputObjectTypeDefinition(builder_, level_ + 1);
    if (!result_) result_ = EnumTypeDefinition(builder_, level_ + 1);
    if (!result_) result_ = UnionTypeDefinition(builder_, level_ + 1);
    if (!result_) result_ = ScalarTypeDefinition(builder_, level_ + 1);
    if (!result_) result_ = ImportDeclaration(builder_, level_ + 1);
    if (!result_) result_ = SchemaDefinition(builder_, level_ + 1);
    if (!result_) result_ = AnnotationDefinition(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, RecoverDefinition_parser_);
    return result_;
  }

  /* ********************************************************** */
  // TYPE | INTERFACE | INPUT | ENUM | UNION | SCALAR | ANNOTATION_DEF | IMPORT | SCHEMA
  static boolean DefinitionKeyword(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "DefinitionKeyword")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, TYPE);
    if (!result_) result_ = consumeToken(builder_, INTERFACE);
    if (!result_) result_ = consumeToken(builder_, INPUT);
    if (!result_) result_ = consumeToken(builder_, ENUM);
    if (!result_) result_ = consumeToken(builder_, UNION);
    if (!result_) result_ = consumeToken(builder_, SCALAR);
    if (!result_) result_ = consumeToken(builder_, ANNOTATION_DEF);
    if (!result_) result_ = consumeToken(builder_, IMPORT);
    if (!result_) result_ = consumeToken(builder_, SCHEMA);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // Definition*
  static boolean Document(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Document")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!Definition(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "Document", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // Annotations? ENUM NamedTypeDef EnumValueDefinitionSet
  public static boolean EnumTypeDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumTypeDefinition")) return false;
    if (!nextTokenIs(builder_, "<enum type definition>", AT_ANNOTATION, ENUM)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ENUM_TYPE_DEFINITION, "<enum type definition>");
    result_ = EnumTypeDefinition_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ENUM);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, NamedTypeDef(builder_, level_ + 1));
    result_ = pinned_ && EnumValueDefinitionSet(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // Annotations?
  private static boolean EnumTypeDefinition_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumTypeDefinition_0")) return false;
    Annotations(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // identifier
  public static boolean EnumValueDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumValueDefinition")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, ENUM_VALUE_DEFINITION, result_);
    return result_;
  }

  /* ********************************************************** */
  // LBRACE EnumValueDefinition CommaEnumValueDefinition* RBRACE
  public static boolean EnumValueDefinitionSet(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumValueDefinitionSet")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ENUM_VALUE_DEFINITION_SET, null);
    result_ = consumeToken(builder_, LBRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, EnumValueDefinition(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, EnumValueDefinitionSet_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // CommaEnumValueDefinition*
  private static boolean EnumValueDefinitionSet_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumValueDefinitionSet_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!CommaEnumValueDefinition(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "EnumValueDefinitionSet_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // Annotations? Property ArgumentsDefinition? COLON CompositeType
  public static boolean FieldDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDefinition")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FIELD_DEFINITION, "<field definition>");
    result_ = FieldDefinition_0(builder_, level_ + 1);
    result_ = result_ && Property(builder_, level_ + 1);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, FieldDefinition_2(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, COLON)) && result_;
    result_ = pinned_ && CompositeType(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, RecoverFieldDefinition_parser_);
    return result_ || pinned_;
  }

  // Annotations?
  private static boolean FieldDefinition_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDefinition_0")) return false;
    Annotations(builder_, level_ + 1);
    return true;
  }

  // ArgumentsDefinition?
  private static boolean FieldDefinition_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDefinition_2")) return false;
    ArgumentsDefinition(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // LBRACE FieldDefinition* RBRACE
  public static boolean FieldDefinitionSet(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDefinitionSet")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FIELD_DEFINITION_SET, null);
    result_ = consumeToken(builder_, LBRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, FieldDefinitionSet_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // FieldDefinition*
  private static boolean FieldDefinitionSet_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDefinitionSet_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!FieldDefinition(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "FieldDefinitionSet_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // IMPLEMENTS NamedType CommaNamedType*
  public static boolean ImplementsInterfaces(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImplementsInterfaces")) return false;
    if (!nextTokenIs(builder_, IMPLEMENTS)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IMPLEMENTS_INTERFACES, null);
    result_ = consumeToken(builder_, IMPLEMENTS);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, NamedType(builder_, level_ + 1));
    result_ = pinned_ && ImplementsInterfaces_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // CommaNamedType*
  private static boolean ImplementsInterfaces_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImplementsInterfaces_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!CommaNamedType(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ImplementsInterfaces_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // IMPORT ImportFileReference
  public static boolean ImportDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportDeclaration")) return false;
    if (!nextTokenIs(builder_, IMPORT)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IMPORT_DECLARATION, null);
    result_ = consumeToken(builder_, IMPORT);
    pinned_ = result_; // pin = 1
    result_ = result_ && ImportFileReference(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // QuotedString
  public static boolean ImportFileReference(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportFileReference")) return false;
    if (!nextTokenIs(builder_, OPEN_QUOTE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = QuotedString(builder_, level_ + 1);
    exit_section_(builder_, marker_, IMPORT_FILE_REFERENCE, result_);
    return result_;
  }

  /* ********************************************************** */
  // Annotations? INPUT NamedTypeDef FieldDefinitionSet
  public static boolean InputObjectTypeDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InputObjectTypeDefinition")) return false;
    if (!nextTokenIs(builder_, "<input object type definition>", AT_ANNOTATION, INPUT)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INPUT_OBJECT_TYPE_DEFINITION, "<input object type definition>");
    result_ = InputObjectTypeDefinition_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, INPUT);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, NamedTypeDef(builder_, level_ + 1));
    result_ = pinned_ && FieldDefinitionSet(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // Annotations?
  private static boolean InputObjectTypeDefinition_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InputObjectTypeDefinition_0")) return false;
    Annotations(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // InputValueDefinitionIdentifier COLON CompositeType
  public static boolean InputValueDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InputValueDefinition")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INPUT_VALUE_DEFINITION, null);
    result_ = InputValueDefinitionIdentifier(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, consumeToken(builder_, COLON));
    result_ = pinned_ && CompositeType(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // identifier
  public static boolean InputValueDefinitionIdentifier(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InputValueDefinitionIdentifier")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, INPUT_VALUE_DEFINITION_IDENTIFIER, result_);
    return result_;
  }

  /* ********************************************************** */
  // InputValueDefinition (COMMA InputValueDefinition)*
  public static boolean InputValueDefinitions(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InputValueDefinitions")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = InputValueDefinition(builder_, level_ + 1);
    result_ = result_ && InputValueDefinitions_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, INPUT_VALUE_DEFINITIONS, result_);
    return result_;
  }

  // (COMMA InputValueDefinition)*
  private static boolean InputValueDefinitions_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InputValueDefinitions_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!InputValueDefinitions_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "InputValueDefinitions_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // COMMA InputValueDefinition
  private static boolean InputValueDefinitions_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InputValueDefinitions_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && InputValueDefinition(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // Annotations? INTERFACE NamedTypeDef FieldDefinitionSet
  public static boolean InterfaceTypeDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InterfaceTypeDefinition")) return false;
    if (!nextTokenIs(builder_, "<interface type definition>", AT_ANNOTATION, INTERFACE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INTERFACE_TYPE_DEFINITION, "<interface type definition>");
    result_ = InterfaceTypeDefinition_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, INTERFACE);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, NamedTypeDef(builder_, level_ + 1));
    result_ = pinned_ && FieldDefinitionSet(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // Annotations?
  private static boolean InterfaceTypeDefinition_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InterfaceTypeDefinition_0")) return false;
    Annotations(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // LBRACKET NamedType RBRACKET REQUIRED?
  public static boolean ListType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ListType")) return false;
    if (!nextTokenIs(builder_, LBRACKET)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LIST_TYPE, null);
    result_ = consumeToken(builder_, LBRACKET);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, NamedType(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, RBRACKET)) && result_;
    result_ = pinned_ && ListType_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // REQUIRED?
  private static boolean ListType_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ListType_3")) return false;
    consumeToken(builder_, REQUIRED);
    return true;
  }

  /* ********************************************************** */
  // identifier (EQUALS | COLON) AnnotationArgumentValue
  public static boolean NamedAnnotationArgument(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NamedAnnotationArgument")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, NAMED_ANNOTATION_ARGUMENT, "<named annotation argument>");
    result_ = consumeToken(builder_, IDENTIFIER);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, NamedAnnotationArgument_1(builder_, level_ + 1));
    result_ = pinned_ && AnnotationArgumentValue(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, RecoverNamedAnnotationArgument_parser_);
    return result_ || pinned_;
  }

  // EQUALS | COLON
  private static boolean NamedAnnotationArgument_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NamedAnnotationArgument_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EQUALS);
    if (!result_) result_ = consumeToken(builder_, COLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // NamedAnnotationArgument CommaNamedAnnotationArgument*
  public static boolean NamedAnnotationArguments(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NamedAnnotationArguments")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = NamedAnnotationArgument(builder_, level_ + 1);
    result_ = result_ && NamedAnnotationArguments_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, NAMED_ANNOTATION_ARGUMENTS, result_);
    return result_;
  }

  // CommaNamedAnnotationArgument*
  private static boolean NamedAnnotationArguments_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NamedAnnotationArguments_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!CommaNamedAnnotationArgument(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "NamedAnnotationArguments_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // identifier
  public static boolean NamedType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NamedType")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, NAMED_TYPE, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier
  public static boolean NamedTypeDef(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NamedTypeDef")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, NAMED_TYPE_DEF, result_);
    return result_;
  }

  /* ********************************************************** */
  // Annotations? TYPE NamedTypeDef ImplementsInterfaces? FieldDefinitionSet
  public static boolean ObjectTypeDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ObjectTypeDefinition")) return false;
    if (!nextTokenIs(builder_, "<object type definition>", AT_ANNOTATION, TYPE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, OBJECT_TYPE_DEFINITION, "<object type definition>");
    result_ = ObjectTypeDefinition_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, TYPE);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, NamedTypeDef(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, ObjectTypeDefinition_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && FieldDefinitionSet(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // Annotations?
  private static boolean ObjectTypeDefinition_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ObjectTypeDefinition_0")) return false;
    Annotations(builder_, level_ + 1);
    return true;
  }

  // ImplementsInterfaces?
  private static boolean ObjectTypeDefinition_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ObjectTypeDefinition_3")) return false;
    ImplementsInterfaces(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // (QUERY | MUTATION | SUBSCRIPTION) COLON NamedType
  public static boolean OperationTypeDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "OperationTypeDefinition")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, OPERATION_TYPE_DEFINITION, "<operation type definition>");
    result_ = OperationTypeDefinition_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, consumeToken(builder_, COLON));
    result_ = pinned_ && NamedType(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, RecoverOperation_parser_);
    return result_ || pinned_;
  }

  // QUERY | MUTATION | SUBSCRIPTION
  private static boolean OperationTypeDefinition_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "OperationTypeDefinition_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, QUERY);
    if (!result_) result_ = consumeToken(builder_, MUTATION);
    if (!result_) result_ = consumeToken(builder_, SUBSCRIPTION);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LBRACE OperationTypeDefinition* RBRACE
  public static boolean OperationTypeDefinitionSet(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "OperationTypeDefinitionSet")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, OPERATION_TYPE_DEFINITION_SET, null);
    result_ = consumeToken(builder_, LBRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, OperationTypeDefinitionSet_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // OperationTypeDefinition*
  private static boolean OperationTypeDefinitionSet_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "OperationTypeDefinitionSet_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!OperationTypeDefinition(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "OperationTypeDefinitionSet_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // PIPE UnionMember
  static boolean PipeUnionMember(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PipeUnionMember")) return false;
    if (!nextTokenIs(builder_, PIPE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, PIPE);
    pinned_ = result_; // pin = 1
    result_ = result_ && UnionMember(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // identifier
  public static boolean Property(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Property")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, PROPERTY, result_);
    return result_;
  }

  /* ********************************************************** */
  // OPEN_QUOTE String? CLOSING_QUOTE
  public static boolean QuotedString(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "QuotedString")) return false;
    if (!nextTokenIs(builder_, OPEN_QUOTE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, QUOTED_STRING, null);
    result_ = consumeToken(builder_, OPEN_QUOTE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, QuotedString_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, CLOSING_QUOTE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // String?
  private static boolean QuotedString_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "QuotedString_1")) return false;
    String(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // !(RBRACE | AT_ANNOTATION | DefinitionKeyword | identifier )
  static boolean RecoverAnnotation(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecoverAnnotation")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !RecoverAnnotation_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // RBRACE | AT_ANNOTATION | DefinitionKeyword | identifier
  private static boolean RecoverAnnotation_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecoverAnnotation_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, AT_ANNOTATION);
    if (!result_) result_ = DefinitionKeyword(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(COLON | RBRACE | AT_ANNOTATION | DefinitionKeyword | identifier)
  static boolean RecoverArgumentsDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecoverArgumentsDefinition")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !RecoverArgumentsDefinition_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // COLON | RBRACE | AT_ANNOTATION | DefinitionKeyword | identifier
  private static boolean RecoverArgumentsDefinition_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecoverArgumentsDefinition_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COLON);
    if (!result_) result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, AT_ANNOTATION);
    if (!result_) result_ = DefinitionKeyword(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(RBRACE | COMMA | DefinitionKeyword | identifier)
  static boolean RecoverCommaEnumValueDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecoverCommaEnumValueDefinition")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !RecoverCommaEnumValueDefinition_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // RBRACE | COMMA | DefinitionKeyword | identifier
  private static boolean RecoverCommaEnumValueDefinition_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecoverCommaEnumValueDefinition_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, COMMA);
    if (!result_) result_ = DefinitionKeyword(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(LBRACE | COMMA | DefinitionKeyword | identifier)
  static boolean RecoverCommaNamedType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecoverCommaNamedType")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !RecoverCommaNamedType_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // LBRACE | COMMA | DefinitionKeyword | identifier
  private static boolean RecoverCommaNamedType_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecoverCommaNamedType_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACE);
    if (!result_) result_ = consumeToken(builder_, COMMA);
    if (!result_) result_ = DefinitionKeyword(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(DefinitionKeyword | AT_ANNOTATION)
  static boolean RecoverDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecoverDefinition")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !RecoverDefinition_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // DefinitionKeyword | AT_ANNOTATION
  private static boolean RecoverDefinition_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecoverDefinition_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = DefinitionKeyword(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, AT_ANNOTATION);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(RBRACE | AT_ANNOTATION | identifier)
  static boolean RecoverFieldDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecoverFieldDefinition")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !RecoverFieldDefinition_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // RBRACE | AT_ANNOTATION | identifier
  private static boolean RecoverFieldDefinition_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecoverFieldDefinition_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, AT_ANNOTATION);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(COMMA | RPAREN)
  static boolean RecoverNamedAnnotationArgument(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecoverNamedAnnotationArgument")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !RecoverNamedAnnotationArgument_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // COMMA | RPAREN
  private static boolean RecoverNamedAnnotationArgument_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecoverNamedAnnotationArgument_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    if (!result_) result_ = consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(RBRACE | QUERY | MUTATION | SUBSCRIPTION)
  static boolean RecoverOperation(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecoverOperation")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !RecoverOperation_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // RBRACE | QUERY | MUTATION | SUBSCRIPTION
  private static boolean RecoverOperation_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecoverOperation_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, QUERY);
    if (!result_) result_ = consumeToken(builder_, MUTATION);
    if (!result_) result_ = consumeToken(builder_, SUBSCRIPTION);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // Annotations? SCALAR NamedTypeDef
  public static boolean ScalarTypeDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ScalarTypeDefinition")) return false;
    if (!nextTokenIs(builder_, "<scalar type definition>", AT_ANNOTATION, SCALAR)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SCALAR_TYPE_DEFINITION, "<scalar type definition>");
    result_ = ScalarTypeDefinition_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SCALAR);
    pinned_ = result_; // pin = 2
    result_ = result_ && NamedTypeDef(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // Annotations?
  private static boolean ScalarTypeDefinition_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ScalarTypeDefinition_0")) return false;
    Annotations(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // SCHEMA OperationTypeDefinitionSet
  public static boolean SchemaDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SchemaDefinition")) return false;
    if (!nextTokenIs(builder_, SCHEMA)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SCHEMA_DEFINITION, null);
    result_ = consumeToken(builder_, SCHEMA);
    pinned_ = result_; // pin = 1
    result_ = result_ && OperationTypeDefinitionSet(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // STRING_BODY
  public static boolean String(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "String")) return false;
    if (!nextTokenIs(builder_, STRING_BODY)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, STRING_BODY);
    exit_section_(builder_, marker_, STRING, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier
  public static boolean UnionMember(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UnionMember")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, UNION_MEMBER, result_);
    return result_;
  }

  /* ********************************************************** */
  // UnionMember PipeUnionMember*
  public static boolean UnionMemberSet(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UnionMemberSet")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = UnionMember(builder_, level_ + 1);
    result_ = result_ && UnionMemberSet_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, UNION_MEMBER_SET, result_);
    return result_;
  }

  // PipeUnionMember*
  private static boolean UnionMemberSet_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UnionMemberSet_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!PipeUnionMember(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "UnionMemberSet_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // Annotations? UNION NamedTypeDef EQUALS UnionMemberSet
  public static boolean UnionTypeDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UnionTypeDefinition")) return false;
    if (!nextTokenIs(builder_, "<union type definition>", AT_ANNOTATION, UNION)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, UNION_TYPE_DEFINITION, "<union type definition>");
    result_ = UnionTypeDefinition_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, UNION);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, NamedTypeDef(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, EQUALS)) && result_;
    result_ = pinned_ && UnionMemberSet(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // Annotations?
  private static boolean UnionTypeDefinition_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UnionTypeDefinition_0")) return false;
    Annotations(builder_, level_ + 1);
    return true;
  }

  final static Parser RecoverAnnotation_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return RecoverAnnotation(builder_, level_ + 1);
    }
  };
  final static Parser RecoverArgumentsDefinition_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return RecoverArgumentsDefinition(builder_, level_ + 1);
    }
  };
  final static Parser RecoverCommaEnumValueDefinition_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return RecoverCommaEnumValueDefinition(builder_, level_ + 1);
    }
  };
  final static Parser RecoverCommaNamedType_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return RecoverCommaNamedType(builder_, level_ + 1);
    }
  };
  final static Parser RecoverDefinition_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return RecoverDefinition(builder_, level_ + 1);
    }
  };
  final static Parser RecoverFieldDefinition_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return RecoverFieldDefinition(builder_, level_ + 1);
    }
  };
  final static Parser RecoverNamedAnnotationArgument_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return RecoverNamedAnnotationArgument(builder_, level_ + 1);
    }
  };
  final static Parser RecoverOperation_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return RecoverOperation(builder_, level_ + 1);
    }
  };
}
