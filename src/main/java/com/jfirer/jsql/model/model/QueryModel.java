package com.jfirer.jsql.model.model;

import com.jfirer.baseutil.STR;
import com.jfirer.baseutil.StringUtil;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.InternalParam;
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.Param;
import com.jfirer.jsql.model.model.query.FixedContentSelect;
import com.jfirer.jsql.model.model.query.TypeAndNameSelect;
import com.jfirer.jsql.model.model.query.Select;
import com.jfirer.jsql.model.param.NoOpParam;
import com.jfirer.jsql.model.support.LockMode;
import com.jfirer.jsql.model.support.SFunction;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class QueryModel implements Model
{
    private         List<Select>          selects     = new LinkedList<>();
    private         List<SFunction<?, ?>> exclude     = new LinkedList<>();
    private         List<Table>           from        = new LinkedList<>();
    private         List<OrderBy>         orderBy     = new LinkedList<>();
    private         List<GroupBy>         groupBy     = new LinkedList<>();
    @Getter
    protected       Page                  page;
    private         Class<?>              returnType;
    protected final List<Object>          paramValues = new ArrayList<>();
    protected       Param                 where;
    protected       LockMode              lockMode;

    public <T> QueryModel addSelect(SFunction<T, ?>... fns)
    {
        for (SFunction<?, ?> fn : fns)
        {
            addSelect(fn, null, null);
        }
        return this;
    }

    private <T> void addSelect(SFunction<T, ?> fn, String function, String asName)
    {
        selects.add(new TypeAndNameSelect(fn.getImplClass(), fn.resolveFieldName(), function, asName, this));
    }

    public void addSelect(TableEntityInfo.ColumnInfo columnInfo, Class<?> implClass)
    {
        selects.add(new TypeAndNameSelect(implClass, columnInfo.propertyName(), null, null, this));
    }

    public <T> QueryModel selectAs(SFunction<T, ?> fn, String asName)
    {
        addSelect(fn, null, asName);
        return this;
    }

    public <T> QueryModel addSelectWithFunction(SFunction<T, ?> fn, String function, String asName)
    {
        addSelect(fn, function, asName);
        return this;
    }

    public <T> QueryModel selectCount(SFunction<T, ?> fn)
    {
        addSelect(fn, "count", null);
        return this;
    }

    public QueryModel selectCount()
    {
        selects.add(new FixedContentSelect("count(*)"));
        returnType = Integer.class;
        return this;
    }

    public <T> QueryModel exclude(SFunction<T, ?>... fns)
    {
        for (SFunction<?, ?> each : fns)
        {
            exclude.add(each);
        }
        return this;
    }

    public QueryModel fromAs(Class<?> ckass, String asName)
    {
        if (from.stream().noneMatch(table -> table.tableClass.equals(ckass) && table.asName.equalsIgnoreCase(asName)))
        {
            from.add(new Table(ckass, asName, "from"));
        }
        return this;
    }

    public QueryModel from(Class<?> ckass)
    {
        fromAs(ckass, null);
        return this;
    }

    @Override
    public ModelResult getResult()
    {
        String sql = getSql();
        if (page != null)
        {
            paramValues.add(page);
        }
        return new ModelResult(sql, paramValues);
    }

    public QueryModel leftJoin(Class ckass)
    {
        from.add(new Table(ckass, "", "left join"));
        return this;
    }

    public QueryModel leftJoin(Class ckass, String asName)
    {
        from.add(new Table(ckass, asName, "left join"));
        return this;
    }

    public QueryModel rightJoin(Class<?> ckass)
    {
        from.add(new Table(ckass, "", "right join"));
        return this;
    }

    public QueryModel rightJoin(Class<?> ckass, String asName)
    {
        from.add(new Table(ckass, asName, "right join"));
        return this;
    }

    public QueryModel fullJoin(Class<?> ckass)
    {
        from.add(new Table(ckass, "", "full join"));
        return this;
    }

    public QueryModel fullJoin(Class<?> ckass, String asName)
    {
        from.add(new Table(ckass, asName, "full join"));
        return this;
    }

    public QueryModel innerJoin(Class<?> ckass)
    {
        from.add(new Table(ckass, "", "inner join"));
        return this;
    }

    public QueryModel innerJoin(Class<?> ckass, String asName)
    {
        from.add(new Table(ckass, asName, "inner join"));
        return this;
    }

    public <E, T> QueryModel on(Param param)
    {
        Table table = from.get(from.size() - 1);
        table.setOn(param);
        return this;
    }

    public <T> QueryModel orderBy(SFunction<T, ?> fn, boolean desc)
    {
        orderBy.add(new OrderBy(fn.getImplClass(), fn.resolveFieldName(), desc));
        return this;
    }

    public <T> QueryModel groupBy(SFunction<T, ?> fn)
    {
        groupBy.add(new GroupBy(fn));
        return this;
    }

    public QueryModel returnType(Class<?> ckass)
    {
        returnType = ckass;
        return this;
    }

    public QueryModel lockMode(LockMode lockMode)
    {
        this.lockMode = lockMode;
        return this;
    }

    public Class<?> getReturnType()
    {
        if (returnType != null)
        {
            return returnType;
        }
        else if (selects.size() > 1)
        {
            return from.get(0).tableClass;
        }
        else
        {
            Select select = this.selects.get(0);
            if (select instanceof FixedContentSelect)
            {
                return from.get(0).tableClass;
            }
            else if (select instanceof TypeAndNameSelect typeAndNameSelect)
            {
                try
                {
                    return typeAndNameSelect.getImplClass().getDeclaredField(typeAndNameSelect.getFieldName()).getType();
                }
                catch (NoSuchFieldException e)
                {
                    throw new RuntimeException(e);
                }
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }
    }

    public QueryModel page(Page page)
    {
        this.page = page;
        return this;
    }

    public QueryModel offset(int offset)
    {
        if (page == null)
        {
            page = new Page();
            page.setFetchSum(true);
            page.setOffset(0);
            page.setSize(10);
        }
        page.setOffset(offset);
        return this;
    }

    public QueryModel limit(int size)
    {
        if (page == null)
        {
            page = new Page();
            page.setFetchSum(true);
            page.setOffset(0);
            page.setSize(10);
        }
        page.setSize(size);
        return this;
    }

    public QueryModel where(Param param)
    {
        this.where = param;
        return this;
    }

    protected String getSql()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("select ");
        if (selects.isEmpty())
        {
            if (from.isEmpty())
            {
                throw new IllegalArgumentException("from数据为空，请检查语句");
            }
            for (Table table : from)
            {
                TableEntityInfo entityInfo = TableEntityInfo.parse(table.tableClass);
                for (TableEntityInfo.ColumnInfo columnInfo : entityInfo.getPropertyNameKeyMap().values())
                {
                    selects.add(new TypeAndNameSelect(table.tableClass, columnInfo.propertyName(), null, null, this));
                }
            }
        }
        else
        {
            if (from.isEmpty())
            {
                Set<? extends Class<?>> collect = selects.stream()//
                                                         .filter(s -> s.implClass() != null)//
                                                         .map(s -> s.implClass()).collect(Collectors.toSet());
                if (collect.size() > 1)
                {
                    throw new IllegalArgumentException("使用Select添加了1张以上的表，需要显式的使用from方法添加表");
                }
                else
                {
                    from(collect.iterator().next());
                }
            }
        }
        if (exclude.isEmpty() == false)
        {
            selects = selects.stream().filter(v -> exclude.stream().noneMatch(ex -> ex.resolveFieldName().equalsIgnoreCase(v.fieldName()) && ex.getImplClass().equals(v.implClass()))).toList();
        }
        String segment = selects.stream().map(select -> select.toSql()).collect(Collectors.joining(","));
        builder.append(segment).append(' ');
        from.forEach(table -> table.append(builder));
        if (where != null && where != NoOpParam.INSTANCE)
        {
            builder.append(" where ");
            ((InternalParam) where).renderSql(this, builder, paramValues);
        }
        else
        {
        }
        if (!groupBy.isEmpty())
        {
            builder.append(" group by ").append(groupBy.stream().map(g -> g.toString(QueryModel.this)).collect(Collectors.joining(",")));
        }
        else
        {
        }
        if (!orderBy.isEmpty())
        {
            builder.append(" order by ");
            builder.append(orderBy.stream().map(o -> o.toString(QueryModel.this)).collect(Collectors.joining(",")));
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
        return builder.toString();
    }

    @Override
    public String findColumnName(Class<?> implClass, String fieldName)
    {
        Table tableAs = from.stream()//
                            .filter(record -> {
                                Class<?> tableClass = record.tableClass;
                                while (tableClass != Object.class)
                                {
                                    if (tableClass.equals(implClass))
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
                            .findAny().orElseThrow(() -> new IllegalArgumentException(STR.format("在from内容中没有类:{}对应的表", implClass.getName())));
        TableEntityInfo tableEntityInfo = TableEntityInfo.parse(tableAs.tableClass);
        if (StringUtil.isBlank(tableAs.asName))
        {
            return tableEntityInfo.getPropertyNameKeyMap().get(fieldName).columnName();
        }
        else
        {
            return tableAs.asName + "." + tableEntityInfo.getPropertyNameKeyMap().get(fieldName).columnName();
        }
    }

    class Table
    {
        private Class<?> tableClass;
        private String   asName;
        private String   mode;
        private Param    on;

        public Table(Class<?> tableClass, String asName, String mode)
        {
            this.tableClass = tableClass;
            this.asName     = asName;
            this.mode       = mode;
        }

        public void setOn(Param on)
        {
            this.on = on;
        }

        public void append(StringBuilder builder)
        {
            TableEntityInfo parse   = TableEntityInfo.parse(tableClass);
            String          segment = StringUtil.isBlank(asName) ? parse.getTableName() : parse.getTableName() + " as " + asName;
            switch (mode)
            {
                case "from" -> builder.append("from " + segment + " ");
                case "left join" -> builder.append(" left join " + segment + " ");
                case "right join" -> builder.append(" right join " + segment + " ");
                case "inner join" -> builder.append(" inner join " + segment + " ");
                case "full join" -> builder.append(" full join " + segment + " ");
                default -> throw new IllegalArgumentException();
            }
            if (on != null)
            {
                builder.append("on ");
                ((InternalParam) on).renderSql(QueryModel.this, builder, paramValues);
            }
        }
    }

    record OrderBy(Class<?> implClass, String fieldName, boolean desc)
    {
        public String toString(QueryModel model)
        {
            return model.findColumnName(implClass, fieldName) + (desc ? " desc" : " asc");
        }
    }

    record GroupBy(SFunction<?, ?> fn)
    {
        public String toString(QueryModel model)
        {
            return model.findColumnName(fn.getImplClass(), fn.resolveFieldName());
        }
    }
}
