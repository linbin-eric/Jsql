package com.jfireframework.sql.mapper;

import java.util.List;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.el.SmcEl;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.sql.mapper.MapperBuilder.SqlContext;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.metadata.TableMetaData;

public class SqlTextAnalyse
{
    
    private static String buildParam(String inject, String[] paramNames, Class<?>[] paramTypes, SqlContext sqlContext)
    {
        boolean before = false;
        boolean after = false;
        if (inject.startsWith("%"))
        {
            inject = inject.substring(1);
            before = true;
        }
        if (inject.endsWith("%"))
        {
            inject = inject.substring(0, inject.length() - 1);
            after = true;
        }
        String result = "";
        if (before)
        {
            result += "\"%\"+";
        }
        result += SmcHelper.buildInvoke(inject, paramNames, paramTypes);
        if (after)
        {
            result += "+\"%\"";
        }
        return result;
    }
    
    private static String handleWithTidle(String context, String section, String[] paramNames, Class<?>[] paramTypes, String sql, SqlContext sqlContext)
    {
        String bk = "\t";
        Class<?> paramType = SmcHelper.getType(section, paramNames, paramTypes);
        if (paramType.equals(String.class))
        {
            context += "{\r\n\tString[] tmp = ((String)" + SmcHelper.buildInvoke(section, paramNames, paramTypes) + ").split(\",\");\r\n";
        }
        else if (paramType.isArray())
        {
            context += "{\r\n\t" + paramType.getComponentType().getName() + "[] tmp = " + SmcHelper.buildInvoke(section, paramNames, paramTypes) + ";\r\n";
        }
        else if (List.class.isAssignableFrom(paramType))
        {
            context += "{\r\n\tjava.util.List tmp = " + SmcHelper.buildInvoke(section, paramNames, paramTypes) + ";\r\n";
        }
        else
        {
            throw new RuntimeException("in操作中存在不识别的类型");
        }
        if (List.class.isAssignableFrom(paramType))
        {
            context += "\tint length = tmp.size();\r\n";
        }
        else
        {
            context += "\tint length = tmp.length;\r\n";
        }
        context += "\tfor(int i=0;i<length;i++){builder.append(\"?,\");}\r\n";
        context += "\tbuilder.deleteLast().append(\")\");\r\n";
        if (List.class.isAssignableFrom(paramType))
        {
            context += "\tfor(int i=0;i<length;i++){list.add(tmp.get(i));}\r\n";
        }
        else
        {
            context += "\tfor(int i=0;i<length;i++){list.add(tmp[i]);}\r\n";
        }
        bk = bk.substring(0, bk.length() - 1);
        context += "}\r\n";
        return context;
    }
    
