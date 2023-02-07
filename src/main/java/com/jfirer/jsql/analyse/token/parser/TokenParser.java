package com.jfirer.jsql.analyse.token.parser;

import com.jfirer.jfireel.expression.util.CharType;
import com.jfirer.jsql.analyse.token.Token;

import java.util.Deque;

public abstract class TokenParser
{
    protected TokenParser next;

    public void setNextParser(TokenParser next)
    {
        this.next = next;
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
