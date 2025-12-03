package cc.jfire.jsql.model.model.query;

public interface Select
{
    String toSql();

    Class<?> implClass();

    String fieldName();
}
