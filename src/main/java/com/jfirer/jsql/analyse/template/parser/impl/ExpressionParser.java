package com.jfirer.jsql.analyse.template.parser.impl;

import com.jfirer.jfireel.expression.Expression;
import com.jfirer.jsql.analyse.exception.IllegalFormatException;
import com.jfirer.jsql.analyse.template.ScanMode;
import com.jfirer.jsql.analyse.template.Template;
import com.jfirer.jsql.analyse.template.execution.Execution;
import com.jfirer.jsql.analyse.template.execution.impl.ExpressionExecution;
import com.jfirer.jsql.analyse.template.parser.Invoker;
import com.jfirer.jsql.analyse.template.parser.TemplateParser;

import java.util.Deque;

public class ExpressionParser extends TemplateParser
{
    @Override
    public int parse(String sentence, int offset, Deque<Execution> executions, Template template, StringBuilder cache, Invoker next)
    {
        if (template.getMode() != ScanMode.LITERALS)
        {
            return next.scan(sentence, offset, executions, template, cache);
        }
        if (getChar(offset, sentence) != '$' || getChar(offset + 1, sentence) != '{')
        {
            return next.scan(sentence, offset, executions, template, cache);
        }
        extractLiterals(cache, executions);
        offset += 2;
        int start  = offset;
        int length = sentence.length();
        while (getChar(offset, sentence) != '}' && offset < length)
        {
            offset++;
        }
        if (offset >= length)
        {
            throw new IllegalFormatException("语法错误，不是闭合的表达式", sentence.substring(0, start));
        }
        ExpressionExecution execution = new ExpressionExecution(Expression.parse(sentence.substring(start, offset)));
        executions.push(execution);
        return offset + 1;
    }
}
