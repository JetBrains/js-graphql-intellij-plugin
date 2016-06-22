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

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t == ANNOTATION) {
      r = Annotation(b, 0);
    }
    else if (t == ANNOTATION_ARGUMENT_VALUE) {
      r = AnnotationArgumentValue(b, 0);
    }
    else if (t == ANNOTATION_ARGUMENTS) {
      r = AnnotationArguments(b, 0);
    }
    else if (t == ANNOTATIONS) {
      r = Annotations(b, 0);
    }
    else if (t == ARGUMENTS_DEFINITION) {
      r = ArgumentsDefinition(b, 0);
    }
    else if (t == COMPOSITE_TYPE) {
      r = CompositeType(b, 0);
    }
    else if (t == ENUM_TYPE_DEFINITION) {
      r = EnumTypeDefinition(b, 0);
    }
    else if (t == ENUM_VALUE_DEFINITION) {
      r = EnumValueDefinition(b, 0);
    }
    else if (t == ENUM_VALUE_DEFINITION_SET) {
      r = EnumValueDefinitionSet(b, 0);
    }
    else if (t == FIELD_DEFINITION) {
      r = FieldDefinition(b, 0);
    }
    else if (t == FIELD_DEFINITION_SET) {
      r = FieldDefinitionSet(b, 0);
    }
    else if (t == IMPLEMENTS_INTERFACES) {
      r = ImplementsInterfaces(b, 0);
    }
    else if (t == IMPORT_DECLARATION) {
      r = ImportDeclaration(b, 0);
    }
    else if (t == IMPORT_FILE_REFERENCE) {
      r = ImportFileReference(b, 0);
    }
    else if (t == INPUT_OBJECT_TYPE_DEFINITION) {
      r = InputObjectTypeDefinition(b, 0);
    }
    else if (t == INPUT_VALUE_DEFINITION) {
      r = InputValueDefinition(b, 0);
    }
    else if (t == INPUT_VALUE_DEFINITIONS) {
      r = InputValueDefinitions(b, 0);
    }
    else if (t == INTERFACE_TYPE_DEFINITION) {
      r = InterfaceTypeDefinition(b, 0);
    }
    else if (t == LIST_TYPE) {
      r = ListType(b, 0);
    }
    else if (t == NAMED_ANNOTATION_ARGUMENT) {
      r = NamedAnnotationArgument(b, 0);
    }
    else if (t == NAMED_ANNOTATION_ARGUMENTS) {
      r = NamedAnnotationArguments(b, 0);
    }
    else if (t == NAMED_TYPE) {
      r = NamedType(b, 0);
    }
    else if (t == NAMED_TYPE_DEF) {
      r = NamedTypeDef(b, 0);
    }
    else if (t == OBJECT_TYPE_DEFINITION) {
      r = ObjectTypeDefinition(b, 0);
    }
    else if (t == OPERATION_TYPE_DEFINITION) {
      r = OperationTypeDefinition(b, 0);
    }
    else if (t == OPERATION_TYPE_DEFINITION_SET) {
      r = OperationTypeDefinitionSet(b, 0);
    }
    else if (t == PROPERTY) {
      r = Property(b, 0);
    }
    else if (t == QUOTED_STRING) {
      r = QuotedString(b, 0);
    }
    else if (t == SCALAR_TYPE_DEFINITION) {
      r = ScalarTypeDefinition(b, 0);
    }
    else if (t == SCHEMA_DEFINITION) {
      r = SchemaDefinition(b, 0);
    }
    else if (t == UNION_MEMBER) {
      r = UnionMember(b, 0);
    }
    else if (t == UNION_MEMBER_SET) {
      r = UnionMemberSet(b, 0);
    }
    else if (t == UNION_TYPE_DEFINITION) {
      r = UnionTypeDefinition(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return Document(b, l + 1);
  }

  /* ********************************************************** */
  // AT_ANNOTATION AnnotationArguments?
  public static boolean Annotation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Annotation")) return false;
    if (!nextTokenIs(b, AT_ANNOTATION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ANNOTATION, null);
    r = consumeToken(b, AT_ANNOTATION);
    p = r; // pin = 1
    r = r && Annotation_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AnnotationArguments?
  private static boolean Annotation_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Annotation_1")) return false;
    AnnotationArguments(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // TRUE | FALSE | NUMBER | QuotedString
  public static boolean AnnotationArgumentValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationArgumentValue")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ANNOTATION_ARGUMENT_VALUE, "<annotation argument value>");
    r = consumeToken(b, TRUE);
    if (!r) r = consumeToken(b, FALSE);
    if (!r) r = consumeToken(b, NUMBER);
    if (!r) r = QuotedString(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LPAREN (AnnotationArgumentValue | NamedAnnotationArguments) RPAREN
  public static boolean AnnotationArguments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationArguments")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ANNOTATION_ARGUMENTS, null);
    r = consumeToken(b, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, AnnotationArguments_1(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AnnotationArgumentValue | NamedAnnotationArguments
  private static boolean AnnotationArguments_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationArguments_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = AnnotationArgumentValue(b, l + 1);
    if (!r) r = NamedAnnotationArguments(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Annotation+
  public static boolean Annotations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Annotations")) return false;
    if (!nextTokenIs(b, AT_ANNOTATION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Annotation(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!Annotation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "Annotations", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, ANNOTATIONS, r);
    return r;
  }

  /* ********************************************************** */
  // LPAREN InputValueDefinitions RPAREN
  public static boolean ArgumentsDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArgumentsDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ARGUMENTS_DEFINITION, "<arguments definition>");
    r = consumeToken(b, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, InputValueDefinitions(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, RecoverArgumentsDefinition_parser_);
    return r || p;
  }

  /* ********************************************************** */
  // ListType | (NamedType REQUIRED?)
  public static boolean CompositeType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompositeType")) return false;
    if (!nextTokenIs(b, "<composite type>", IDENTIFIER, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOSITE_TYPE, "<composite type>");
    r = ListType(b, l + 1);
    if (!r) r = CompositeType_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // NamedType REQUIRED?
  private static boolean CompositeType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompositeType_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = NamedType(b, l + 1);
    r = r && CompositeType_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // REQUIRED?
  private static boolean CompositeType_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompositeType_1_1")) return false;
    consumeToken(b, REQUIRED);
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
  static boolean Definition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Definition")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ObjectTypeDefinition(b, l + 1);
    if (!r) r = InterfaceTypeDefinition(b, l + 1);
    if (!r) r = InputObjectTypeDefinition(b, l + 1);
    if (!r) r = EnumTypeDefinition(b, l + 1);
    if (!r) r = UnionTypeDefinition(b, l + 1);
    if (!r) r = ScalarTypeDefinition(b, l + 1);
    if (!r) r = ImportDeclaration(b, l + 1);
    if (!r) r = SchemaDefinition(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Definition*
  static boolean Document(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Document")) return false;
    int c = current_position_(b);
    while (true) {
      if (!Definition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "Document", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // ENUM NamedTypeDef EnumValueDefinitionSet
  public static boolean EnumTypeDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumTypeDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENUM_TYPE_DEFINITION, "<enum type definition>");
    r = consumeToken(b, ENUM);
    p = r; // pin = 1
    r = r && report_error_(b, NamedTypeDef(b, l + 1));
    r = p && EnumValueDefinitionSet(b, l + 1) && r;
    exit_section_(b, l, m, r, p, RecoverDefinition_parser_);
    return r || p;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean EnumValueDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumValueDefinition")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, ENUM_VALUE_DEFINITION, r);
    return r;
  }

  /* ********************************************************** */
  // LBRACE EnumValueDefinition (COMMA EnumValueDefinition)* RBRACE
  public static boolean EnumValueDefinitionSet(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumValueDefinitionSet")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && EnumValueDefinition(b, l + 1);
    r = r && EnumValueDefinitionSet_2(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, ENUM_VALUE_DEFINITION_SET, r);
    return r;
  }

  // (COMMA EnumValueDefinition)*
  private static boolean EnumValueDefinitionSet_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumValueDefinitionSet_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!EnumValueDefinitionSet_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "EnumValueDefinitionSet_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA EnumValueDefinition
  private static boolean EnumValueDefinitionSet_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumValueDefinitionSet_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && EnumValueDefinition(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Annotations? Property ArgumentsDefinition? COLON CompositeType
  public static boolean FieldDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition")) return false;
    if (!nextTokenIs(b, "<field definition>", AT_ANNOTATION, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FIELD_DEFINITION, "<field definition>");
    r = FieldDefinition_0(b, l + 1);
    r = r && Property(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, FieldDefinition_2(b, l + 1));
    r = p && report_error_(b, consumeToken(b, COLON)) && r;
    r = p && CompositeType(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // Annotations?
  private static boolean FieldDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition_0")) return false;
    Annotations(b, l + 1);
    return true;
  }

  // ArgumentsDefinition?
  private static boolean FieldDefinition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition_2")) return false;
    ArgumentsDefinition(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // LBRACE FieldDefinition* RBRACE
  public static boolean FieldDefinitionSet(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinitionSet")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FIELD_DEFINITION_SET, null);
    r = consumeToken(b, LBRACE);
    p = r; // pin = 1
    r = r && report_error_(b, FieldDefinitionSet_1(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // FieldDefinition*
  private static boolean FieldDefinitionSet_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinitionSet_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!FieldDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "FieldDefinitionSet_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // IMPLEMENTS NamedType (COMMA NamedType)*
  public static boolean ImplementsInterfaces(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ImplementsInterfaces")) return false;
    if (!nextTokenIs(b, IMPLEMENTS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IMPLEMENTS_INTERFACES, null);
    r = consumeToken(b, IMPLEMENTS);
    p = r; // pin = 1
    r = r && report_error_(b, NamedType(b, l + 1));
    r = p && ImplementsInterfaces_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA NamedType)*
  private static boolean ImplementsInterfaces_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ImplementsInterfaces_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!ImplementsInterfaces_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ImplementsInterfaces_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA NamedType
  private static boolean ImplementsInterfaces_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ImplementsInterfaces_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && NamedType(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IMPORT ImportFileReference
  public static boolean ImportDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ImportDeclaration")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IMPORT_DECLARATION, "<import declaration>");
    r = consumeToken(b, IMPORT);
    p = r; // pin = 1
    r = r && ImportFileReference(b, l + 1);
    exit_section_(b, l, m, r, p, RecoverDefinition_parser_);
    return r || p;
  }

  /* ********************************************************** */
  // QuotedString
  public static boolean ImportFileReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ImportFileReference")) return false;
    if (!nextTokenIs(b, OPEN_QUOTE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = QuotedString(b, l + 1);
    exit_section_(b, m, IMPORT_FILE_REFERENCE, r);
    return r;
  }

  /* ********************************************************** */
  // INPUT NamedTypeDef FieldDefinitionSet
  public static boolean InputObjectTypeDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InputObjectTypeDefinition")) return false;
    if (!nextTokenIs(b, INPUT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INPUT_OBJECT_TYPE_DEFINITION, null);
    r = consumeToken(b, INPUT);
    p = r; // pin = 1
    r = r && report_error_(b, NamedTypeDef(b, l + 1));
    r = p && FieldDefinitionSet(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // IDENTIFIER COLON CompositeType
  public static boolean InputValueDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InputValueDefinition")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INPUT_VALUE_DEFINITION, null);
    r = consumeTokens(b, 1, IDENTIFIER, COLON);
    p = r; // pin = 1
    r = r && CompositeType(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // InputValueDefinition (COMMA InputValueDefinition)*
  public static boolean InputValueDefinitions(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InputValueDefinitions")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = InputValueDefinition(b, l + 1);
    r = r && InputValueDefinitions_1(b, l + 1);
    exit_section_(b, m, INPUT_VALUE_DEFINITIONS, r);
    return r;
  }

  // (COMMA InputValueDefinition)*
  private static boolean InputValueDefinitions_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InputValueDefinitions_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!InputValueDefinitions_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "InputValueDefinitions_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA InputValueDefinition
  private static boolean InputValueDefinitions_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InputValueDefinitions_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && InputValueDefinition(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // INTERFACE NamedTypeDef FieldDefinitionSet
  public static boolean InterfaceTypeDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InterfaceTypeDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INTERFACE_TYPE_DEFINITION, "<interface type definition>");
    r = consumeToken(b, INTERFACE);
    p = r; // pin = 1
    r = r && report_error_(b, NamedTypeDef(b, l + 1));
    r = p && FieldDefinitionSet(b, l + 1) && r;
    exit_section_(b, l, m, r, p, RecoverDefinition_parser_);
    return r || p;
  }

  /* ********************************************************** */
  // LBRACKET NamedType RBRACKET
  public static boolean ListType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ListType")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LIST_TYPE, null);
    r = consumeToken(b, LBRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, NamedType(b, l + 1));
    r = p && consumeToken(b, RBRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // IDENTIFIER EQUALS AnnotationArgumentValue
  public static boolean NamedAnnotationArgument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamedAnnotationArgument")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, NAMED_ANNOTATION_ARGUMENT, null);
    r = consumeTokens(b, 1, IDENTIFIER, EQUALS);
    p = r; // pin = 1
    r = r && AnnotationArgumentValue(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // NamedAnnotationArgument (COMMA NamedAnnotationArgument)*
  public static boolean NamedAnnotationArguments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamedAnnotationArguments")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = NamedAnnotationArgument(b, l + 1);
    r = r && NamedAnnotationArguments_1(b, l + 1);
    exit_section_(b, m, NAMED_ANNOTATION_ARGUMENTS, r);
    return r;
  }

  // (COMMA NamedAnnotationArgument)*
  private static boolean NamedAnnotationArguments_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamedAnnotationArguments_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!NamedAnnotationArguments_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "NamedAnnotationArguments_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA NamedAnnotationArgument
  private static boolean NamedAnnotationArguments_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamedAnnotationArguments_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && NamedAnnotationArgument(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean NamedType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamedType")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, NAMED_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean NamedTypeDef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamedTypeDef")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, NAMED_TYPE_DEF, r);
    return r;
  }

  /* ********************************************************** */
  // TYPE NamedTypeDef ImplementsInterfaces? FieldDefinitionSet
  public static boolean ObjectTypeDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_TYPE_DEFINITION, "<object type definition>");
    r = consumeToken(b, TYPE);
    p = r; // pin = 1
    r = r && report_error_(b, NamedTypeDef(b, l + 1));
    r = p && report_error_(b, ObjectTypeDefinition_2(b, l + 1)) && r;
    r = p && FieldDefinitionSet(b, l + 1) && r;
    exit_section_(b, l, m, r, p, RecoverDefinition_parser_);
    return r || p;
  }

  // ImplementsInterfaces?
  private static boolean ObjectTypeDefinition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeDefinition_2")) return false;
    ImplementsInterfaces(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (QUERY | MUTATION | SUBSCRIPTION) COLON NamedType
  public static boolean OperationTypeDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OperationTypeDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OPERATION_TYPE_DEFINITION, "<operation type definition>");
    r = OperationTypeDefinition_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, COLON));
    r = p && NamedType(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // QUERY | MUTATION | SUBSCRIPTION
  private static boolean OperationTypeDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OperationTypeDefinition_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, QUERY);
    if (!r) r = consumeToken(b, MUTATION);
    if (!r) r = consumeToken(b, SUBSCRIPTION);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LBRACE OperationTypeDefinition* RBRACE
  public static boolean OperationTypeDefinitionSet(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OperationTypeDefinitionSet")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && OperationTypeDefinitionSet_1(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, OPERATION_TYPE_DEFINITION_SET, r);
    return r;
  }

  // OperationTypeDefinition*
  private static boolean OperationTypeDefinitionSet_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OperationTypeDefinitionSet_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!OperationTypeDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "OperationTypeDefinitionSet_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean Property(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Property")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, PROPERTY, r);
    return r;
  }

  /* ********************************************************** */
  // OPEN_QUOTE STRING_BODY? CLOSING_QUOTE
  public static boolean QuotedString(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "QuotedString")) return false;
    if (!nextTokenIs(b, OPEN_QUOTE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, QUOTED_STRING, null);
    r = consumeToken(b, OPEN_QUOTE);
    p = r; // pin = 1
    r = r && report_error_(b, QuotedString_1(b, l + 1));
    r = p && consumeToken(b, CLOSING_QUOTE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // STRING_BODY?
  private static boolean QuotedString_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "QuotedString_1")) return false;
    consumeToken(b, STRING_BODY);
    return true;
  }

  /* ********************************************************** */
  // !(COLON)
  static boolean RecoverArgumentsDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverArgumentsDefinition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, COLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(TYPE | INTERFACE | INPUT | ENUM | UNION | SCALAR | IMPORT | SCHEMA)
  static boolean RecoverDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverDefinition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !RecoverDefinition_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // TYPE | INTERFACE | INPUT | ENUM | UNION | SCALAR | IMPORT | SCHEMA
  private static boolean RecoverDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverDefinition_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TYPE);
    if (!r) r = consumeToken(b, INTERFACE);
    if (!r) r = consumeToken(b, INPUT);
    if (!r) r = consumeToken(b, ENUM);
    if (!r) r = consumeToken(b, UNION);
    if (!r) r = consumeToken(b, SCALAR);
    if (!r) r = consumeToken(b, IMPORT);
    if (!r) r = consumeToken(b, SCHEMA);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SCALAR NamedTypeDef
  public static boolean ScalarTypeDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ScalarTypeDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SCALAR_TYPE_DEFINITION, "<scalar type definition>");
    r = consumeToken(b, SCALAR);
    p = r; // pin = 1
    r = r && NamedTypeDef(b, l + 1);
    exit_section_(b, l, m, r, p, RecoverDefinition_parser_);
    return r || p;
  }

  /* ********************************************************** */
  // SCHEMA OperationTypeDefinitionSet
  public static boolean SchemaDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SchemaDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SCHEMA_DEFINITION, "<schema definition>");
    r = consumeToken(b, SCHEMA);
    p = r; // pin = 1
    r = r && OperationTypeDefinitionSet(b, l + 1);
    exit_section_(b, l, m, r, p, RecoverDefinition_parser_);
    return r || p;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean UnionMember(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnionMember")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, UNION_MEMBER, r);
    return r;
  }

  /* ********************************************************** */
  // UnionMember (PIPE UnionMember)
  public static boolean UnionMemberSet(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnionMemberSet")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = UnionMember(b, l + 1);
    r = r && UnionMemberSet_1(b, l + 1);
    exit_section_(b, m, UNION_MEMBER_SET, r);
    return r;
  }

  // PIPE UnionMember
  private static boolean UnionMemberSet_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnionMemberSet_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PIPE);
    r = r && UnionMember(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // UNION NamedTypeDef EQUALS UnionMemberSet
  public static boolean UnionTypeDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnionTypeDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, UNION_TYPE_DEFINITION, "<union type definition>");
    r = consumeToken(b, UNION);
    p = r; // pin = 1
    r = r && report_error_(b, NamedTypeDef(b, l + 1));
    r = p && report_error_(b, consumeToken(b, EQUALS)) && r;
    r = p && UnionMemberSet(b, l + 1) && r;
    exit_section_(b, l, m, r, p, RecoverDefinition_parser_);
    return r || p;
  }

  final static Parser RecoverArgumentsDefinition_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return RecoverArgumentsDefinition(b, l + 1);
    }
  };
  final static Parser RecoverDefinition_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return RecoverDefinition(b, l + 1);
    }
  };
}
