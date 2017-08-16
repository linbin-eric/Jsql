package com.jfireframework.sql.parse.lexer.token;

public class Token
{
    private final TokenType tokenType;
    private final String    literals;
    
    public Token(TokenType tokenType, String literals)
    {
        this.tokenType = tokenType;
        this.literals = literals;
    }
    
    public TokenType getTokenType()
    {
        return tokenType;
    }
    
    public String getLiterals()
    {
        return literals;
    }
    
}
