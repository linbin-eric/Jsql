package com.jfirer.jsql.analyse.token.parser.impl;

import com.jfirer.jsql.analyse.token.Token;
import com.jfirer.jsql.analyse.token.parser.TokenParser;

import java.util.Deque;

public class SkipWhiteSpaceParser extends TokenParser
{
    @Override
    public int parse(String sql, int offset, Deque<Token> tokens)
    {
        offset = skipWhiteSpace(offset, sql);
        return next.parse(sql, offset, tokens);
    }

    protected int skipWhiteSpace(int offset, String el)
    {
        do
        {
            char c = getChar(offset, el);
            if ( c == '\r' || c == '\n' || c == '\t')
            {
                offset++;
            }
            else
            {
                return offset;
            }
        }
        while (true);
    }
}
