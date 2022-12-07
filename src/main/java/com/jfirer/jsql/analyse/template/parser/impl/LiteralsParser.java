package com.jfirer.jsql.analyse.template.parser.impl;

import com.jfirer.jsql.analyse.template.ScanMode;
import com.jfirer.jsql.analyse.template.Template;
import com.jfirer.jsql.analyse.template.execution.Execution;
import com.jfirer.jsql.analyse.template.parser.Invoker;
import com.jfirer.jsql.analyse.template.parser.TemplateParser;

import java.util.Deque;

public class LiteralsParser extends TemplateParser
{
    @Override
    public int parse(String sentence, int offset, Deque<Execution> executions, Template template, StringBuilder cache, Invoker next)
    {
        if (template.getMode() != ScanMode.LITERALS)
        {
            offset = skipWhiteSpace(offset, sentence);
            return offset;
        }
        cache.append(getChar(offset, sentence));
        return offset + 1;
    }
}
