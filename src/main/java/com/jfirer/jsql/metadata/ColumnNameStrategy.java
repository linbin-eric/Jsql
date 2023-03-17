package com.jfirer.jsql.metadata;

import java.util.function.IntFunction;

public interface ColumnNameStrategy
{
    /**
     * 将类的属性名称转换为对应的数据库列名称
     *
     * @return
     */
    String toColumnName(String name);

    class OriginallyName implements ColumnNameStrategy
    {
        @Override
        public String toColumnName(String name)
        {
            return name;
        }
    }

    class LowerCaseName implements ColumnNameStrategy
    {
        static LowerCaseName instance = new LowerCaseName();

        @Override
        public String toColumnName(String name)
        {
            return ColumnNameStrategy.getString(name, c -> (char) Character.toLowerCase(c));
        }
    }

    class UpperCaseName implements ColumnNameStrategy
    {
        static UpperCaseName instance = new UpperCaseName();

        @Override
        public String toColumnName(String name)
        {
            return ColumnNameStrategy.getString(name, c -> (char) Character.toUpperCase(c));
        }
    }

    private static String getString(String name, IntFunction<Character> fn)
    {
        StringBuilder cache = new StringBuilder(20);
        int           index = 0;
        while (index < name.length())
        {
            char c = name.charAt(index);
            if (c >= 'A' && c <= 'Z')
            {
                cache.append('_').append(fn.apply(c));
            }
            else
            {
                cache.append(fn.apply(c));
            }
            index += 1;
        }
        return cache.toString().toLowerCase();
    }
}
