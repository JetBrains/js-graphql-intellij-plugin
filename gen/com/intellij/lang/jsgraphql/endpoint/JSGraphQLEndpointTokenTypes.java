// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.endpoint;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.endpoint.psi.impl.*;

public interface JSGraphQLEndpointTokenTypes {

  IElementType ANNOTATION = new JSGraphQLEndpointTokenType("ANNOTATION");
  IElementType ANNOTATIONS = new JSGraphQLEndpointTokenType("ANNOTATIONS");
  IElementType ANNOTATION_ARGUMENTS = new JSGraphQLEndpointTokenType("ANNOTATION_ARGUMENTS");
  IElementType ANNOTATION_ARGUMENT_VALUE = new JSGraphQLEndpointTokenType("ANNOTATION_ARGUMENT_VALUE");
  IElementType ARGUMENTS_DEFINITION = new JSGraphQLEndpointTokenType("ARGUMENTS_DEFINITION");
  IElementType COMPOSITE_TYPE = new JSGraphQLEndpointTokenType("COMPOSITE_TYPE");
  IElementType ENUM_TYPE_DEFINITION = new JSGraphQLEndpointTokenType("ENUM_TYPE_DEFINITION");
  IElementType ENUM_VALUE_DEFINITION = new JSGraphQLEndpointTokenType("ENUM_VALUE_DEFINITION");
  IElementType ENUM_VALUE_DEFINITION_SET = new JSGraphQLEndpointTokenType("ENUM_VALUE_DEFINITION_SET");
  IElementType FIELD_DEFINITION = new JSGraphQLEndpointTokenType("FIELD_DEFINITION");
  IElementType FIELD_DEFINITION_SET = new JSGraphQLEndpointTokenType("FIELD_DEFINITION_SET");
  IElementType IMPLEMENTS_INTERFACES = new JSGraphQLEndpointTokenType("IMPLEMENTS_INTERFACES");
  IElementType IMPORT_DECLARATION = new JSGraphQLEndpointTokenType("IMPORT_DECLARATION");
  IElementType IMPORT_FILE_REFERENCE = new JSGraphQLEndpointTokenType("IMPORT_FILE_REFERENCE");
  IElementType INPUT_OBJECT_TYPE_DEFINITION = new JSGraphQLEndpointTokenType("INPUT_OBJECT_TYPE_DEFINITION");
  IElementType INPUT_VALUE_DEFINITION = new JSGraphQLEndpointTokenType("INPUT_VALUE_DEFINITION");
  IElementType INPUT_VALUE_DEFINITIONS = new JSGraphQLEndpointTokenType("INPUT_VALUE_DEFINITIONS");
  IElementType INPUT_VALUE_DEFINITION_IDENTIFIER = new JSGraphQLEndpointTokenType("INPUT_VALUE_DEFINITION_IDENTIFIER");
  IElementType INTERFACE_TYPE_DEFINITION = new JSGraphQLEndpointTokenType("INTERFACE_TYPE_DEFINITION");
  IElementType LIST_TYPE = new JSGraphQLEndpointTokenType("LIST_TYPE");
  IElementType NAMED_ANNOTATION_ARGUMENT = new JSGraphQLEndpointTokenType("NAMED_ANNOTATION_ARGUMENT");
  IElementType NAMED_ANNOTATION_ARGUMENTS = new JSGraphQLEndpointTokenType("NAMED_ANNOTATION_ARGUMENTS");
  IElementType NAMED_TYPE = new JSGraphQLEndpointTokenType("NAMED_TYPE");
  IElementType NAMED_TYPE_DEF = new JSGraphQLEndpointTokenType("NAMED_TYPE_DEF");
  IElementType OBJECT_TYPE_DEFINITION = new JSGraphQLEndpointTokenType("OBJECT_TYPE_DEFINITION");
  IElementType OPERATION_TYPE_DEFINITION = new JSGraphQLEndpointTokenType("OPERATION_TYPE_DEFINITION");
  IElementType OPERATION_TYPE_DEFINITION_SET = new JSGraphQLEndpointTokenType("OPERATION_TYPE_DEFINITION_SET");
  IElementType PROPERTY = new JSGraphQLEndpointTokenType("PROPERTY");
  IElementType QUOTED_STRING = new JSGraphQLEndpointTokenType("QUOTED_STRING");
  IElementType SCALAR_TYPE_DEFINITION = new JSGraphQLEndpointTokenType("SCALAR_TYPE_DEFINITION");
  IElementType SCHEMA_DEFINITION = new JSGraphQLEndpointTokenType("SCHEMA_DEFINITION");
  IElementType STRING = new JSGraphQLEndpointTokenType("STRING");
  IElementType UNION_MEMBER = new JSGraphQLEndpointTokenType("UNION_MEMBER");
  IElementType UNION_MEMBER_SET = new JSGraphQLEndpointTokenType("UNION_MEMBER_SET");
  IElementType UNION_TYPE_DEFINITION = new JSGraphQLEndpointTokenType("UNION_TYPE_DEFINITION");

