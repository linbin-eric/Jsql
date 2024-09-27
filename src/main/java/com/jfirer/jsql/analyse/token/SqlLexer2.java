package com.jfirer.jsql.analyse.token;

import com.jfirer.baseutil.STR;
import com.jfirer.jsql.metadata.TableEntityInfo;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SqlLexer2
{
    private static final int TEXT      = 1;
    private static final int PARAM     = 2;
    private static final int VARIABLE  = 3;
    private static final int EXECUTION = 4;

    public static String parse(String sql, Class<?>... entities)
    {
        List<Segment> list = parseSegments(sql);
        /**
         * 需要分析的有几种情况：
         * 1、User 类的简单名称
         * 2、name 类的属性名称
         * 3、user.name  类的别名.类的属性名
         * 4、User.name 类的简单名称.类的属性名
         * 5、concat(user.name,'ss');
         * 6、concat(name,'ss')
         * 7、concat(User.name,'ss')
         */
        Map<String, TableEntityInfo> tableEntityInfoMap = findHitTableEntityInfoMap(list, entities);
        Map<String, String>          tableAsNameMap     = parseTableAsNameMap(list, tableEntityInfoMap);
        replaceEntityNameToTableName(list, tableEntityInfoMap);
        replacePropertyNameToColumnName(list, tableEntityInfoMap);
        replaceEntityPropertyNameToColumnName(list, tableAsNameMap, tableEntityInfoMap);
        return list.stream().map(Segment::getContent).collect(Collectors.joining());
    }

    private static void replaceEntityPropertyNameToColumnName(List<Segment> list, Map<String, String> tableAsNameMap, Map<String, TableEntityInfo> tableEntityInfoMap)
    {
        for (Segment segment : list)
        {
            if (segment.getType() != TEXT)
            {
                continue;
            }
            String[]      split   = segment.getContent().split(" ");
            StringBuilder builder = new StringBuilder();
            for (String word : split)
            {
                if (word.indexOf('.') != -1 && word.split("\\.").length == 2)
                {
                    String[] split1                = word.split("\\.");
                    String   aliasNameOrEntityName = split1[0];
                    String   propertyName          = split[1];
                    if (tableAsNameMap.containsKey(aliasNameOrEntityName))
                    {
                        TableEntityInfo tableEntityInfo = tableEntityInfoMap.get(tableAsNameMap.get(aliasNameOrEntityName));
                        builder.append(STR.format("{}.{} ", aliasNameOrEntityName, tableEntityInfo.getPropertyNameKeyMap().get(propertyName).columnName()));
                    }
                    else if (tableEntityInfoMap.containsKey(aliasNameOrEntityName))
                    {
                        TableEntityInfo tableEntityInfo = tableEntityInfoMap.get(aliasNameOrEntityName);
                        builder.append(STR.format("{}.{} ", aliasNameOrEntityName, tableEntityInfo.getPropertyNameKeyMap().get(propertyName).columnName()));
                    }
                    else
                    {
                        builder.append(word).append(' ');
                    }
                }
                else
                {
                    builder.append(word).append(' ');
                }
            }
            if (!segment.content.endsWith(" "))
            {
                builder.deleteCharAt(builder.length() - 1);
            }
            segment.setContent(builder.toString());
        }
    }

    private static void replacePropertyNameToColumnName(List<Segment> list, Map<String, TableEntityInfo> hitTableEntityInfoMap)
    {
        Collection<TableEntityInfo> tableEntityInfos = hitTableEntityInfoMap.values();
        for (Segment segment : list)
        {
            if (segment.getType() != TEXT)
            {
                continue;
            }
            String[]      split   = segment.getContent().split(" ");
            StringBuilder builder = new StringBuilder();
            for (String word : split)
            {
                if (tableEntityInfos.stream().filter(tableEntityInfo -> tableEntityInfo.getPropertyNameKeyMap().containsKey(word)).count() > 1)
                {
                    throw new IllegalArgumentException("sql 中包含重复的无法区分的属性名:" + word);
                }
                else
                {
                    Optional<TableEntityInfo.ColumnInfo> any = tableEntityInfos.stream().map(tableEntityInfo -> tableEntityInfo.getPropertyNameKeyMap().get(word)).filter(Objects::nonNull).findAny();
                    if (any.isPresent())
                    {
                        builder.append(any.get().columnName()).append(' ');
                    }
                    else
                    {
                        List<TableEntityInfo.ColumnInfo> matchColumns = tableEntityInfos.stream().flatMap(tableEntityInfo -> tableEntityInfo.getPropertyNameKeyMap().values().stream())//
                                                                                        .filter(columnInfo -> word.contains(columnInfo.propertyName()) && isIndependent(word, columnInfo.propertyName()))//
                                                                                        .toList();
                        if (matchColumns.isEmpty())
                        {
                            builder.append(word).append(' ');
                        }
                        else
                        {
                            Set<String> set = new HashSet<>();
                            for (TableEntityInfo.ColumnInfo matchColumn : matchColumns)
                            {
                                if (!set.add(matchColumn.propertyName()))
                                {
                                    throw new IllegalArgumentException("sql 中包含重复的无法区分的属性名:" + matchColumns.get(0).propertyName());
                                }
                            }
                            for (TableEntityInfo.ColumnInfo columnInfo : matchColumns)
                            {
                                word = word.replace(columnInfo.propertyName(), columnInfo.columnName());
                            }
                            builder.append(word).append(' ');
                        }
                    }
                }
            }
            if (!segment.content.endsWith(" "))
            {
                builder.deleteCharAt(builder.length() - 1);
            }
            segment.setContent(builder.toString());
        }
    }

    private static boolean isIndependent(String s, String str)
    {
        int     index   = s.indexOf(str);
        boolean findPre = false, findAfter = false;
        for (int i = index - 1; i >= 0; i--)
        {
            if (s.charAt(i) == ' ')
            {
                ;
            }
            else if (s.charAt(i) == ',' || s.charAt(i) == '(')
            {
                findPre = true;
                break;
            }
        }
        if (!findPre)
        {
            return false;
        }
        for (int i = index + str.length(); i < s.length(); i++)
        {
            if (s.charAt(i) == ' ')
            {
                ;
            }
            else if (s.charAt(i) == ',' || s.charAt(i) == ')')
            {
                findAfter = true;
                break;
            }
        }
        return findAfter;
    }

    private static Map<String, TableEntityInfo> findHitTableEntityInfoMap(List<Segment> list, Class<?>... entities)
    {
        Map<String, TableEntityInfo> tableEntityInfoMap    = Arrays.stream(entities).map(TableEntityInfo::parse).collect(Collectors.toMap(TableEntityInfo::getClassSimpleName, Function.identity()));
        Map<String, TableEntityInfo> hitTableEntityInfoMap = new HashMap<>();
        for (Segment each : list)
        {
            if (each.getType() != TEXT)
            {
                continue;
            }
            String[] split = each.getContent().split(" ");
            for (String s : split)
            {
                if (tableEntityInfoMap.containsKey(s))
                {
                    hitTableEntityInfoMap.put(s, tableEntityInfoMap.get(s));
                }
            }
        }
        return hitTableEntityInfoMap;
    }

    private static void replaceEntityNameToTableName(List<Segment> list, Map<String, TableEntityInfo> tableEntityInfoMap)
    {
        for (Segment each : list)
        {
            if (each.getType() != TEXT)
            {
                continue;
            }
            String[]      split   = each.content.split(" ");
            StringBuilder builder = new StringBuilder();
            for (String s : split)
            {
                if (tableEntityInfoMap.containsKey(s))
                {
                    builder.append(tableEntityInfoMap.get(s).getTableName()).append(' ');
                }
                else
                {
                    builder.append(s).append(' ');
                }
            }
            if (!each.content.endsWith(" "))
            {
                builder.deleteCharAt(builder.length() - 1);
            }
            each.content = builder.toString();
        }
    }

    /**
     * key 是表别名，value是类简单名
     *
     * @param list
     * @param tableEntityInfoMap
     * @return
     */
    private static Map<String, String> parseTableAsNameMap(List<Segment> list, Map<String, TableEntityInfo> tableEntityInfoMap)
    {
        Map<String, String> tableAsNameMap = new HashMap<>();
        for (Segment each : list)
        {
            if (each.getType() != TEXT)
            {
                continue;
            }
            String[] split = each.content.split(" ");
            for (int i = 0; i < split.length; i++)
            {
                if (tableEntityInfoMap.containsKey(split[i]))
                {
                    if (i + 2 < split.length && split[i + 1].equalsIgnoreCase("as"))
                    {
                        tableAsNameMap.put(split[i + 2], split[i]);
                    }
                    else if (i + 1 > split.length && !KeyWord.isKeyWord(split[i + 1]))
                    {
                        tableAsNameMap.put(split[i + 1], split[i]);
                    }
                }
            }
        }
        return tableAsNameMap;
    }

    /**
     * 需要忽略的情况：
     * ${}内的
     * #{}内的
     * <% %>内的
     */
    private static List<Segment> parseSegments(String sql)
    {
        List<Segment> list  = new LinkedList<>();
        int           type  = TEXT;
        int           start = 0;
        for (int i = 0; i < sql.length(); i++)
        {
            char c = sql.charAt(i);
            switch (type)
            {
                case TEXT ->
                {
                    if (c == '#' && i + 1 < sql.length() && sql.charAt(i + 1) == '{')
                    {
                        list.add(new Segment().setType(TEXT).setContent(sql.substring(start, i)));
                        start = i;
                        type  = VARIABLE;
                    }
                    else if (c == '$' && i + 1 < sql.length() && sql.charAt(i + 1) == '{')
                    {
                        list.add(new Segment().setType(TEXT).setContent(sql.substring(start, i)));
                        start = i;
                        type  = PARAM;
                    }
                    else if (c == '<' && i + 1 < sql.length() && sql.charAt(i + 1) == '%')
                    {
                        list.add(new Segment().setType(TEXT).setContent(sql.substring(start, i)));
                        start = i;
                        type  = EXECUTION;
                    }
                    else
                    {
                        ;
                    }
                }
                case PARAM, VARIABLE ->
                {
                    if (c == '}')
                    {
                        list.add(new Segment().setContent(sql.substring(start, i + 1)).setType(type));
                        start = i + 1;
                        type  = TEXT;
                    }
                    else
                    {
                        ;
                    }
                }
                case EXECUTION ->
                {
                    if (c == '%' && i + 1 < sql.length() && sql.charAt(i + 1) == '>')
                    {
                        list.add(new Segment().setContent(sql.substring(start, i + 2)).setType(type));
                        start = i + 2;
                        type  = TEXT;
                    }
                    else
                    {
                        ;
                    }
                }
            }
        }
        if (start < sql.length())
        {
            list.add(new Segment().setContent(sql.substring(start)).setType(TEXT));
        }
        return list;
    }

    @Data
    @Accessors(chain = true)
    static class Segment
    {
        String content;
        int    type;
    }
}
