package com.jfirer.jsql.analyse.template;

import com.jfirer.jsql.analyse.exception.IllegalFormatException;
import com.jfirer.jsql.analyse.template.execution.Execution;
import com.jfirer.jsql.analyse.template.execution.impl.StringExecution;
import com.jfirer.jsql.analyse.template.parser.Invoker;
import com.jfirer.jsql.analyse.template.parser.TemplateParser;
import com.jfirer.jsql.analyse.template.parser.impl.*;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Template
{
    private static final ThreadLocal<StringBuilder> LOCAL = new ThreadLocal<StringBuilder>()
    {
        @Override
        protected StringBuilder initialValue()
        {
            return new StringBuilder();
        }
    };
    private final        Execution[]                runtimeExecutions;
    private              ScanMode                   mode;
    private static final Invoker                    DEFAULT_HEAD;
    static
    {
        TemplateParser[] parsers = new TemplateParser[]{ //
                new ExecutionBeginParser(), //
                new ExecutionEndParser(), //
                new IfParser(), //
                new ElseParser(), //
                new ForEachParser(), //
                new EndBraceParser(), //
                new ExpressionParser(), //
                new TemplateCharactersParser(), //
                new CollectionParser(), //
                new LiteralsParser(), //
        };
        Invoker pred = new Invoker()
        {
            @Override
            public int scan(String sentence, int offset, Deque<Execution> executions, Template template, StringBuilder cache)
            {
                return offset;
            }
        };
        for (int i = parsers.length - 1; i > -1; i--)
        {
            final TemplateParser parser = parsers[i];
            final Invoker        next   = pred;
            pred = new Invoker()
            {
                @Override
                public int scan(String sentence, int offset, Deque<Execution> executions1, Template template, StringBuilder cache)
                {
                    return parser.parse(sentence, offset, executions1, template, cache, next);
                }
            };
        }
        DEFAULT_HEAD = pred;
    }
    public ScanMode getMode()
    {
        return mode;
    }

    public void setMode(ScanMode mode)
    {
        this.mode = mode;
    }

    private Template(String sentence)
    {
        StringBuilder cache  = new StringBuilder();
        int           offset = 0;
        int           length = sentence.length();
        mode = ScanMode.LITERALS;
        Deque<Execution> executions = new LinkedList<Execution>();
        while (offset < length)
        {
            int result = DEFAULT_HEAD.scan(sentence, offset, executions, this, cache);
            if (result == offset)
            {
                throw new IllegalFormatException("没有解析器可以识别", sentence.substring(0, offset));
            }
            offset = result;
        }
        if (cache.length() != 0)
        {
            Execution execution = new StringExecution(cache.toString());
            executions.push(execution);
        }
        for (Execution each : executions)
        {
            each.check();
        }
        Deque<Execution> array = new LinkedList<Execution>();
        while (!executions.isEmpty())
        {
            array.push(executions.pollFirst());
        }
        runtimeExecutions = array.toArray(new Execution[0]);
        executions = null;
        mode = null;
    }

    /**
     * 解析模板文本，返回解析后的sql语句。并且填充参数到对应的params中
     *
     * @param variables
     * @param params
     * @return
     */
    public String render(Map<String, Object> variables, List<Object> params)
    {
        StringBuilder cache = LOCAL.get();
        for (Execution execution : runtimeExecutions)
        {
            execution.execute(variables, cache, params);
        }
        String result = cache.toString();
        cache.setLength(0);
        return result;
    }

    public static Template parse(String sentence)
    {
        return new Template(sentence);
    }
}