    /**
     * 分析动态sql语句，并且生成动态sql情况下的前置的热编码代码部分
     * 
     * @param sql
     * @param paramNames
     * @param paramTypes
     * @return
     * @throws NoSuchFieldException
     * @throws SecurityException
     */
    public static String analyseDynamicText(String sql, String[] paramNames, Class<?>[] paramTypes, MetaContext metaContext, SqlContext sqlContext) throws NoSuchFieldException, SecurityException
    {
        sql = transMapSql(sql, sqlContext, metaContext);
        String context = "com.jfireframework.baseutil.collection.StringCache builder = new com.jfireframework.baseutil.collection.StringCache();\r\n" + "java.util.List list = new java.util.ArrayList();\r\n";
        int pre = 0;
        int now = 0;
        String section = null;
        while (now < sql.length())
        {
            char c = sql.charAt(now);
            if (c == '\'')
            {
                now = sql.indexOf('\'', now + 1);
                now += 1;
                continue;
            }
            else if (c == '<' && sql.charAt(now + 1) == 'i' && sql.charAt(now + 2) == 'f')
            {
                section = sql.substring(pre, now);
                context += "builder.append(\"" + section + "\");\r\n";
                pre = now + 4;// <if(
                now = sql.indexOf(")>", pre); // )>
                String ifContent = sql.substring(pre, now);
                context += "if(" + SmcEl.createIf(ifContent, paramNames, paramTypes) + ")\r\n";
                context += "{\r\n";
                now += 2;
                pre = now;
                continue;
            }
            else if (c == '<' && sql.charAt(now + 1) == '/' && sql.charAt(now + 2) == 'i' && sql.charAt(now + 3) == 'f')
            {
                context += "}\r\n";
                now += 5;
                pre = now;
            }
            else if (c == '@')
            {
                section = sql.substring(pre, now);
                context += "builder.append(\"" + section + "\").append('?');\r\n";
                pre = now + 1;
                now++;
                now = getEndFlag(sql, now);
                section = sql.substring(pre, now);
                context += "list.add(" + section + ");\r\n";
                pre = now;
                continue;
            }
            else if (c == '$')
            {
                if (sql.charAt(now + 1) == '~')
                {
                    section = sql.substring(pre, now);
                    context += "builder.append(\"" + section + "\").append(\" (  \");\r\n";
                }
                else
                {
                    section = sql.substring(pre, now);
                    context += "builder.append(\"" + section + "\").append('?');\r\n";
                }
                pre = now + 1;
                now++;
                now = getEndFlag(sql, now);
                if (sql.charAt(pre) == '~')
                {
                    section = sql.substring(pre, now);
                    section = section.substring(1);
                    context = handleWithTidle(context, section, paramNames, paramTypes, sql, sqlContext);
                }
                else
                {
                    section = sql.substring(pre, now);
                    context += "list.add(" + buildParam(section, paramNames, paramTypes, sqlContext) + ");\r\n";
                }
                pre = now;
                continue;
            }
            else if (c == '{')
            {
                section = sql.substring(pre, now);
                context += "builder.append(\"" + section + "\");\r\n";
                pre = now + 1;
                now = sql.indexOf('}', pre);
                section = sql.substring(pre, now);
                context += "builder.append(" + buildParam(section, paramNames, paramTypes, sqlContext) + ");\r\n";
                now += 1;
                pre = now;
                continue;
            }
            else
            {
                now++;
                continue;
            }
        }
        section = sql.substring(pre, now);
        if (section.equals("") == false)
        {
            context += "builder.append(\"" + section + "\");\r\n";
        }
        context += "String sql = builder.toString();\r\n";
        return context;
    }
    
    private static void findTableNameAndAliasName(String sql, SqlContext sqlContext, MetaContext metaContext)
    {
        String simpleClassName = null;
        int end = 0;
        int index = 0;
        boolean as = false;
        boolean hasIf = false;
        while (index < sql.length())
        {
            char c = sql.charAt(index);
            if (c == '\'')
            {
                end = sql.indexOf('\'', index + 1);
                end++;
                index = end;
                continue;
            }
            else if (c == '<' && sql.charAt(index + 1) == 'i' && sql.charAt(index + 2) == 'f')
            {
                index = sql.indexOf(")>", index);
                index += 2;
                hasIf = true;
                continue;
            }
            else if (c == '<' && sql.charAt(index + 1) == '/' && sql.charAt(index + 2) == 'i' && sql.charAt(index + 3) == 'f' && sql.charAt(index + 4) == '>')
            {
                index += 5;
                hasIf = false;
                continue;
            }
            else if (c == '$')
            {
                end = getEndFlag(sql, index);
                index = end + 1;
                continue;
            }
            else if (c == '{')
            {
                end = sql.indexOf('}', index);
                index = end + 1;
                continue;
            }
            else if (c == ' ')
            {
                index += 1;
                continue;
            }
            else if (c == ' ' || c == ',' || c == '(' || c == ')' || c == '+' || c == '=' || c == '-' || c == '!' || c == '>' || c == '<')
            {
                index += 1;
                continue;
            }
            else if (c == 'a' && index < sql.length() - 2 && sql.charAt(index + 1) == 's' && sql.charAt(index + 2) == ' ')
            {
                as = true;
                index += 2;
                continue;
            }
            else if (c >= 'A' && c <= 'Z')
            {
                end = getEndFlag(sql, index);
                String tmp = sql.substring(index, end);
                if (tmp.indexOf('.') == -1)
                {
                    simpleClassName = tmp;
                    TableMetaData<?> tableMetaData = metaContext.get(simpleClassName);
                    if (tableMetaData == null)
                    {
                        throw new NullPointerException("无法识别" + simpleClassName);
                    }
                    sqlContext.addMetaData(tableMetaData);
                }
                index = end + 1;
                continue;
            }
            else
            {
                end = getEndFlag(sql, index);
                String tmp = sql.substring(index, end);
                if (tmp.indexOf('.') == -1 && as)
                {
                    as = false;
                    // 因为存在对字段进行as的情形，所以如果simpleClassName为null，就意味着是这种情况。忽略即可
                    if (simpleClassName != null)
                    {
                        String alias = tmp;
                        sqlContext.addAliasName(alias, metaContext.get(simpleClassName));
                        simpleClassName = null;
                    }
                }
                else
                {
                    ;
                }
                index = end + 1;
                continue;
            }
        }
        if (hasIf)
        {
            throw new UnsupportedOperationException(StringUtil.format("sql语句没有使用</if>来结束,存在语法错误。请检查:{}", sql));
        }
    }
    
