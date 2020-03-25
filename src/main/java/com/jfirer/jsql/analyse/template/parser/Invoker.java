package com.jfirer.jsql.analyse.template.parser;

import com.jfirer.jsql.analyse.template.Template;
import com.jfirer.jsql.analyse.template.execution.Execution;

import java.util.Deque;

public interface Invoker
{
    int scan(String sentence, int offset, Deque<Execution> executions, Template template, StringBuilder cache);
}
