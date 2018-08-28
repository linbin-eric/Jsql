package com.jfireframework.sql.analyse.template.parser;

import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.analyse.template.Template;
import com.jfireframework.sql.analyse.template.execution.Execution;

import java.util.Deque;

public interface Invoker
{
    int scan(String sentence, int offset, Deque<Execution> executions, Template template, StringCache cache);
}
