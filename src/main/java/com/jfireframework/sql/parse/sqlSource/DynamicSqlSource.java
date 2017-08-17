package com.jfireframework.sql.parse.sqlSource;

import java.lang.reflect.Method;
import java.util.List;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.sql.SqlSession;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.parse.lexer.Lexer;
import com.jfireframework.sql.parse.lexer.token.Expression;
import com.jfireframework.sql.parse.lexer.token.Token;
import com.jfireframework.sql.resultsettransfer.ResultsetTransferStore;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public class DynamicSqlSource extends AbstractSqlSource
{
    private final ResultsetTransferStore resultsetTransferStore;
    private final JdbcTypeDictionary     jdbcTypeDictionary;
    
    public DynamicSqlSource(ResultsetTransferStore resultsetTransferStore, JdbcTypeDictionary jdbcTypeDictionary)
    {
        this.resultsetTransferStore = resultsetTransferStore;
        this.jdbcTypeDictionary = jdbcTypeDictionary;
    }
    
    @Override
    public String parseSingleQuery(Lexer lexer, Method method)
    {
        String[] paramNames = method.getAnnotation(Sql.class).paramNames().split(",");
        Class<?>[] paramTypes = method.getParameterTypes();
        String methodBody = "";
        methodBody += SqlSession.class.getName() + " session = sessionFactory.getCurrentSession();\r\n";
        methodBody += "if(session==null){throw new java.lang.NullPointerException(\"current session 为空，请检查\");}\r\n";
        methodBody += "com.jfireframework.baseutil.collection.StringCache builder = new com.jfireframework.baseutil.collection.StringCache();\r\n";
        methodBody += "java.util.List list = new java.util.ArrayList();\r\n";
        StringCache sqlCache = new StringCache();
        for (Token token : lexer.getTokens())
        {
            if (token.getTokenType() == Expression.BRACE)
            {
                methodBody += "builder.append(\"" + sqlCache.toString() + "\");\r\n";
                sqlCache.clear();
                methodBody += "builder.append(" + buildParam(token.getLiterals().substring(1, token.getLiterals().length() - 1), paramNames, paramTypes) + ").append(' ')\r\n";
            }
            else if (token.getTokenType() == Expression.VARIABLE)
            {
                methodBody += "builder.append(\"" + sqlCache.toString() + "\");\r\n";
                sqlCache.clear();
                methodBody += "builder.append(\"? \")\r\n";
                methodBody += "list.add(" + buildParam(token.getLiterals().substring(1), paramNames, paramTypes) + ");\r\n";
            }
            else if (token.getTokenType() == Expression.VARIABLE_WITH_TIDLE)
            {
                methodBody += "builder.append(\"" + sqlCache.toString() + "\");\r\n";
                sqlCache.clear();
                String content = token.getLiterals().substring(2);
                Class<?> paramType = SmcHelper.getType(content, paramNames, paramTypes);
                if (paramType == String.class)
                {
                    methodBody += "{\r\n\tString[] tmp = ((String)" + buildInvoke(content, paramNames, paramTypes) + ").split(\",\");\r\n";
                }
                else if (paramType.isArray())
                {
                    methodBody += "{\r\n\t" + paramType.getComponentType().getName() + "[] tmp = " + buildInvoke(content, paramNames, paramTypes) + ";\r\n";
                }
                else if (List.class.isAssignableFrom(paramType))
                {
                    methodBody += "{\r\n\tjava.util.List tmp = " + buildInvoke(content, paramNames, paramTypes) + ";\r\n";
                }
                else
                {
                    throw new RuntimeException("in操作中存在不识别的类型");
                }
                if (List.class.isAssignableFrom(paramType))
                {
                    methodBody += "\tint length = tmp.size();\r\n";
                }
                else
                {
                    methodBody += "\tint length = tmp.length;\r\n";
                }
                methodBody += "\tfor(int i=0;i<length;i++){builder.append(\"?,\");}\r\n";
                methodBody += "\tif(builder.isCommaLast()){builder.deleteLast().append(\\\")\\\")}\r\n";
                if (List.class.isAssignableFrom(paramType))
                {
                    methodBody += "\tfor(int i=0;i<length;i++){list.add(tmp.get(i));}\r\n";
                }
                else
                {
                    methodBody += "\tfor(int i=0;i<length;i++){list.add(tmp[i]);}\r\n";
                }
                methodBody += "}\r\n";
            }
            else if (token.getTokenType() == Expression.CONSTANT)
            {
                methodBody += "builder.append(\"" + sqlCache.toString() + "\");\r\n";
                sqlCache.clear();
                methodBody += "builder.append(\"? \")\r\n";
                methodBody += "list.add(" + token.getLiterals().substring(1) + ");\r\n";
            }
            else if (token.getTokenType() == Expression.IF)
            {
                methodBody += "builder.append(\"" + sqlCache.toString() + "\");\r\n";
                sqlCache.clear();
                String literals = token.getLiterals();
                literals = literals.substring(4);
                literals = literals.substring(0, literals.length() - 2);
                methodBody += "if(" + createIf(literals, paramNames, paramTypes) + ")\r\n";
                methodBody += "{\r\n";
            }
            else if (token.getTokenType() == Expression.ENDIF)
            {
                methodBody += "}\r\n";
            }
            else
            {
                sqlCache.append(token.getLiterals()).append(" ");
            }
        }
        return methodBody;
    }
    
    @Override
    public String parseListQuery(Lexer lexer, MetaContext metaContext, Method method)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String parsePageQuery(Lexer lexer, MetaContext metaContext, Method method)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String parseUpdate(Lexer lexer, MetaContext metaContext, Method method)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public String createIf(String el, String[] paramNames, Class<?>[] types)
    {
        StringCache cache = new StringCache();
        int index = 0;
        int end = 0;
        while (index < el.length())
        {
            char c = el.charAt(index);
            if (c == '$')
            {
                end = getEndFlag(el, index);
                String content = el.substring(index + 1, end);
                cache.append(SmcHelper.buildInvoke(content, paramNames, types));
                Class<?> type = SmcHelper.getType(content, paramNames, types);
                if (type == String.class)
                {
                    index = end;
                    while ((c = el.charAt(index)) == ' ')
                    {
                        index += 1;
                    }
                    if (c == '=' && el.charAt(index + 1) == '=')
                    {
                        index += 2;
                        do
                        {
                            end = getEndFlag(el, index + 1);
                            content = el.substring(index, end);
                            index = end;
                            content = content.trim();
                        } while ("".equals(content));
                        if (content.equals("null"))
                        {
                            cache.append("==null");
                        }
                        else
                        {
                            if (content.charAt(0) != '"' || content.endsWith("\"") == false)
                            {
                                throw new UnsupportedOperationException(StringUtil.format("解析条件语句存在问题，其if判断中存在对String的比较，但是却没有使用'\"'将字符串包围。请检查:{}", el));
                            }
                            cache.append(".equals(").append(content).append(")");
                        }
                    }
                    else if (c == '!' && el.charAt(index + 1) == '=')
                    {
                        index += 2;
                        do
                        {
                            end = getEndFlag(el, index + 1);
                            content = el.substring(index, end);
                            index = end;
                            content = content.trim();
                        } while ("".equals(content));
                        if (content.equals("null"))
                        {
                            cache.append("!=null");
                        }
                        else
                        {
                            if (content.charAt(0) != '"' || content.endsWith("\"") == false)
                            {
                                throw new UnsupportedOperationException(StringUtil.format("解析条件语句存在问题，其if判断中存在对String的比较，但是却没有使用'\"'将字符串包围。请检查:{}", el));
                            }
                            cache.append(".equals(").append(content).append(")==false");
                        }
                        index = end;
                    }
                    else
                    {
                        throw new UnsupportedOperationException();
                    }
                }
            }
            else if (c == ' ')
            {
                cache.append(' ');
                index += 1;
                continue;
            }
            else
            {
                end = getEndFlag(el, index + 1);
                String content = el.substring(index, end);
                cache.append(content);
            }
            index = end;
        }
        return cache.toString();
    }
    
    private int getEndFlag(String sql, int start)
    {
        while (start < sql.length())
        {
            char c = sql.charAt(start);
            if (c == '>' || c == '<' || c == '!' || c == '=' || c == ' ' || c == ',' //
                    || c == '#' || c == '+' || c == '-' || c == '(' || c == ')' || c == ']' || c == '[')
            {
                break;
            }
            start++;
        }
        return start;
    }
    
}
