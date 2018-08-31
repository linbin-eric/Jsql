package com.jfireframework.sql.analyse.token.parser;

import com.jfireframework.jfireel.expression.util.CharType;
import com.jfireframework.sql.analyse.token.Token;

import java.util.Deque;

public abstract class TokenParser
{
    protected TokenParser next;

    public void setNextParser(TokenParser next)
    {
        this.next = next;
    }

    protected int skipWhiteSpace(int offset, String el)
    {
        while (CharType.isWhitespace(getChar(offset, el)))
        {
            offset++;
        }
        return offset;
    }

    public abstract int parse(String sql, int offset, Deque<Token> tokens);

    protected char getChar(int offset, String el)
    {
        return offset >= el.length() ? (char) CharType.EOI : el.charAt(offset);
    }


    public static final class EmptyParser extends TokenParser
    {

        @Override
        public int parse(String sql, int offset, Deque<Token> tokens)
        {
            return offset;
        }

    }
}
