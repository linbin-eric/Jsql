package cc.jfire.jsql.analyse.token;

import cc.jfire.jsql.metadata.TableEntityInfo;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SqlLexer
{
    private static final int TEXT      = 1;
    private static final int PARAM     = 2;
    private static final int VARIABLE  = 3;
    private static final int EXECUTION = 4;

    /**
     * 解析SQL语句，将其中的实体类名和属性名转换为对应的数据库表名和列名。
     * <p>
     * 该方法支持以下几种名称转换场景：
     * <ul>
     *   <li>实体类简单名称 → 表名（如：User → user_table）</li>
     *   <li>属性名称 → 列名（如：userName → user_name）</li>
     *   <li>别名.属性名 → 别名.列名（如：u.userName → u.user_name）</li>
     *   <li>类名.属性名 → 表名.列名（如：User.userName → user_table.user_name）</li>
     *   <li>函数中的属性引用（如：concat(user.name,'ss') → concat(u.name_col,'ss')）</li>
     * </ul>
     * <p>
     * 解析过程会自动跳过以下特殊语法块，不对其内容进行转换：
     * <ul>
     *   <li>${...} - 参数占位符</li>
     *   <li>#{...} - 变量占位符</li>
     *   <li>&lt;% %&gt; - 执行块</li>
     * </ul>
     * <p>
     * 处理流程：
     * <ol>
     *   <li>将SQL分割为多个Segment片段，区分普通文本和特殊语法块</li>
     *   <li>识别SQL中实际使用的实体类</li>
     *   <li>解析表别名映射关系（如：User as u 或 User u）</li>
     *   <li>将实体类名替换为数据库表名</li>
     *   <li>将"别名.属性名"或"类名.属性名"格式替换为对应的列名</li>
     *   <li>将独立的属性名替换为列名</li>
     * </ol>
     *
     * @param sql      原始SQL语句，可包含实体类名和属性名
     * @param entities 可能在SQL中使用的实体类数组，用于提供名称映射信息
     * @return 转换后的SQL语句，其中实体类名和属性名已替换为数据库表名和列名
     */
    public static String parse(String sql, Class<?>... entities)
    {
        List<Segment> list         = parseSegments(sql);
        List<Segment> pureTextList = list.stream().filter(Segment::isText).collect(Collectors.toList());
        /**
         * 需要分析的有几种情况：
         * 1、User 类的简单名称
         * 2、name 类的属性名称
         * 3、user.name  类的别名.类的属性名
         * 4、User.name 类的简单名称.类的属性名
         * 5、concat(user.name,'ss');
         * 6、concat(User.name,'ss')
         * 7、concat(name,'ss')
         * 8、concat(User.name1,u.na2,name3)
         * u.na2->u.name3
         * name3->nnnn3
         * 如何解决一个类是User，表名是user，而它的一个字段也是user，对应的列名是user2。当第一次将User替换为user后，后续检查到user，又会将user替换为user2.就导致了错误。
         * 简而言之，如何解决字段名和表名重复带来的冲突
         */
        Map<String, TableEntityInfo> tableEntityInfoMap = findUsedEntity(entities, list);
        Map<String, String>          entityAliasNameMap = parseTableAsNameMap(pureTextList, tableEntityInfoMap);
        replaceEntityNameToTableName(pureTextList, tableEntityInfoMap);
        list         = flatmapForNewList(list);
        pureTextList = list.stream().filter(Segment::isText).collect(Collectors.toList());
        replaceEntityPropertyNameToColumnName(pureTextList, entityAliasNameMap, tableEntityInfoMap);
        list         = flatmapForNewList(list);
        pureTextList = list.stream().filter(Segment::isText).collect(Collectors.toList());
        replacePropertyNameToColumnName(pureTextList, tableEntityInfoMap);
        list = flatmapForNewList(list);
        return String.join(" ", list.stream().map(Segment::getContent).toList());
    }

    private static Map<String, TableEntityInfo> findUsedEntity(Class<?>[] entities, List<Segment> list)
    {
        Map<String, TableEntityInfo> tableEntityInfoMap = Arrays.stream(entities).map(TableEntityInfo::parse).collect(Collectors.toMap(TableEntityInfo::getClassSimpleName, Function.identity()));
        List<String>                 hit                = new LinkedList<>();
        for (Segment each : list)
        {
            Set<String> entityNames = tableEntityInfoMap.keySet();
            for (String entityName : entityNames)
            {
                if (each.getContent().contains(entityName) && isIndependent(each.getContent(), entityName))
                {
                    hit.add(entityName);
                }
            }
        }
        return hit.stream().distinct().map(tableEntityInfoMap::get).collect(Collectors.toMap(TableEntityInfo::getClassSimpleName, Function.identity()));
    }

    private static List<Segment> flatmapForNewList(List<Segment> list)
    {
        List<Segment> newList = new ArrayList<>();
        for (Segment segment : list)
        {
            if (segment.getSplit() == null)
            {
                newList.add(segment);
            }
            else
            {
                newList.addAll(segment.getSplit());
            }
        }
        list = newList;
        return list;
    }

    private static void replaceEntityPropertyNameToColumnName(List<Segment> list, Map<String, String> tableAsNameMap, Map<String, TableEntityInfo> tableEntityInfoMap)
    {
        for (Segment segment : list)
        {
            if (segment.isMarkForEntity())
            {
                continue;
            }
            Set<String> aliasTableProperties = tableAsNameMap.entrySet().stream()//
                                                             .flatMap(entry -> tableEntityInfoMap.get(entry.getValue()).getPropertyNameKeyMap().keySet().stream().map(propertyName -> entry.getKey() + "." + propertyName))//
                                                             .collect(Collectors.toSet());
            Set<String> entityTableProperties = tableEntityInfoMap.values().stream()//
                                                                  .flatMap(tableEntityInfo -> tableEntityInfo.getPropertyNameKeyMap().values().stream().map(columnInfo -> tableEntityInfo.getClassSimpleName() + "." + columnInfo.propertyName()))//
                                                                  .collect(Collectors.toSet());
            List<String> matches   = new LinkedList<>();
            String       word      = segment.getContent();
            String       finalWord = word;
            aliasTableProperties.stream().filter(alias -> finalWord.contains(alias) && isIndependent(finalWord, alias)).forEach(matches::add);
            entityTableProperties.stream().filter(entity -> finalWord.contains(entity) && isIndependent(finalWord, entity)).forEach(matches::add);
            if (matches.isEmpty())
            {
                ;
            }
            else
            {
                List<Segment> segments = new ArrayList<>();
                for (String match : matches)
                {
                    int      index           = word.indexOf(match);
                    String[] nameAndProperty = match.split("\\.");
                    String   name            = nameAndProperty[0];
                    String   propertyName    = nameAndProperty[1];
                    String   columnName      = null;
                    if (aliasTableProperties.contains(match))
                    {
                        columnName = tableEntityInfoMap.get(tableAsNameMap.get(name)).getPropertyNameKeyMap().get(propertyName).columnName();
                    }
                    else if (entityTableProperties.contains(match))
                    {
                        columnName = tableEntityInfoMap.get(name).getPropertyNameKeyMap().get(propertyName).columnName();
                        name       = tableEntityInfoMap.get(name).getTableName();
                    }
                    if (index != 0)
                    {
                        segments.add(new Segment().setType(TEXT).setContent(word.substring(0, index)));
                    }
                    segments.add(new Segment().setType(TEXT).setContent(name + "." + columnName).setMarkForEntity(true));
                    word = word.substring(index + match.length());
                }
                if (word.length() != 0)
                {
                    segments.add(new Segment().setType(TEXT).setContent(word));
                }
                segment.setSplit(segments);
            }
        }
    }

    private static void replacePropertyNameToColumnName(List<Segment> list, Map<String, TableEntityInfo> hitTableEntityInfoMap)
    {
        Collection<TableEntityInfo> tableEntityInfos = hitTableEntityInfoMap.values();
        for (Segment segment : list)
        {
            if (segment.isMarkForEntity())
            {
                continue;
            }
            String word      = segment.getContent();
            String finalWord = word;
            List<TableEntityInfo.ColumnInfo> matchColumns = tableEntityInfos.stream().flatMap(tableEntityInfo -> tableEntityInfo.getPropertyNameKeyMap().values().stream())//
                                                                            .filter(columnInfo -> finalWord.contains(columnInfo.propertyName()) && isIndependent(finalWord, columnInfo.propertyName()))//
                                                                            .toList();
            if (matchColumns.isEmpty())
            {
                ;
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
                List<Segment> segments = new ArrayList<>();
                for (TableEntityInfo.ColumnInfo columnInfo : matchColumns)
                {
                    int index = word.indexOf(columnInfo.propertyName());
                    if (index != 0)
                    {
                        segments.add(new Segment().setType(TEXT).setContent(word.substring(0, index)));
                    }
                    segments.add(new Segment().setType(TEXT).setContent(columnInfo.columnName()).setMarkForEntity(true));
                    word = word.substring(index + columnInfo.propertyName().length());
                }
                if (word.length() != 0)
                {
                    segments.add(new Segment().setType(TEXT).setContent(word));
                }
                segment.setSplit(segments);
            }
        }
    }

    private static boolean isIndependent(String s, String str)
    {
        if (s.equals(str))
        {
            return true;
        }
        int     index   = s.indexOf(str);
        boolean findPre = true, findAfter = true;
        int     i       = index - 1;
        if (i >= 0)
        {
            char c = s.charAt(i);
            if (c == ',' || c == '(' || c == ')' ||//
                c == '!' || c == '#' || c == '>' || c == '<' || c == ';' || c == '?'//
                || c == ':' || c == '=' || c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '^'//
                || c == '&' || c == '|' || c == '~' || c == '$' || c == '@' || c == '`' || c == ']' || c == '}' || c == ' ')
            {
                findPre = true;
            }
            else
            {
                findPre = false;
            }
        }
        if (!findPre)
        {
            return false;
        }
        i = index + str.length();
        if (i < s.length())
        {
            char c = s.charAt(i);
            if (c == ' ')
            {
                ;
            }
            else if (c == ',' || c == ')' ||//
                     c == '!' || c == '#' || c == '>' || c == '<' || c == ';' || c == '?'//
                     || c == ':' || c == '=' || c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '^'//
                     || c == '&' || c == '|' || c == '~' || c == '$' || c == '@' || c == '`' || c == ']' || c == '}')
            {
                findAfter = true;
            }
            else
            {
                findAfter = false;
            }
        }
        return findAfter;
    }

    private static void replaceEntityNameToTableName(List<Segment> list, Map<String, TableEntityInfo> tableEntityInfoMap)
    {
        for (Segment each : list)
        {
            Set<String>  entityNames = tableEntityInfoMap.keySet();
            List<String> matches     = new LinkedList<>();
            for (String entityName : entityNames)
            {
                if (each.getContent().contains(entityName) && isIndependent(each.getContent(), entityName))
                {
                    matches.add(entityName);
                }
            }
            if (matches.isEmpty())
            {
                ;
            }
            else
            {
                List<Segment> segments = new ArrayList<>();
                String        s        = each.getContent();
                for (String match : matches)
                {
                    int    index     = s.indexOf(match);
                    String substring = s.substring(0, index);
                    if (index != 0)
                    {
                        segments.add(new Segment().setType(TEXT).setContent(substring));
                    }
                    segments.add(new Segment().setType(TEXT).setContent(tableEntityInfoMap.get(match).getTableName()).setMarkForEntity(true));
                    s = s.substring(index + match.length());
                }
                if (s.length() != 0)
                {
                    segments.add(new Segment().setType(TEXT).setContent(s));
                }
                each.setSplit(segments);
            }
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
        Map<String, String> entityAliasNameMap = new HashMap<>();
        for (int i = 0; i < list.size(); i++)
        {
            Segment segment = list.get(i);
            if (tableEntityInfoMap.containsKey(segment.getContent()))
            {
                if (i + 2 < list.size() && list.get(i + 1).getContent().equalsIgnoreCase("as"))
                {
                    entityAliasNameMap.put(list.get(i + 2).getContent(), segment.getContent());
                }
                else if (i + 1 < list.size() && !KeyWord.isKeyWord(list.get(i + 1).getContent()))
                {
                    entityAliasNameMap.put(list.get(i + 1).getContent(), segment.getContent());
                }
            }
        }
        return entityAliasNameMap;
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
                        list.addAll(Arrays.stream(sql.substring(start, i).split(" ")).map(str -> new Segment().setType(TEXT).setContent(str)).toList());
                        start = i;
                        type  = VARIABLE;
                    }
                    else if (c == '$' && i + 1 < sql.length() && sql.charAt(i + 1) == '{')
                    {
                        list.addAll(Arrays.stream(sql.substring(start, i).split(" ")).map(str -> new Segment().setType(TEXT).setContent(str)).toList());
                        start = i;
                        type  = PARAM;
                    }
                    else if (c == '<' && i + 1 < sql.length() && sql.charAt(i + 1) == '%')
                    {
                        list.addAll(Arrays.stream(sql.substring(start, i).split(" ")).map(str -> new Segment().setType(TEXT).setContent(str)).toList());
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
            list.addAll(Arrays.stream(sql.substring(start).split(" ")).map(str -> new Segment().setType(TEXT).setContent(str)).toList());
        }
        return list.stream().filter(Predicate.not(segment -> segment.isEmptyText())).toList();
    }

    @Data
    @Accessors(chain = true)
    static class Segment
    {
        String        content;
        int           type;
        boolean       markForEntity = false;
        List<Segment> split;

        public boolean isText()
        {
            return TEXT == type;
        }

        public boolean isEmptyText()
        {
            return TEXT == type && content.trim().equals("");
        }
    }
}
