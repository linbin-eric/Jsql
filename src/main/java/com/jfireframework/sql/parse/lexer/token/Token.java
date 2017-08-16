package com.jfireframework.sql.parse.lexer.token;

public class Token
{
    private final TokenType tokenType;
    private final String    literals;
    private final int       endPosition;
    
    public Token(TokenType tokenType, String literals, int endPosition)
    {
        this.tokenType = tokenType;
        this.literals = literals;
        this.endPosition = endPosition;
    }
    
    public TokenType getTokenType()
    {
        return tokenType;
    }
    
    public String getLiterals()
    {
        return literals;
    }
    
    public int getEndPosition()
    {
        return endPosition;
    }
    
}
