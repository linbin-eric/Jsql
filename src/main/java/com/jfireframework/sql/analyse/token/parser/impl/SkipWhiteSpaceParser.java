package com.jfireframework.sql.analyse.token.parser.impl;

import com.jfireframework.sql.analyse.token.Token;
import com.jfireframework.sql.analyse.token.parser.TokenParser;

import java.util.Deque;

public class SkipWhiteSpaceParser extends TokenParser
{

    @Override
    public int parse(String sql, int offset, Deque<Token> tokens)
    {
        offset = skipWhiteSpace(offset, sql);
        return next.parse(sql, offset, tokens);
    }

}
