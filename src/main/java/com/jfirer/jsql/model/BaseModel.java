package com.jfirer.jsql.model;

import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.support.LockMode;
import com.jfirer.jsql.model.support.SFunction;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BaseModel implements Model
{
    public enum ModelType
    {
        query, delete, update, insert, batchInsert
    }

    public record ModelResult(String sql, Class<?> returnType, List<Object> paramValues,
                              TableEntityInfo.PkReturnType pkReturnType)
    {
    }

    record Table(Class<?> tableClass, String asName, String mode)
    {
        @Override
        public String toString()
        {
            TableEntityInfo parse   = TableEntityInfo.parse(tableClass);
            String          segment = parse.getTableName().equals(asName) ? parse.getTableName() : parse.getTableName() + " as " + asName;
            switch (mode)
            {
                case "from":
                    return "from " + segment + " ";
                case "left join":
                    return " left join " + segment + " ";
                case "right join":
                    return " right join " + segment + " ";
                case "inner join":
                    return " inner join " + segment + " ";
                case "full join":
                    return " full join " + segment + " ";
                default:
                    throw new IllegalArgumentException();
            }
        }

        public void append(StringBuilder builder)
        {
            builder.append(toString());
        }
    }

    record On(Param param)
    {
        public void append(BaseModel model, StringBuilder builder, List<Object> params)
        {
            builder.append(" on ");
            ((InternalParam) param).renderSql(model, builder, params);
        }
    }

    record Update(Class ckass)
    {
        public String toString()
        {
            return "update " + TableEntityInfo.parse(ckass).getTableName() + " set ";
        }
    }

    record Delete(Class ckass)
    {
        @Override
        public String toString()
        {
            return "delete from " + TableEntityInfo.parse(ckass).getTableName();
        }
    }

    class Select
    {
        /*模式1：直接设定select字段的内容*/
        final String content;
        String className;
        String fieldName;
        /*---*/
        /*模式2：通过fn来解析出select字段的内容*/ SFunction<?, ?> fn;
        String    function;
        String    asName;
        BaseModel model;
        /*---*/

        public Select(SFunction<?, ?> fn, String function, String asName, BaseModel model)
        {
            this.fn       = fn;
            this.function = function;
            this.asName   = asName;
            this.model    = model;
            this.content  = null;
        }

        public Select(String content)
        {
            this.content = content;
        }

        public Select(String content, String className, String fieldName)
        {
            this.content   = content;
            this.className = className;
            this.fieldName = fieldName;
        }

        @Override
        public String toString()
        {
            if (content == null)
            {
                String result;
                if (function == null)
                {
                    result = model.findColumnName(fn);
                }
                else
                {
                    result = function + "(" + model.findColumnName(fn) + ")";
                }
                if (asName != null)
                {
                    result += " as " + asName;
                }
                return result;
            }
            else
            {
                return content;
            }
        }
    }

    record set(String columnName, Object value, boolean anotherField)
    {
    }

    record OrderBy(SFunction<?, ?> fn, boolean desc, BaseModel model)
    {
        @Override
        public String toString()
        {
            String columnName = model.findColumnName(fn);
            return desc ? columnName + " desc" : columnName + " asc";
        }
    }

    record GroupBy(SFunction<?, ?> fn, BaseModel model)
    {
        @Override
        public String toString()
        {
            return model.findColumnName(fn);
        }
    }

    public record Insert(String columnName, Object value)
    {
    }

    List<Record>          from;
    List<Select>          select;
    List<SFunction<?, ?>> exclude;
    List<Record>          orderBy;
    List<Record>          groupBy;
    protected       ModelType                    type;
    protected       Param                        param;
    protected       Class<?>                     returnType;
    protected       Page                         page;
    protected       TableEntityInfo.PkReturnType pkReturnType;
    protected       LockMode                     lockMode;
    protected final List<Object>                 paramValues  = new ArrayList<>();
    protected       boolean                      alreadyBuild = false;

    public BaseModel()
    {
        type    = ModelType.query;
        select  = new LinkedList<>();
        from    = new LinkedList<>();
        exclude = new LinkedList<>();
        orderBy = new LinkedList<>();
        groupBy = new LinkedList<>();
    }

    @Override
    public Model fromAs(Class<?> ckass, String asName)
    {
        from.add(new Table(ckass, asName, "from"));
        return this;
    }

    @Override
    public <T> Model addSelect(SFunction<T, ?>... fns)
    {
        for (SFunction<T, ?> fn : fns)
        {
            select.add(new Select(fn, null, null, this));
        }
        return this;
    }

    public String findColumnName(SFunction<?, ?> fn)
    {
        String implClass = fn.getImplClass();
        switch (type)
        {
            case query ->
            {
                Table tableAs = from.stream()//
                                    .filter(record -> record instanceof Table) //
                                    .filter(record -> {
                                        Class<?> tableClass = ((Table) record).tableClass();
                                        while (tableClass != Object.class)
                                        {
                                            if (tableClass.getName().equals(implClass))
                                            {
                                                return true;
                                            }
                                            else
                                            {
                                                tableClass = tableClass.getSuperclass();
                                            }
                                        }
                                        return false;
                                    })//
                                    .map(record -> ((Table) record))//
                                    .findAny().orElseThrow();
                return tableAs.asName + "." + TableEntityInfo.parse(tableAs.tableClass).getPropertyNameKeyMap().get(fn.resolveFieldName()).columnName();
            }
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    @Override
    public <T> Model selectAs(SFunction<T, ?> fn, String asName)
    {
        select.add(new Select(fn, null, asName, this));
        return this;
    }

    public <T> Model addSelectWithFunction(SFunction<T, ?> fn, String function, String asName)
    {
        select.add(new Select(fn, function, asName, this));
        return this;
    }

    @Override
    public <T> Model exclude(SFunction<T, ?>... fns)
    {
        for (SFunction<?, ?> each : fns)
        {
            exclude.add(each);
        }
        return this;
    }

    public <T> Model selectCount(SFunction<T, ?> fn)
    {
        select.add(new Select(fn, "count", null, this));
        return this;
    }

    public Model selectCount()
    {
        select.add(new Select("count(*)"));
        returnType = Integer.class;
        return this;
    }

    @Override
    public Model leftJoin(Class ckass)
    {
        from.add(new Table(ckass, TableEntityInfo.parse(ckass).getTableName(), "left join"));
        return this;
    }

    @Override
    public Model rightJoin(Class<?> ckass)
    {
        from.add(new Table(ckass, TableEntityInfo.parse(ckass).getTableName(), "right join"));
        return this;
    }

    @Override
    public Model fullJoin(Class<?> ckass)
    {
        from.add(new Table(ckass, TableEntityInfo.parse(ckass).getTableName(), "full join"));
        return this;
    }

    @Override
    public Model innerJoin(Class<?> ckass)
    {
        from.add(new Table(ckass, TableEntityInfo.parse(ckass).getTableName(), "inner join"));
        return this;
    }

    @Override
    public <E, T> Model on(Param param)
    {
        from.add(new On(param));
        return this;
    }

    @Override
    public Model where(Param param)
    {
        this.param = param;
        return this;
    }

    @Override
    public <T> Model orderBy(SFunction<T, ?> fn, boolean desc)
    {
        orderBy.add(new OrderBy(fn, desc, this));
        return this;
    }

    @Override
    public <T> Model groupBy(SFunction<T, ?> fn)
    {
        groupBy.add(new GroupBy(fn, this));
        return this;
    }

    @Override
    public Model returnType(Class<?> ckass)
    {
        returnType = ckass;
        return this;
    }

    @Override
    public Model lockMode(LockMode lockMode)
    {
        this.lockMode = lockMode;
        return this;
    }

    protected Class<?> getReturnType()
    {
        if (type == ModelType.query)
        {
            if (returnType != null)
            {
                return returnType;
            }
            else if (select.size() > 1)
            {
                return ((Table) from.get(0)).tableClass;
            }
            else
            {
                Select select = this.select.get(0);
                if (select.fn == null)
                {
                    return ((Table) from.get(0)).tableClass;
                }
                String fieldName     = select.fn.resolveFieldName();
                String implClass     = select.fn.getImplClass();
                Field  declaredField = null;
                try
                {
                    declaredField = Thread.currentThread().getContextClassLoader().loadClass(implClass).getDeclaredField(fieldName);
                    return declaredField.getType();
                }
                catch (NoSuchFieldException | ClassNotFoundException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        else
        {
            return null;
        }
    }

    @Override
    public Model page(Page page)
    {
        this.page = page;
        return this;
    }

    @Override
    public Model limit(int size)
    {
        page = new Page();
        page.setFetchSum(false);
        page.setSize(size);
        page.setOffset(0);
        return this;
    }

    @Override
    public ModelResult getResult()
    {
        if (alreadyBuild == false)
        {
            alreadyBuild = true;
        }
        else
        {
            throw new IllegalStateException();
        }
        String sql = getSql();
        if (page != null)
        {
            paramValues.add(page);
        }
        return new ModelResult(sql, getReturnType(), paramValues, pkReturnType);
    }

    protected String getSql()
    {
        StringBuilder builder = new StringBuilder();
        switch (type)
        {
            case query ->
            {
                builder.append("select ");
                if (select.isEmpty())
                {
                    if (from.isEmpty())
                    {
                        throw new IllegalArgumentException("from数据为空，请检查语句");
                    }
                    from.stream()//
                        .filter(record -> record instanceof Table)//
                        .map(record -> ((Table) record))//
                        .forEach(table -> TableEntityInfo.parse(table.tableClass).getPropertyNameKeyMap().values().forEach(columnInfo -> select.add(new Select(table.asName() + "." + columnInfo.columnName(), table.tableClass.getName(), columnInfo.propertyName()))));
                }
                if (from.isEmpty())
                {
                    String tableClassName = select.stream().filter(select -> select.fn != null).map(select -> select.fn.getImplClass()).findFirst().orElseThrow();
                    try
                    {
                        from(Thread.currentThread().getContextClassLoader().loadClass(tableClassName));
                    }
                    catch (ClassNotFoundException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                if (exclude.isEmpty() == false)
                {
                    select = select.stream().filter(v -> {
                        if (v.content == null)
                        {
                            return exclude.stream().noneMatch(ex -> ex.resolveFieldName().equalsIgnoreCase(v.fn.resolveFieldName()) && ex.getImplClass() == v.fn.getImplClass());
                        }
                        else if (v.className == null)
                        {
                            return true;
                        }
                        else
                        {
                            return exclude.stream().noneMatch(ex -> ex.resolveFieldName().equalsIgnoreCase(v.fieldName) && ex.getImplClass().equalsIgnoreCase(v.className));
                        }
                    }).toList();
                }
                String segment = select.stream().map(select -> select.toString()).collect(Collectors.joining(","));
                builder.append(segment).append(' ');
                from.stream().forEach(record -> {
                    if (record instanceof Table table)
                    {
                        table.append(builder);
                    }
                    else
                    {
                        ((On) record).append(BaseModel.this, builder, paramValues);
                    }
                });
                if (param != null)
                {
                    builder.append(" where ");
                    ((InternalParam) param).renderSql(this, builder, paramValues);
                }
                else
                {
                }
                if (!groupBy.isEmpty())
                {
                    builder.append(" group by ").append(groupBy.stream().map(record -> record.toString()).collect(Collectors.joining(",")));
                }
                else
                {
                }
                if (!orderBy.isEmpty())
                {
                    builder.append(" order by ");
                    String orderBy = this.orderBy.stream().map(record -> record.toString()).collect(Collectors.joining(","));
                    builder.append(orderBy);
                }
                else
                {
                }
                if (lockMode != null)
                {
                    switch (lockMode)
                    {
                        case SHARE -> builder.append(" lock in share mode ");
                        case UPDATE -> builder.append(" for update ");
                    }
                }
            }
//            case delete ->
//            {
//                builder.append(delete.toString());
//                if (param != null)
//                {
//                    builder.append(" where ");
//                    ((InternalParam) param).renderSql(this, builder, paramValues);
//                }
//                else
//                {
//                }
//            }
//            case update ->
//            {
//                builder.append(update.toString()).append(" ");
//                String setSegment = set.stream().map(record -> {
//                    BaseModel.set set = (set) record;
//                    if (set.value instanceof BaseModel m)
//                    {
//                        ModelResult result = m.getResult();
//                        paramValues.addAll(result.paramValues());
//                        return ((set) record).columnName + "=( " + result.sql + ")";
//                    }
//                    else if (set.anotherField)
//                    {
//                        return ((set) record).columnName + " = " + ((BaseModel.set) record).value;
//                    }
//                    else
//                    {
//                        paramValues.add(set.value);
//                        return ((set) record).columnName + "=?";
//                    }
//                }).collect(Collectors.joining(","));
//                builder.append(setSegment);
//                if (param != null)
//                {
//                    builder.append(" where ");
//                    ((InternalParam) param).renderSql(this, builder, paramValues);
//                }
//                else
//                {
//                }
//            }
//
        }
        return builder.toString();
    }
}







