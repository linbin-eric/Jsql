package com.jfirer.jsql.analyse.token.parser.impl;

import com.jfirer.jsql.analyse.exception.IllegalFormatException;
import com.jfirer.jsql.analyse.token.Token;
import com.jfirer.jsql.analyse.token.TokenType;
import com.jfirer.jsql.analyse.token.parser.TokenParser;

import java.util.Deque;

public class ExpressionParser extends TokenParser
{

    @Override
    public int parse(String sql, int offset, Deque<Token> tokens)
    {
        if ( getChar(offset, sql) != '$' || getChar(offset + 1, sql) != '{' )
        {
            return next.parse(sql, offset, tokens);
        }
        int length = sql.length();
        int index = offset;
        while (offset < length && getChar(offset, sql) != '}')
        {
            offset++;
        }
        if ( offset > length )
        {
            throw new IllegalFormatException("表达式没有被}结束", sql.substring(index));
        }
        tokens.push(new Token(sql.substring(index, offset + 1), TokenType.EXPRESSION));
        return offset + 1;
    }

}
