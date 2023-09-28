package com.jfirer.jsql.analyse.token.parser.impl;

import com.jfirer.jsql.analyse.token.Symbol;
import com.jfirer.jsql.analyse.token.Token;
import com.jfirer.jsql.analyse.token.parser.TokenParser;

import java.util.Deque;

public class SkipWhiteSpaceParser extends TokenParser
{
    @Override
    public int parse(String sql, int offset, Deque<Token> tokens)
    {
        int next_offset = skipWhiteSpace(offset, sql);
        if (next_offset != offset)
        {
            tokens.push(new Token(Symbol.SPACE));
        }
        return next.parse(sql, next_offset, tokens);
    }

    protected int skipWhiteSpace(int offset, String el)
    {
        do
        {
            char c = getChar(offset, el);
            if (c == '\r' || c == '\n' || c == '\t')
            {
                offset++;
            }
            else
            {
                return offset;
            }
        } while (true);
    }
}
