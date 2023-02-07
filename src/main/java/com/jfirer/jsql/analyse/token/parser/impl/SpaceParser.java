package com.jfirer.jsql.analyse.token.parser.impl;

import com.jfirer.jsql.analyse.token.Symbol;
import com.jfirer.jsql.analyse.token.Token;
import com.jfirer.jsql.analyse.token.parser.TokenParser;

import java.util.Deque;

public class SpaceParser extends TokenParser
{
    @Override
    public int parse(String sql, int offset, Deque<Token> tokens)
    {
        char c = getChar(offset, sql);
        if (c == ' ')
        {
            offset += 1;
            tokens.push(new Token(Symbol.SPACE));
            return offset;
        }
        else
        {
            return next.parse(sql, offset, tokens);
        }
    }
}
