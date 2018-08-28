package com.jfireframework.sql.analyse.token.parser.impl;

import com.jfireframework.sql.analyse.token.Symbol;
import com.jfireframework.sql.analyse.token.Token;
import com.jfireframework.sql.analyse.token.parser.TokenParser;

import java.util.Deque;

public class SymbolParser extends TokenParser
{

    @Override
    public int parse(String sql, int offset, Deque<Token> tokens)
    {
        String mayBeSymbol = new String(new char[]{getChar(offset, sql), getChar(offset + 1, sql)});
        if ( Symbol.isSymbol(mayBeSymbol) )
        {
            tokens.push(new Token(Symbol.getSymbol(mayBeSymbol)));
            return offset + 2;
        }
        mayBeSymbol = String.valueOf(getChar(offset, sql));
        if ( Symbol.isSymbol(mayBeSymbol) )
        {
            tokens.push(new Token(Symbol.getSymbol(mayBeSymbol)));
            return offset + 1;
        }
        return next.parse(sql, offset, tokens);
    }

}
