package com.jfirer.jsql.analyse.token.parser.impl;

import com.jfirer.jsql.analyse.token.Token;
import com.jfirer.jsql.analyse.token.TokenType;
import com.jfirer.jsql.analyse.token.parser.TokenParser;

import java.util.Deque;

public class TextParser extends TokenParser
{
    @Override
    public int parse(String sql, int offset, Deque<Token> tokens)
    {
        if (getChar(offset, sql) != '\'' && getChar(offset, sql) != '"')
        {
            return next.parse(sql, offset, tokens);
        }
        if (getChar(offset, sql) == '\'')
        {
            int index = offset;
            offset += 1;
            int length = sql.length();
            while (offset < length && getChar(offset, sql) != '\'')
            {
                offset++;
            }
            String text = sql.substring(index, offset + 1);
            tokens.push(new Token(text, TokenType.TEXT));
            return offset + 1;
        }
        else
        {
            int index = offset;
            offset += 1;
            int length = sql.length();
            while (offset < length && getChar(offset, sql) != '"')
            {
                offset++;
            }
            String text = sql.substring(index, offset + 1);
            tokens.push(new Token(text.replace("\"","\\\""), TokenType.TEXT));
            return offset + 1;
        }
    }
}
