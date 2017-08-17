package com.jfireframework.sql.parse.lexer;

import java.util.ArrayList;
import java.util.List;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.parse.lexer.analyzer.CharType;
import com.jfireframework.sql.parse.lexer.analyzer.Tokenizer;
import com.jfireframework.sql.parse.lexer.token.Token;

public class Lexer
{
    private List<Token>  tokens = new ArrayList<Token>();
    
    private final String sql;
    private int          offset = 0;
    
    public Lexer(String sql)
    {
        this.sql = sql;
        Token currentToken;
        while ((currentToken = nextToken()) != null)
        {
            tokens.add(currentToken);
        }
    }
    
    @Override
    public String toString()
    {
        StringCache cache = new StringCache();
        for (Token each : tokens)
        {
            cache.append(each.getLiterals()).append(' ');
        }
        if (tokens.isEmpty() == false)
        {
            cache.deleteLast();
        }
        return cache.toString();
    }
    
    Token nextToken()
    {
        skipIgnoredToken();
        Token currentToken = null;
        if (isLeftBraceBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanBrace();
        }
        else if (isVariableBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanVariable();
        }
        else if (isIfBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanIf();
        }
        else if (isEndIfBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanEndIf();
        }
        else if (isIdentifierBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanIdentifier();
        }
        else if (isHexDecimalBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanHexDecimal();
        }
        else if (isNumberBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanNumber();
        }
        else if (isSymbolBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanSymbol();
        }
        else if (isCharsBegin())
        {
            currentToken = new Tokenizer(sql, offset).scanChars();
        }
        else if (isEnd())
        {
            return null;
        }
        offset = currentToken.getEndPosition();
        return currentToken;
    }
    
    /**
     * 
     * @return
     */
    private boolean isLeftBraceBegin()
    {
        return getCurrentChar(0) == '{';
    }
    
    private boolean isIdentifierBegin()
    {
        return isIdentifierBegin(getCurrentChar(0));
    }
    
    private boolean isIdentifierBegin(final char ch)
    {
        return CharType.isAlphabet(ch);
    }
    
    private boolean isIfBegin()
    {
        if (getCurrentChar(0) == '<' && getCurrentChar(1) == 'i' && getCurrentChar(2) == 'f' && getCurrentChar(3) == '(')
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private boolean isEndIfBegin()
    {
        if (getCurrentChar(0) == '<' && getCurrentChar(1) == '/' && getCurrentChar(2) == 'i' && getCurrentChar(3) == 'f' && getCurrentChar(4) == '>')
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    protected boolean isVariableBegin()
    {
        return getCurrentChar(0) == '$';
    }
    
    protected boolean isHintBegin()
    {
        return false;
    }
    
    protected boolean isCommentBegin()
    {
        char current = getCurrentChar(0);
        char next = getCurrentChar(1);
        return '/' == current && '/' == next || '-' == current && '-' == next || '/' == current && '*' == next;
    }
    
    private boolean isHexDecimalBegin()
    {
        return '0' == getCurrentChar(0) && 'x' == getCurrentChar(1);
    }
    
    private boolean isNumberBegin()
    {
        return CharType.isDigital(getCurrentChar(0)) //
                || ('.' == getCurrentChar(0) && CharType.isDigital(getCurrentChar(1))//
                        || ('-' == getCurrentChar(0) && ('.' == getCurrentChar(0) || CharType.isDigital(getCurrentChar(1)))));
    }
    
    private boolean isSymbolBegin()
    {
        return CharType.isSymbol(getCurrentChar(0));
    }
    
    private boolean isCharsBegin()
    {
        return '\'' == getCurrentChar(0) || '\"' == getCurrentChar(0);
    }
    
    private boolean isEnd()
    {
        return offset >= sql.length();
    }
    
    protected final char getCurrentChar(final int offset)
    {
        return this.offset + offset >= sql.length() ? (char) CharType.EOI : sql.charAt(this.offset + offset);
    }
    
    private void skipIgnoredToken()
    {
        offset = new Tokenizer(sql, offset).skipWhitespace();
    }
}
