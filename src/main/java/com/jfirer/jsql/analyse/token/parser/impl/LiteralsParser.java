package com.jfirer.jsql.analyse.token.parser.impl;

import com.jfirer.jfireel.expression.CharType;
import com.jfirer.jsql.analyse.exception.IllegalFormatException;
import com.jfirer.jsql.analyse.token.KeyWord;
import com.jfirer.jsql.analyse.token.Token;
import com.jfirer.jsql.analyse.token.parser.TokenParser;

import java.util.Deque;

public class LiteralsParser extends TokenParser
{
    @Override
    public int parse(String sql, int offset, Deque<Token> tokens)
    {
        char c = getChar(offset, sql);
        if (!CharType.isAlphabet(c))
        {
            return next.parse(sql, offset, tokens);
        }
        int index = offset;
        offset++;
        int dotCount = 0;
        while (CharType.isAlphabet(c = getChar(offset, sql))//
               || CharType.isDigital(c) || c == '_'//
               || (c == '.' && 0 == dotCount++))
        {
            offset++;
        }
        if (dotCount > 1)
        {
            throw new IllegalFormatException("非法格式的引用", sql.substring(index, offset));
        }
        String literals = sql.substring(index, offset);
        if (KeyWord.isKeyWord(literals))
        {
            tokens.push(new Token(KeyWord.getKeyWord(literals)));
        }
        else
        {
            tokens.push(new Token(literals));
        }
        return offset;
    }
}
