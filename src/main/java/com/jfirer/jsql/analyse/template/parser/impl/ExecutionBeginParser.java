package com.jfirer.jsql.analyse.template.parser.impl;

import com.jfirer.jsql.analyse.template.ScanMode;
import com.jfirer.jsql.analyse.template.Template;
import com.jfirer.jsql.analyse.template.execution.Execution;
import com.jfirer.jsql.analyse.template.parser.Invoker;
import com.jfirer.jsql.analyse.template.parser.TemplateParser;

import java.util.Deque;

public class ExecutionBeginParser extends TemplateParser
{
    @Override
    public int parse(String sentence, int offset, Deque<Execution> executions, Template template, StringBuilder cache, Invoker next)
    {
        if (isExecutionBegin(offset, sentence) == false)
        {
            return next.scan(sentence, offset, executions, template, cache);
        }
        offset += 2;
        template.setMode(ScanMode.EXECUTION);
        extractLiterals(cache, executions);
        offset = skipWhiteSpace(offset, sentence);
        return offset;
    }
}
