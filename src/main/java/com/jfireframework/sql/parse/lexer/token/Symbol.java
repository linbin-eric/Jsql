package com.jfireframework.sql.parse.lexer.token;

import java.util.HashMap;
import java.util.Map;

public enum Symbol implements TokenType
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
            symbols.put(each.getLiterals(), each);
        }
    }
    
    private Symbol(String literals)
    {
        this.literals = literals;
    }
    
    private final String literals;
    
    /**
     * 通过字面量查找词法符号.
     * 
     * @param literals 字面量
     * @return 词法符号
     */
    public static Symbol literalsOf(final String literals)
    {
        return symbols.get(literals);
    }
    
    private String getLiterals()
    {
        return literals;
    }
}