    private static String replaceEntityNameAndFieldName(String sql, SqlContext sqlContext, MetaContext metaContext)
    {
        String simpleClassName = null;
        int end = 0;
        int index = 0;
        boolean as = false;
        StringCache cache = new StringCache();
        while (index < sql.length())
        {
            char c = sql.charAt(index);
            if (c == '\'')
            {
                end = sql.indexOf('\'', index + 1);
                end++;
                cache.append(sql.substring(index, end));
                index = end;
                continue;
            }
            else if (c == '<' && sql.charAt(index + 1) == 'i' && sql.charAt(index + 2) == 'f')
            {
                end = sql.indexOf(")>", index);
                cache.append(sql.substring(index, end + 2));
                index = end + 2;
                continue;
            }
            else if (c == '<' && sql.charAt(index + 1) == '/' && sql.charAt(index + 2) == 'i' && sql.charAt(index + 3) == 'f' && sql.charAt(index + 4) == '>')
            {
                index += 5;
                cache.append("</if>");
                continue;
            }
            else if (c == '$')
            {
                end = getEndFlag(sql, index);
                cache.append(sql.substring(index, end));
                index = end;
                continue;
            }
            else if (c == '{')
            {
                end = sql.indexOf('}', index);
                cache.append(sql.substring(index, end + 1));
                index = end + 1;
                continue;
            }
            else if (c == ' ')
            {
                index += 1;
                cache.append(c);
                continue;
            }
            else if (c == '(' || c == ')')
            {
                index += 1;
                cache.append(c);
                continue;
            }
            else if (c == ' ' || c == ',' || c == '(' || c == '+' || c == '=' || c == '-' || c == '!' || c == '>' || c == '<')
            {
                index += 1;
                cache.append(c);
                continue;
            }
            else if (c == 'a' && index < sql.length() - 2 && sql.charAt(index + 1) == 's' && sql.charAt(index + 2) == ' ')
            {
                as = true;
                index += 3;
                cache.append("as ");
                continue;
            }
            else if (c >= 'A' && c <= 'Z')
            {
                end = getEndFlag(sql, index);
                String tmp = sql.substring(index, end);
                if (tmp.indexOf('.') == -1)
                {
                    simpleClassName = tmp;
                    cache.append(metaContext.get(simpleClassName).getTableName());
                }
                index = end;
                continue;
            }
            else
            {
                end = getEndFlag(sql, index);
                String tmp = sql.substring(index, end);
                if (tmp.indexOf('.') == -1)
                {
                    if (as)
                    {
                        as = false;
                        String alias = tmp;
                        cache.append(alias);
                    }
                    else
                    {
                        if (sqlContext.getDbColName(tmp) != null)
                        {
                            cache.append(sqlContext.getDbColName(tmp));
                        }
                        else
                        {
                            cache.append(tmp);
                        }
                    }
                }
                else
                {
                    if (sqlContext.getDbColName(tmp) != null)
                    {
                        cache.append(sqlContext.getDbColName(tmp));
                    }
                    else
                    {
                        cache.append(tmp);
                    }
                }
                index = end;
                continue;
            }
        }
        return cache.toString();
    }
    
    /**
     * 将sql语句中的类映射和字段映射替换为各自映射的数据库表名和字段名
     * 
     * @param sql
     * @return
     */
    public static String transMapSql(String sql, SqlContext sqlContext, MetaContext metaContext)
    {
        findTableNameAndAliasName(sql, sqlContext, metaContext);
        return replaceEntityNameAndFieldName(sql, sqlContext, metaContext);
    }
    
