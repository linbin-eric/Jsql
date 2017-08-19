
package com.jfireframework.sql.parse.lexer.analyzer;

import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.sql.parse.lexer.token.DefaultKeyWord;
import com.jfireframework.sql.parse.lexer.token.Expression;
import com.jfireframework.sql.parse.lexer.token.Literals;
import com.jfireframework.sql.parse.lexer.token.Symbol;
import com.jfireframework.sql.parse.lexer.token.Token;
import com.jfireframework.sql.parse.lexer.token.TokenType;

public class Tokenizer
{
    private final String input;
    private final int    offset;
    
    public Tokenizer(String input, int offset)
    {
        this.input = input;
        this.offset = offset;
    }
    
    /**
     * 跳过空格.
     * 
     * @return 跳过空格后的偏移量
     */
    public int skipWhitespace()
    {
        int length = 0;
        while (CharType.isWhitespace(charAt(offset + length)))
        {
            length++;
        }
        return offset + length;
    }
    
    /**
     * 扫描数字.
     *
     * @return 数字标记
     */
    public Token scanNumber()
    {
        int length = 0;
        if ('-' == charAt(offset + length))
        {
            length++;
        }
        length += getDigitalLength(offset + length);
        boolean isFloat = false;
        if ('.' == charAt(offset + length))
        {
            isFloat = true;
            length++;
            length += getDigitalLength(offset + length);
        }
        if (isBinaryNumber(offset + length))
        {
            isFloat = true;
            length++;
        }
        return new Token(isFloat ? Literals.FLOAT : Literals.INT, input.substring(offset, offset + length), offset + length);
    }
    
    private boolean isBinaryNumber(final int offset)
    {
        char current = charAt(offset);
        return 'f' == current || 'F' == current || 'd' == current || 'D' == current;
    }
    
    private int getDigitalLength(final int offset)
    {
        int result = 0;
        while (CharType.isDigital(charAt(offset + result)))
        {
            result++;
        }
        return result;
    }
    
    private int getLengthUntilTerminatedChar(final char terminatedChar)
    {
        int length = 1;
        while (terminatedChar != charAt(offset + length))
        {
            if (offset + length >= input.length())
            {
                throw new UnsupportedOperationException(StringUtil.format("输入的sql有问题，从:{}开始没有结束字符串", offset));
            }
            length++;
        }
        return length + 1;
    }
    
    private int getLengthUntilTerminatedChars(final char... terminatedChars)
    {
        int length = 1;
        do
        {
            boolean hit = true;
            for (int i = 0; i < terminatedChars.length; i++)
            {
                if (charAt(offset + length + i) != terminatedChars[i])
                {
                    hit = false;
                    break;
                }
            }
            if (hit)
            {
                return length + terminatedChars.length;
            }
            length += 1;
            if (offset + length >= input.length())
            {
                throw new UnsupportedOperationException(StringUtil.format("输入的sql有问题，从:{}开始没有结束字符串", offset));
            }
        } while (true);
    }
    
    /**
     * 扫描十六进制数.
     *
     * @return 十六进制数标记
     */
    public Token scanHexDecimal()
    {
        int length = 2;
        if ('-' == charAt(offset + length))
        {
            length++;
        }
        while (isHex(charAt(offset + length)))
        {
            length++;
        }
        return new Token(Literals.HEX, input.substring(offset, offset + length), offset + length);
    }
    
    /**
     * 扫描字符串.
     *
     * @return 字符串标记
     */
    public Token scanChars()
    {
        return scanChars(charAt(offset));
    }
    
    private boolean isHex(final char ch)
    {
        return ch >= 'A' && ch <= 'F' || ch >= 'a' && ch <= 'f' || CharType.isDigital(ch);
    }
    
    private Token scanChars(final char terminatedChar)
    {
        int length = getLengthUntilTerminatedChar(terminatedChar);
        String content = input.substring(offset, offset + length);
        return new Token(Literals.CHARS, content, offset + length);
    }
    
