package com.jfireframework.sql.analyse.token;

import java.util.HashMap;
import java.util.Map;

public enum Symbol
{
	LEFT_PAREN("("), //
	RIGHT_PAREN(")"), //
	LEFT_BRACE("{"), //
	RIGHT_BRACE("}"), //
	LEFT_BRACKET("["), //
	RIGHT_BRACKET("]"), //
	SEMI(";"), //
	COMMA(","), //
	DOT("."), //
	DOUBLE_DOT(".."), //
	PLUS("+"), //
	SUB("-"), //
	STAR("*"), //
	SLASH("/"), //
	QUESTION("?"), //
	EQ("="), //
	GT(">"), //
	LT("<"), //
	BANG("!"), //
	TILDE("~"), //
	CARET("^"), //
	PERCENT("%"), //
	COLON(":"), //
	DOUBLE_COLON("::"), //
	COLON_EQ(":="), //
	LT_EQ("<="), //
	GT_EQ(">="), //
	LT_EQ_GT("<=>"), //
	LT_GT("<>"), //
	BANG_EQ("!="), //
	BANG_GT("!>"), //
	BANG_LT("!<"), //
	AMP("&"), //
	BAR("|"), //
	DOUBLE_AMP("&&"), //
	DOUBLE_BAR("||"), //
	DOUBLE_LT("<<"), //
	DOUBLE_GT(">>"), //
	MONKEYS_AT("@"), //
	POUND("#");
	
	private static Map<String, Symbol> symbols = new HashMap<String, Symbol>(128);
	
	static
	{
		for (Symbol each : Symbol.values())
		{
			symbols.put(each.literals, each);
		}
	}
	
	private Symbol(String literals)
	{
		this.literals = literals;
	}
	
	public String literals()
	{
		return literals;
	}
	
	private final String literals;
	
	public static boolean isSymbol(String literals)
	{
		return symbols.containsKey(literals);
	}
	
	public static Symbol getSymbol(String literals)
	{
		return symbols.get(literals);
	}
	
}
