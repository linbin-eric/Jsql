package com.jfirer.jsql.analyse.template.parser.impl;

import com.jfirer.jsql.analyse.template.ScanMode;
import com.jfirer.jsql.analyse.template.Template;
import com.jfirer.jsql.analyse.template.execution.Execution;
import com.jfirer.jsql.analyse.template.parser.Invoker;
import com.jfirer.jsql.analyse.template.parser.TemplateParser;

import java.util.Deque;

public class ExecutionEndParser extends TemplateParser
{

    @Override
    public int parse(String sentence, int offset, Deque<Execution> executions, Template template, StringBuilder cache, Invoker next)
    {
        if ( template.getMode() != ScanMode.EXECUTION //
                || '%' != getChar(offset, sentence) //
                || '>' != getChar(offset + 1, sentence) )
        {
            return next.scan(sentence, offset, executions, template, cache);
        }
        template.setMode(ScanMode.LITERALS);
        offset += 2;
        return offset;
    }

}
