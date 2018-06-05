package com.jfireframework.sql.analyse.template.parser;

import java.util.Deque;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.analyse.template.Template;
import com.jfireframework.sql.analyse.template.execution.Execution;

public interface Invoker
{
	int scan(String sentence, int offset, Deque<Execution> executions, Template template, StringCache cache);
}
