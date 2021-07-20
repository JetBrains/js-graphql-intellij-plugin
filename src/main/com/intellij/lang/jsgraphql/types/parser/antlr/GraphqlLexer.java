// Generated from Graphql.g4 by ANTLR 4.8

    package com.intellij.lang.jsgraphql.types.parser.antlr;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class GraphqlLexer extends Lexer {
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
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8",
			"T__9", "T__10", "T__11", "T__12", "T__13", "BooleanValue", "NullValue",
			"FRAGMENT", "QUERY", "MUTATION", "SUBSCRIPTION", "SCHEMA", "SCALAR",
			"TYPE", "INTERFACE", "IMPLEMENTS", "ENUM", "UNION", "INPUT", "EXTEND",
			"DIRECTIVE", "ON_KEYWORD", "REPEATABLE", "NAME", "IntValue", "IntegerPart",
			"NegativeSign", "NonZeroDigit", "FloatValue", "FractionalPart", "ExponentPart",
			"ExponentIndicator", "Sign", "Digit", "StringValue", "BlockStringCharacter",
			"StringCharacter", "EscapedCharacter", "EscapedUnicode", "Hex", "ExtendedSourceCharacter",
			"ExtendedSourceCharacterWithoutLineFeed", "Comment", "LF", "CR", "LineTerminator",
			"Space", "Tab", "Comma", "UnicodeBOM"
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


	    public boolean isDigit(int c) {
	        return c >= '0' && c <= '9';
	    }
	    public boolean isNameStart(int c) {
	        return '_' == c ||
	          (c >= 'A' && c <= 'Z') ||
	          (c >= 'a' && c <= 'z');
	    }
	    public boolean isDot(int c) {
	        return '.' == c;
	    }


	public GraphqlLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Graphql.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 33:
			return IntValue_sempred((RuleContext)_localctx, predIndex);
		case 37:
			return FloatValue_sempred((RuleContext)_localctx, predIndex);
		case 43:
			return StringValue_sempred((RuleContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean IntValue_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return  !isDigit(_input.LA(1)) && !isDot(_input.LA(1)) && !isNameStart(_input.LA(1))  ;
		}
		return true;
	}
	private boolean FloatValue_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return  !isDigit(_input.LA(1)) && !isDot(_input.LA(1)) && !isNameStart(_input.LA(1))  ;
		case 2:
			return  !isDigit(_input.LA(1)) && !isDot(_input.LA(1)) && !isNameStart(_input.LA(1))  ;
		case 3:
			return  !isDigit(_input.LA(1)) && !isDot(_input.LA(1)) && !isNameStart(_input.LA(1))  ;
		}
		return true;
	}
	private boolean StringValue_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 4:
			return  _input.LA(1) != '"';
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2.\u01c8\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\3\2\3"+
		"\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13"+
		"\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20"+
		"\3\20\3\20\3\20\3\20\3\20\5\20\u00a1\n\20\3\21\3\21\3\21\3\21\3\21\3\22"+
		"\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\23"+
		"\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\26\3\26\3\26\3\26\3\26\3\26"+
		"\3\26\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\31"+
		"\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\33\3\33\3\33\3\33\3\33\3\34\3\34\3\34"+
		"\3\34\3\34\3\34\3\35\3\35\3\35\3\35\3\35\3\35\3\36\3\36\3\36\3\36\3\36"+
		"\3\36\3\36\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3 \3 \3 "+
		"\3!\3!\3!\3!\3!\3!\3!\3!\3!\3!\3!\3\"\3\"\7\"\u0127\n\"\f\"\16\"\u012a"+
		"\13\"\3#\3#\3#\3$\5$\u0130\n$\3$\3$\5$\u0134\n$\3$\3$\7$\u0138\n$\f$\16"+
		"$\u013b\13$\5$\u013d\n$\3%\3%\3&\3&\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3"+
		"\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\5\'\u0153\n\'\3(\3(\6(\u0157\n(\r(\16("+
		"\u0158\3)\3)\5)\u015d\n)\3)\6)\u0160\n)\r)\16)\u0161\3*\3*\3+\3+\3,\3"+
		",\3-\3-\3-\3-\3-\3-\6-\u0170\n-\r-\16-\u0171\3-\3-\3-\3-\3-\3-\3-\7-\u017b"+
		"\n-\f-\16-\u017e\13-\3-\3-\3-\5-\u0183\n-\3.\3.\3.\3.\3.\5.\u018a\n.\3"+
		"/\5/\u018d\n/\3/\3/\3/\3/\3/\3/\5/\u0195\n/\3\60\3\60\3\61\3\61\3\61\3"+
		"\61\3\61\3\62\3\62\3\63\3\63\3\64\3\64\3\65\3\65\7\65\u01a6\n\65\f\65"+
		"\16\65\u01a9\13\65\3\65\3\65\3\66\3\66\3\66\3\66\3\67\3\67\3\67\3\67\3"+
		"8\38\38\38\39\39\39\39\3:\3:\3:\3:\3;\3;\3;\3;\3<\3<\3<\3<\3\u017c\2="+
		"\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20"+
		"\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37"+
		"= ?!A\"C#E$G\2I\2K\2M%O\2Q\2S\2U\2W\2Y&[\2]\2_\2a\2c\2e\2g\2i\'k(m)o*"+
		"q+s,u-w.\3\2\16\5\2C\\aac|\6\2\62;C\\aac|\4\2GGgg\4\2--//\n\2$$\61\61"+
		"^^ddhhppttvv\5\2\62;CHch\3\2\f\f\3\2\17\17\3\2\u202a\u202b\3\2\"\"\3\2"+
		"\13\13\3\2\uff01\uff01\5\6\2\13\2\13\2\"\2#\2%\2]\2_\2\1\22\5\2\13\2\f"+
		"\2\17\2\17\2\"\2\1\22\4\2\13\2\13\2\"\2\1\22\u01cb\2\3\3\2\2\2\2\5\3\2"+
		"\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21"+
		"\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2"+
		"\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3"+
		"\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3"+
		"\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3"+
		"\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2M\3\2\2\2\2Y\3\2\2\2\2i\3\2\2"+
		"\2\2k\3\2\2\2\2m\3\2\2\2\2o\3\2\2\2\2q\3\2\2\2\2s\3\2\2\2\2u\3\2\2\2\2"+
		"w\3\2\2\2\3y\3\2\2\2\5{\3\2\2\2\7}\3\2\2\2\t\177\3\2\2\2\13\u0081\3\2"+
		"\2\2\r\u0083\3\2\2\2\17\u0085\3\2\2\2\21\u0087\3\2\2\2\23\u0089\3\2\2"+
		"\2\25\u008b\3\2\2\2\27\u008d\3\2\2\2\31\u008f\3\2\2\2\33\u0091\3\2\2\2"+
		"\35\u0093\3\2\2\2\37\u00a0\3\2\2\2!\u00a2\3\2\2\2#\u00a7\3\2\2\2%\u00b0"+
		"\3\2\2\2\'\u00b6\3\2\2\2)\u00bf\3\2\2\2+\u00cc\3\2\2\2-\u00d3\3\2\2\2"+
		"/\u00da\3\2\2\2\61\u00df\3\2\2\2\63\u00e9\3\2\2\2\65\u00f4\3\2\2\2\67"+
		"\u00f9\3\2\2\29\u00ff\3\2\2\2;\u0105\3\2\2\2=\u010c\3\2\2\2?\u0116\3\2"+
		"\2\2A\u0119\3\2\2\2C\u0124\3\2\2\2E\u012b\3\2\2\2G\u013c\3\2\2\2I\u013e"+
		"\3\2\2\2K\u0140\3\2\2\2M\u0152\3\2\2\2O\u0154\3\2\2\2Q\u015a\3\2\2\2S"+
		"\u0163\3\2\2\2U\u0165\3\2\2\2W\u0167\3\2\2\2Y\u0182\3\2\2\2[\u0189\3\2"+
		"\2\2]\u0194\3\2\2\2_\u0196\3\2\2\2a\u0198\3\2\2\2c\u019d\3\2\2\2e\u019f"+
		"\3\2\2\2g\u01a1\3\2\2\2i\u01a3\3\2\2\2k\u01ac\3\2\2\2m\u01b0\3\2\2\2o"+
		"\u01b4\3\2\2\2q\u01b8\3\2\2\2s\u01bc\3\2\2\2u\u01c0\3\2\2\2w\u01c4\3\2"+
		"\2\2yz\7}\2\2z\4\3\2\2\2{|\7\177\2\2|\6\3\2\2\2}~\7<\2\2~\b\3\2\2\2\177"+
		"\u0080\7(\2\2\u0080\n\3\2\2\2\u0081\u0082\7*\2\2\u0082\f\3\2\2\2\u0083"+
		"\u0084\7+\2\2\u0084\16\3\2\2\2\u0085\u0086\7?\2\2\u0086\20\3\2\2\2\u0087"+
		"\u0088\7~\2\2\u0088\22\3\2\2\2\u0089\u008a\7B\2\2\u008a\24\3\2\2\2\u008b"+
		"\u008c\7]\2\2\u008c\26\3\2\2\2\u008d\u008e\7_\2\2\u008e\30\3\2\2\2\u008f"+
		"\u0090\7&\2\2\u0090\32\3\2\2\2\u0091\u0092\7#\2\2\u0092\34\3\2\2\2\u0093"+
		"\u0094\7\60\2\2\u0094\u0095\7\60\2\2\u0095\u0096\7\60\2\2\u0096\36\3\2"+
		"\2\2\u0097\u0098\7v\2\2\u0098\u0099\7t\2\2\u0099\u009a\7w\2\2\u009a\u00a1"+
		"\7g\2\2\u009b\u009c\7h\2\2\u009c\u009d\7c\2\2\u009d\u009e\7n\2\2\u009e"+
		"\u009f\7u\2\2\u009f\u00a1\7g\2\2\u00a0\u0097\3\2\2\2\u00a0\u009b\3\2\2"+
		"\2\u00a1 \3\2\2\2\u00a2\u00a3\7p\2\2\u00a3\u00a4\7w\2\2\u00a4\u00a5\7"+
		"n\2\2\u00a5\u00a6\7n\2\2\u00a6\"\3\2\2\2\u00a7\u00a8\7h\2\2\u00a8\u00a9"+
		"\7t\2\2\u00a9\u00aa\7c\2\2\u00aa\u00ab\7i\2\2\u00ab\u00ac\7o\2\2\u00ac"+
		"\u00ad\7g\2\2\u00ad\u00ae\7p\2\2\u00ae\u00af\7v\2\2\u00af$\3\2\2\2\u00b0"+
		"\u00b1\7s\2\2\u00b1\u00b2\7w\2\2\u00b2\u00b3\7g\2\2\u00b3\u00b4\7t\2\2"+
		"\u00b4\u00b5\7{\2\2\u00b5&\3\2\2\2\u00b6\u00b7\7o\2\2\u00b7\u00b8\7w\2"+
		"\2\u00b8\u00b9\7v\2\2\u00b9\u00ba\7c\2\2\u00ba\u00bb\7v\2\2\u00bb\u00bc"+
		"\7k\2\2\u00bc\u00bd\7q\2\2\u00bd\u00be\7p\2\2\u00be(\3\2\2\2\u00bf\u00c0"+
		"\7u\2\2\u00c0\u00c1\7w\2\2\u00c1\u00c2\7d\2\2\u00c2\u00c3\7u\2\2\u00c3"+
		"\u00c4\7e\2\2\u00c4\u00c5\7t\2\2\u00c5\u00c6\7k\2\2\u00c6\u00c7\7r\2\2"+
		"\u00c7\u00c8\7v\2\2\u00c8\u00c9\7k\2\2\u00c9\u00ca\7q\2\2\u00ca\u00cb"+
		"\7p\2\2\u00cb*\3\2\2\2\u00cc\u00cd\7u\2\2\u00cd\u00ce\7e\2\2\u00ce\u00cf"+
		"\7j\2\2\u00cf\u00d0\7g\2\2\u00d0\u00d1\7o\2\2\u00d1\u00d2\7c\2\2\u00d2"+
		",\3\2\2\2\u00d3\u00d4\7u\2\2\u00d4\u00d5\7e\2\2\u00d5\u00d6\7c\2\2\u00d6"+
		"\u00d7\7n\2\2\u00d7\u00d8\7c\2\2\u00d8\u00d9\7t\2\2\u00d9.\3\2\2\2\u00da"+
		"\u00db\7v\2\2\u00db\u00dc\7{\2\2\u00dc\u00dd\7r\2\2\u00dd\u00de\7g\2\2"+
		"\u00de\60\3\2\2\2\u00df\u00e0\7k\2\2\u00e0\u00e1\7p\2\2\u00e1\u00e2\7"+
		"v\2\2\u00e2\u00e3\7g\2\2\u00e3\u00e4\7t\2\2\u00e4\u00e5\7h\2\2\u00e5\u00e6"+
		"\7c\2\2\u00e6\u00e7\7e\2\2\u00e7\u00e8\7g\2\2\u00e8\62\3\2\2\2\u00e9\u00ea"+
		"\7k\2\2\u00ea\u00eb\7o\2\2\u00eb\u00ec\7r\2\2\u00ec\u00ed\7n\2\2\u00ed"+
		"\u00ee\7g\2\2\u00ee\u00ef\7o\2\2\u00ef\u00f0\7g\2\2\u00f0\u00f1\7p\2\2"+
		"\u00f1\u00f2\7v\2\2\u00f2\u00f3\7u\2\2\u00f3\64\3\2\2\2\u00f4\u00f5\7"+
		"g\2\2\u00f5\u00f6\7p\2\2\u00f6\u00f7\7w\2\2\u00f7\u00f8\7o\2\2\u00f8\66"+
		"\3\2\2\2\u00f9\u00fa\7w\2\2\u00fa\u00fb\7p\2\2\u00fb\u00fc\7k\2\2\u00fc"+
		"\u00fd\7q\2\2\u00fd\u00fe\7p\2\2\u00fe8\3\2\2\2\u00ff\u0100\7k\2\2\u0100"+
		"\u0101\7p\2\2\u0101\u0102\7r\2\2\u0102\u0103\7w\2\2\u0103\u0104\7v\2\2"+
		"\u0104:\3\2\2\2\u0105\u0106\7g\2\2\u0106\u0107\7z\2\2\u0107\u0108\7v\2"+
		"\2\u0108\u0109\7g\2\2\u0109\u010a\7p\2\2\u010a\u010b\7f\2\2\u010b<\3\2"+
		"\2\2\u010c\u010d\7f\2\2\u010d\u010e\7k\2\2\u010e\u010f\7t\2\2\u010f\u0110"+
		"\7g\2\2\u0110\u0111\7e\2\2\u0111\u0112\7v\2\2\u0112\u0113\7k\2\2\u0113"+
		"\u0114\7x\2\2\u0114\u0115\7g\2\2\u0115>\3\2\2\2\u0116\u0117\7q\2\2\u0117"+
		"\u0118\7p\2\2\u0118@\3\2\2\2\u0119\u011a\7t\2\2\u011a\u011b\7g\2\2\u011b"+
		"\u011c\7r\2\2\u011c\u011d\7g\2\2\u011d\u011e\7c\2\2\u011e\u011f\7v\2\2"+
		"\u011f\u0120\7c\2\2\u0120\u0121\7d\2\2\u0121\u0122\7n\2\2\u0122\u0123"+
		"\7g\2\2\u0123B\3\2\2\2\u0124\u0128\t\2\2\2\u0125\u0127\t\3\2\2\u0126\u0125"+
		"\3\2\2\2\u0127\u012a\3\2\2\2\u0128\u0126\3\2\2\2\u0128\u0129\3\2\2\2\u0129"+
		"D\3\2\2\2\u012a\u0128\3\2\2\2\u012b\u012c\5G$\2\u012c\u012d\6#\2\2\u012d"+
		"F\3\2\2\2\u012e\u0130\5I%\2\u012f\u012e\3\2\2\2\u012f\u0130\3\2\2\2\u0130"+
		"\u0131\3\2\2\2\u0131\u013d\7\62\2\2\u0132\u0134\5I%\2\u0133\u0132\3\2"+
		"\2\2\u0133\u0134\3\2\2\2\u0134\u0135\3\2\2\2\u0135\u0139\5K&\2\u0136\u0138"+
		"\5W,\2\u0137\u0136\3\2\2\2\u0138\u013b\3\2\2\2\u0139\u0137\3\2\2\2\u0139"+
		"\u013a\3\2\2\2\u013a\u013d\3\2\2\2\u013b\u0139\3\2\2\2\u013c\u012f\3\2"+
		"\2\2\u013c\u0133\3\2\2\2\u013dH\3\2\2\2\u013e\u013f\7/\2\2\u013fJ\3\2"+
		"\2\2\u0140\u0141\4\63;\2\u0141L\3\2\2\2\u0142\u0143\5G$\2\u0143\u0144"+
		"\5O(\2\u0144\u0145\5Q)\2\u0145\u0146\3\2\2\2\u0146\u0147\6\'\3\2\u0147"+
		"\u0153\3\2\2\2\u0148\u0149\5G$\2\u0149\u014a\5O(\2\u014a\u014b\3\2\2\2"+
		"\u014b\u014c\6\'\4\2\u014c\u0153\3\2\2\2\u014d\u014e\5G$\2\u014e\u014f"+
		"\5Q)\2\u014f\u0150\3\2\2\2\u0150\u0151\6\'\5\2\u0151\u0153\3\2\2\2\u0152"+
		"\u0142\3\2\2\2\u0152\u0148\3\2\2\2\u0152\u014d\3\2\2\2\u0153N\3\2\2\2"+
		"\u0154\u0156\7\60\2\2\u0155\u0157\5W,\2\u0156\u0155\3\2\2\2\u0157\u0158"+
		"\3\2\2\2\u0158\u0156\3\2\2\2\u0158\u0159\3\2\2\2\u0159P\3\2\2\2\u015a"+
		"\u015c\5S*\2\u015b\u015d\5U+\2\u015c\u015b\3\2\2\2\u015c\u015d\3\2\2\2"+
		"\u015d\u015f\3\2\2\2\u015e\u0160\5W,\2\u015f\u015e\3\2\2\2\u0160\u0161"+
		"\3\2\2\2\u0161\u015f\3\2\2\2\u0161\u0162\3\2\2\2\u0162R\3\2\2\2\u0163"+
		"\u0164\t\4\2\2\u0164T\3\2\2\2\u0165\u0166\t\5\2\2\u0166V\3\2\2\2\u0167"+
		"\u0168\4\62;\2\u0168X\3\2\2\2\u0169\u016a\7$\2\2\u016a\u016b\7$\2\2\u016b"+
		"\u016c\3\2\2\2\u016c\u0183\6-\6\2\u016d\u016f\7$\2\2\u016e\u0170\5]/\2"+
		"\u016f\u016e\3\2\2\2\u0170\u0171\3\2\2\2\u0171\u016f\3\2\2\2\u0171\u0172"+
		"\3\2\2\2\u0172\u0173\3\2\2\2\u0173\u0174\7$\2\2\u0174\u0183\3\2\2\2\u0175"+
		"\u0176\7$\2\2\u0176\u0177\7$\2\2\u0177\u0178\7$\2\2\u0178\u017c\3\2\2"+
		"\2\u0179\u017b\5[.\2\u017a\u0179\3\2\2\2\u017b\u017e\3\2\2\2\u017c\u017d"+
		"\3\2\2\2\u017c\u017a\3\2\2\2\u017d\u017f\3\2\2\2\u017e\u017c\3\2\2\2\u017f"+
		"\u0180\7$\2\2\u0180\u0181\7$\2\2\u0181\u0183\7$\2\2\u0182\u0169\3\2\2"+
		"\2\u0182\u016d\3\2\2\2\u0182\u0175\3\2\2\2\u0183Z\3\2\2\2\u0184\u0185"+
		"\7^\2\2\u0185\u0186\7$\2\2\u0186\u0187\7$\2\2\u0187\u018a\7$\2\2\u0188"+
		"\u018a\5e\63\2\u0189\u0184\3\2\2\2\u0189\u0188\3\2\2\2\u018a\\\3\2\2\2"+
		"\u018b\u018d\t\16\2\2\u018c\u018b\3\2\2\2\u018d\u0195\3\2\2\2\u018e\u018f"+
		"\7^\2\2\u018f\u0190\7w\2\2\u0190\u0191\3\2\2\2\u0191\u0195\5a\61\2\u0192"+
		"\u0193\7^\2\2\u0193\u0195\5_\60\2\u0194\u018c\3\2\2\2\u0194\u018e\3\2"+
		"\2\2\u0194\u0192\3\2\2\2\u0195^\3\2\2\2\u0196\u0197\t\6\2\2\u0197`\3\2"+
		"\2\2\u0198\u0199\5c\62\2\u0199\u019a\5c\62\2\u019a\u019b\5c\62\2\u019b"+
		"\u019c\5c\62\2\u019cb\3\2\2\2\u019d\u019e\t\7\2\2\u019ed\3\2\2\2\u019f"+
		"\u01a0\t\17\2\2\u01a0f\3\2\2\2\u01a1\u01a2\t\20\2\2\u01a2h\3\2\2\2\u01a3"+
		"\u01a7\7%\2\2\u01a4\u01a6\5g\64\2\u01a5\u01a4\3\2\2\2\u01a6\u01a9\3\2"+
		"\2\2\u01a7\u01a5\3\2\2\2\u01a7\u01a8\3\2\2\2\u01a8\u01aa\3\2\2\2\u01a9"+
		"\u01a7\3\2\2\2\u01aa\u01ab\b\65\2\2\u01abj\3\2\2\2\u01ac\u01ad\t\b\2\2"+
		"\u01ad\u01ae\3\2\2\2\u01ae\u01af\b\66\3\2\u01afl\3\2\2\2\u01b0\u01b1\t"+
		"\t\2\2\u01b1\u01b2\3\2\2\2\u01b2\u01b3\b\67\3\2\u01b3n\3\2\2\2\u01b4\u01b5"+
		"\t\n\2\2\u01b5\u01b6\3\2\2\2\u01b6\u01b7\b8\3\2\u01b7p\3\2\2\2\u01b8\u01b9"+
		"\t\13\2\2\u01b9\u01ba\3\2\2\2\u01ba\u01bb\b9\3\2\u01bbr\3\2\2\2\u01bc"+
		"\u01bd\t\f\2\2\u01bd\u01be\3\2\2\2\u01be\u01bf\b:\3\2\u01bft\3\2\2\2\u01c0"+
		"\u01c1\7.\2\2\u01c1\u01c2\3\2\2\2\u01c2\u01c3\b;\3\2\u01c3v\3\2\2\2\u01c4"+
		"\u01c5\t\r\2\2\u01c5\u01c6\3\2\2\2\u01c6\u01c7\b<\3\2\u01c7x\3\2\2\2\24"+
		"\2\u00a0\u0128\u012f\u0133\u0139\u013c\u0152\u0158\u015c\u0161\u0171\u017c"+
		"\u0182\u0189\u018c\u0194\u01a7\4\2\4\2\2\5\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
