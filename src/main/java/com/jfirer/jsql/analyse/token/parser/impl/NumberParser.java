package com.jfirer.jsql.analyse.token.parser.impl;

import com.jfirer.jsql.analyse.exception.IllegalFormatException;
import com.jfirer.jsql.analyse.token.Token;
import com.jfirer.jsql.analyse.token.TokenType;
import com.jfirer.jsql.analyse.token.parser.TokenParser;
import com.jfirer.jfireel.expression.util.CharType;

import java.util.Deque;

public class NumberParser extends TokenParser
{

    @Override
    public int parse(String sql, int offset, Deque<Token> tokens)
    {
        char c = getChar(offset, sql);
        int index = offset;
        if ( c == '-' )
        {
            // 这种情况说明这是一个负数
            if ( tokens.peek() != null && tokens.peek().getTokenType() == TokenType.SYMBOL && CharType.isDigital(getChar(offset + 1, sql)) )
            {
                offset += 2;
            }
            else
            {
                return next.parse(sql, offset, tokens);
            }
        }
        else if ( CharType.isDigital(c) )
        {
            offset += 1;
        }
        else
        {
            return next.parse(sql, offset, tokens);
        }
        int length = sql.length();
        int dotCount = 0;
        while (offset < length//
                && (//
                CharType.isDigital(getChar(offset, sql))//
                        || ('.' == getChar(offset, sql) && 0 == dotCount++)))
        {
            offset++;
        }
        if ( dotCount > 1 )
        {
            throw new IllegalFormatException("非法格式的数字", sql.substring(index, offset));
        }
        String number = sql.substring(index, offset);
        tokens.push(new Token(number, TokenType.NUMBER));
        return offset;
    }

}
