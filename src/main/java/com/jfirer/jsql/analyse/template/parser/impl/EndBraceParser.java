package com.jfirer.jsql.analyse.template.parser.impl;

import com.jfirer.jsql.analyse.exception.IllegalFormatException;
import com.jfirer.jsql.analyse.template.ScanMode;
import com.jfirer.jsql.analyse.template.Template;
import com.jfirer.jsql.analyse.template.execution.Execution;
import com.jfirer.jsql.analyse.template.execution.WithBodyExecution;
import com.jfirer.jsql.analyse.template.execution.impl.ElseExecution;
import com.jfirer.jsql.analyse.template.execution.impl.ElseIfExecution;
import com.jfirer.jsql.analyse.template.execution.impl.IfExecution;
import com.jfirer.jsql.analyse.template.parser.Invoker;
import com.jfirer.jsql.analyse.template.parser.TemplateParser;

import java.util.Deque;
import java.util.LinkedList;

public class EndBraceParser extends TemplateParser
{
    @Override
    public int parse(String sentence, int offset, Deque<Execution> executions, Template template, StringBuilder cache, Invoker next)
    {
        if (template.getMode() != ScanMode.EXECUTION || getChar(offset, sentence) != '}')
        {
            return next.scan(sentence, offset, executions, template, cache);
        }
        Deque<Execution> array = new LinkedList<Execution>();
        Execution        pop;
        while ((pop = executions.pollFirst()) != null)
        {
            if (!(pop instanceof WithBodyExecution) || !((WithBodyExecution) pop).isBodyNotSet())
            {
                array.push(pop);
            }
            else
            {
                break;
            }
        }
        if (pop == null)
        {
            throw new IllegalFormatException("结束符}前面没有开始符号", sentence.substring(0, offset));
        }
        ((WithBodyExecution) pop).setBody(array.toArray(emptyBody));
        if (pop instanceof ElseExecution)
        {
            if (executions.peek() == null || !(executions.peek() instanceof IfExecution))
            {
                throw new IllegalFormatException("else 节点之前没有if节点", sentence.substring(0, offset));
            }
            ((IfExecution) executions.peek()).setElse((ElseExecution) pop);
        }
        else if (pop instanceof ElseIfExecution)
        {
            if (executions.peek() == null || !(executions.peek() instanceof IfExecution))
            {
                throw new IllegalFormatException("else if 节点之前没有if节点", sentence.substring(0, offset));
            }
            ((IfExecution) executions.peek()).addElseIf((ElseIfExecution) pop);
        }
        else
        {
            executions.push(pop);
        }
        offset += 1;
        return offset;
    }
}
