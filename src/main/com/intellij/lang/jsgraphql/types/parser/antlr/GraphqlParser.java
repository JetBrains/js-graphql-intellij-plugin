// Generated from Graphql.g4 by ANTLR 4.8

    package com.intellij.lang.jsgraphql.types.parser.antlr;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class GraphqlParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9,
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, BooleanValue=15, NullValue=16,
		FRAGMENT=17, QUERY=18, MUTATION=19, SUBSCRIPTION=20, SCHEMA=21, SCALAR=22,
		TYPE=23, INTERFACE=24, IMPLEMENTS=25, ENUM=26, UNION=27, INPUT=28, EXTEND=29,
		DIRECTIVE=30, ON_KEYWORD=31, REPEATABLE=32, NAME=33, IntValue=34, FloatValue=35,
		StringValue=36, Comment=37, LF=38, CR=39, LineTerminator=40, Space=41,
		Tab=42, Comma=43, UnicodeBOM=44;
	public static final int
		RULE_document = 0, RULE_definition = 1, RULE_typeSystemDefinition = 2,
		RULE_typeSystemExtension = 3, RULE_schemaDefinition = 4, RULE_schemaExtension = 5,
		RULE_operationTypeDefinition = 6, RULE_typeDefinition = 7, RULE_typeExtension = 8,
		RULE_emptyParentheses = 9, RULE_scalarTypeDefinition = 10, RULE_scalarTypeExtensionDefinition = 11,
		RULE_objectTypeDefinition = 12, RULE_objectTypeExtensionDefinition = 13,
		RULE_implementsInterfaces = 14, RULE_fieldsDefinition = 15, RULE_extensionFieldsDefinition = 16,
		RULE_fieldDefinition = 17, RULE_argumentsDefinition = 18, RULE_inputValueDefinition = 19,
		RULE_interfaceTypeDefinition = 20, RULE_interfaceTypeExtensionDefinition = 21,
		RULE_unionTypeDefinition = 22, RULE_unionTypeExtensionDefinition = 23,
		RULE_unionMembership = 24, RULE_unionMembers = 25, RULE_enumTypeDefinition = 26,
		RULE_enumTypeExtensionDefinition = 27, RULE_enumValueDefinitions = 28,
		RULE_extensionEnumValueDefinitions = 29, RULE_enumValueDefinition = 30,
		RULE_inputObjectTypeDefinition = 31, RULE_inputObjectTypeExtensionDefinition = 32,
		RULE_inputObjectValueDefinitions = 33, RULE_extensionInputObjectValueDefinitions = 34,
		RULE_directiveDefinition = 35, RULE_directiveLocation = 36, RULE_directiveLocations = 37,
		RULE_operationType = 38, RULE_description = 39, RULE_enumValue = 40, RULE_arrayValue = 41,
		RULE_arrayValueWithVariable = 42, RULE_objectValue = 43, RULE_objectValueWithVariable = 44,
		RULE_objectField = 45, RULE_objectFieldWithVariable = 46, RULE_directives = 47,
		RULE_directive = 48, RULE_arguments = 49, RULE_argument = 50, RULE_baseName = 51,
		RULE_fragmentName = 52, RULE_enumValueName = 53, RULE_name = 54, RULE_value = 55,
		RULE_valueWithVariable = 56, RULE_variable = 57, RULE_defaultValue = 58,
		RULE_type = 59, RULE_typeName = 60, RULE_listType = 61, RULE_nonNullType = 62,
		RULE_operationDefinition = 63, RULE_variableDefinitions = 64, RULE_variableDefinition = 65,
		RULE_selectionSet = 66, RULE_selection = 67, RULE_field = 68, RULE_alias = 69,
		RULE_fragmentSpread = 70, RULE_inlineFragment = 71, RULE_fragmentDefinition = 72,
		RULE_typeCondition = 73;
	private static String[] makeRuleNames() {
		return new String[] {
			"document", "definition", "typeSystemDefinition", "typeSystemExtension",
			"schemaDefinition", "schemaExtension", "operationTypeDefinition", "typeDefinition",
			"typeExtension", "emptyParentheses", "scalarTypeDefinition", "scalarTypeExtensionDefinition",
			"objectTypeDefinition", "objectTypeExtensionDefinition", "implementsInterfaces",
			"fieldsDefinition", "extensionFieldsDefinition", "fieldDefinition", "argumentsDefinition",
			"inputValueDefinition", "interfaceTypeDefinition", "interfaceTypeExtensionDefinition",
			"unionTypeDefinition", "unionTypeExtensionDefinition", "unionMembership",
			"unionMembers", "enumTypeDefinition", "enumTypeExtensionDefinition",
			"enumValueDefinitions", "extensionEnumValueDefinitions", "enumValueDefinition",
			"inputObjectTypeDefinition", "inputObjectTypeExtensionDefinition", "inputObjectValueDefinitions",
			"extensionInputObjectValueDefinitions", "directiveDefinition", "directiveLocation",
			"directiveLocations", "operationType", "description", "enumValue", "arrayValue",
			"arrayValueWithVariable", "objectValue", "objectValueWithVariable", "objectField",
			"objectFieldWithVariable", "directives", "directive", "arguments", "argument",
			"baseName", "fragmentName", "enumValueName", "name", "value", "valueWithVariable",
			"variable", "defaultValue", "type", "typeName", "listType", "nonNullType",
			"operationDefinition", "variableDefinitions", "variableDefinition", "selectionSet",
			"selection", "field", "alias", "fragmentSpread", "inlineFragment", "fragmentDefinition",
			"typeCondition"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "'}'", "':'", "'&'", "'('", "')'", "'='", "'|'", "'@'",
			"'['", "']'", "'$'", "'!'", "'...'", null, "'null'", "'fragment'", "'query'",
			"'mutation'", "'subscription'", "'schema'", "'scalar'", "'type'", "'interface'",
			"'implements'", "'enum'", "'union'", "'input'", "'extend'", "'directive'",
			"'on'", "'repeatable'", null, null, null, null, null, null, null, null,
			null, null, "','"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, "BooleanValue", "NullValue", "FRAGMENT", "QUERY", "MUTATION",
			"SUBSCRIPTION", "SCHEMA", "SCALAR", "TYPE", "INTERFACE", "IMPLEMENTS",
			"ENUM", "UNION", "INPUT", "EXTEND", "DIRECTIVE", "ON_KEYWORD", "REPEATABLE",
			"NAME", "IntValue", "FloatValue", "StringValue", "Comment", "LF", "CR",
			"LineTerminator", "Space", "Tab", "Comma", "UnicodeBOM"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Graphql.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public GraphqlParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class DocumentContext extends ParserRuleContext {
		public List<DefinitionContext> definition() {
			return getRuleContexts(DefinitionContext.class);
		}
		public DefinitionContext definition(int i) {
			return getRuleContext(DefinitionContext.class,i);
		}
		public DocumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_document; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterDocument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitDocument(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitDocument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DocumentContext document() throws RecognitionException {
		DocumentContext _localctx = new DocumentContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_document);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(149);
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(148);
				definition();
				}
				}
				setState(151);
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << FRAGMENT) | (1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << SCHEMA) | (1L << SCALAR) | (1L << TYPE) | (1L << INTERFACE) | (1L << ENUM) | (1L << UNION) | (1L << INPUT) | (1L << EXTEND) | (1L << DIRECTIVE) | (1L << StringValue))) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DefinitionContext extends ParserRuleContext {
		public OperationDefinitionContext operationDefinition() {
			return getRuleContext(OperationDefinitionContext.class,0);
		}
		public FragmentDefinitionContext fragmentDefinition() {
			return getRuleContext(FragmentDefinitionContext.class,0);
		}
		public TypeSystemDefinitionContext typeSystemDefinition() {
			return getRuleContext(TypeSystemDefinitionContext.class,0);
		}
		public TypeSystemExtensionContext typeSystemExtension() {
			return getRuleContext(TypeSystemExtensionContext.class,0);
		}
		public DefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefinitionContext definition() throws RecognitionException {
		DefinitionContext _localctx = new DefinitionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_definition);
		try {
			setState(157);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case QUERY:
			case MUTATION:
			case SUBSCRIPTION:
				enterOuterAlt(_localctx, 1);
				{
				setState(153);
				operationDefinition();
				}
				break;
			case FRAGMENT:
				enterOuterAlt(_localctx, 2);
				{
				setState(154);
				fragmentDefinition();
				}
				break;
			case SCHEMA:
			case SCALAR:
			case TYPE:
			case INTERFACE:
			case ENUM:
			case UNION:
			case INPUT:
			case DIRECTIVE:
			case StringValue:
				enterOuterAlt(_localctx, 3);
				{
				setState(155);
				typeSystemDefinition();
				}
				break;
			case EXTEND:
				enterOuterAlt(_localctx, 4);
				{
				setState(156);
				typeSystemExtension();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeSystemDefinitionContext extends ParserRuleContext {
		public SchemaDefinitionContext schemaDefinition() {
			return getRuleContext(SchemaDefinitionContext.class,0);
		}
		public TypeDefinitionContext typeDefinition() {
			return getRuleContext(TypeDefinitionContext.class,0);
		}
		public DirectiveDefinitionContext directiveDefinition() {
			return getRuleContext(DirectiveDefinitionContext.class,0);
		}
		public TypeSystemDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeSystemDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterTypeSystemDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitTypeSystemDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitTypeSystemDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeSystemDefinitionContext typeSystemDefinition() throws RecognitionException {
		TypeSystemDefinitionContext _localctx = new TypeSystemDefinitionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_typeSystemDefinition);
		try {
			setState(162);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(159);
				schemaDefinition();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(160);
				typeDefinition();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(161);
				directiveDefinition();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeSystemExtensionContext extends ParserRuleContext {
		public SchemaExtensionContext schemaExtension() {
			return getRuleContext(SchemaExtensionContext.class,0);
		}
		public TypeExtensionContext typeExtension() {
			return getRuleContext(TypeExtensionContext.class,0);
		}
		public TypeSystemExtensionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeSystemExtension; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterTypeSystemExtension(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitTypeSystemExtension(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitTypeSystemExtension(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeSystemExtensionContext typeSystemExtension() throws RecognitionException {
		TypeSystemExtensionContext _localctx = new TypeSystemExtensionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_typeSystemExtension);
		try {
			setState(166);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(164);
				schemaExtension();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(165);
				typeExtension();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SchemaDefinitionContext extends ParserRuleContext {
		public TerminalNode SCHEMA() { return getToken(GraphqlParser.SCHEMA, 0); }
		public DescriptionContext description() {
			return getRuleContext(DescriptionContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public List<OperationTypeDefinitionContext> operationTypeDefinition() {
			return getRuleContexts(OperationTypeDefinitionContext.class);
		}
		public OperationTypeDefinitionContext operationTypeDefinition(int i) {
			return getRuleContext(OperationTypeDefinitionContext.class,i);
		}
		public SchemaDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_schemaDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterSchemaDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitSchemaDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitSchemaDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SchemaDefinitionContext schemaDefinition() throws RecognitionException {
		SchemaDefinitionContext _localctx = new SchemaDefinitionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_schemaDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(169);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==StringValue) {
				{
				setState(168);
				description();
				}
			}

			setState(171);
			match(SCHEMA);
			setState(173);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(172);
				directives();
				}
			}

			setState(175);
			match(T__0);
			setState(177);
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(176);
				operationTypeDefinition();
				}
				}
				setState(179);
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << StringValue))) != 0) );
			setState(181);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SchemaExtensionContext extends ParserRuleContext {
		public TerminalNode EXTEND() { return getToken(GraphqlParser.EXTEND, 0); }
		public TerminalNode SCHEMA() { return getToken(GraphqlParser.SCHEMA, 0); }
		public List<DirectivesContext> directives() {
			return getRuleContexts(DirectivesContext.class);
		}
		public DirectivesContext directives(int i) {
			return getRuleContext(DirectivesContext.class,i);
		}
		public List<OperationTypeDefinitionContext> operationTypeDefinition() {
			return getRuleContexts(OperationTypeDefinitionContext.class);
		}
		public OperationTypeDefinitionContext operationTypeDefinition(int i) {
			return getRuleContext(OperationTypeDefinitionContext.class,i);
		}
		public SchemaExtensionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_schemaExtension; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterSchemaExtension(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitSchemaExtension(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitSchemaExtension(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SchemaExtensionContext schemaExtension() throws RecognitionException {
		SchemaExtensionContext _localctx = new SchemaExtensionContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_schemaExtension);
		int _la;
		try {
			setState(203);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(183);
				match(EXTEND);
				setState(184);
				match(SCHEMA);
				setState(186);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__8) {
					{
					setState(185);
					directives();
					}
				}

				setState(188);
				match(T__0);
				setState(190);
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(189);
					operationTypeDefinition();
					}
					}
					setState(192);
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << StringValue))) != 0) );
				setState(194);
				match(T__1);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(196);
				match(EXTEND);
				setState(197);
				match(SCHEMA);
				setState(199);
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(198);
					directives();
					}
					}
					setState(201);
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==T__8 );
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OperationTypeDefinitionContext extends ParserRuleContext {
		public OperationTypeContext operationType() {
			return getRuleContext(OperationTypeContext.class,0);
		}
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public DescriptionContext description() {
			return getRuleContext(DescriptionContext.class,0);
		}
		public OperationTypeDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operationTypeDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterOperationTypeDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitOperationTypeDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitOperationTypeDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperationTypeDefinitionContext operationTypeDefinition() throws RecognitionException {
		OperationTypeDefinitionContext _localctx = new OperationTypeDefinitionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_operationTypeDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(206);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==StringValue) {
				{
				setState(205);
				description();
				}
			}

			setState(208);
			operationType();
			setState(209);
			match(T__2);
			setState(210);
			typeName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeDefinitionContext extends ParserRuleContext {
		public ScalarTypeDefinitionContext scalarTypeDefinition() {
			return getRuleContext(ScalarTypeDefinitionContext.class,0);
		}
		public ObjectTypeDefinitionContext objectTypeDefinition() {
			return getRuleContext(ObjectTypeDefinitionContext.class,0);
		}
		public InterfaceTypeDefinitionContext interfaceTypeDefinition() {
			return getRuleContext(InterfaceTypeDefinitionContext.class,0);
		}
		public UnionTypeDefinitionContext unionTypeDefinition() {
			return getRuleContext(UnionTypeDefinitionContext.class,0);
		}
		public EnumTypeDefinitionContext enumTypeDefinition() {
			return getRuleContext(EnumTypeDefinitionContext.class,0);
		}
		public InputObjectTypeDefinitionContext inputObjectTypeDefinition() {
			return getRuleContext(InputObjectTypeDefinitionContext.class,0);
		}
		public TypeDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterTypeDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitTypeDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitTypeDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeDefinitionContext typeDefinition() throws RecognitionException {
		TypeDefinitionContext _localctx = new TypeDefinitionContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_typeDefinition);
		try {
			setState(218);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(212);
				scalarTypeDefinition();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(213);
				objectTypeDefinition();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(214);
				interfaceTypeDefinition();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(215);
				unionTypeDefinition();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(216);
				enumTypeDefinition();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(217);
				inputObjectTypeDefinition();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeExtensionContext extends ParserRuleContext {
		public ObjectTypeExtensionDefinitionContext objectTypeExtensionDefinition() {
			return getRuleContext(ObjectTypeExtensionDefinitionContext.class,0);
		}
		public InterfaceTypeExtensionDefinitionContext interfaceTypeExtensionDefinition() {
			return getRuleContext(InterfaceTypeExtensionDefinitionContext.class,0);
		}
		public UnionTypeExtensionDefinitionContext unionTypeExtensionDefinition() {
			return getRuleContext(UnionTypeExtensionDefinitionContext.class,0);
		}
		public ScalarTypeExtensionDefinitionContext scalarTypeExtensionDefinition() {
			return getRuleContext(ScalarTypeExtensionDefinitionContext.class,0);
		}
		public EnumTypeExtensionDefinitionContext enumTypeExtensionDefinition() {
			return getRuleContext(EnumTypeExtensionDefinitionContext.class,0);
		}
		public InputObjectTypeExtensionDefinitionContext inputObjectTypeExtensionDefinition() {
			return getRuleContext(InputObjectTypeExtensionDefinitionContext.class,0);
		}
		public TypeExtensionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeExtension; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterTypeExtension(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitTypeExtension(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitTypeExtension(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeExtensionContext typeExtension() throws RecognitionException {
		TypeExtensionContext _localctx = new TypeExtensionContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_typeExtension);
		try {
			setState(226);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(220);
				objectTypeExtensionDefinition();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(221);
				interfaceTypeExtensionDefinition();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(222);
				unionTypeExtensionDefinition();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(223);
				scalarTypeExtensionDefinition();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(224);
				enumTypeExtensionDefinition();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(225);
				inputObjectTypeExtensionDefinition();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EmptyParenthesesContext extends ParserRuleContext {
		public EmptyParenthesesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_emptyParentheses; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterEmptyParentheses(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitEmptyParentheses(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitEmptyParentheses(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EmptyParenthesesContext emptyParentheses() throws RecognitionException {
		EmptyParenthesesContext _localctx = new EmptyParenthesesContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_emptyParentheses);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(228);
			match(T__0);
			setState(229);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ScalarTypeDefinitionContext extends ParserRuleContext {
		public TerminalNode SCALAR() { return getToken(GraphqlParser.SCALAR, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public DescriptionContext description() {
			return getRuleContext(DescriptionContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public ScalarTypeDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_scalarTypeDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterScalarTypeDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitScalarTypeDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitScalarTypeDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ScalarTypeDefinitionContext scalarTypeDefinition() throws RecognitionException {
		ScalarTypeDefinitionContext _localctx = new ScalarTypeDefinitionContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_scalarTypeDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(232);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==StringValue) {
				{
				setState(231);
				description();
				}
			}

			setState(234);
			match(SCALAR);
			setState(235);
			name();
			setState(237);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(236);
				directives();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ScalarTypeExtensionDefinitionContext extends ParserRuleContext {
		public TerminalNode EXTEND() { return getToken(GraphqlParser.EXTEND, 0); }
		public TerminalNode SCALAR() { return getToken(GraphqlParser.SCALAR, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public ScalarTypeExtensionDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_scalarTypeExtensionDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterScalarTypeExtensionDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitScalarTypeExtensionDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitScalarTypeExtensionDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ScalarTypeExtensionDefinitionContext scalarTypeExtensionDefinition() throws RecognitionException {
		ScalarTypeExtensionDefinitionContext _localctx = new ScalarTypeExtensionDefinitionContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_scalarTypeExtensionDefinition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(239);
			match(EXTEND);
			setState(240);
			match(SCALAR);
			setState(241);
			name();
			setState(242);
			directives();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ObjectTypeDefinitionContext extends ParserRuleContext {
		public TerminalNode TYPE() { return getToken(GraphqlParser.TYPE, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public DescriptionContext description() {
			return getRuleContext(DescriptionContext.class,0);
		}
		public ImplementsInterfacesContext implementsInterfaces() {
			return getRuleContext(ImplementsInterfacesContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public FieldsDefinitionContext fieldsDefinition() {
			return getRuleContext(FieldsDefinitionContext.class,0);
		}
		public ObjectTypeDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectTypeDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterObjectTypeDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitObjectTypeDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitObjectTypeDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectTypeDefinitionContext objectTypeDefinition() throws RecognitionException {
		ObjectTypeDefinitionContext _localctx = new ObjectTypeDefinitionContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_objectTypeDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(245);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==StringValue) {
				{
				setState(244);
				description();
				}
			}

			setState(247);
			match(TYPE);
			setState(248);
			name();
			setState(250);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IMPLEMENTS) {
				{
				setState(249);
				implementsInterfaces(0);
				}
			}

			setState(253);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(252);
				directives();
				}
			}

			setState(256);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				{
				setState(255);
				fieldsDefinition();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ObjectTypeExtensionDefinitionContext extends ParserRuleContext {
		public TerminalNode EXTEND() { return getToken(GraphqlParser.EXTEND, 0); }
		public TerminalNode TYPE() { return getToken(GraphqlParser.TYPE, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public ExtensionFieldsDefinitionContext extensionFieldsDefinition() {
			return getRuleContext(ExtensionFieldsDefinitionContext.class,0);
		}
		public ImplementsInterfacesContext implementsInterfaces() {
			return getRuleContext(ImplementsInterfacesContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public EmptyParenthesesContext emptyParentheses() {
			return getRuleContext(EmptyParenthesesContext.class,0);
		}
		public ObjectTypeExtensionDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectTypeExtensionDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterObjectTypeExtensionDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitObjectTypeExtensionDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitObjectTypeExtensionDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectTypeExtensionDefinitionContext objectTypeExtensionDefinition() throws RecognitionException {
		ObjectTypeExtensionDefinitionContext _localctx = new ObjectTypeExtensionDefinitionContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_objectTypeExtensionDefinition);
		int _la;
		try {
			setState(284);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(258);
				match(EXTEND);
				setState(259);
				match(TYPE);
				setState(260);
				name();
				setState(262);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IMPLEMENTS) {
					{
					setState(261);
					implementsInterfaces(0);
					}
				}

				setState(265);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__8) {
					{
					setState(264);
					directives();
					}
				}

				setState(267);
				extensionFieldsDefinition();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(269);
				match(EXTEND);
				setState(270);
				match(TYPE);
				setState(271);
				name();
				setState(273);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IMPLEMENTS) {
					{
					setState(272);
					implementsInterfaces(0);
					}
				}

				setState(275);
				directives();
				setState(277);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
				case 1:
					{
					setState(276);
					emptyParentheses();
					}
					break;
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(279);
				match(EXTEND);
				setState(280);
				match(TYPE);
				setState(281);
				name();
				setState(282);
				implementsInterfaces(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ImplementsInterfacesContext extends ParserRuleContext {
		public TerminalNode IMPLEMENTS() { return getToken(GraphqlParser.IMPLEMENTS, 0); }
		public List<TypeNameContext> typeName() {
			return getRuleContexts(TypeNameContext.class);
		}
		public TypeNameContext typeName(int i) {
			return getRuleContext(TypeNameContext.class,i);
		}
		public ImplementsInterfacesContext implementsInterfaces() {
			return getRuleContext(ImplementsInterfacesContext.class,0);
		}
		public ImplementsInterfacesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_implementsInterfaces; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterImplementsInterfaces(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitImplementsInterfaces(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitImplementsInterfaces(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImplementsInterfacesContext implementsInterfaces() throws RecognitionException {
		return implementsInterfaces(0);
	}

	private ImplementsInterfacesContext implementsInterfaces(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ImplementsInterfacesContext _localctx = new ImplementsInterfacesContext(_ctx, _parentState);
		ImplementsInterfacesContext _prevctx = _localctx;
		int _startState = 28;
		enterRecursionRule(_localctx, 28, RULE_implementsInterfaces, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(287);
			match(IMPLEMENTS);
			setState(289);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(288);
				match(T__3);
				}
			}

			setState(292);
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(291);
					typeName();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(294);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
			} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
			}
			_ctx.stop = _input.LT(-1);
			setState(301);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ImplementsInterfacesContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_implementsInterfaces);
					setState(296);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(297);
					match(T__3);
					setState(298);
					typeName();
					}
					}
				}
				setState(303);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class FieldsDefinitionContext extends ParserRuleContext {
		public List<FieldDefinitionContext> fieldDefinition() {
			return getRuleContexts(FieldDefinitionContext.class);
		}
		public FieldDefinitionContext fieldDefinition(int i) {
			return getRuleContext(FieldDefinitionContext.class,i);
		}
		public FieldsDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldsDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterFieldsDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitFieldsDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitFieldsDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldsDefinitionContext fieldsDefinition() throws RecognitionException {
		FieldsDefinitionContext _localctx = new FieldsDefinitionContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_fieldsDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(304);
			match(T__0);
			setState(308);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BooleanValue) | (1L << NullValue) | (1L << FRAGMENT) | (1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << SCHEMA) | (1L << SCALAR) | (1L << TYPE) | (1L << INTERFACE) | (1L << IMPLEMENTS) | (1L << ENUM) | (1L << UNION) | (1L << INPUT) | (1L << EXTEND) | (1L << DIRECTIVE) | (1L << ON_KEYWORD) | (1L << REPEATABLE) | (1L << NAME) | (1L << StringValue))) != 0)) {
				{
				{
				setState(305);
				fieldDefinition();
				}
				}
				setState(310);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(311);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtensionFieldsDefinitionContext extends ParserRuleContext {
		public List<FieldDefinitionContext> fieldDefinition() {
			return getRuleContexts(FieldDefinitionContext.class);
		}
		public FieldDefinitionContext fieldDefinition(int i) {
			return getRuleContext(FieldDefinitionContext.class,i);
		}
		public ExtensionFieldsDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extensionFieldsDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterExtensionFieldsDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitExtensionFieldsDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitExtensionFieldsDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExtensionFieldsDefinitionContext extensionFieldsDefinition() throws RecognitionException {
		ExtensionFieldsDefinitionContext _localctx = new ExtensionFieldsDefinitionContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_extensionFieldsDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(313);
			match(T__0);
			setState(315);
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(314);
				fieldDefinition();
				}
				}
				setState(317);
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BooleanValue) | (1L << NullValue) | (1L << FRAGMENT) | (1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << SCHEMA) | (1L << SCALAR) | (1L << TYPE) | (1L << INTERFACE) | (1L << IMPLEMENTS) | (1L << ENUM) | (1L << UNION) | (1L << INPUT) | (1L << EXTEND) | (1L << DIRECTIVE) | (1L << ON_KEYWORD) | (1L << REPEATABLE) | (1L << NAME) | (1L << StringValue))) != 0) );
			setState(319);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FieldDefinitionContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public DescriptionContext description() {
			return getRuleContext(DescriptionContext.class,0);
		}
		public ArgumentsDefinitionContext argumentsDefinition() {
			return getRuleContext(ArgumentsDefinitionContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public FieldDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterFieldDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitFieldDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitFieldDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldDefinitionContext fieldDefinition() throws RecognitionException {
		FieldDefinitionContext _localctx = new FieldDefinitionContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_fieldDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(322);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==StringValue) {
				{
				setState(321);
				description();
				}
			}

			setState(324);
			name();
			setState(326);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__4) {
				{
				setState(325);
				argumentsDefinition();
				}
			}

			setState(328);
			match(T__2);
			setState(329);
			type();
			setState(331);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(330);
				directives();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArgumentsDefinitionContext extends ParserRuleContext {
		public List<InputValueDefinitionContext> inputValueDefinition() {
			return getRuleContexts(InputValueDefinitionContext.class);
		}
		public InputValueDefinitionContext inputValueDefinition(int i) {
			return getRuleContext(InputValueDefinitionContext.class,i);
		}
		public ArgumentsDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_argumentsDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterArgumentsDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitArgumentsDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitArgumentsDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgumentsDefinitionContext argumentsDefinition() throws RecognitionException {
		ArgumentsDefinitionContext _localctx = new ArgumentsDefinitionContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_argumentsDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(333);
			match(T__4);
			setState(335);
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(334);
				inputValueDefinition();
				}
				}
				setState(337);
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BooleanValue) | (1L << NullValue) | (1L << FRAGMENT) | (1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << SCHEMA) | (1L << SCALAR) | (1L << TYPE) | (1L << INTERFACE) | (1L << IMPLEMENTS) | (1L << ENUM) | (1L << UNION) | (1L << INPUT) | (1L << EXTEND) | (1L << DIRECTIVE) | (1L << ON_KEYWORD) | (1L << REPEATABLE) | (1L << NAME) | (1L << StringValue))) != 0) );
			setState(339);
			match(T__5);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InputValueDefinitionContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public DescriptionContext description() {
			return getRuleContext(DescriptionContext.class,0);
		}
		public DefaultValueContext defaultValue() {
			return getRuleContext(DefaultValueContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public InputValueDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inputValueDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterInputValueDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitInputValueDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitInputValueDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InputValueDefinitionContext inputValueDefinition() throws RecognitionException {
		InputValueDefinitionContext _localctx = new InputValueDefinitionContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_inputValueDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(342);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==StringValue) {
				{
				setState(341);
				description();
				}
			}

			setState(344);
			name();
			setState(345);
			match(T__2);
			setState(346);
			type();
			setState(348);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__6) {
				{
				setState(347);
				defaultValue();
				}
			}

			setState(351);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(350);
				directives();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InterfaceTypeDefinitionContext extends ParserRuleContext {
		public TerminalNode INTERFACE() { return getToken(GraphqlParser.INTERFACE, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public DescriptionContext description() {
			return getRuleContext(DescriptionContext.class,0);
		}
		public ImplementsInterfacesContext implementsInterfaces() {
			return getRuleContext(ImplementsInterfacesContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public FieldsDefinitionContext fieldsDefinition() {
			return getRuleContext(FieldsDefinitionContext.class,0);
		}
		public InterfaceTypeDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceTypeDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterInterfaceTypeDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitInterfaceTypeDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitInterfaceTypeDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceTypeDefinitionContext interfaceTypeDefinition() throws RecognitionException {
		InterfaceTypeDefinitionContext _localctx = new InterfaceTypeDefinitionContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_interfaceTypeDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(354);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==StringValue) {
				{
				setState(353);
				description();
				}
			}

			setState(356);
			match(INTERFACE);
			setState(357);
			name();
			setState(359);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IMPLEMENTS) {
				{
				setState(358);
				implementsInterfaces(0);
				}
			}

			setState(362);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(361);
				directives();
				}
			}

			setState(365);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
			case 1:
				{
				setState(364);
				fieldsDefinition();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InterfaceTypeExtensionDefinitionContext extends ParserRuleContext {
		public TerminalNode EXTEND() { return getToken(GraphqlParser.EXTEND, 0); }
		public TerminalNode INTERFACE() { return getToken(GraphqlParser.INTERFACE, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public ExtensionFieldsDefinitionContext extensionFieldsDefinition() {
			return getRuleContext(ExtensionFieldsDefinitionContext.class,0);
		}
		public ImplementsInterfacesContext implementsInterfaces() {
			return getRuleContext(ImplementsInterfacesContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public EmptyParenthesesContext emptyParentheses() {
			return getRuleContext(EmptyParenthesesContext.class,0);
		}
		public InterfaceTypeExtensionDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceTypeExtensionDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterInterfaceTypeExtensionDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitInterfaceTypeExtensionDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitInterfaceTypeExtensionDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceTypeExtensionDefinitionContext interfaceTypeExtensionDefinition() throws RecognitionException {
		InterfaceTypeExtensionDefinitionContext _localctx = new InterfaceTypeExtensionDefinitionContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_interfaceTypeExtensionDefinition);
		int _la;
		try {
			setState(393);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(367);
				match(EXTEND);
				setState(368);
				match(INTERFACE);
				setState(369);
				name();
				setState(371);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IMPLEMENTS) {
					{
					setState(370);
					implementsInterfaces(0);
					}
				}

				setState(374);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__8) {
					{
					setState(373);
					directives();
					}
				}

				setState(376);
				extensionFieldsDefinition();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(378);
				match(EXTEND);
				setState(379);
				match(INTERFACE);
				setState(380);
				name();
				setState(382);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IMPLEMENTS) {
					{
					setState(381);
					implementsInterfaces(0);
					}
				}

				setState(384);
				directives();
				setState(386);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
				case 1:
					{
					setState(385);
					emptyParentheses();
					}
					break;
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(388);
				match(EXTEND);
				setState(389);
				match(INTERFACE);
				setState(390);
				name();
				setState(391);
				implementsInterfaces(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnionTypeDefinitionContext extends ParserRuleContext {
		public TerminalNode UNION() { return getToken(GraphqlParser.UNION, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public DescriptionContext description() {
			return getRuleContext(DescriptionContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public UnionMembershipContext unionMembership() {
			return getRuleContext(UnionMembershipContext.class,0);
		}
		public UnionTypeDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unionTypeDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterUnionTypeDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitUnionTypeDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitUnionTypeDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnionTypeDefinitionContext unionTypeDefinition() throws RecognitionException {
		UnionTypeDefinitionContext _localctx = new UnionTypeDefinitionContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_unionTypeDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(396);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==StringValue) {
				{
				setState(395);
				description();
				}
			}

			setState(398);
			match(UNION);
			setState(399);
			name();
			setState(401);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(400);
				directives();
				}
			}

			setState(404);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__6) {
				{
				setState(403);
				unionMembership();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnionTypeExtensionDefinitionContext extends ParserRuleContext {
		public TerminalNode EXTEND() { return getToken(GraphqlParser.EXTEND, 0); }
		public TerminalNode UNION() { return getToken(GraphqlParser.UNION, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public UnionMembershipContext unionMembership() {
			return getRuleContext(UnionMembershipContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public UnionTypeExtensionDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unionTypeExtensionDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterUnionTypeExtensionDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitUnionTypeExtensionDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitUnionTypeExtensionDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnionTypeExtensionDefinitionContext unionTypeExtensionDefinition() throws RecognitionException {
		UnionTypeExtensionDefinitionContext _localctx = new UnionTypeExtensionDefinitionContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_unionTypeExtensionDefinition);
		int _la;
		try {
			setState(419);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(406);
				match(EXTEND);
				setState(407);
				match(UNION);
				setState(408);
				name();
				setState(410);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__8) {
					{
					setState(409);
					directives();
					}
				}

				setState(412);
				unionMembership();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(414);
				match(EXTEND);
				setState(415);
				match(UNION);
				setState(416);
				name();
				setState(417);
				directives();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnionMembershipContext extends ParserRuleContext {
		public UnionMembersContext unionMembers() {
			return getRuleContext(UnionMembersContext.class,0);
		}
		public UnionMembershipContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unionMembership; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterUnionMembership(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitUnionMembership(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitUnionMembership(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnionMembershipContext unionMembership() throws RecognitionException {
		UnionMembershipContext _localctx = new UnionMembershipContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_unionMembership);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(421);
			match(T__6);
			setState(422);
			unionMembers(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnionMembersContext extends ParserRuleContext {
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public UnionMembersContext unionMembers() {
			return getRuleContext(UnionMembersContext.class,0);
		}
		public UnionMembersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unionMembers; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterUnionMembers(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitUnionMembers(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitUnionMembers(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnionMembersContext unionMembers() throws RecognitionException {
		return unionMembers(0);
	}

	private UnionMembersContext unionMembers(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		UnionMembersContext _localctx = new UnionMembersContext(_ctx, _parentState);
		UnionMembersContext _prevctx = _localctx;
		int _startState = 50;
		enterRecursionRule(_localctx, 50, RULE_unionMembers, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(426);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__7) {
				{
				setState(425);
				match(T__7);
				}
			}

			setState(428);
			typeName();
			}
			_ctx.stop = _input.LT(-1);
			setState(435);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,52,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new UnionMembersContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_unionMembers);
					setState(430);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(431);
					match(T__7);
					setState(432);
					typeName();
					}
					}
				}
				setState(437);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,52,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class EnumTypeDefinitionContext extends ParserRuleContext {
		public TerminalNode ENUM() { return getToken(GraphqlParser.ENUM, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public DescriptionContext description() {
			return getRuleContext(DescriptionContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public EnumValueDefinitionsContext enumValueDefinitions() {
			return getRuleContext(EnumValueDefinitionsContext.class,0);
		}
		public EnumTypeDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumTypeDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterEnumTypeDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitEnumTypeDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitEnumTypeDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumTypeDefinitionContext enumTypeDefinition() throws RecognitionException {
		EnumTypeDefinitionContext _localctx = new EnumTypeDefinitionContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_enumTypeDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(439);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==StringValue) {
				{
				setState(438);
				description();
				}
			}

			setState(441);
			match(ENUM);
			setState(442);
			name();
			setState(444);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(443);
				directives();
				}
			}

			setState(447);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,55,_ctx) ) {
			case 1:
				{
				setState(446);
				enumValueDefinitions();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EnumTypeExtensionDefinitionContext extends ParserRuleContext {
		public TerminalNode EXTEND() { return getToken(GraphqlParser.EXTEND, 0); }
		public TerminalNode ENUM() { return getToken(GraphqlParser.ENUM, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public ExtensionEnumValueDefinitionsContext extensionEnumValueDefinitions() {
			return getRuleContext(ExtensionEnumValueDefinitionsContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public EmptyParenthesesContext emptyParentheses() {
			return getRuleContext(EmptyParenthesesContext.class,0);
		}
		public EnumTypeExtensionDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumTypeExtensionDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterEnumTypeExtensionDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitEnumTypeExtensionDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitEnumTypeExtensionDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumTypeExtensionDefinitionContext enumTypeExtensionDefinition() throws RecognitionException {
		EnumTypeExtensionDefinitionContext _localctx = new EnumTypeExtensionDefinitionContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_enumTypeExtensionDefinition);
		int _la;
		try {
			setState(464);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,58,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(449);
				match(EXTEND);
				setState(450);
				match(ENUM);
				setState(451);
				name();
				setState(453);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__8) {
					{
					setState(452);
					directives();
					}
				}

				setState(455);
				extensionEnumValueDefinitions();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(457);
				match(EXTEND);
				setState(458);
				match(ENUM);
				setState(459);
				name();
				setState(460);
				directives();
				setState(462);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,57,_ctx) ) {
				case 1:
					{
					setState(461);
					emptyParentheses();
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EnumValueDefinitionsContext extends ParserRuleContext {
		public List<EnumValueDefinitionContext> enumValueDefinition() {
			return getRuleContexts(EnumValueDefinitionContext.class);
		}
		public EnumValueDefinitionContext enumValueDefinition(int i) {
			return getRuleContext(EnumValueDefinitionContext.class,i);
		}
		public EnumValueDefinitionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumValueDefinitions; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterEnumValueDefinitions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitEnumValueDefinitions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitEnumValueDefinitions(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumValueDefinitionsContext enumValueDefinitions() throws RecognitionException {
		EnumValueDefinitionsContext _localctx = new EnumValueDefinitionsContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_enumValueDefinitions);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(466);
			match(T__0);
			setState(470);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FRAGMENT) | (1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << SCHEMA) | (1L << SCALAR) | (1L << TYPE) | (1L << INTERFACE) | (1L << IMPLEMENTS) | (1L << ENUM) | (1L << UNION) | (1L << INPUT) | (1L << EXTEND) | (1L << DIRECTIVE) | (1L << ON_KEYWORD) | (1L << REPEATABLE) | (1L << NAME) | (1L << StringValue))) != 0)) {
				{
				{
				setState(467);
				enumValueDefinition();
				}
				}
				setState(472);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(473);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtensionEnumValueDefinitionsContext extends ParserRuleContext {
		public List<EnumValueDefinitionContext> enumValueDefinition() {
			return getRuleContexts(EnumValueDefinitionContext.class);
		}
		public EnumValueDefinitionContext enumValueDefinition(int i) {
			return getRuleContext(EnumValueDefinitionContext.class,i);
		}
		public ExtensionEnumValueDefinitionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extensionEnumValueDefinitions; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterExtensionEnumValueDefinitions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitExtensionEnumValueDefinitions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitExtensionEnumValueDefinitions(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExtensionEnumValueDefinitionsContext extensionEnumValueDefinitions() throws RecognitionException {
		ExtensionEnumValueDefinitionsContext _localctx = new ExtensionEnumValueDefinitionsContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_extensionEnumValueDefinitions);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(475);
			match(T__0);
			setState(477);
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(476);
				enumValueDefinition();
				}
				}
				setState(479);
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FRAGMENT) | (1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << SCHEMA) | (1L << SCALAR) | (1L << TYPE) | (1L << INTERFACE) | (1L << IMPLEMENTS) | (1L << ENUM) | (1L << UNION) | (1L << INPUT) | (1L << EXTEND) | (1L << DIRECTIVE) | (1L << ON_KEYWORD) | (1L << REPEATABLE) | (1L << NAME) | (1L << StringValue))) != 0) );
			setState(481);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EnumValueDefinitionContext extends ParserRuleContext {
		public EnumValueContext enumValue() {
			return getRuleContext(EnumValueContext.class,0);
		}
		public DescriptionContext description() {
			return getRuleContext(DescriptionContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public EnumValueDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumValueDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterEnumValueDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitEnumValueDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitEnumValueDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumValueDefinitionContext enumValueDefinition() throws RecognitionException {
		EnumValueDefinitionContext _localctx = new EnumValueDefinitionContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_enumValueDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(484);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==StringValue) {
				{
				setState(483);
				description();
				}
			}

			setState(486);
			enumValue();
			setState(488);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(487);
				directives();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InputObjectTypeDefinitionContext extends ParserRuleContext {
		public TerminalNode INPUT() { return getToken(GraphqlParser.INPUT, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public DescriptionContext description() {
			return getRuleContext(DescriptionContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public InputObjectValueDefinitionsContext inputObjectValueDefinitions() {
			return getRuleContext(InputObjectValueDefinitionsContext.class,0);
		}
		public InputObjectTypeDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inputObjectTypeDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterInputObjectTypeDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitInputObjectTypeDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitInputObjectTypeDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InputObjectTypeDefinitionContext inputObjectTypeDefinition() throws RecognitionException {
		InputObjectTypeDefinitionContext _localctx = new InputObjectTypeDefinitionContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_inputObjectTypeDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(491);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==StringValue) {
				{
				setState(490);
				description();
				}
			}

			setState(493);
			match(INPUT);
			setState(494);
			name();
			setState(496);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(495);
				directives();
				}
			}

			setState(499);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,65,_ctx) ) {
			case 1:
				{
				setState(498);
				inputObjectValueDefinitions();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InputObjectTypeExtensionDefinitionContext extends ParserRuleContext {
		public TerminalNode EXTEND() { return getToken(GraphqlParser.EXTEND, 0); }
		public TerminalNode INPUT() { return getToken(GraphqlParser.INPUT, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public ExtensionInputObjectValueDefinitionsContext extensionInputObjectValueDefinitions() {
			return getRuleContext(ExtensionInputObjectValueDefinitionsContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public EmptyParenthesesContext emptyParentheses() {
			return getRuleContext(EmptyParenthesesContext.class,0);
		}
		public InputObjectTypeExtensionDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inputObjectTypeExtensionDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterInputObjectTypeExtensionDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitInputObjectTypeExtensionDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitInputObjectTypeExtensionDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InputObjectTypeExtensionDefinitionContext inputObjectTypeExtensionDefinition() throws RecognitionException {
		InputObjectTypeExtensionDefinitionContext _localctx = new InputObjectTypeExtensionDefinitionContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_inputObjectTypeExtensionDefinition);
		int _la;
		try {
			setState(516);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,68,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(501);
				match(EXTEND);
				setState(502);
				match(INPUT);
				setState(503);
				name();
				setState(505);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__8) {
					{
					setState(504);
					directives();
					}
				}

				setState(507);
				extensionInputObjectValueDefinitions();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(509);
				match(EXTEND);
				setState(510);
				match(INPUT);
				setState(511);
				name();
				setState(512);
				directives();
				setState(514);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,67,_ctx) ) {
				case 1:
					{
					setState(513);
					emptyParentheses();
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InputObjectValueDefinitionsContext extends ParserRuleContext {
		public List<InputValueDefinitionContext> inputValueDefinition() {
			return getRuleContexts(InputValueDefinitionContext.class);
		}
		public InputValueDefinitionContext inputValueDefinition(int i) {
			return getRuleContext(InputValueDefinitionContext.class,i);
		}
		public InputObjectValueDefinitionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inputObjectValueDefinitions; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterInputObjectValueDefinitions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitInputObjectValueDefinitions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitInputObjectValueDefinitions(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InputObjectValueDefinitionsContext inputObjectValueDefinitions() throws RecognitionException {
		InputObjectValueDefinitionsContext _localctx = new InputObjectValueDefinitionsContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_inputObjectValueDefinitions);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(518);
			match(T__0);
			setState(522);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BooleanValue) | (1L << NullValue) | (1L << FRAGMENT) | (1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << SCHEMA) | (1L << SCALAR) | (1L << TYPE) | (1L << INTERFACE) | (1L << IMPLEMENTS) | (1L << ENUM) | (1L << UNION) | (1L << INPUT) | (1L << EXTEND) | (1L << DIRECTIVE) | (1L << ON_KEYWORD) | (1L << REPEATABLE) | (1L << NAME) | (1L << StringValue))) != 0)) {
				{
				{
				setState(519);
				inputValueDefinition();
				}
				}
				setState(524);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(525);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtensionInputObjectValueDefinitionsContext extends ParserRuleContext {
		public List<InputValueDefinitionContext> inputValueDefinition() {
			return getRuleContexts(InputValueDefinitionContext.class);
		}
		public InputValueDefinitionContext inputValueDefinition(int i) {
			return getRuleContext(InputValueDefinitionContext.class,i);
		}
		public ExtensionInputObjectValueDefinitionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extensionInputObjectValueDefinitions; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterExtensionInputObjectValueDefinitions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitExtensionInputObjectValueDefinitions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitExtensionInputObjectValueDefinitions(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExtensionInputObjectValueDefinitionsContext extensionInputObjectValueDefinitions() throws RecognitionException {
		ExtensionInputObjectValueDefinitionsContext _localctx = new ExtensionInputObjectValueDefinitionsContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_extensionInputObjectValueDefinitions);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(527);
			match(T__0);
			setState(529);
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(528);
				inputValueDefinition();
				}
				}
				setState(531);
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BooleanValue) | (1L << NullValue) | (1L << FRAGMENT) | (1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << SCHEMA) | (1L << SCALAR) | (1L << TYPE) | (1L << INTERFACE) | (1L << IMPLEMENTS) | (1L << ENUM) | (1L << UNION) | (1L << INPUT) | (1L << EXTEND) | (1L << DIRECTIVE) | (1L << ON_KEYWORD) | (1L << REPEATABLE) | (1L << NAME) | (1L << StringValue))) != 0) );
			setState(533);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DirectiveDefinitionContext extends ParserRuleContext {
		public TerminalNode DIRECTIVE() { return getToken(GraphqlParser.DIRECTIVE, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public TerminalNode ON_KEYWORD() { return getToken(GraphqlParser.ON_KEYWORD, 0); }
		public DirectiveLocationsContext directiveLocations() {
			return getRuleContext(DirectiveLocationsContext.class,0);
		}
		public DescriptionContext description() {
			return getRuleContext(DescriptionContext.class,0);
		}
		public ArgumentsDefinitionContext argumentsDefinition() {
			return getRuleContext(ArgumentsDefinitionContext.class,0);
		}
		public TerminalNode REPEATABLE() { return getToken(GraphqlParser.REPEATABLE, 0); }
		public DirectiveDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directiveDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterDirectiveDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitDirectiveDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitDirectiveDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectiveDefinitionContext directiveDefinition() throws RecognitionException {
		DirectiveDefinitionContext _localctx = new DirectiveDefinitionContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_directiveDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(536);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==StringValue) {
				{
				setState(535);
				description();
				}
			}

			setState(538);
			match(DIRECTIVE);
			setState(539);
			match(T__8);
			setState(540);
			name();
			setState(542);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__4) {
				{
				setState(541);
				argumentsDefinition();
				}
			}

			setState(545);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==REPEATABLE) {
				{
				setState(544);
				match(REPEATABLE);
				}
			}

			setState(547);
			match(ON_KEYWORD);
			setState(548);
			directiveLocations(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DirectiveLocationContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public DirectiveLocationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directiveLocation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterDirectiveLocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitDirectiveLocation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitDirectiveLocation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectiveLocationContext directiveLocation() throws RecognitionException {
		DirectiveLocationContext _localctx = new DirectiveLocationContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_directiveLocation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(550);
			name();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DirectiveLocationsContext extends ParserRuleContext {
		public DirectiveLocationContext directiveLocation() {
			return getRuleContext(DirectiveLocationContext.class,0);
		}
		public DirectiveLocationsContext directiveLocations() {
			return getRuleContext(DirectiveLocationsContext.class,0);
		}
		public DirectiveLocationsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directiveLocations; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterDirectiveLocations(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitDirectiveLocations(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitDirectiveLocations(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectiveLocationsContext directiveLocations() throws RecognitionException {
		return directiveLocations(0);
	}

	private DirectiveLocationsContext directiveLocations(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		DirectiveLocationsContext _localctx = new DirectiveLocationsContext(_ctx, _parentState);
		DirectiveLocationsContext _prevctx = _localctx;
		int _startState = 74;
		enterRecursionRule(_localctx, 74, RULE_directiveLocations, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(553);
			directiveLocation();
			}
			_ctx.stop = _input.LT(-1);
			setState(560);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,74,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new DirectiveLocationsContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_directiveLocations);
					setState(555);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(556);
					match(T__7);
					setState(557);
					directiveLocation();
					}
					}
				}
				setState(562);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,74,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class OperationTypeContext extends ParserRuleContext {
		public TerminalNode SUBSCRIPTION() { return getToken(GraphqlParser.SUBSCRIPTION, 0); }
		public TerminalNode MUTATION() { return getToken(GraphqlParser.MUTATION, 0); }
		public TerminalNode QUERY() { return getToken(GraphqlParser.QUERY, 0); }
		public OperationTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operationType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterOperationType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitOperationType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitOperationType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperationTypeContext operationType() throws RecognitionException {
		OperationTypeContext _localctx = new OperationTypeContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_operationType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(563);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DescriptionContext extends ParserRuleContext {
		public TerminalNode StringValue() { return getToken(GraphqlParser.StringValue, 0); }
		public DescriptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_description; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterDescription(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitDescription(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitDescription(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DescriptionContext description() throws RecognitionException {
		DescriptionContext _localctx = new DescriptionContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_description);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(565);
			match(StringValue);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EnumValueContext extends ParserRuleContext {
		public EnumValueNameContext enumValueName() {
			return getRuleContext(EnumValueNameContext.class,0);
		}
		public EnumValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterEnumValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitEnumValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitEnumValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumValueContext enumValue() throws RecognitionException {
		EnumValueContext _localctx = new EnumValueContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_enumValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(567);
			enumValueName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayValueContext extends ParserRuleContext {
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public ArrayValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterArrayValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitArrayValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitArrayValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayValueContext arrayValue() throws RecognitionException {
		ArrayValueContext _localctx = new ArrayValueContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_arrayValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(569);
			match(T__9);
			setState(573);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__9) | (1L << BooleanValue) | (1L << NullValue) | (1L << FRAGMENT) | (1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << SCHEMA) | (1L << SCALAR) | (1L << TYPE) | (1L << INTERFACE) | (1L << IMPLEMENTS) | (1L << ENUM) | (1L << UNION) | (1L << INPUT) | (1L << EXTEND) | (1L << DIRECTIVE) | (1L << ON_KEYWORD) | (1L << REPEATABLE) | (1L << NAME) | (1L << IntValue) | (1L << FloatValue) | (1L << StringValue))) != 0)) {
				{
				{
				setState(570);
				value();
				}
				}
				setState(575);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(576);
			match(T__10);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayValueWithVariableContext extends ParserRuleContext {
		public List<ValueWithVariableContext> valueWithVariable() {
			return getRuleContexts(ValueWithVariableContext.class);
		}
		public ValueWithVariableContext valueWithVariable(int i) {
			return getRuleContext(ValueWithVariableContext.class,i);
		}
		public ArrayValueWithVariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayValueWithVariable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterArrayValueWithVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitArrayValueWithVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitArrayValueWithVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayValueWithVariableContext arrayValueWithVariable() throws RecognitionException {
		ArrayValueWithVariableContext _localctx = new ArrayValueWithVariableContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_arrayValueWithVariable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(578);
			match(T__9);
			setState(582);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__9) | (1L << T__11) | (1L << BooleanValue) | (1L << NullValue) | (1L << FRAGMENT) | (1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << SCHEMA) | (1L << SCALAR) | (1L << TYPE) | (1L << INTERFACE) | (1L << IMPLEMENTS) | (1L << ENUM) | (1L << UNION) | (1L << INPUT) | (1L << EXTEND) | (1L << DIRECTIVE) | (1L << ON_KEYWORD) | (1L << REPEATABLE) | (1L << NAME) | (1L << IntValue) | (1L << FloatValue) | (1L << StringValue))) != 0)) {
				{
				{
				setState(579);
				valueWithVariable();
				}
				}
				setState(584);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(585);
			match(T__10);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ObjectValueContext extends ParserRuleContext {
		public List<ObjectFieldContext> objectField() {
			return getRuleContexts(ObjectFieldContext.class);
		}
		public ObjectFieldContext objectField(int i) {
			return getRuleContext(ObjectFieldContext.class,i);
		}
		public ObjectValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterObjectValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitObjectValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitObjectValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectValueContext objectValue() throws RecognitionException {
		ObjectValueContext _localctx = new ObjectValueContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_objectValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(587);
			match(T__0);
			setState(591);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BooleanValue) | (1L << NullValue) | (1L << FRAGMENT) | (1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << SCHEMA) | (1L << SCALAR) | (1L << TYPE) | (1L << INTERFACE) | (1L << IMPLEMENTS) | (1L << ENUM) | (1L << UNION) | (1L << INPUT) | (1L << EXTEND) | (1L << DIRECTIVE) | (1L << ON_KEYWORD) | (1L << REPEATABLE) | (1L << NAME))) != 0)) {
				{
				{
				setState(588);
				objectField();
				}
				}
				setState(593);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(594);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ObjectValueWithVariableContext extends ParserRuleContext {
		public List<ObjectFieldWithVariableContext> objectFieldWithVariable() {
			return getRuleContexts(ObjectFieldWithVariableContext.class);
		}
		public ObjectFieldWithVariableContext objectFieldWithVariable(int i) {
			return getRuleContext(ObjectFieldWithVariableContext.class,i);
		}
		public ObjectValueWithVariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectValueWithVariable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterObjectValueWithVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitObjectValueWithVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitObjectValueWithVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectValueWithVariableContext objectValueWithVariable() throws RecognitionException {
		ObjectValueWithVariableContext _localctx = new ObjectValueWithVariableContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_objectValueWithVariable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(596);
			match(T__0);
			setState(600);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BooleanValue) | (1L << NullValue) | (1L << FRAGMENT) | (1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << SCHEMA) | (1L << SCALAR) | (1L << TYPE) | (1L << INTERFACE) | (1L << IMPLEMENTS) | (1L << ENUM) | (1L << UNION) | (1L << INPUT) | (1L << EXTEND) | (1L << DIRECTIVE) | (1L << ON_KEYWORD) | (1L << REPEATABLE) | (1L << NAME))) != 0)) {
				{
				{
				setState(597);
				objectFieldWithVariable();
				}
				}
				setState(602);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(603);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ObjectFieldContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public ObjectFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterObjectField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitObjectField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitObjectField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectFieldContext objectField() throws RecognitionException {
		ObjectFieldContext _localctx = new ObjectFieldContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_objectField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(605);
			name();
			setState(606);
			match(T__2);
			setState(607);
			value();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ObjectFieldWithVariableContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public ValueWithVariableContext valueWithVariable() {
			return getRuleContext(ValueWithVariableContext.class,0);
		}
		public ObjectFieldWithVariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectFieldWithVariable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterObjectFieldWithVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitObjectFieldWithVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitObjectFieldWithVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectFieldWithVariableContext objectFieldWithVariable() throws RecognitionException {
		ObjectFieldWithVariableContext _localctx = new ObjectFieldWithVariableContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_objectFieldWithVariable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(609);
			name();
			setState(610);
			match(T__2);
			setState(611);
			valueWithVariable();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DirectivesContext extends ParserRuleContext {
		public List<DirectiveContext> directive() {
			return getRuleContexts(DirectiveContext.class);
		}
		public DirectiveContext directive(int i) {
			return getRuleContext(DirectiveContext.class,i);
		}
		public DirectivesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directives; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterDirectives(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitDirectives(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitDirectives(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectivesContext directives() throws RecognitionException {
		DirectivesContext _localctx = new DirectivesContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_directives);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(614);
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(613);
					directive();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(616);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,79,_ctx);
			} while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DirectiveContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public DirectiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directive; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterDirective(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitDirective(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitDirective(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectiveContext directive() throws RecognitionException {
		DirectiveContext _localctx = new DirectiveContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_directive);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(618);
			match(T__8);
			setState(619);
			name();
			setState(621);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__4) {
				{
				setState(620);
				arguments();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArgumentsContext extends ParserRuleContext {
		public List<ArgumentContext> argument() {
			return getRuleContexts(ArgumentContext.class);
		}
		public ArgumentContext argument(int i) {
			return getRuleContext(ArgumentContext.class,i);
		}
		public ArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arguments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitArguments(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgumentsContext arguments() throws RecognitionException {
		ArgumentsContext _localctx = new ArgumentsContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_arguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(623);
			match(T__4);
			setState(625);
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(624);
				argument();
				}
				}
				setState(627);
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BooleanValue) | (1L << NullValue) | (1L << FRAGMENT) | (1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << SCHEMA) | (1L << SCALAR) | (1L << TYPE) | (1L << INTERFACE) | (1L << IMPLEMENTS) | (1L << ENUM) | (1L << UNION) | (1L << INPUT) | (1L << EXTEND) | (1L << DIRECTIVE) | (1L << ON_KEYWORD) | (1L << REPEATABLE) | (1L << NAME))) != 0) );
			setState(629);
			match(T__5);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArgumentContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public ValueWithVariableContext valueWithVariable() {
			return getRuleContext(ValueWithVariableContext.class,0);
		}
		public ArgumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_argument; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterArgument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitArgument(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitArgument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgumentContext argument() throws RecognitionException {
		ArgumentContext _localctx = new ArgumentContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_argument);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(631);
			name();
			setState(632);
			match(T__2);
			setState(633);
			valueWithVariable();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BaseNameContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(GraphqlParser.NAME, 0); }
		public TerminalNode FRAGMENT() { return getToken(GraphqlParser.FRAGMENT, 0); }
		public TerminalNode QUERY() { return getToken(GraphqlParser.QUERY, 0); }
		public TerminalNode MUTATION() { return getToken(GraphqlParser.MUTATION, 0); }
		public TerminalNode SUBSCRIPTION() { return getToken(GraphqlParser.SUBSCRIPTION, 0); }
		public TerminalNode SCHEMA() { return getToken(GraphqlParser.SCHEMA, 0); }
		public TerminalNode SCALAR() { return getToken(GraphqlParser.SCALAR, 0); }
		public TerminalNode TYPE() { return getToken(GraphqlParser.TYPE, 0); }
		public TerminalNode INTERFACE() { return getToken(GraphqlParser.INTERFACE, 0); }
		public TerminalNode IMPLEMENTS() { return getToken(GraphqlParser.IMPLEMENTS, 0); }
		public TerminalNode ENUM() { return getToken(GraphqlParser.ENUM, 0); }
		public TerminalNode UNION() { return getToken(GraphqlParser.UNION, 0); }
		public TerminalNode INPUT() { return getToken(GraphqlParser.INPUT, 0); }
		public TerminalNode EXTEND() { return getToken(GraphqlParser.EXTEND, 0); }
		public TerminalNode DIRECTIVE() { return getToken(GraphqlParser.DIRECTIVE, 0); }
		public TerminalNode REPEATABLE() { return getToken(GraphqlParser.REPEATABLE, 0); }
		public BaseNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_baseName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterBaseName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitBaseName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitBaseName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BaseNameContext baseName() throws RecognitionException {
		BaseNameContext _localctx = new BaseNameContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_baseName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(635);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FRAGMENT) | (1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << SCHEMA) | (1L << SCALAR) | (1L << TYPE) | (1L << INTERFACE) | (1L << IMPLEMENTS) | (1L << ENUM) | (1L << UNION) | (1L << INPUT) | (1L << EXTEND) | (1L << DIRECTIVE) | (1L << REPEATABLE) | (1L << NAME))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FragmentNameContext extends ParserRuleContext {
		public BaseNameContext baseName() {
			return getRuleContext(BaseNameContext.class,0);
		}
		public TerminalNode BooleanValue() { return getToken(GraphqlParser.BooleanValue, 0); }
		public TerminalNode NullValue() { return getToken(GraphqlParser.NullValue, 0); }
		public FragmentNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fragmentName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterFragmentName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitFragmentName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitFragmentName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FragmentNameContext fragmentName() throws RecognitionException {
		FragmentNameContext _localctx = new FragmentNameContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_fragmentName);
		try {
			setState(640);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FRAGMENT:
			case QUERY:
			case MUTATION:
			case SUBSCRIPTION:
			case SCHEMA:
			case SCALAR:
			case TYPE:
			case INTERFACE:
			case IMPLEMENTS:
			case ENUM:
			case UNION:
			case INPUT:
			case EXTEND:
			case DIRECTIVE:
			case REPEATABLE:
			case NAME:
				enterOuterAlt(_localctx, 1);
				{
				setState(637);
				baseName();
				}
				break;
			case BooleanValue:
				enterOuterAlt(_localctx, 2);
				{
				setState(638);
				match(BooleanValue);
				}
				break;
			case NullValue:
				enterOuterAlt(_localctx, 3);
				{
				setState(639);
				match(NullValue);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EnumValueNameContext extends ParserRuleContext {
		public BaseNameContext baseName() {
			return getRuleContext(BaseNameContext.class,0);
		}
		public TerminalNode ON_KEYWORD() { return getToken(GraphqlParser.ON_KEYWORD, 0); }
		public EnumValueNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumValueName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterEnumValueName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitEnumValueName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitEnumValueName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumValueNameContext enumValueName() throws RecognitionException {
		EnumValueNameContext _localctx = new EnumValueNameContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_enumValueName);
		try {
			setState(644);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FRAGMENT:
			case QUERY:
			case MUTATION:
			case SUBSCRIPTION:
			case SCHEMA:
			case SCALAR:
			case TYPE:
			case INTERFACE:
			case IMPLEMENTS:
			case ENUM:
			case UNION:
			case INPUT:
			case EXTEND:
			case DIRECTIVE:
			case REPEATABLE:
			case NAME:
				enterOuterAlt(_localctx, 1);
				{
				setState(642);
				baseName();
				}
				break;
			case ON_KEYWORD:
				enterOuterAlt(_localctx, 2);
				{
				setState(643);
				match(ON_KEYWORD);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NameContext extends ParserRuleContext {
		public BaseNameContext baseName() {
			return getRuleContext(BaseNameContext.class,0);
		}
		public TerminalNode BooleanValue() { return getToken(GraphqlParser.BooleanValue, 0); }
		public TerminalNode NullValue() { return getToken(GraphqlParser.NullValue, 0); }
		public TerminalNode ON_KEYWORD() { return getToken(GraphqlParser.ON_KEYWORD, 0); }
		public NameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NameContext name() throws RecognitionException {
		NameContext _localctx = new NameContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_name);
		try {
			setState(650);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FRAGMENT:
			case QUERY:
			case MUTATION:
			case SUBSCRIPTION:
			case SCHEMA:
			case SCALAR:
			case TYPE:
			case INTERFACE:
			case IMPLEMENTS:
			case ENUM:
			case UNION:
			case INPUT:
			case EXTEND:
			case DIRECTIVE:
			case REPEATABLE:
			case NAME:
				enterOuterAlt(_localctx, 1);
				{
				setState(646);
				baseName();
				}
				break;
			case BooleanValue:
				enterOuterAlt(_localctx, 2);
				{
				setState(647);
				match(BooleanValue);
				}
				break;
			case NullValue:
				enterOuterAlt(_localctx, 3);
				{
				setState(648);
				match(NullValue);
				}
				break;
			case ON_KEYWORD:
				enterOuterAlt(_localctx, 4);
				{
				setState(649);
				match(ON_KEYWORD);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueContext extends ParserRuleContext {
		public TerminalNode StringValue() { return getToken(GraphqlParser.StringValue, 0); }
		public TerminalNode IntValue() { return getToken(GraphqlParser.IntValue, 0); }
		public TerminalNode FloatValue() { return getToken(GraphqlParser.FloatValue, 0); }
		public TerminalNode BooleanValue() { return getToken(GraphqlParser.BooleanValue, 0); }
		public TerminalNode NullValue() { return getToken(GraphqlParser.NullValue, 0); }
		public EnumValueContext enumValue() {
			return getRuleContext(EnumValueContext.class,0);
		}
		public ArrayValueContext arrayValue() {
			return getRuleContext(ArrayValueContext.class,0);
		}
		public ObjectValueContext objectValue() {
			return getRuleContext(ObjectValueContext.class,0);
		}
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_value);
		try {
			setState(660);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringValue:
				enterOuterAlt(_localctx, 1);
				{
				setState(652);
				match(StringValue);
				}
				break;
			case IntValue:
				enterOuterAlt(_localctx, 2);
				{
				setState(653);
				match(IntValue);
				}
				break;
			case FloatValue:
				enterOuterAlt(_localctx, 3);
				{
				setState(654);
				match(FloatValue);
				}
				break;
			case BooleanValue:
				enterOuterAlt(_localctx, 4);
				{
				setState(655);
				match(BooleanValue);
				}
				break;
			case NullValue:
				enterOuterAlt(_localctx, 5);
				{
				setState(656);
				match(NullValue);
				}
				break;
			case FRAGMENT:
			case QUERY:
			case MUTATION:
			case SUBSCRIPTION:
			case SCHEMA:
			case SCALAR:
			case TYPE:
			case INTERFACE:
			case IMPLEMENTS:
			case ENUM:
			case UNION:
			case INPUT:
			case EXTEND:
			case DIRECTIVE:
			case ON_KEYWORD:
			case REPEATABLE:
			case NAME:
				enterOuterAlt(_localctx, 6);
				{
				setState(657);
				enumValue();
				}
				break;
			case T__9:
				enterOuterAlt(_localctx, 7);
				{
				setState(658);
				arrayValue();
				}
				break;
			case T__0:
				enterOuterAlt(_localctx, 8);
				{
				setState(659);
				objectValue();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueWithVariableContext extends ParserRuleContext {
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public TerminalNode StringValue() { return getToken(GraphqlParser.StringValue, 0); }
		public TerminalNode IntValue() { return getToken(GraphqlParser.IntValue, 0); }
		public TerminalNode FloatValue() { return getToken(GraphqlParser.FloatValue, 0); }
		public TerminalNode BooleanValue() { return getToken(GraphqlParser.BooleanValue, 0); }
		public TerminalNode NullValue() { return getToken(GraphqlParser.NullValue, 0); }
		public EnumValueContext enumValue() {
			return getRuleContext(EnumValueContext.class,0);
		}
		public ArrayValueWithVariableContext arrayValueWithVariable() {
			return getRuleContext(ArrayValueWithVariableContext.class,0);
		}
		public ObjectValueWithVariableContext objectValueWithVariable() {
			return getRuleContext(ObjectValueWithVariableContext.class,0);
		}
		public ValueWithVariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valueWithVariable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterValueWithVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitValueWithVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitValueWithVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueWithVariableContext valueWithVariable() throws RecognitionException {
		ValueWithVariableContext _localctx = new ValueWithVariableContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_valueWithVariable);
		try {
			setState(671);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__11:
				enterOuterAlt(_localctx, 1);
				{
				setState(662);
				variable();
				}
				break;
			case StringValue:
				enterOuterAlt(_localctx, 2);
				{
				setState(663);
				match(StringValue);
				}
				break;
			case IntValue:
				enterOuterAlt(_localctx, 3);
				{
				setState(664);
				match(IntValue);
				}
				break;
			case FloatValue:
				enterOuterAlt(_localctx, 4);
				{
				setState(665);
				match(FloatValue);
				}
				break;
			case BooleanValue:
				enterOuterAlt(_localctx, 5);
				{
				setState(666);
				match(BooleanValue);
				}
				break;
			case NullValue:
				enterOuterAlt(_localctx, 6);
				{
				setState(667);
				match(NullValue);
				}
				break;
			case FRAGMENT:
			case QUERY:
			case MUTATION:
			case SUBSCRIPTION:
			case SCHEMA:
			case SCALAR:
			case TYPE:
			case INTERFACE:
			case IMPLEMENTS:
			case ENUM:
			case UNION:
			case INPUT:
			case EXTEND:
			case DIRECTIVE:
			case ON_KEYWORD:
			case REPEATABLE:
			case NAME:
				enterOuterAlt(_localctx, 7);
				{
				setState(668);
				enumValue();
				}
				break;
			case T__9:
				enterOuterAlt(_localctx, 8);
				{
				setState(669);
				arrayValueWithVariable();
				}
				break;
			case T__0:
				enterOuterAlt(_localctx, 9);
				{
				setState(670);
				objectValueWithVariable();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public VariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableContext variable() throws RecognitionException {
		VariableContext _localctx = new VariableContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_variable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(673);
			match(T__11);
			setState(674);
			name();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DefaultValueContext extends ParserRuleContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public DefaultValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defaultValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterDefaultValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitDefaultValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitDefaultValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefaultValueContext defaultValue() throws RecognitionException {
		DefaultValueContext _localctx = new DefaultValueContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_defaultValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(676);
			match(T__6);
			setState(677);
			value();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeContext extends ParserRuleContext {
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public ListTypeContext listType() {
			return getRuleContext(ListTypeContext.class,0);
		}
		public NonNullTypeContext nonNullType() {
			return getRuleContext(NonNullTypeContext.class,0);
		}
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_type);
		try {
			setState(682);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,87,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(679);
				typeName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(680);
				listType();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(681);
				nonNullType();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeNameContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public TypeNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterTypeName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitTypeName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitTypeName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeNameContext typeName() throws RecognitionException {
		TypeNameContext _localctx = new TypeNameContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_typeName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(684);
			name();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ListTypeContext extends ParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public ListTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_listType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterListType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitListType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitListType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ListTypeContext listType() throws RecognitionException {
		ListTypeContext _localctx = new ListTypeContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_listType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(686);
			match(T__9);
			setState(687);
			type();
			setState(688);
			match(T__10);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NonNullTypeContext extends ParserRuleContext {
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public ListTypeContext listType() {
			return getRuleContext(ListTypeContext.class,0);
		}
		public NonNullTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonNullType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterNonNullType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitNonNullType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitNonNullType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NonNullTypeContext nonNullType() throws RecognitionException {
		NonNullTypeContext _localctx = new NonNullTypeContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_nonNullType);
		try {
			setState(696);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BooleanValue:
			case NullValue:
			case FRAGMENT:
			case QUERY:
			case MUTATION:
			case SUBSCRIPTION:
			case SCHEMA:
			case SCALAR:
			case TYPE:
			case INTERFACE:
			case IMPLEMENTS:
			case ENUM:
			case UNION:
			case INPUT:
			case EXTEND:
			case DIRECTIVE:
			case ON_KEYWORD:
			case REPEATABLE:
			case NAME:
				enterOuterAlt(_localctx, 1);
				{
				setState(690);
				typeName();
				setState(691);
				match(T__12);
				}
				break;
			case T__9:
				enterOuterAlt(_localctx, 2);
				{
				setState(693);
				listType();
				setState(694);
				match(T__12);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OperationDefinitionContext extends ParserRuleContext {
		public SelectionSetContext selectionSet() {
			return getRuleContext(SelectionSetContext.class,0);
		}
		public OperationTypeContext operationType() {
			return getRuleContext(OperationTypeContext.class,0);
		}
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public VariableDefinitionsContext variableDefinitions() {
			return getRuleContext(VariableDefinitionsContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public OperationDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operationDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterOperationDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitOperationDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitOperationDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperationDefinitionContext operationDefinition() throws RecognitionException {
		OperationDefinitionContext _localctx = new OperationDefinitionContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_operationDefinition);
		int _la;
		try {
			setState(711);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(698);
				selectionSet();
				}
				break;
			case QUERY:
			case MUTATION:
			case SUBSCRIPTION:
				enterOuterAlt(_localctx, 2);
				{
				setState(699);
				operationType();
				setState(701);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BooleanValue) | (1L << NullValue) | (1L << FRAGMENT) | (1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << SCHEMA) | (1L << SCALAR) | (1L << TYPE) | (1L << INTERFACE) | (1L << IMPLEMENTS) | (1L << ENUM) | (1L << UNION) | (1L << INPUT) | (1L << EXTEND) | (1L << DIRECTIVE) | (1L << ON_KEYWORD) | (1L << REPEATABLE) | (1L << NAME))) != 0)) {
					{
					setState(700);
					name();
					}
				}

				setState(704);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__4) {
					{
					setState(703);
					variableDefinitions();
					}
				}

				setState(707);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__8) {
					{
					setState(706);
					directives();
					}
				}

				setState(709);
				selectionSet();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableDefinitionsContext extends ParserRuleContext {
		public List<VariableDefinitionContext> variableDefinition() {
			return getRuleContexts(VariableDefinitionContext.class);
		}
		public VariableDefinitionContext variableDefinition(int i) {
			return getRuleContext(VariableDefinitionContext.class,i);
		}
		public VariableDefinitionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableDefinitions; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterVariableDefinitions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitVariableDefinitions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitVariableDefinitions(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableDefinitionsContext variableDefinitions() throws RecognitionException {
		VariableDefinitionsContext _localctx = new VariableDefinitionsContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_variableDefinitions);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(713);
			match(T__4);
			setState(715);
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(714);
				variableDefinition();
				}
				}
				setState(717);
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==T__11 );
			setState(719);
			match(T__5);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableDefinitionContext extends ParserRuleContext {
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public DefaultValueContext defaultValue() {
			return getRuleContext(DefaultValueContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public VariableDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterVariableDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitVariableDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitVariableDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableDefinitionContext variableDefinition() throws RecognitionException {
		VariableDefinitionContext _localctx = new VariableDefinitionContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_variableDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(721);
			variable();
			setState(722);
			match(T__2);
			setState(723);
			type();
			setState(725);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__6) {
				{
				setState(724);
				defaultValue();
				}
			}

			setState(728);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(727);
				directives();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SelectionSetContext extends ParserRuleContext {
		public List<SelectionContext> selection() {
			return getRuleContexts(SelectionContext.class);
		}
		public SelectionContext selection(int i) {
			return getRuleContext(SelectionContext.class,i);
		}
		public SelectionSetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectionSet; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterSelectionSet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitSelectionSet(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitSelectionSet(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectionSetContext selectionSet() throws RecognitionException {
		SelectionSetContext _localctx = new SelectionSetContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_selectionSet);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(730);
			match(T__0);
			setState(732);
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(731);
				selection();
				}
				}
				setState(734);
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__13) | (1L << BooleanValue) | (1L << NullValue) | (1L << FRAGMENT) | (1L << QUERY) | (1L << MUTATION) | (1L << SUBSCRIPTION) | (1L << SCHEMA) | (1L << SCALAR) | (1L << TYPE) | (1L << INTERFACE) | (1L << IMPLEMENTS) | (1L << ENUM) | (1L << UNION) | (1L << INPUT) | (1L << EXTEND) | (1L << DIRECTIVE) | (1L << ON_KEYWORD) | (1L << REPEATABLE) | (1L << NAME))) != 0) );
			setState(736);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SelectionContext extends ParserRuleContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public FragmentSpreadContext fragmentSpread() {
			return getRuleContext(FragmentSpreadContext.class,0);
		}
		public InlineFragmentContext inlineFragment() {
			return getRuleContext(InlineFragmentContext.class,0);
		}
		public SelectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selection; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterSelection(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitSelection(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitSelection(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectionContext selection() throws RecognitionException {
		SelectionContext _localctx = new SelectionContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_selection);
		try {
			setState(741);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,97,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(738);
				field();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(739);
				fragmentSpread();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(740);
				inlineFragment();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FieldContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public SelectionSetContext selectionSet() {
			return getRuleContext(SelectionSetContext.class,0);
		}
		public FieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_field; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldContext field() throws RecognitionException {
		FieldContext _localctx = new FieldContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_field);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(744);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,98,_ctx) ) {
			case 1:
				{
				setState(743);
				alias();
				}
				break;
			}
			setState(746);
			name();
			setState(748);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__4) {
				{
				setState(747);
				arguments();
				}
			}

			setState(751);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(750);
				directives();
				}
			}

			setState(754);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(753);
				selectionSet();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AliasContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public AliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitAlias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitAlias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AliasContext alias() throws RecognitionException {
		AliasContext _localctx = new AliasContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(756);
			name();
			setState(757);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FragmentSpreadContext extends ParserRuleContext {
		public FragmentNameContext fragmentName() {
			return getRuleContext(FragmentNameContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public FragmentSpreadContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fragmentSpread; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterFragmentSpread(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitFragmentSpread(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitFragmentSpread(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FragmentSpreadContext fragmentSpread() throws RecognitionException {
		FragmentSpreadContext _localctx = new FragmentSpreadContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_fragmentSpread);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(759);
			match(T__13);
			setState(760);
			fragmentName();
			setState(762);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(761);
				directives();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InlineFragmentContext extends ParserRuleContext {
		public SelectionSetContext selectionSet() {
			return getRuleContext(SelectionSetContext.class,0);
		}
		public TypeConditionContext typeCondition() {
			return getRuleContext(TypeConditionContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public InlineFragmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inlineFragment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterInlineFragment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitInlineFragment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitInlineFragment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InlineFragmentContext inlineFragment() throws RecognitionException {
		InlineFragmentContext _localctx = new InlineFragmentContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_inlineFragment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(764);
			match(T__13);
			setState(766);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ON_KEYWORD) {
				{
				setState(765);
				typeCondition();
				}
			}

			setState(769);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(768);
				directives();
				}
			}

			setState(771);
			selectionSet();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FragmentDefinitionContext extends ParserRuleContext {
		public TerminalNode FRAGMENT() { return getToken(GraphqlParser.FRAGMENT, 0); }
		public FragmentNameContext fragmentName() {
			return getRuleContext(FragmentNameContext.class,0);
		}
		public TypeConditionContext typeCondition() {
			return getRuleContext(TypeConditionContext.class,0);
		}
		public SelectionSetContext selectionSet() {
			return getRuleContext(SelectionSetContext.class,0);
		}
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public FragmentDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fragmentDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterFragmentDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitFragmentDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitFragmentDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FragmentDefinitionContext fragmentDefinition() throws RecognitionException {
		FragmentDefinitionContext _localctx = new FragmentDefinitionContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_fragmentDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(773);
			match(FRAGMENT);
			setState(774);
			fragmentName();
			setState(775);
			typeCondition();
			setState(777);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(776);
				directives();
				}
			}

			setState(779);
			selectionSet();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeConditionContext extends ParserRuleContext {
		public TerminalNode ON_KEYWORD() { return getToken(GraphqlParser.ON_KEYWORD, 0); }
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public TypeConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeCondition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).enterTypeCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener) ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlListener)listener).exitTypeCondition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor) return ((com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlVisitor<? extends T>)visitor).visitTypeCondition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeConditionContext typeCondition() throws RecognitionException {
		TypeConditionContext _localctx = new TypeConditionContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_typeCondition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(781);
			match(ON_KEYWORD);
			setState(782);
			typeName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 14:
			return implementsInterfaces_sempred((ImplementsInterfacesContext)_localctx, predIndex);
		case 25:
			return unionMembers_sempred((UnionMembersContext)_localctx, predIndex);
		case 37:
			return directiveLocations_sempred((DirectiveLocationsContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean implementsInterfaces_sempred(ImplementsInterfacesContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean unionMembers_sempred(UnionMembersContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean directiveLocations_sempred(DirectiveLocationsContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3.\u0313\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\3\2\6\2\u0098\n\2\r\2\16\2\u0099\3\3\3\3\3\3\3\3\5\3\u00a0"+
		"\n\3\3\4\3\4\3\4\5\4\u00a5\n\4\3\5\3\5\5\5\u00a9\n\5\3\6\5\6\u00ac\n\6"+
		"\3\6\3\6\5\6\u00b0\n\6\3\6\3\6\6\6\u00b4\n\6\r\6\16\6\u00b5\3\6\3\6\3"+
		"\7\3\7\3\7\5\7\u00bd\n\7\3\7\3\7\6\7\u00c1\n\7\r\7\16\7\u00c2\3\7\3\7"+
		"\3\7\3\7\3\7\6\7\u00ca\n\7\r\7\16\7\u00cb\5\7\u00ce\n\7\3\b\5\b\u00d1"+
		"\n\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\5\t\u00dd\n\t\3\n\3\n\3\n"+
		"\3\n\3\n\3\n\5\n\u00e5\n\n\3\13\3\13\3\13\3\f\5\f\u00eb\n\f\3\f\3\f\3"+
		"\f\5\f\u00f0\n\f\3\r\3\r\3\r\3\r\3\r\3\16\5\16\u00f8\n\16\3\16\3\16\3"+
		"\16\5\16\u00fd\n\16\3\16\5\16\u0100\n\16\3\16\5\16\u0103\n\16\3\17\3\17"+
		"\3\17\3\17\5\17\u0109\n\17\3\17\5\17\u010c\n\17\3\17\3\17\3\17\3\17\3"+
		"\17\3\17\5\17\u0114\n\17\3\17\3\17\5\17\u0118\n\17\3\17\3\17\3\17\3\17"+
		"\3\17\5\17\u011f\n\17\3\20\3\20\3\20\5\20\u0124\n\20\3\20\6\20\u0127\n"+
		"\20\r\20\16\20\u0128\3\20\3\20\3\20\7\20\u012e\n\20\f\20\16\20\u0131\13"+
		"\20\3\21\3\21\7\21\u0135\n\21\f\21\16\21\u0138\13\21\3\21\3\21\3\22\3"+
		"\22\6\22\u013e\n\22\r\22\16\22\u013f\3\22\3\22\3\23\5\23\u0145\n\23\3"+
		"\23\3\23\5\23\u0149\n\23\3\23\3\23\3\23\5\23\u014e\n\23\3\24\3\24\6\24"+
		"\u0152\n\24\r\24\16\24\u0153\3\24\3\24\3\25\5\25\u0159\n\25\3\25\3\25"+
		"\3\25\3\25\5\25\u015f\n\25\3\25\5\25\u0162\n\25\3\26\5\26\u0165\n\26\3"+
		"\26\3\26\3\26\5\26\u016a\n\26\3\26\5\26\u016d\n\26\3\26\5\26\u0170\n\26"+
		"\3\27\3\27\3\27\3\27\5\27\u0176\n\27\3\27\5\27\u0179\n\27\3\27\3\27\3"+
		"\27\3\27\3\27\3\27\5\27\u0181\n\27\3\27\3\27\5\27\u0185\n\27\3\27\3\27"+
		"\3\27\3\27\3\27\5\27\u018c\n\27\3\30\5\30\u018f\n\30\3\30\3\30\3\30\5"+
		"\30\u0194\n\30\3\30\5\30\u0197\n\30\3\31\3\31\3\31\3\31\5\31\u019d\n\31"+
		"\3\31\3\31\3\31\3\31\3\31\3\31\3\31\5\31\u01a6\n\31\3\32\3\32\3\32\3\33"+
		"\3\33\5\33\u01ad\n\33\3\33\3\33\3\33\3\33\3\33\7\33\u01b4\n\33\f\33\16"+
		"\33\u01b7\13\33\3\34\5\34\u01ba\n\34\3\34\3\34\3\34\5\34\u01bf\n\34\3"+
		"\34\5\34\u01c2\n\34\3\35\3\35\3\35\3\35\5\35\u01c8\n\35\3\35\3\35\3\35"+
		"\3\35\3\35\3\35\3\35\5\35\u01d1\n\35\5\35\u01d3\n\35\3\36\3\36\7\36\u01d7"+
		"\n\36\f\36\16\36\u01da\13\36\3\36\3\36\3\37\3\37\6\37\u01e0\n\37\r\37"+
		"\16\37\u01e1\3\37\3\37\3 \5 \u01e7\n \3 \3 \5 \u01eb\n \3!\5!\u01ee\n"+
		"!\3!\3!\3!\5!\u01f3\n!\3!\5!\u01f6\n!\3\"\3\"\3\"\3\"\5\"\u01fc\n\"\3"+
		"\"\3\"\3\"\3\"\3\"\3\"\3\"\5\"\u0205\n\"\5\"\u0207\n\"\3#\3#\7#\u020b"+
		"\n#\f#\16#\u020e\13#\3#\3#\3$\3$\6$\u0214\n$\r$\16$\u0215\3$\3$\3%\5%"+
		"\u021b\n%\3%\3%\3%\3%\5%\u0221\n%\3%\5%\u0224\n%\3%\3%\3%\3&\3&\3\'\3"+
		"\'\3\'\3\'\3\'\3\'\7\'\u0231\n\'\f\'\16\'\u0234\13\'\3(\3(\3)\3)\3*\3"+
		"*\3+\3+\7+\u023e\n+\f+\16+\u0241\13+\3+\3+\3,\3,\7,\u0247\n,\f,\16,\u024a"+
		"\13,\3,\3,\3-\3-\7-\u0250\n-\f-\16-\u0253\13-\3-\3-\3.\3.\7.\u0259\n."+
		"\f.\16.\u025c\13.\3.\3.\3/\3/\3/\3/\3\60\3\60\3\60\3\60\3\61\6\61\u0269"+
		"\n\61\r\61\16\61\u026a\3\62\3\62\3\62\5\62\u0270\n\62\3\63\3\63\6\63\u0274"+
		"\n\63\r\63\16\63\u0275\3\63\3\63\3\64\3\64\3\64\3\64\3\65\3\65\3\66\3"+
		"\66\3\66\5\66\u0283\n\66\3\67\3\67\5\67\u0287\n\67\38\38\38\38\58\u028d"+
		"\n8\39\39\39\39\39\39\39\39\59\u0297\n9\3:\3:\3:\3:\3:\3:\3:\3:\3:\5:"+
		"\u02a2\n:\3;\3;\3;\3<\3<\3<\3=\3=\3=\5=\u02ad\n=\3>\3>\3?\3?\3?\3?\3@"+
		"\3@\3@\3@\3@\3@\5@\u02bb\n@\3A\3A\3A\5A\u02c0\nA\3A\5A\u02c3\nA\3A\5A"+
		"\u02c6\nA\3A\3A\5A\u02ca\nA\3B\3B\6B\u02ce\nB\rB\16B\u02cf\3B\3B\3C\3"+
		"C\3C\3C\5C\u02d8\nC\3C\5C\u02db\nC\3D\3D\6D\u02df\nD\rD\16D\u02e0\3D\3"+
		"D\3E\3E\3E\5E\u02e8\nE\3F\5F\u02eb\nF\3F\3F\5F\u02ef\nF\3F\5F\u02f2\n"+
		"F\3F\5F\u02f5\nF\3G\3G\3G\3H\3H\3H\5H\u02fd\nH\3I\3I\5I\u0301\nI\3I\5"+
		"I\u0304\nI\3I\3I\3J\3J\3J\3J\5J\u030c\nJ\3J\3J\3K\3K\3K\3K\2\5\36\64L"+
		"L\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDF"+
		"HJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c"+
		"\u008e\u0090\u0092\u0094\2\4\3\2\24\26\4\2\23 \"#\2\u0351\2\u0097\3\2"+
		"\2\2\4\u009f\3\2\2\2\6\u00a4\3\2\2\2\b\u00a8\3\2\2\2\n\u00ab\3\2\2\2\f"+
		"\u00cd\3\2\2\2\16\u00d0\3\2\2\2\20\u00dc\3\2\2\2\22\u00e4\3\2\2\2\24\u00e6"+
		"\3\2\2\2\26\u00ea\3\2\2\2\30\u00f1\3\2\2\2\32\u00f7\3\2\2\2\34\u011e\3"+
		"\2\2\2\36\u0120\3\2\2\2 \u0132\3\2\2\2\"\u013b\3\2\2\2$\u0144\3\2\2\2"+
		"&\u014f\3\2\2\2(\u0158\3\2\2\2*\u0164\3\2\2\2,\u018b\3\2\2\2.\u018e\3"+
		"\2\2\2\60\u01a5\3\2\2\2\62\u01a7\3\2\2\2\64\u01aa\3\2\2\2\66\u01b9\3\2"+
		"\2\28\u01d2\3\2\2\2:\u01d4\3\2\2\2<\u01dd\3\2\2\2>\u01e6\3\2\2\2@\u01ed"+
		"\3\2\2\2B\u0206\3\2\2\2D\u0208\3\2\2\2F\u0211\3\2\2\2H\u021a\3\2\2\2J"+
		"\u0228\3\2\2\2L\u022a\3\2\2\2N\u0235\3\2\2\2P\u0237\3\2\2\2R\u0239\3\2"+
		"\2\2T\u023b\3\2\2\2V\u0244\3\2\2\2X\u024d\3\2\2\2Z\u0256\3\2\2\2\\\u025f"+
		"\3\2\2\2^\u0263\3\2\2\2`\u0268\3\2\2\2b\u026c\3\2\2\2d\u0271\3\2\2\2f"+
		"\u0279\3\2\2\2h\u027d\3\2\2\2j\u0282\3\2\2\2l\u0286\3\2\2\2n\u028c\3\2"+
		"\2\2p\u0296\3\2\2\2r\u02a1\3\2\2\2t\u02a3\3\2\2\2v\u02a6\3\2\2\2x\u02ac"+
		"\3\2\2\2z\u02ae\3\2\2\2|\u02b0\3\2\2\2~\u02ba\3\2\2\2\u0080\u02c9\3\2"+
		"\2\2\u0082\u02cb\3\2\2\2\u0084\u02d3\3\2\2\2\u0086\u02dc\3\2\2\2\u0088"+
		"\u02e7\3\2\2\2\u008a\u02ea\3\2\2\2\u008c\u02f6\3\2\2\2\u008e\u02f9\3\2"+
		"\2\2\u0090\u02fe\3\2\2\2\u0092\u0307\3\2\2\2\u0094\u030f\3\2\2\2\u0096"+
		"\u0098\5\4\3\2\u0097\u0096\3\2\2\2\u0098\u0099\3\2\2\2\u0099\u0097\3\2"+
		"\2\2\u0099\u009a\3\2\2\2\u009a\3\3\2\2\2\u009b\u00a0\5\u0080A\2\u009c"+
		"\u00a0\5\u0092J\2\u009d\u00a0\5\6\4\2\u009e\u00a0\5\b\5\2\u009f\u009b"+
		"\3\2\2\2\u009f\u009c\3\2\2\2\u009f\u009d\3\2\2\2\u009f\u009e\3\2\2\2\u00a0"+
		"\5\3\2\2\2\u00a1\u00a5\5\n\6\2\u00a2\u00a5\5\20\t\2\u00a3\u00a5\5H%\2"+
		"\u00a4\u00a1\3\2\2\2\u00a4\u00a2\3\2\2\2\u00a4\u00a3\3\2\2\2\u00a5\7\3"+
		"\2\2\2\u00a6\u00a9\5\f\7\2\u00a7\u00a9\5\22\n\2\u00a8\u00a6\3\2\2\2\u00a8"+
		"\u00a7\3\2\2\2\u00a9\t\3\2\2\2\u00aa\u00ac\5P)\2\u00ab\u00aa\3\2\2\2\u00ab"+
		"\u00ac\3\2\2\2\u00ac\u00ad\3\2\2\2\u00ad\u00af\7\27\2\2\u00ae\u00b0\5"+
		"`\61\2\u00af\u00ae\3\2\2\2\u00af\u00b0\3\2\2\2\u00b0\u00b1\3\2\2\2\u00b1"+
		"\u00b3\7\3\2\2\u00b2\u00b4\5\16\b\2\u00b3\u00b2\3\2\2\2\u00b4\u00b5\3"+
		"\2\2\2\u00b5\u00b3\3\2\2\2\u00b5\u00b6\3\2\2\2\u00b6\u00b7\3\2\2\2\u00b7"+
		"\u00b8\7\4\2\2\u00b8\13\3\2\2\2\u00b9\u00ba\7\37\2\2\u00ba\u00bc\7\27"+
		"\2\2\u00bb\u00bd\5`\61\2\u00bc\u00bb\3\2\2\2\u00bc\u00bd\3\2\2\2\u00bd"+
		"\u00be\3\2\2\2\u00be\u00c0\7\3\2\2\u00bf\u00c1\5\16\b\2\u00c0\u00bf\3"+
		"\2\2\2\u00c1\u00c2\3\2\2\2\u00c2\u00c0\3\2\2\2\u00c2\u00c3\3\2\2\2\u00c3"+
		"\u00c4\3\2\2\2\u00c4\u00c5\7\4\2\2\u00c5\u00ce\3\2\2\2\u00c6\u00c7\7\37"+
		"\2\2\u00c7\u00c9\7\27\2\2\u00c8\u00ca\5`\61\2\u00c9\u00c8\3\2\2\2\u00ca"+
		"\u00cb\3\2\2\2\u00cb\u00c9\3\2\2\2\u00cb\u00cc\3\2\2\2\u00cc\u00ce\3\2"+
		"\2\2\u00cd\u00b9\3\2\2\2\u00cd\u00c6\3\2\2\2\u00ce\r\3\2\2\2\u00cf\u00d1"+
		"\5P)\2\u00d0\u00cf\3\2\2\2\u00d0\u00d1\3\2\2\2\u00d1\u00d2\3\2\2\2\u00d2"+
		"\u00d3\5N(\2\u00d3\u00d4\7\5\2\2\u00d4\u00d5\5z>\2\u00d5\17\3\2\2\2\u00d6"+
		"\u00dd\5\26\f\2\u00d7\u00dd\5\32\16\2\u00d8\u00dd\5*\26\2\u00d9\u00dd"+
		"\5.\30\2\u00da\u00dd\5\66\34\2\u00db\u00dd\5@!\2\u00dc\u00d6\3\2\2\2\u00dc"+
		"\u00d7\3\2\2\2\u00dc\u00d8\3\2\2\2\u00dc\u00d9\3\2\2\2\u00dc\u00da\3\2"+
		"\2\2\u00dc\u00db\3\2\2\2\u00dd\21\3\2\2\2\u00de\u00e5\5\34\17\2\u00df"+
		"\u00e5\5,\27\2\u00e0\u00e5\5\60\31\2\u00e1\u00e5\5\30\r\2\u00e2\u00e5"+
		"\58\35\2\u00e3\u00e5\5B\"\2\u00e4\u00de\3\2\2\2\u00e4\u00df\3\2\2\2\u00e4"+
		"\u00e0\3\2\2\2\u00e4\u00e1\3\2\2\2\u00e4\u00e2\3\2\2\2\u00e4\u00e3\3\2"+
		"\2\2\u00e5\23\3\2\2\2\u00e6\u00e7\7\3\2\2\u00e7\u00e8\7\4\2\2\u00e8\25"+
		"\3\2\2\2\u00e9\u00eb\5P)\2\u00ea\u00e9\3\2\2\2\u00ea\u00eb\3\2\2\2\u00eb"+
		"\u00ec\3\2\2\2\u00ec\u00ed\7\30\2\2\u00ed\u00ef\5n8\2\u00ee\u00f0\5`\61"+
		"\2\u00ef\u00ee\3\2\2\2\u00ef\u00f0\3\2\2\2\u00f0\27\3\2\2\2\u00f1\u00f2"+
		"\7\37\2\2\u00f2\u00f3\7\30\2\2\u00f3\u00f4\5n8\2\u00f4\u00f5\5`\61\2\u00f5"+
		"\31\3\2\2\2\u00f6\u00f8\5P)\2\u00f7\u00f6\3\2\2\2\u00f7\u00f8\3\2\2\2"+
		"\u00f8\u00f9\3\2\2\2\u00f9\u00fa\7\31\2\2\u00fa\u00fc\5n8\2\u00fb\u00fd"+
		"\5\36\20\2\u00fc\u00fb\3\2\2\2\u00fc\u00fd\3\2\2\2\u00fd\u00ff\3\2\2\2"+
		"\u00fe\u0100\5`\61\2\u00ff\u00fe\3\2\2\2\u00ff\u0100\3\2\2\2\u0100\u0102"+
		"\3\2\2\2\u0101\u0103\5 \21\2\u0102\u0101\3\2\2\2\u0102\u0103\3\2\2\2\u0103"+
		"\33\3\2\2\2\u0104\u0105\7\37\2\2\u0105\u0106\7\31\2\2\u0106\u0108\5n8"+
		"\2\u0107\u0109\5\36\20\2\u0108\u0107\3\2\2\2\u0108\u0109\3\2\2\2\u0109"+
		"\u010b\3\2\2\2\u010a\u010c\5`\61\2\u010b\u010a\3\2\2\2\u010b\u010c\3\2"+
		"\2\2\u010c\u010d\3\2\2\2\u010d\u010e\5\"\22\2\u010e\u011f\3\2\2\2\u010f"+
		"\u0110\7\37\2\2\u0110\u0111\7\31\2\2\u0111\u0113\5n8\2\u0112\u0114\5\36"+
		"\20\2\u0113\u0112\3\2\2\2\u0113\u0114\3\2\2\2\u0114\u0115\3\2\2\2\u0115"+
		"\u0117\5`\61\2\u0116\u0118\5\24\13\2\u0117\u0116\3\2\2\2\u0117\u0118\3"+
		"\2\2\2\u0118\u011f\3\2\2\2\u0119\u011a\7\37\2\2\u011a\u011b\7\31\2\2\u011b"+
		"\u011c\5n8\2\u011c\u011d\5\36\20\2\u011d\u011f\3\2\2\2\u011e\u0104\3\2"+
		"\2\2\u011e\u010f\3\2\2\2\u011e\u0119\3\2\2\2\u011f\35\3\2\2\2\u0120\u0121"+
		"\b\20\1\2\u0121\u0123\7\33\2\2\u0122\u0124\7\6\2\2\u0123\u0122\3\2\2\2"+
		"\u0123\u0124\3\2\2\2\u0124\u0126\3\2\2\2\u0125\u0127\5z>\2\u0126\u0125"+
		"\3\2\2\2\u0127\u0128\3\2\2\2\u0128\u0126\3\2\2\2\u0128\u0129\3\2\2\2\u0129"+
		"\u012f\3\2\2\2\u012a\u012b\f\3\2\2\u012b\u012c\7\6\2\2\u012c\u012e\5z"+
		">\2\u012d\u012a\3\2\2\2\u012e\u0131\3\2\2\2\u012f\u012d\3\2\2\2\u012f"+
		"\u0130\3\2\2\2\u0130\37\3\2\2\2\u0131\u012f\3\2\2\2\u0132\u0136\7\3\2"+
		"\2\u0133\u0135\5$\23\2\u0134\u0133\3\2\2\2\u0135\u0138\3\2\2\2\u0136\u0134"+
		"\3\2\2\2\u0136\u0137\3\2\2\2\u0137\u0139\3\2\2\2\u0138\u0136\3\2\2\2\u0139"+
		"\u013a\7\4\2\2\u013a!\3\2\2\2\u013b\u013d\7\3\2\2\u013c\u013e\5$\23\2"+
		"\u013d\u013c\3\2\2\2\u013e\u013f\3\2\2\2\u013f\u013d\3\2\2\2\u013f\u0140"+
		"\3\2\2\2\u0140\u0141\3\2\2\2\u0141\u0142\7\4\2\2\u0142#\3\2\2\2\u0143"+
		"\u0145\5P)\2\u0144\u0143\3\2\2\2\u0144\u0145\3\2\2\2\u0145\u0146\3\2\2"+
		"\2\u0146\u0148\5n8\2\u0147\u0149\5&\24\2\u0148\u0147\3\2\2\2\u0148\u0149"+
		"\3\2\2\2\u0149\u014a\3\2\2\2\u014a\u014b\7\5\2\2\u014b\u014d\5x=\2\u014c"+
		"\u014e\5`\61\2\u014d\u014c\3\2\2\2\u014d\u014e\3\2\2\2\u014e%\3\2\2\2"+
		"\u014f\u0151\7\7\2\2\u0150\u0152\5(\25\2\u0151\u0150\3\2\2\2\u0152\u0153"+
		"\3\2\2\2\u0153\u0151\3\2\2\2\u0153\u0154\3\2\2\2\u0154\u0155\3\2\2\2\u0155"+
		"\u0156\7\b\2\2\u0156\'\3\2\2\2\u0157\u0159\5P)\2\u0158\u0157\3\2\2\2\u0158"+
		"\u0159\3\2\2\2\u0159\u015a\3\2\2\2\u015a\u015b\5n8\2\u015b\u015c\7\5\2"+
		"\2\u015c\u015e\5x=\2\u015d\u015f\5v<\2\u015e\u015d\3\2\2\2\u015e\u015f"+
		"\3\2\2\2\u015f\u0161\3\2\2\2\u0160\u0162\5`\61\2\u0161\u0160\3\2\2\2\u0161"+
		"\u0162\3\2\2\2\u0162)\3\2\2\2\u0163\u0165\5P)\2\u0164\u0163\3\2\2\2\u0164"+
		"\u0165\3\2\2\2\u0165\u0166\3\2\2\2\u0166\u0167\7\32\2\2\u0167\u0169\5"+
		"n8\2\u0168\u016a\5\36\20\2\u0169\u0168\3\2\2\2\u0169\u016a\3\2\2\2\u016a"+
		"\u016c\3\2\2\2\u016b\u016d\5`\61\2\u016c\u016b\3\2\2\2\u016c\u016d\3\2"+
		"\2\2\u016d\u016f\3\2\2\2\u016e\u0170\5 \21\2\u016f\u016e\3\2\2\2\u016f"+
		"\u0170\3\2\2\2\u0170+\3\2\2\2\u0171\u0172\7\37\2\2\u0172\u0173\7\32\2"+
		"\2\u0173\u0175\5n8\2\u0174\u0176\5\36\20\2\u0175\u0174\3\2\2\2\u0175\u0176"+
		"\3\2\2\2\u0176\u0178\3\2\2\2\u0177\u0179\5`\61\2\u0178\u0177\3\2\2\2\u0178"+
		"\u0179\3\2\2\2\u0179\u017a\3\2\2\2\u017a\u017b\5\"\22\2\u017b\u018c\3"+
		"\2\2\2\u017c\u017d\7\37\2\2\u017d\u017e\7\32\2\2\u017e\u0180\5n8\2\u017f"+
		"\u0181\5\36\20\2\u0180\u017f\3\2\2\2\u0180\u0181\3\2\2\2\u0181\u0182\3"+
		"\2\2\2\u0182\u0184\5`\61\2\u0183\u0185\5\24\13\2\u0184\u0183\3\2\2\2\u0184"+
		"\u0185\3\2\2\2\u0185\u018c\3\2\2\2\u0186\u0187\7\37\2\2\u0187\u0188\7"+
		"\32\2\2\u0188\u0189\5n8\2\u0189\u018a\5\36\20\2\u018a\u018c\3\2\2\2\u018b"+
		"\u0171\3\2\2\2\u018b\u017c\3\2\2\2\u018b\u0186\3\2\2\2\u018c-\3\2\2\2"+
		"\u018d\u018f\5P)\2\u018e\u018d\3\2\2\2\u018e\u018f\3\2\2\2\u018f\u0190"+
		"\3\2\2\2\u0190\u0191\7\35\2\2\u0191\u0193\5n8\2\u0192\u0194\5`\61\2\u0193"+
		"\u0192\3\2\2\2\u0193\u0194\3\2\2\2\u0194\u0196\3\2\2\2\u0195\u0197\5\62"+
		"\32\2\u0196\u0195\3\2\2\2\u0196\u0197\3\2\2\2\u0197/\3\2\2\2\u0198\u0199"+
		"\7\37\2\2\u0199\u019a\7\35\2\2\u019a\u019c\5n8\2\u019b\u019d\5`\61\2\u019c"+
		"\u019b\3\2\2\2\u019c\u019d\3\2\2\2\u019d\u019e\3\2\2\2\u019e\u019f\5\62"+
		"\32\2\u019f\u01a6\3\2\2\2\u01a0\u01a1\7\37\2\2\u01a1\u01a2\7\35\2\2\u01a2"+
		"\u01a3\5n8\2\u01a3\u01a4\5`\61\2\u01a4\u01a6\3\2\2\2\u01a5\u0198\3\2\2"+
		"\2\u01a5\u01a0\3\2\2\2\u01a6\61\3\2\2\2\u01a7\u01a8\7\t\2\2\u01a8\u01a9"+
		"\5\64\33\2\u01a9\63\3\2\2\2\u01aa\u01ac\b\33\1\2\u01ab\u01ad\7\n\2\2\u01ac"+
		"\u01ab\3\2\2\2\u01ac\u01ad\3\2\2\2\u01ad\u01ae\3\2\2\2\u01ae\u01af\5z"+
		">\2\u01af\u01b5\3\2\2\2\u01b0\u01b1\f\3\2\2\u01b1\u01b2\7\n\2\2\u01b2"+
		"\u01b4\5z>\2\u01b3\u01b0\3\2\2\2\u01b4\u01b7\3\2\2\2\u01b5\u01b3\3\2\2"+
		"\2\u01b5\u01b6\3\2\2\2\u01b6\65\3\2\2\2\u01b7\u01b5\3\2\2\2\u01b8\u01ba"+
		"\5P)\2\u01b9\u01b8\3\2\2\2\u01b9\u01ba\3\2\2\2\u01ba\u01bb\3\2\2\2\u01bb"+
		"\u01bc\7\34\2\2\u01bc\u01be\5n8\2\u01bd\u01bf\5`\61\2\u01be\u01bd\3\2"+
		"\2\2\u01be\u01bf\3\2\2\2\u01bf\u01c1\3\2\2\2\u01c0\u01c2\5:\36\2\u01c1"+
		"\u01c0\3\2\2\2\u01c1\u01c2\3\2\2\2\u01c2\67\3\2\2\2\u01c3\u01c4\7\37\2"+
		"\2\u01c4\u01c5\7\34\2\2\u01c5\u01c7\5n8\2\u01c6\u01c8\5`\61\2\u01c7\u01c6"+
		"\3\2\2\2\u01c7\u01c8\3\2\2\2\u01c8\u01c9\3\2\2\2\u01c9\u01ca\5<\37\2\u01ca"+
		"\u01d3\3\2\2\2\u01cb\u01cc\7\37\2\2\u01cc\u01cd\7\34\2\2\u01cd\u01ce\5"+
		"n8\2\u01ce\u01d0\5`\61\2\u01cf\u01d1\5\24\13\2\u01d0\u01cf\3\2\2\2\u01d0"+
		"\u01d1\3\2\2\2\u01d1\u01d3\3\2\2\2\u01d2\u01c3\3\2\2\2\u01d2\u01cb\3\2"+
		"\2\2\u01d39\3\2\2\2\u01d4\u01d8\7\3\2\2\u01d5\u01d7\5> \2\u01d6\u01d5"+
		"\3\2\2\2\u01d7\u01da\3\2\2\2\u01d8\u01d6\3\2\2\2\u01d8\u01d9\3\2\2\2\u01d9"+
		"\u01db\3\2\2\2\u01da\u01d8\3\2\2\2\u01db\u01dc\7\4\2\2\u01dc;\3\2\2\2"+
		"\u01dd\u01df\7\3\2\2\u01de\u01e0\5> \2\u01df\u01de\3\2\2\2\u01e0\u01e1"+
		"\3\2\2\2\u01e1\u01df\3\2\2\2\u01e1\u01e2\3\2\2\2\u01e2\u01e3\3\2\2\2\u01e3"+
		"\u01e4\7\4\2\2\u01e4=\3\2\2\2\u01e5\u01e7\5P)\2\u01e6\u01e5\3\2\2\2\u01e6"+
		"\u01e7\3\2\2\2\u01e7\u01e8\3\2\2\2\u01e8\u01ea\5R*\2\u01e9\u01eb\5`\61"+
		"\2\u01ea\u01e9\3\2\2\2\u01ea\u01eb\3\2\2\2\u01eb?\3\2\2\2\u01ec\u01ee"+
		"\5P)\2\u01ed\u01ec\3\2\2\2\u01ed\u01ee\3\2\2\2\u01ee\u01ef\3\2\2\2\u01ef"+
		"\u01f0\7\36\2\2\u01f0\u01f2\5n8\2\u01f1\u01f3\5`\61\2\u01f2\u01f1\3\2"+
		"\2\2\u01f2\u01f3\3\2\2\2\u01f3\u01f5\3\2\2\2\u01f4\u01f6\5D#\2\u01f5\u01f4"+
		"\3\2\2\2\u01f5\u01f6\3\2\2\2\u01f6A\3\2\2\2\u01f7\u01f8\7\37\2\2\u01f8"+
		"\u01f9\7\36\2\2\u01f9\u01fb\5n8\2\u01fa\u01fc\5`\61\2\u01fb\u01fa\3\2"+
		"\2\2\u01fb\u01fc\3\2\2\2\u01fc\u01fd\3\2\2\2\u01fd\u01fe\5F$\2\u01fe\u0207"+
		"\3\2\2\2\u01ff\u0200\7\37\2\2\u0200\u0201\7\36\2\2\u0201\u0202\5n8\2\u0202"+
		"\u0204\5`\61\2\u0203\u0205\5\24\13\2\u0204\u0203\3\2\2\2\u0204\u0205\3"+
		"\2\2\2\u0205\u0207\3\2\2\2\u0206\u01f7\3\2\2\2\u0206\u01ff\3\2\2\2\u0207"+
		"C\3\2\2\2\u0208\u020c\7\3\2\2\u0209\u020b\5(\25\2\u020a\u0209\3\2\2\2"+
		"\u020b\u020e\3\2\2\2\u020c\u020a\3\2\2\2\u020c\u020d\3\2\2\2\u020d\u020f"+
		"\3\2\2\2\u020e\u020c\3\2\2\2\u020f\u0210\7\4\2\2\u0210E\3\2\2\2\u0211"+
		"\u0213\7\3\2\2\u0212\u0214\5(\25\2\u0213\u0212\3\2\2\2\u0214\u0215\3\2"+
		"\2\2\u0215\u0213\3\2\2\2\u0215\u0216\3\2\2\2\u0216\u0217\3\2\2\2\u0217"+
		"\u0218\7\4\2\2\u0218G\3\2\2\2\u0219\u021b\5P)\2\u021a\u0219\3\2\2\2\u021a"+
		"\u021b\3\2\2\2\u021b\u021c\3\2\2\2\u021c\u021d\7 \2\2\u021d\u021e\7\13"+
		"\2\2\u021e\u0220\5n8\2\u021f\u0221\5&\24\2\u0220\u021f\3\2\2\2\u0220\u0221"+
		"\3\2\2\2\u0221\u0223\3\2\2\2\u0222\u0224\7\"\2\2\u0223\u0222\3\2\2\2\u0223"+
		"\u0224\3\2\2\2\u0224\u0225\3\2\2\2\u0225\u0226\7!\2\2\u0226\u0227\5L\'"+
		"\2\u0227I\3\2\2\2\u0228\u0229\5n8\2\u0229K\3\2\2\2\u022a\u022b\b\'\1\2"+
		"\u022b\u022c\5J&\2\u022c\u0232\3\2\2\2\u022d\u022e\f\3\2\2\u022e\u022f"+
		"\7\n\2\2\u022f\u0231\5J&\2\u0230\u022d\3\2\2\2\u0231\u0234\3\2\2\2\u0232"+
		"\u0230\3\2\2\2\u0232\u0233\3\2\2\2\u0233M\3\2\2\2\u0234\u0232\3\2\2\2"+
		"\u0235\u0236\t\2\2\2\u0236O\3\2\2\2\u0237\u0238\7&\2\2\u0238Q\3\2\2\2"+
		"\u0239\u023a\5l\67\2\u023aS\3\2\2\2\u023b\u023f\7\f\2\2\u023c\u023e\5"+
		"p9\2\u023d\u023c\3\2\2\2\u023e\u0241\3\2\2\2\u023f\u023d\3\2\2\2\u023f"+
		"\u0240\3\2\2\2\u0240\u0242\3\2\2\2\u0241\u023f\3\2\2\2\u0242\u0243\7\r"+
		"\2\2\u0243U\3\2\2\2\u0244\u0248\7\f\2\2\u0245\u0247\5r:\2\u0246\u0245"+
		"\3\2\2\2\u0247\u024a\3\2\2\2\u0248\u0246\3\2\2\2\u0248\u0249\3\2\2\2\u0249"+
		"\u024b\3\2\2\2\u024a\u0248\3\2\2\2\u024b\u024c\7\r\2\2\u024cW\3\2\2\2"+
		"\u024d\u0251\7\3\2\2\u024e\u0250\5\\/\2\u024f\u024e\3\2\2\2\u0250\u0253"+
		"\3\2\2\2\u0251\u024f\3\2\2\2\u0251\u0252\3\2\2\2\u0252\u0254\3\2\2\2\u0253"+
		"\u0251\3\2\2\2\u0254\u0255\7\4\2\2\u0255Y\3\2\2\2\u0256\u025a\7\3\2\2"+
		"\u0257\u0259\5^\60\2\u0258\u0257\3\2\2\2\u0259\u025c\3\2\2\2\u025a\u0258"+
		"\3\2\2\2\u025a\u025b\3\2\2\2\u025b\u025d\3\2\2\2\u025c\u025a\3\2\2\2\u025d"+
		"\u025e\7\4\2\2\u025e[\3\2\2\2\u025f\u0260\5n8\2\u0260\u0261\7\5\2\2\u0261"+
		"\u0262\5p9\2\u0262]\3\2\2\2\u0263\u0264\5n8\2\u0264\u0265\7\5\2\2\u0265"+
		"\u0266\5r:\2\u0266_\3\2\2\2\u0267\u0269\5b\62\2\u0268\u0267\3\2\2\2\u0269"+
		"\u026a\3\2\2\2\u026a\u0268\3\2\2\2\u026a\u026b\3\2\2\2\u026ba\3\2\2\2"+
		"\u026c\u026d\7\13\2\2\u026d\u026f\5n8\2\u026e\u0270\5d\63\2\u026f\u026e"+
		"\3\2\2\2\u026f\u0270\3\2\2\2\u0270c\3\2\2\2\u0271\u0273\7\7\2\2\u0272"+
		"\u0274\5f\64\2\u0273\u0272\3\2\2\2\u0274\u0275\3\2\2\2\u0275\u0273\3\2"+
		"\2\2\u0275\u0276\3\2\2\2\u0276\u0277\3\2\2\2\u0277\u0278\7\b\2\2\u0278"+
		"e\3\2\2\2\u0279\u027a\5n8\2\u027a\u027b\7\5\2\2\u027b\u027c\5r:\2\u027c"+
		"g\3\2\2\2\u027d\u027e\t\3\2\2\u027ei\3\2\2\2\u027f\u0283\5h\65\2\u0280"+
		"\u0283\7\21\2\2\u0281\u0283\7\22\2\2\u0282\u027f\3\2\2\2\u0282\u0280\3"+
		"\2\2\2\u0282\u0281\3\2\2\2\u0283k\3\2\2\2\u0284\u0287\5h\65\2\u0285\u0287"+
		"\7!\2\2\u0286\u0284\3\2\2\2\u0286\u0285\3\2\2\2\u0287m\3\2\2\2\u0288\u028d"+
		"\5h\65\2\u0289\u028d\7\21\2\2\u028a\u028d\7\22\2\2\u028b\u028d\7!\2\2"+
		"\u028c\u0288\3\2\2\2\u028c\u0289\3\2\2\2\u028c\u028a\3\2\2\2\u028c\u028b"+
		"\3\2\2\2\u028do\3\2\2\2\u028e\u0297\7&\2\2\u028f\u0297\7$\2\2\u0290\u0297"+
		"\7%\2\2\u0291\u0297\7\21\2\2\u0292\u0297\7\22\2\2\u0293\u0297\5R*\2\u0294"+
		"\u0297\5T+\2\u0295\u0297\5X-\2\u0296\u028e\3\2\2\2\u0296\u028f\3\2\2\2"+
		"\u0296\u0290\3\2\2\2\u0296\u0291\3\2\2\2\u0296\u0292\3\2\2\2\u0296\u0293"+
		"\3\2\2\2\u0296\u0294\3\2\2\2\u0296\u0295\3\2\2\2\u0297q\3\2\2\2\u0298"+
		"\u02a2\5t;\2\u0299\u02a2\7&\2\2\u029a\u02a2\7$\2\2\u029b\u02a2\7%\2\2"+
		"\u029c\u02a2\7\21\2\2\u029d\u02a2\7\22\2\2\u029e\u02a2\5R*\2\u029f\u02a2"+
		"\5V,\2\u02a0\u02a2\5Z.\2\u02a1\u0298\3\2\2\2\u02a1\u0299\3\2\2\2\u02a1"+
		"\u029a\3\2\2\2\u02a1\u029b\3\2\2\2\u02a1\u029c\3\2\2\2\u02a1\u029d\3\2"+
		"\2\2\u02a1\u029e\3\2\2\2\u02a1\u029f\3\2\2\2\u02a1\u02a0\3\2\2\2\u02a2"+
		"s\3\2\2\2\u02a3\u02a4\7\16\2\2\u02a4\u02a5\5n8\2\u02a5u\3\2\2\2\u02a6"+
		"\u02a7\7\t\2\2\u02a7\u02a8\5p9\2\u02a8w\3\2\2\2\u02a9\u02ad\5z>\2\u02aa"+
		"\u02ad\5|?\2\u02ab\u02ad\5~@\2\u02ac\u02a9\3\2\2\2\u02ac\u02aa\3\2\2\2"+
		"\u02ac\u02ab\3\2\2\2\u02ady\3\2\2\2\u02ae\u02af\5n8\2\u02af{\3\2\2\2\u02b0"+
		"\u02b1\7\f\2\2\u02b1\u02b2\5x=\2\u02b2\u02b3\7\r\2\2\u02b3}\3\2\2\2\u02b4"+
		"\u02b5\5z>\2\u02b5\u02b6\7\17\2\2\u02b6\u02bb\3\2\2\2\u02b7\u02b8\5|?"+
		"\2\u02b8\u02b9\7\17\2\2\u02b9\u02bb\3\2\2\2\u02ba\u02b4\3\2\2\2\u02ba"+
		"\u02b7\3\2\2\2\u02bb\177\3\2\2\2\u02bc\u02ca\5\u0086D\2\u02bd\u02bf\5"+
		"N(\2\u02be\u02c0\5n8\2\u02bf\u02be\3\2\2\2\u02bf\u02c0\3\2\2\2\u02c0\u02c2"+
		"\3\2\2\2\u02c1\u02c3\5\u0082B\2\u02c2\u02c1\3\2\2\2\u02c2\u02c3\3\2\2"+
		"\2\u02c3\u02c5\3\2\2\2\u02c4\u02c6\5`\61\2\u02c5\u02c4\3\2\2\2\u02c5\u02c6"+
		"\3\2\2\2\u02c6\u02c7\3\2\2\2\u02c7\u02c8\5\u0086D\2\u02c8\u02ca\3\2\2"+
		"\2\u02c9\u02bc\3\2\2\2\u02c9\u02bd\3\2\2\2\u02ca\u0081\3\2\2\2\u02cb\u02cd"+
		"\7\7\2\2\u02cc\u02ce\5\u0084C\2\u02cd\u02cc\3\2\2\2\u02ce\u02cf\3\2\2"+
		"\2\u02cf\u02cd\3\2\2\2\u02cf\u02d0\3\2\2\2\u02d0\u02d1\3\2\2\2\u02d1\u02d2"+
		"\7\b\2\2\u02d2\u0083\3\2\2\2\u02d3\u02d4\5t;\2\u02d4\u02d5\7\5\2\2\u02d5"+
		"\u02d7\5x=\2\u02d6\u02d8\5v<\2\u02d7\u02d6\3\2\2\2\u02d7\u02d8\3\2\2\2"+
		"\u02d8\u02da\3\2\2\2\u02d9\u02db\5`\61\2\u02da\u02d9\3\2\2\2\u02da\u02db"+
		"\3\2\2\2\u02db\u0085\3\2\2\2\u02dc\u02de\7\3\2\2\u02dd\u02df\5\u0088E"+
		"\2\u02de\u02dd\3\2\2\2\u02df\u02e0\3\2\2\2\u02e0\u02de\3\2\2\2\u02e0\u02e1"+
		"\3\2\2\2\u02e1\u02e2\3\2\2\2\u02e2\u02e3\7\4\2\2\u02e3\u0087\3\2\2\2\u02e4"+
		"\u02e8\5\u008aF\2\u02e5\u02e8\5\u008eH\2\u02e6\u02e8\5\u0090I\2\u02e7"+
		"\u02e4\3\2\2\2\u02e7\u02e5\3\2\2\2\u02e7\u02e6\3\2\2\2\u02e8\u0089\3\2"+
		"\2\2\u02e9\u02eb\5\u008cG\2\u02ea\u02e9\3\2\2\2\u02ea\u02eb\3\2\2\2\u02eb"+
		"\u02ec\3\2\2\2\u02ec\u02ee\5n8\2\u02ed\u02ef\5d\63\2\u02ee\u02ed\3\2\2"+
		"\2\u02ee\u02ef\3\2\2\2\u02ef\u02f1\3\2\2\2\u02f0\u02f2\5`\61\2\u02f1\u02f0"+
		"\3\2\2\2\u02f1\u02f2\3\2\2\2\u02f2\u02f4\3\2\2\2\u02f3\u02f5\5\u0086D"+
		"\2\u02f4\u02f3\3\2\2\2\u02f4\u02f5\3\2\2\2\u02f5\u008b\3\2\2\2\u02f6\u02f7"+
		"\5n8\2\u02f7\u02f8\7\5\2\2\u02f8\u008d\3\2\2\2\u02f9\u02fa\7\20\2\2\u02fa"+
		"\u02fc\5j\66\2\u02fb\u02fd\5`\61\2\u02fc\u02fb\3\2\2\2\u02fc\u02fd\3\2"+
		"\2\2\u02fd\u008f\3\2\2\2\u02fe\u0300\7\20\2\2\u02ff\u0301\5\u0094K\2\u0300"+
		"\u02ff\3\2\2\2\u0300\u0301\3\2\2\2\u0301\u0303\3\2\2\2\u0302\u0304\5`"+
		"\61\2\u0303\u0302\3\2\2\2\u0303\u0304\3\2\2\2\u0304\u0305\3\2\2\2\u0305"+
		"\u0306\5\u0086D\2\u0306\u0091\3\2\2\2\u0307\u0308\7\23\2\2\u0308\u0309"+
		"\5j\66\2\u0309\u030b\5\u0094K\2\u030a\u030c\5`\61\2\u030b\u030a\3\2\2"+
		"\2\u030b\u030c\3\2\2\2\u030c\u030d\3\2\2\2\u030d\u030e\5\u0086D\2\u030e"+
		"\u0093\3\2\2\2\u030f\u0310\7!\2\2\u0310\u0311\5z>\2\u0311\u0095\3\2\2"+
		"\2l\u0099\u009f\u00a4\u00a8\u00ab\u00af\u00b5\u00bc\u00c2\u00cb\u00cd"+
		"\u00d0\u00dc\u00e4\u00ea\u00ef\u00f7\u00fc\u00ff\u0102\u0108\u010b\u0113"+
		"\u0117\u011e\u0123\u0128\u012f\u0136\u013f\u0144\u0148\u014d\u0153\u0158"+
		"\u015e\u0161\u0164\u0169\u016c\u016f\u0175\u0178\u0180\u0184\u018b\u018e"+
		"\u0193\u0196\u019c\u01a5\u01ac\u01b5\u01b9\u01be\u01c1\u01c7\u01d0\u01d2"+
		"\u01d8\u01e1\u01e6\u01ea\u01ed\u01f2\u01f5\u01fb\u0204\u0206\u020c\u0215"+
		"\u021a\u0220\u0223\u0232\u023f\u0248\u0251\u025a\u026a\u026f\u0275\u0282"+
		"\u0286\u028c\u0296\u02a1\u02ac\u02ba\u02bf\u02c2\u02c5\u02c9\u02cf\u02d7"+
		"\u02da\u02e0\u02e7\u02ea\u02ee\u02f1\u02f4\u02fc\u0300\u0303\u030b";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
