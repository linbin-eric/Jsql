package com.jfirer.jsql.metadata;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@FunctionalInterface
public interface ColumnNameStrategy
{
    /**
     * 将类的属性名称转换为对应的数据库列名称
     *
     * @return
     */
    String toColumnName(String fieldName);

    ColumnNameStrategy                                                     LOW_CASE              = new LowCase();
    ConcurrentMap<Class<? extends ColumnNameStrategy>, ColumnNameStrategy> STRATEGY_INSTANCE_MAP = new ConcurrentHashMap<>();

    class LowCase implements ColumnNameStrategy
    {
        @Override
        public String toColumnName(String name)
        {
            // 处理连续的大写字母、字母数字混合等情况
            StringBuilder result = new StringBuilder();
            char[] chars = name.toCharArray();

            for (int i = 0; i < chars.length; i++)
            {
                char current = chars[i];

                if (i > 0)
                {
                    char previous = chars[i - 1];

                    // 小写字母后跟大写字母
                    if (Character.isLowerCase(previous) && Character.isUpperCase(current))
                    {
                        result.append('_');
                    }
                    // 连续大写字母
                    else if (Character.isUpperCase(previous) && Character.isUpperCase(current))
                    {
                        result.append('_');
                    }
                    // 字母后面有数字
                    else if (Character.isLetter(previous) && Character.isDigit(current))
                    {
                        result.append('_');
                    }
                    // 数字后面是字母
                    else if (Character.isDigit(previous) && Character.isLetter(current))
                    {
                        result.append('_');
                    }
                }

                result.append(Character.toLowerCase(current));
            }

            return result.toString();
        }
    }

    class SameName implements ColumnNameStrategy
    {
        @Override
        public String toColumnName(String name)
        {
            return name;
        }
    }

    static ColumnNameStrategy find(Class<? extends ColumnNameStrategy> columnNameStrategy)
    {
        return STRATEGY_INSTANCE_MAP.computeIfAbsent(columnNameStrategy, k -> {
            try
            {
                return columnNameStrategy.getConstructor().newInstance();
            }
            catch (Throwable e)
            {
                throw new RuntimeException(e);
            }
        });
    }
}