    /**
     * 从start处开始，在sql中遇到一些特定字符则返回当前的位置
     * 
     * @param sql
     * @param start
     * @return
     */
    private static int getEndFlag(String sql, int start)
    {
        while (start < sql.length())
        {
            char c = sql.charAt(start);
            if (c == '(' && start + 1 < sql.length() && sql.charAt(start + 1) == ')')
            {
                start += 2;
                continue;
            }
            if (c == '>' || c == '<' || c == '!' || c == '=' || c == ' ' || c == ',' //
                    || c == '+' || c == '-' || c == '(' || c == ')')
            {
                break;
            }
            start++;
        }
        return start;
    }
    
    /**
     * 分析格式化的sql语句，根据格式化语句和方法形参名称数组得出标准sql语句，和对应的object[]形的参数数组
     * 
     * @param originalSql
     * @param paramNames
     * @return
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    public static void analyseStaticText(String originalSql, String[] paramNames, Class<?>[] paramTypes, MetaContext metaContext, SqlContext sqlContext) throws NoSuchFieldException, SecurityException
    {
        String sql = transMapSql(originalSql, sqlContext, metaContext);
        StringCache formatSql = new StringCache();
        int length = sql.length();
        char c;
        int now = 0;
        int variateStart = 0;
        while (now < length)
        {
            c = sql.charAt(now);
            switch (c)
            {
                case '$':
                    variateStart = now + 1;
                    now = getEndFlag(sql, now);
                    formatSql.append('?');
                    String inject = sql.substring(variateStart, now);
                    String param = buildParam(inject, paramNames, paramTypes, sqlContext);
                    sqlContext.addParams(param);
                    break;
                case '@':
                    variateStart = now + 1;
                    now = getEndFlag(sql, now);
                    formatSql.append('?');
                    sqlContext.addParams(sql.substring(variateStart, now));
                    break;
                default:
                    formatSql.append(c);
                    now++;
                    break;
            }
        }
        sqlContext.setSql(formatSql.toString());
    }
    
    /**
     * 检查是否是动态sql
     * 
     * @param sql
     * @return
     */
    public static boolean isDynamic(String sql)
    {
        int now = 0;
        boolean dynamic = false;
        while (now < sql.length())
        {
            char c = sql.charAt(now);
            switch (c)
            {
                case '\'':
                {
                    
                    int end = sql.indexOf('\'', now + 1);
                    if (end == -1)
                    {
                        throw new UnsupportedOperationException(StringUtil.format("sql语句存在问题，'符号没有正确结束。请检查:{},并且关注:{}", sql, sql.subSequence(0, now)));
                    }
                    now = end + 1;
                    break;
                }
                case '<':
                {
                    if (sql.startsWith("<if", now))
                    {
                        dynamic = true;
                        int end = sql.indexOf(")>", now);
                        if (end == -1)
                        {
                            throw new UnsupportedOperationException(StringUtil.format("sql语句存在问题，if标识符没有被结束。请检查:{},并且关注:{}", sql, sql.subSequence(0, now)));
                        }
                        else
                        {
                            now = sql.indexOf("</if>", end);
                            if (end == -1)
                            {
                                throw new UnsupportedOperationException(StringUtil.format("sql语句存在问题，if标识符没有被结束。请检查:{},并且关注:{}", sql, sql.subSequence(0, now)));
                            }
                            now = end + 5;
                            break;
                        }
                    }
                    else
                    {
                        now += 1;
                        break;
                    }
                }
                case '{':
                {
                    int end = sql.indexOf('}', now);
                    if (end == -1)
                    {
                        throw new UnsupportedOperationException(StringUtil.format("sql语句存在问题,{标识没有结束符}.请检查:{}，并且关注:{}", sql, sql.substring(0, now)));
                    }
                    now = end + 1;
                    dynamic = true;
                    break;
                }
                case '~':
                    Verify.True(now < sql.length() - 1, "sql语句存在错误，符号~不应该在最后一个，请检查{}", sql);
                    Verify.True(sql.charAt(now - 1) == '$', "sql语句存在错误，符号~前面是$。请检查{}，并关注{}", sql, sql.substring(0, now));
                    dynamic = true;
                    now += 1;
                    break;
                default:
                    now += 1;
                    break;
            }
        }
        return dynamic;
    }
    
}
