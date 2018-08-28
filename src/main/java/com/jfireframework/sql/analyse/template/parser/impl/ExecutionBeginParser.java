package com.jfireframework.sql.analyse.template.parser.impl;

import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.analyse.template.ScanMode;
import com.jfireframework.sql.analyse.template.Template;
import com.jfireframework.sql.analyse.template.execution.Execution;
import com.jfireframework.sql.analyse.template.parser.Invoker;
import com.jfireframework.sql.analyse.template.parser.TemplateParser;

import java.util.Deque;

public class ExecutionBeginParser extends TemplateParser
{

    @Override
    public int parse(String sentence, int offset, Deque<Execution> executions, Template template, StringCache cache, Invoker next)
    {
        if ( isExecutionBegin(offset, sentence) == false )
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
