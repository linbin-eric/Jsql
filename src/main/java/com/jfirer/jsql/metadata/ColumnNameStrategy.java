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
            return name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
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
