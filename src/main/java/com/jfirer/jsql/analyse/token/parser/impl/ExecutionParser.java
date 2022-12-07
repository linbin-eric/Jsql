package com.jfirer.jsql.analyse.token.parser.impl;

import com.jfirer.jsql.analyse.exception.IllegalFormatException;
import com.jfirer.jsql.analyse.token.Token;
import com.jfirer.jsql.analyse.token.TokenType;
import com.jfirer.jsql.analyse.token.parser.TokenParser;

import java.util.Deque;

public class ExecutionParser extends TokenParser
{
    @Override
    public int parse(String sql, int offset, Deque<Token> tokens)
    {
        if (getChar(offset, sql) != '<' || getChar(offset + 1, sql) != '%')
        {
            return next.parse(sql, offset, tokens);
        }
        int length = sql.length();
        int index  = offset;
        while (offset < length)
        {
            if (getChar(offset, sql) == '%' && getChar(offset + 1, sql) == '>')
            {
                String executionText = sql.substring(index, offset + 2);
                tokens.push(new Token(executionText, TokenType.EXECUTION));
                offset += 2;
                break;
            }
            offset++;
        }
        if (offset > length)
        {
            throw new IllegalFormatException("语句执行没有被%>结束", sql.substring(index));
        }
        return offset;
    }
}
