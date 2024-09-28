package com.jfirer.jsql.metadata;

@FunctionalInterface
public interface ColumnNameStrategy
{
    /**
     * 将类的属性名称转换为对应的数据库列名称
     *
     * @return
     */
    String toColumnName(String name);

    ColumnNameStrategy LOW_CASE = name -> name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();

    class LowCase implements ColumnNameStrategy
    {
        @Override
        public String toColumnName(String name)
        {
            return name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        }
    }
}
