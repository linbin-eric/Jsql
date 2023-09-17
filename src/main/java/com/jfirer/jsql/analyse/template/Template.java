package com.jfirer.jsql.analyse.template;

import com.jfirer.jfireel.expression.Expression;
import com.jfirer.jfireel.expression.Operand;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Template
{
    public static final Object print(Map<String, Object> params, Operand[] operands)
    {
        StringBuilder outputStr = (StringBuilder) params.get("outputStr");
        outputStr.append(operands[0].calculate(params));
        return null;
    }

    public static final Object printParam(Map<String, Object> params, Operand[] operands)
    {
        ((StringBuilder) params.get("outputStr")).append("?");
        ((List<Object>) params.get("sqlParams")).add(operands[0].calculate(params));
        return null;
    }

    public static Object printCollection(Map<String, Object> params, Operand[] operands)
    {
        Object        result    = operands[0].calculate(params);
        StringBuilder builder   = (StringBuilder) params.get("outputStr");
        List<Object>  sqlParams = (List<Object>) params.get("sqlParams");
        if (result instanceof Collection<?>)
        {
            builder.append("(");
            for (Object each : (Collection<?>) result)
            {
                builder.append("?,");
                sqlParams.add(each);
            }
            builder.setLength(builder.length() - 1);
            builder.append(") ");
        }
        else if (result instanceof String)
        {
            String[] split = ((String) result).split(",");
            builder.append("(");
            for (String each : split)
            {
                builder.append("?,");
                sqlParams.add(each);
            }
            if (split.length != 0)
            {
                builder.setLength(builder.length() - 1);
            }
            builder.append(") ");
        }
        else if (result.getClass().isArray())
        {
            if (!result.getClass().getComponentType().isPrimitive())
            {
                builder.append("(");
                for (Object each : (Object[]) result)
                {
                    builder.append("?,");
                    sqlParams.add(each);
                }
                builder.setLength(builder.length() - 1);
                builder.append(") ");
            }
            else if (result instanceof int[])
            {
                builder.append("(");
                for (int each : (int[]) result)
                {
                    builder.append("?,");
                    sqlParams.add(each);
                }
                builder.setLength(builder.length() - 1);
                builder.append(") ");
            }
            else if (result instanceof boolean[])
            {
                builder.append("(");
                for (boolean each : (boolean[]) result)
                {
                    builder.append("?,");
                    sqlParams.add(each);
                }
                builder.setLength(builder.length() - 1);
                builder.append(") ");
            }
            else if (result instanceof char[])
            {
                builder.append("(");
                for (char each : (char[]) result)
                {
                    builder.append("?,");
                    sqlParams.add(each);
                }
                builder.setLength(builder.length() - 1);
                builder.append(") ");
            }
            else if (result instanceof byte[])
            {
                builder.append("(");
                for (byte each : (byte[]) result)
                {
                    builder.append("?,");
                    sqlParams.add(each);
                }
                builder.setLength(builder.length() - 1);
                builder.append(") ");
            }
            else if (result instanceof short[])
            {
                builder.append("(");
                for (short each : (short[]) result)
                {
                    builder.append("?,");
                    sqlParams.add(each);
                }
                builder.setLength(builder.length() - 1);
                builder.append(") ");
            }
            else if (result instanceof long[])
            {
                builder.append("(");
                for (long each : (long[]) result)
                {
                    builder.append("?,");
                    sqlParams.add(each);
                }
                builder.setLength(builder.length() - 1);
                builder.append(") ");
            }
            else if (result instanceof float[])
            {
                builder.append("(");
                for (float each : (float[]) result)
                {
                    builder.append("?,");
                    sqlParams.add(each);
                }
                builder.setLength(builder.length() - 1);
                builder.append(") ");
            }
            else if (result instanceof double[])
            {
                builder.append("(");
                for (double each : (double[]) result)
                {
                    builder.append("?,");
                    sqlParams.add(each);
                }
                builder.setLength(builder.length() - 1);
                builder.append(") ");
            }
        }
        else
        {
            throw new IllegalArgumentException("参数不正确，应该放入集合或者数组，请检查");
        }
        return null;
    }

    static
    {
        Expression.registerInnerCall("print", Template::print);
        Expression.registerInnerCall("printParam", Template::printParam);
        Expression.registerInnerCall("printCollection", Template::printCollection);
    }

    private static final int     IN_TEXT       = 1;
    private static final int     IN_CODE_AREA  = 2;
    private static final int     IN_VARIABLE   = 3;
    private static final int     IN_PARAM      = 4;
    private static final int     IN_COLLECTION = 5;
    private final        Operand operand;

    private Template(Operand operand)
    {
        this.operand = operand;
    }

    public static Template parse(String content)
    {
        StringBuilder builder = new StringBuilder();
        int           type    = IN_TEXT;
        int           length  = content.length();
        int           index   = 0;
        int           mark    = 0;
        while (index < length)
        {
            char c = content.charAt(index);
            switch (type)
            {
                case IN_CODE_AREA ->
                {
                    if (c == '%' && index + 1 < length && content.charAt(index + 1) == '>')
                    {
                        builder.append(content.substring(mark, index));
                        mark = index += 2;
                        type = IN_TEXT;
                    }
                    else
                    {
                        index += 1;
                    }
                }
                case IN_TEXT ->
                {
                    if (c == '#' && index + 1 < length && content.charAt(index + 1) == '{')
                    {
                        if (mark != index)
                        {
                            builder.append("print(\"").append(content.substring(mark, index)).append("\");\r\n");
                        }
                        mark = index += 2;
                        type = IN_VARIABLE;
                    }
                    else if (c == '$' && index + 1 < length && content.charAt(index + 1) == '{')
                    {
                        if (mark != index)
                        {
                            builder.append("print(\"").append(content.substring(mark, index)).append("\");\r\n");
                        }
                        mark = index += 2;
                        type = IN_PARAM;
                    }
                    else if (c == '~' && index + 1 < length && content.charAt(index + 1) == '{')
                    {
                        if (mark != index)
                        {
                            builder.append("print(\"").append(content.substring(mark, index)).append("\");\r\n");
                        }
                        mark = index += 2;
                        type = IN_COLLECTION;
                    }
                    else if (c == '<' && index + 1 < length && content.charAt(index + 1) == '%')
                    {
                        if (mark != index)
                        {
                            builder.append("print(\"").append(content.substring(mark, index)).append("\");\r\n");
                        }
                        mark = index += 2;
                        type = IN_CODE_AREA;
                    }
                    else
                    {
                        index += 1;
                    }
                }
                case IN_VARIABLE ->
                {
                    if (c == '}')
                    {
                        builder.append("print(").append(content.substring(mark, index)).append(");\r\n");
                        mark = index += 1;
                        type = IN_TEXT;
                    }
                    else
                    {
                        index += 1;
                    }
                }
                case IN_PARAM ->
                {
                    if (c == '}')
                    {
                        String sub = content.substring(mark, index);
                        builder.append("printParam(" + sub + ");\r\n");
                        mark = index += 1;
                        type = IN_TEXT;
                    }
                    else
                    {
                        index += 1;
                    }
                }
                case IN_COLLECTION ->
                {
                    if (c == '}')
                    {
                        String sub = content.substring(mark, index);
                        builder.append("printCollection(" + sub + ");\r\n");
                        mark = index += 1;
                        type = IN_TEXT;
                    }
                    else
                    {
                        index += 1;
                    }
                }
            }
        }
        if (type != IN_TEXT)
        {
            throw new IllegalStateException("解析模板不正确，模板没有被正确结束");
        }
        if (mark != index)
        {
            builder.append("print(\"").append(content.substring(mark, index)).append("\");\r\n");
        }
        return new Template(Expression.parseMutli(builder.toString()));
    }

    public String render(Map<String, Object> variables, List<Object> sqlParams)
    {
        StringBuilder stringBuilder = new StringBuilder();
        variables.put("outputStr", stringBuilder);
        variables.put("sqlParams", sqlParams);
        operand.calculate(variables);
        return stringBuilder.toString();
    }
}
