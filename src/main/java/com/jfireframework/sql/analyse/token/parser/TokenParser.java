package com.jfireframework.sql.analyse.token.parser;

import java.util.Deque;
import com.jfireframework.jfireel.lexer.util.CharType;
import com.jfireframework.sql.analyse.token.Token;

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
	
	protected boolean isExecutionBegin(int offset, String sentence)
	{
		char c1 = getChar(offset, sentence);
		char c2 = getChar(offset + 1, sentence);
		if (c1 != '<' || c2 != '%')
		{
			return false;
		}
		return true;
	}
	
	/**
	 * offset当前位置为'(',寻找与之配对的)结束符.返回寻找到)位置。如果找不到，则返回-1
	 * 
	 * @param sentence
	 * @param offset
	 * @param leftBracketIndex
	 * @return
	 */
	protected int findEndRightBracket(String sentence, int offset)
	{
		offset++;
		int length = sentence.length();
		int countForLeftBracket = 0;
		do
		{
			char c = getChar(offset, sentence);
			if (c == '(')
			{
				countForLeftBracket++;
			}
			else if (c == ')')
			{
				if (countForLeftBracket > 0)
				{
					countForLeftBracket--;
				}
				else
				{
					// 此时找到if的括号的封闭括号
					break;
				}
			}
			offset++;
		} while (offset < length);
		if (offset >= length)
		{
			return -1;
		}
		return offset;
	}
	
	/**
	 * 搜索执行语句的结尾，也就是%>所在位置。返回>的坐标。如果没有找到，返回-1
	 * 
	 * @param startIndex
	 * @param sentence
	 * @return
	 */
	protected int findExectionEnd(int startIndex, String sentence)
	{
		int offset = startIndex;
		int length = sentence.length();
		while (offset < length)
		{
			char c = getChar(offset, sentence);
			if (c == '%')
			{
				offset = skipWhiteSpace(offset + 1, sentence);
				c = getChar(offset, sentence);
				if (c == '>')
				{
					return offset;
				}
			}
			offset++;
		}
		return -1;
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
