package com.jfirer.jsql.analyse.token.parser.impl;

import com.jfirer.jsql.analyse.exception.IllegalFormatException;
import com.jfirer.jsql.analyse.token.Token;
import com.jfirer.jsql.analyse.token.TokenType;
import com.jfirer.jsql.analyse.token.parser.TokenParser;

import java.util.Deque;

public class AutoCollectionParser extends TokenParser
{
    @Override
    public int parse(String sql, int offset, Deque<Token> tokens)
    {
        if (getChar(offset, sql) != '~' || getChar(offset + 1, sql) != '{')
        {
            return next.parse(sql, offset, tokens);
        }
        int index  = offset;
        int length = sql.length();
        while (offset < length && getChar(offset, sql) != '}')
        {
            offset++;
        }
        if (offset > length)
        {
            throw new IllegalFormatException("自动集合没有被}结束", sql.substring(offset));
        }
        tokens.push(new Token(sql.substring(index, offset + 1), TokenType.AUTO_COLLECTION));
        return offset + 1;
    }
}