  IElementType AT_ANNOTATION = new JSGraphQLEndpointTokenType("AT_ANNOTATION");
  IElementType CLOSING_QUOTE = new JSGraphQLEndpointTokenType("CLOSING_QUOTE");
  IElementType COLON = new JSGraphQLEndpointTokenType(":");
  IElementType COMMA = new JSGraphQLEndpointTokenType(",");
  IElementType ENUM = new JSGraphQLEndpointTokenType("enum");
  IElementType EQUALS = new JSGraphQLEndpointTokenType("=");
  IElementType FALSE = new JSGraphQLEndpointTokenType("false");
  IElementType IDENTIFIER = new JSGraphQLEndpointTokenType("identifier");
  IElementType IMPLEMENTS = new JSGraphQLEndpointTokenType("implements");
  IElementType IMPORT = new JSGraphQLEndpointTokenType("import");
  IElementType INPUT = new JSGraphQLEndpointTokenType("input");
  IElementType INTERFACE = new JSGraphQLEndpointTokenType("interface");
  IElementType LBRACE = new JSGraphQLEndpointTokenType("{");
  IElementType LBRACKET = new JSGraphQLEndpointTokenType("[");
  IElementType LINE_COMMENT = new JSGraphQLEndpointTokenType("LINE_COMMENT");
  IElementType LPAREN = new JSGraphQLEndpointTokenType("(");
  IElementType MUTATION = new JSGraphQLEndpointTokenType("mutation");
  IElementType NUMBER = new JSGraphQLEndpointTokenType("number");
  IElementType OPEN_QUOTE = new JSGraphQLEndpointTokenType("OPEN_QUOTE");
  IElementType PIPE = new JSGraphQLEndpointTokenType("|");
  IElementType QUERY = new JSGraphQLEndpointTokenType("query");
  IElementType RBRACE = new JSGraphQLEndpointTokenType("}");
  IElementType RBRACKET = new JSGraphQLEndpointTokenType("]");
  IElementType REQUIRED = new JSGraphQLEndpointTokenType("!");
  IElementType RPAREN = new JSGraphQLEndpointTokenType(")");
  IElementType SCALAR = new JSGraphQLEndpointTokenType("scalar");
  IElementType SCHEMA = new JSGraphQLEndpointTokenType("schema");
  IElementType STRING_BODY = new JSGraphQLEndpointTokenType("STRING_BODY");
  IElementType SUBSCRIPTION = new JSGraphQLEndpointTokenType("subscription");
  IElementType TRUE = new JSGraphQLEndpointTokenType("true");
  IElementType TYPE = new JSGraphQLEndpointTokenType("type");
  IElementType UNION = new JSGraphQLEndpointTokenType("union");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == ANNOTATION) {
        return new JSGraphQLEndpointAnnotationImpl(node);
      }
      else if (type == ANNOTATIONS) {
        return new JSGraphQLEndpointAnnotationsImpl(node);
      }
      else if (type == ANNOTATION_ARGUMENTS) {
        return new JSGraphQLEndpointAnnotationArgumentsImpl(node);
      }
      else if (type == ANNOTATION_ARGUMENT_VALUE) {
        return new JSGraphQLEndpointAnnotationArgumentValueImpl(node);
      }
      else if (type == ARGUMENTS_DEFINITION) {
        return new JSGraphQLEndpointArgumentsDefinitionImpl(node);
      }
      else if (type == COMPOSITE_TYPE) {
        return new JSGraphQLEndpointCompositeTypeImpl(node);
      }
      else if (type == ENUM_TYPE_DEFINITION) {
        return new JSGraphQLEndpointEnumTypeDefinitionImpl(node);
      }
      else if (type == ENUM_VALUE_DEFINITION) {
        return new JSGraphQLEndpointEnumValueDefinitionImpl(node);
      }
      else if (type == ENUM_VALUE_DEFINITION_SET) {
        return new JSGraphQLEndpointEnumValueDefinitionSetImpl(node);
      }
      else if (type == FIELD_DEFINITION) {
        return new JSGraphQLEndpointFieldDefinitionImpl(node);
      }
      else if (type == FIELD_DEFINITION_SET) {
        return new JSGraphQLEndpointFieldDefinitionSetImpl(node);
      }
      else if (type == IMPLEMENTS_INTERFACES) {
        return new JSGraphQLEndpointImplementsInterfacesImpl(node);
      }
      else if (type == IMPORT_DECLARATION) {
        return new JSGraphQLEndpointImportDeclarationImpl(node);
      }
      else if (type == IMPORT_FILE_REFERENCE) {
        return new JSGraphQLEndpointImportFileReferenceImpl(node);
      }
      else if (type == INPUT_OBJECT_TYPE_DEFINITION) {
        return new JSGraphQLEndpointInputObjectTypeDefinitionImpl(node);
      }
      else if (type == INPUT_VALUE_DEFINITION) {
        return new JSGraphQLEndpointInputValueDefinitionImpl(node);
      }
      else if (type == INPUT_VALUE_DEFINITIONS) {
        return new JSGraphQLEndpointInputValueDefinitionsImpl(node);
      }
      else if (type == INPUT_VALUE_DEFINITION_IDENTIFIER) {
        return new JSGraphQLEndpointInputValueDefinitionIdentifierImpl(node);
      }
      else if (type == INTERFACE_TYPE_DEFINITION) {
        return new JSGraphQLEndpointInterfaceTypeDefinitionImpl(node);
      }
      else if (type == LIST_TYPE) {
        return new JSGraphQLEndpointListTypeImpl(node);
      }
      else if (type == NAMED_ANNOTATION_ARGUMENT) {
        return new JSGraphQLEndpointNamedAnnotationArgumentImpl(node);
      }
      else if (type == NAMED_ANNOTATION_ARGUMENTS) {
        return new JSGraphQLEndpointNamedAnnotationArgumentsImpl(node);
      }
      else if (type == NAMED_TYPE) {
        return new JSGraphQLEndpointNamedTypeImpl(node);
      }
      else if (type == NAMED_TYPE_DEF) {
        return new JSGraphQLEndpointNamedTypeDefImpl(node);
      }
      else if (type == OBJECT_TYPE_DEFINITION) {
        return new JSGraphQLEndpointObjectTypeDefinitionImpl(node);
      }
      else if (type == OPERATION_TYPE_DEFINITION) {
        return new JSGraphQLEndpointOperationTypeDefinitionImpl(node);
      }
      else if (type == OPERATION_TYPE_DEFINITION_SET) {
        return new JSGraphQLEndpointOperationTypeDefinitionSetImpl(node);
      }
      else if (type == PROPERTY) {
        return new JSGraphQLEndpointPropertyImpl(node);
      }
      else if (type == QUOTED_STRING) {
        return new JSGraphQLEndpointQuotedStringImpl(node);
      }
      else if (type == SCALAR_TYPE_DEFINITION) {
        return new JSGraphQLEndpointScalarTypeDefinitionImpl(node);
      }
      else if (type == SCHEMA_DEFINITION) {
        return new JSGraphQLEndpointSchemaDefinitionImpl(node);
      }
      else if (type == STRING) {
        return new JSGraphQLEndpointStringImpl(node);
      }
      else if (type == UNION_MEMBER) {
        return new JSGraphQLEndpointUnionMemberImpl(node);
      }
      else if (type == UNION_MEMBER_SET) {
        return new JSGraphQLEndpointUnionMemberSetImpl(node);
      }
      else if (type == UNION_TYPE_DEFINITION) {
        return new JSGraphQLEndpointUnionTypeDefinitionImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
