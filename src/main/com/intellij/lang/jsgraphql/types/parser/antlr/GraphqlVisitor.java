// Generated from Graphql.g4 by ANTLR 4.8

    package com.intellij.lang.jsgraphql.types.parser.antlr;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link GraphqlParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface GraphqlVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#document}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocument(GraphqlParser.DocumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinition(GraphqlParser.DefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#typeSystemDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeSystemDefinition(GraphqlParser.TypeSystemDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#typeSystemExtension}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeSystemExtension(GraphqlParser.TypeSystemExtensionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#schemaDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaDefinition(GraphqlParser.SchemaDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#schemaExtension}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaExtension(GraphqlParser.SchemaExtensionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#operationTypeDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperationTypeDefinition(GraphqlParser.OperationTypeDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#typeDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDefinition(GraphqlParser.TypeDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#typeExtension}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeExtension(GraphqlParser.TypeExtensionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#emptyParentheses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyParentheses(GraphqlParser.EmptyParenthesesContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#scalarTypeDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScalarTypeDefinition(GraphqlParser.ScalarTypeDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#scalarTypeExtensionDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScalarTypeExtensionDefinition(GraphqlParser.ScalarTypeExtensionDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#objectTypeDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectTypeDefinition(GraphqlParser.ObjectTypeDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#objectTypeExtensionDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectTypeExtensionDefinition(GraphqlParser.ObjectTypeExtensionDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#implementsInterfaces}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImplementsInterfaces(GraphqlParser.ImplementsInterfacesContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#fieldsDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldsDefinition(GraphqlParser.FieldsDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#extensionFieldsDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtensionFieldsDefinition(GraphqlParser.ExtensionFieldsDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#fieldDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldDefinition(GraphqlParser.FieldDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#argumentsDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentsDefinition(GraphqlParser.ArgumentsDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#inputValueDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInputValueDefinition(GraphqlParser.InputValueDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#interfaceTypeDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceTypeDefinition(GraphqlParser.InterfaceTypeDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#interfaceTypeExtensionDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterfaceTypeExtensionDefinition(GraphqlParser.InterfaceTypeExtensionDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#unionTypeDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnionTypeDefinition(GraphqlParser.UnionTypeDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#unionTypeExtensionDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnionTypeExtensionDefinition(GraphqlParser.UnionTypeExtensionDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#unionMembership}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnionMembership(GraphqlParser.UnionMembershipContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#unionMembers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnionMembers(GraphqlParser.UnionMembersContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#enumTypeDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumTypeDefinition(GraphqlParser.EnumTypeDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#enumTypeExtensionDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumTypeExtensionDefinition(GraphqlParser.EnumTypeExtensionDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#enumValueDefinitions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumValueDefinitions(GraphqlParser.EnumValueDefinitionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#extensionEnumValueDefinitions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtensionEnumValueDefinitions(GraphqlParser.ExtensionEnumValueDefinitionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#enumValueDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumValueDefinition(GraphqlParser.EnumValueDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#inputObjectTypeDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInputObjectTypeDefinition(GraphqlParser.InputObjectTypeDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#inputObjectTypeExtensionDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInputObjectTypeExtensionDefinition(GraphqlParser.InputObjectTypeExtensionDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#inputObjectValueDefinitions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInputObjectValueDefinitions(GraphqlParser.InputObjectValueDefinitionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#extensionInputObjectValueDefinitions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtensionInputObjectValueDefinitions(GraphqlParser.ExtensionInputObjectValueDefinitionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#directiveDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirectiveDefinition(GraphqlParser.DirectiveDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#directiveLocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirectiveLocation(GraphqlParser.DirectiveLocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#directiveLocations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirectiveLocations(GraphqlParser.DirectiveLocationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#operationType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperationType(GraphqlParser.OperationTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#description}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescription(GraphqlParser.DescriptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#enumValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumValue(GraphqlParser.EnumValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#arrayValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayValue(GraphqlParser.ArrayValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#arrayValueWithVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayValueWithVariable(GraphqlParser.ArrayValueWithVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#objectValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectValue(GraphqlParser.ObjectValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#objectValueWithVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectValueWithVariable(GraphqlParser.ObjectValueWithVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#objectField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectField(GraphqlParser.ObjectFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#objectFieldWithVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectFieldWithVariable(GraphqlParser.ObjectFieldWithVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#directives}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirectives(GraphqlParser.DirectivesContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#directive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirective(GraphqlParser.DirectiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#arguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArguments(GraphqlParser.ArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#argument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgument(GraphqlParser.ArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#baseName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBaseName(GraphqlParser.BaseNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#fragmentName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFragmentName(GraphqlParser.FragmentNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#enumValueName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumValueName(GraphqlParser.EnumValueNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(GraphqlParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(GraphqlParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#valueWithVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueWithVariable(GraphqlParser.ValueWithVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(GraphqlParser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#defaultValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaultValue(GraphqlParser.DefaultValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(GraphqlParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeName(GraphqlParser.TypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#listType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListType(GraphqlParser.ListTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#nonNullType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonNullType(GraphqlParser.NonNullTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#operationDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperationDefinition(GraphqlParser.OperationDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#variableDefinitions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDefinitions(GraphqlParser.VariableDefinitionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#variableDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDefinition(GraphqlParser.VariableDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#selectionSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectionSet(GraphqlParser.SelectionSetContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#selection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelection(GraphqlParser.SelectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField(GraphqlParser.FieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(GraphqlParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#fragmentSpread}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFragmentSpread(GraphqlParser.FragmentSpreadContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#inlineFragment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInlineFragment(GraphqlParser.InlineFragmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#fragmentDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFragmentDefinition(GraphqlParser.FragmentDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link GraphqlParser#typeCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeCondition(GraphqlParser.TypeConditionContext ctx);
}