    public Token scanIdentifier()
    {
        int length = 0;
        while (isIdentifierChar(charAt(offset + length)))
        {
            length++;
        }
        String literals = input.substring(offset, offset + length);
        if (DefaultKeyWord.getDefaultKeyWord(literals) != null)
        {
            return new Token(DefaultKeyWord.getDefaultKeyWord(literals), literals, offset + length);
        }
        if (charAt(offset) >= 'A' && charAt(offset) <= 'Z')
        {
            if (literals.contains("."))
            {
                return new Token(Literals.FIELD, literals, offset + length);
            }
            else
            {
                return new Token(Literals.ENTITY, literals, offset + length);
            }
        }
        else
        {
            return new Token(Literals.TEXT, literals, offset + length);
        }
    }
    
    private boolean isIdentifierChar(final char ch)
    {
        return CharType.isAlphabet(ch) || CharType.isDigital(ch) || ch == '.';
    }
    
    public Token scanConstant()
    {
        int length = 1;
        do
        {
            char c = charAt(offset + length);
            if (c == '(' && charAt(offset + length + 1) == ')')
            {
                return new Token(Expression.CONSTANT, input.substring(offset, offset + length + 2), offset + length + 2);
            }
            if (c == '>' || c == '<' || c == '!' || c == '=' || c == ' ' || c == ',' //
                    || c == '+' || c == '-' || c == '(' || c == ')')
            {
                return new Token(Expression.CONSTANT, input.substring(offset, offset + length), offset + length);
            }
            length += 1;
        } while (offset + length <= input.length());
        return new Token(Expression.CONSTANT, input.substring(offset), offset + length);
    }
    
    public Token scanVariable()
    {
        int length = 1;
        TokenType tokenType = Expression.VARIABLE;
        if ('~' == charAt(offset + length))
        {
            length += 1;
            tokenType = Expression.VARIABLE_WITH_TIDLE;
        }
        else if ('%' == charAt(offset + length))
        {
            length += 1;
            tokenType = Expression.VARIABLE;
        }
        do
        {
            char c = charAt(offset + length);
            if (c == '(' && charAt(offset + length + 1) == ')')
            {
                return new Token(tokenType, input.substring(offset, offset + length + 2), offset + length + 2);
            }
            if (c == '>' || c == '<' || c == '!' || c == '=' || c == ' ' || c == ',' //
                    || c == '+' || c == '-' || c == '(' || c == ')')
            {
                return new Token(tokenType, input.substring(offset, offset + length), offset + length);
            }
            length += 1;
        } while (offset + length <= input.length());
        return new Token(tokenType, input.substring(offset), offset + length);
    }
    
    public Token scanIf()
    {
        int length = getLengthUntilTerminatedChars(')', '>');
        return new Token(Expression.IF, input.substring(offset, offset + length), offset + length);
    }
    
    public Token scanEndIf()
    {
        return new Token(Expression.ENDIF, input.substring(offset, offset + 5), offset + 5);
    }
    
    public Token scanBrace()
    {
        int length = getLengthUntilTerminatedChar('}');
        return new Token(Expression.BRACE, input.substring(offset, offset + length), offset + length);
    }
    
    /**
     * 扫描符号.
     *
     * @return 符号标记
     */
    public Token scanSymbol()
    {
        int length = 0;
        while (CharType.isSymbol(charAt(offset + length)))
        {
            length++;
        }
        String literals = input.substring(offset, offset + length);
        Symbol symbol;
        while (null == (symbol = Symbol.literalsOf(literals)))
        {
            length--;
            literals = input.substring(offset, offset + length);
        }
        return new Token(symbol, literals, offset + length);
    }
    
    private char charAt(final int index)
    {
        return index >= input.length() ? (char) CharType.EOI : input.charAt(index);
    }
}
