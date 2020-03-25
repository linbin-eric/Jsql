package com.jfirer.jsql.analyse.token;

public class Token
{
    private TokenType tokenType;
    private String literals;

    public Token(KeyWord keyWord)
    {
        tokenType = TokenType.KEYWORD;
        literals = keyWord.name().toUpperCase();
    }

    public Token(Symbol symbol)
    {
        tokenType = TokenType.SYMBOL;
        literals = symbol.literals();
    }

    public Token(String literals)
    {
        this.literals = literals;
        tokenType = TokenType.IDENTIFIER;
    }

    public Token(String literals, TokenType tokenType)
    {
        this.literals = literals;
        this.tokenType = tokenType;
    }

    public TokenType getTokenType()
    {
        return tokenType;
    }

    public String getListerals()
    {
        return literals;
    }

    public void setListerals(String literals)
    {
        this.literals = literals;
    }

    public void setTokenType(TokenType tokenType)
    {
        this.tokenType = tokenType;
    }

}
