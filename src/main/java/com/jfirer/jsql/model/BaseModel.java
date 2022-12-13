package com.jfirer.jsql.model;

import com.jfirer.jsql.annotation.AutoIncrement;
import com.jfirer.jsql.annotation.PkGenerator;
import com.jfirer.jsql.annotation.Sequence;
import com.jfirer.jsql.model.support.LockMode;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.impl.SpecialPkEqParam;
import com.jfirer.jsql.model.impl.InternalParam;
import com.jfirer.jsql.model.support.SFunction;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class BaseModel implements Model
{
    enum ModelType
    {
        query,
        delete,
        update,
        insert;
    }

    public record FromAs(Class tableClass, String asName)
    {
        public String toString()
        {
            TableEntityInfo parse = TableEntityInfo.parse(tableClass);
            if (parse.getTableName().equals(asName))
            {
                return parse.getTableName();
            }
            else
            {
                return parse.getTableName() + " as " + asName;
            }
        }
    }

    record LeftJoin(Class<?> tableClass)
    {
        @Override
        public String toString()
        {
            return "left join " + TableEntityInfo.parse(tableClass).getTableName();
        }
    }

    record RightJoin(Class<?> tableClass)
    {
        @Override
        public String toString()
        {
            return "right join " + TableEntityInfo.parse(tableClass).getTableName();
        }
    }

    record InnerJoin(Class<?> tableClass)
    {
        @Override
        public String toString()
        {
            return "inner join " + TableEntityInfo.parse(tableClass).getTableName();
        }
    }

    record FullJoin(Class<?> tableClass)
    {
        @Override
        public String toString()
        {
            return "full join " + TableEntityInfo.parse(tableClass).getTableName();
        }
    }

    record JoinOn(String segment)
    {
        @Override
        public String toString()
        {
            return segment;
        }
    }

    record Update(Class ckass)
    {
        public String toString()
        {
            return "update " + TableEntityInfo.parse(ckass).getTableName() + " set ";
        }
    }

    record InsertInto(Class<?> ckass)
    {
        @Override
        public String toString()
        {
            return "insert into " + TableEntityInfo.parse(ckass).getTableName() + " ";
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

    record SelectAs(String columnName, String asName)
    {
        @Override
        public String toString()
        {
            return columnName + " as " + asName;
        }
    }

    record Select(String columnName)
    {
        @Override
        public String toString()
        {
            return columnName;
        }
    }

    record Count(String columnName)
    {
        @Override
        public String toString()
        {
            return "count(" + columnName + ")";
        }
    }

    record set(String columnName, Object value) {}

    record OrderBy(String columnName, boolean desc)
    {
        @Override
        public String toString()
        {
            return desc ? columnName + " desc" : columnName + " asc";
        }
    }

    record GroupBy(String columnName)
    {
        @Override
        public String toString()
        {
            return columnName;
        }
    }

    record Insert(String columnName, Object value) {}

    List<Record> from    = new LinkedList<>();
    List<Record> select  = new LinkedList<>();
    List<Record> set     = new LinkedList<>();
    List<Record> orderBy = new LinkedList<>();
    List<Record> groupBy = new LinkedList<>();
    List<Record> insert  = new LinkedList<>();
    private ModelType                    type;
    private Update                       update;
    private Delete                       delete;
    private InsertInto                   insertInto;
    private Param                        param;
    private Class<?>                     returnType;
    private Page                         page;
    private TableEntityInfo.PkReturnType pkReturnType;
    private LockMode                     lockMode;

    public void setDelete(Delete delete)
    {
        type = ModelType.delete;
        this.delete = delete;
    }

    public void setUpdate(Update update)
    {
        type = ModelType.update;
        this.update = update;
    }

    public void addFromAs(FromAs fromAs)
    {
        type = ModelType.query;
        this.from.add(fromAs);
    }

    public void setInsertInto(InsertInto insertInto)
    {
        type = ModelType.insert;
        this.insertInto = insertInto;
    }

    @Override
    public <T> Model select(SFunction<T, ?>... fns)
    {
        for (SFunction<T, ?> fn : fns)
        {
            findColumnNameAndConsumer(from, fn, (tableName, columnName) -> select.add(new Select(tableName + "." + columnName)));
        }
        return this;
    }

    @Override
    public Model selectAll(Class<?> ckass)
    {
        from.stream()//
            .filter(record -> {
                if (record instanceof FromAs from)
                {
                    return from.tableClass == ckass;
                }
                else
                {
                    return false;
                }
            })//
            .findAny()//
            .ifPresent(record -> {
                FromAs fromAs = (FromAs) record;
                TableEntityInfo.parse(fromAs.tableClass).getPropertyNameKeyMap().values().forEach(columnInfo -> select.add(new Select(fromAs.asName() + "." + columnInfo.columnName())));
            });
        return this;
    }

    public static void findColumnNameAndConsumer(List<Record> from, SFunction<?, ?> fn, BiConsumer<String, String> biConsumer)
    {
        String implClass = fn.getImplClass();
        from.stream()//
            .filter(record -> {
                if (record instanceof FromAs fAs)
                {
                    return fAs.tableClass().getName().equals(implClass);
                }
                else
                {
                    return false;
                }
            }) //
            .map(record -> ((FromAs) record))//
            .findAny()//
            .ifPresentOrElse(//
                             fromAs -> biConsumer.accept(fromAs.asName(), Objects.requireNonNull(TableEntityInfo.parse(fromAs.tableClass())//
                                                                                                                .getPropertyNameKeyMap()//
                                                                                                                .get(fn.resolveFieldName()))//
                                                                                 .columnName()),//
                             () -> {
                                 throw new IllegalArgumentException();
                             });
    }

    @Override
    public <T> Model selectAs(SFunction<T, ?> fn, String asName)
    {
        findColumnNameAndConsumer(from, fn, (tableName, columnName) -> select.add(new SelectAs(tableName + "." + columnName, asName)));
        return this;
    }

    @Override
    public <T> Model selectCount(SFunction<T, ?> fn)
    {
        type = ModelType.query;
        findColumnNameAndConsumer(from, fn, (tableName, columnName) -> select.add(new Count(tableName + "." + columnName)));
        return this;
    }

    @Override
    public Model selectCount()
    {
        type = ModelType.query;
        select.add(new Select("count(*)"));
        return this;
    }

    @Override
    public <T> Model set(SFunction<T, ?> fn, Object value)
    {
        TableEntityInfo            entityInfo = TableEntityInfo.parse(update.ckass);
        TableEntityInfo.ColumnInfo columnInfo = entityInfo.getPropertyNameKeyMap().get(fn.resolveFieldName());
        set.add(new set(entityInfo.getTableName() + "." + columnInfo.columnName(), value));
        return this;
    }

    @Override
    public <T> Model insert(SFunction<T, ?> fn, Object value)
    {
        TableEntityInfo            entityInfo = TableEntityInfo.parse(insertInto.ckass);
        TableEntityInfo.ColumnInfo columnInfo = entityInfo.getPropertyNameKeyMap().get(fn.resolveFieldName());
        insert.add(new Insert(columnInfo.columnName(), value));
        return this;
    }

    @Override
    public Model leftJoin(Class ckass)
    {
        from.add(new LeftJoin(ckass));
        return this;
    }

    @Override
    public Model rightJoin(Class<?> ckass)
    {
        from.add(new RightJoin(ckass));
        return this;
    }

    @Override
    public Model fullJoin(Class<?> ckass)
    {
        from.add(new FullJoin(ckass));
        return this;
    }

    @Override
    public Model innerJoin(Class<?> ckass)
    {
        from.add(new InnerJoin(ckass));
        return this;
    }

    @Override
    public <E, T> Model on(SFunction<T, ?> fn1, SFunction<E, ?> fn2)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("on ");
        findColumnNameAndConsumer(from, fn1, (tableName, columnName) -> builder.append(tableName + "." + columnName));
        builder.append(" = ");
        findColumnNameAndConsumer(from, fn2, (tableName, columnName) -> builder.append(tableName + "." + columnName));
        from.add(new JoinOn(builder.toString()));
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
        findColumnNameAndConsumer(from, fn, (tableName, columnName) -> orderBy.add(new OrderBy(tableName + "." + columnName, desc)));
        return this;
    }

    @Override
    public <T> Model groupBy(SFunction<T, ?> fn)
    {
        findColumnNameAndConsumer(from, fn, (tableName, columnName) -> groupBy.add(new GroupBy(tableName + "." + columnName)));
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

    private Class<?> getReturnType()
    {
        if (type == ModelType.query)
        {
            return returnType == null ? ((FromAs) from.get(0)).tableClass : returnType;
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

    public Page getPage()
    {
        return page;
    }

    private List<Object> paramValues = new LinkedList<>();

    public record ModelResult(String sql, Class<?> returnType, List<Object> paramValues, TableEntityInfo.PkReturnType pkReturnType) {}

    @Override
    public ModelResult getResult()
    {
        String sql = getSql();
        if (page != null)
        {
            paramValues.add(page);
        }
        return new ModelResult(sql, getReturnType(), paramValues, pkReturnType);
    }

    private String getSql()
    {
        paramValues.clear();
        StringBuilder builder = new StringBuilder();
        switch (type)
        {
            case query ->
            {
                builder.append("select ");
                if (select.isEmpty())
                {
                    builder.append("*");
                }
                else
                {
                    String segment = select.stream().map(select -> select.toString()).collect(Collectors.joining(","));
                    builder.append(segment);
                }
                builder.append(" from ");
                String fromSegment = from.stream().map(fromAs -> fromAs.toString()).collect(Collectors.joining(" "));
                builder.append(fromSegment);
                if (param != null)
                {
                    builder.append(" where ");
                    ((InternalParam) param).renderSql(from, builder, paramValues);
                }
                else
                {
                    ;
                }
                if (groupBy.isEmpty() == false)
                {
                    builder.append(" group by ").append(groupBy.stream().map(record -> record.toString()).collect(Collectors.joining(",")));
                }
                else
                {
                    ;
                }
                if (orderBy.isEmpty() == false)
                {
                    builder.append(" order by ");
                    String orderBy = this.orderBy.stream().map(record -> record.toString()).collect(Collectors.joining(","));
                    builder.append(orderBy);
                }
                else
                {
                    ;
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
            case delete ->
            {
                builder.append(delete.toString());
                if (param != null)
                {
                    builder.append(" where ");
                    ((InternalParam) param).renderSql(delete.ckass, builder, paramValues);
                }
                else
                {
                    ;
                }
            }
            case update ->
            {
                builder.append(update.toString()).append(" ");
                String setSegment = set.stream().map(record -> {
                    BaseModel.set set = (set) record;
                    if (set.value instanceof BaseModel m)
                    {
                        ModelResult result = m.getResult();
                        paramValues.addAll(result.paramValues());
                        return ((set) record).columnName + "=( " + result.sql + ")";
                    }
                    else
                    {
                        paramValues.add(set.value);
                        return ((set) record).columnName + "=?";
                    }
                }).collect(Collectors.joining(","));
                builder.append(setSegment);
                if (param != null)
                {
                    builder.append(" where ");
                    ((InternalParam) param).renderSql(update.ckass, builder, paramValues);
                }
                else
                {
                    ;
                }
            }
            case insert ->
            {
                builder.append(insertInto.toString());
                record WrapperData(String columnName, String valueSegment) {}
                WrapperData[] array = insert.stream().map(record -> {
                    Insert insert = (Insert) record;
                    if (insert.value instanceof Model m)
                    {
                        ModelResult result = m.getResult();
                        paramValues.addAll(result.paramValues);
                        return new WrapperData(insert.columnName, result.sql);
                    }
                    else if (insert.value instanceof Sequence sequence)
                    {
                        return new WrapperData(insert.columnName, sequence.value() + ".NEXTVAL");
                    }
                    else
                    {
                        paramValues.add(insert.value);
                        return new WrapperData(insert.columnName, "?");
                    }
                }).toArray(WrapperData[]::new);
                builder.append(" ( ").append(Arrays.stream(array).map(data -> data.columnName).collect(Collectors.joining(","))).append(") values ( ");
                builder.append(Arrays.stream(array).map(data -> data.valueSegment.equals("?") ? "?" : "(" + data.valueSegment + ")").collect(Collectors.joining(","))).append(")");
            }
        }
        return builder.toString();
    }

    public <T> void insert(T entity)
    {
        TableEntityInfo entityInfo = TableEntityInfo.parse(entity.getClass());
        setInsertInto(new InsertInto(entityInfo.getEntityClass()));
        for (TableEntityInfo.ColumnInfo columnInfo : entityInfo.getPropertyNameKeyMap().values())
        {
            insert.add(new Insert(columnInfo.columnName(), columnInfo.accessor().get(entity)));
        }
    }

    public <T> void update(T entity)
    {
        setUpdate(new Update(entity.getClass()));
        TableEntityInfo entityInfo = TableEntityInfo.parse(entity.getClass());
        for (TableEntityInfo.ColumnInfo columnInfo : entityInfo.getPropertyNameKeyMap().values())
        {
            set.add(new set(columnInfo.columnName(), columnInfo.accessor().get(entity)));
        }
        param = new SpecialPkEqParam(entity);
    }

    /**
     * 保存一个对象到数据库。会根据该对象的主键属性是否为空进行不同的行为。
     * 1、不存在主键的，则按照全量插入处理。
     * 2.1、存在主键，且主键属性有值，按照全量插入处理。
     * 2.2、存在主键，主键属性为空，主键有PkGenerator注解，则使用对应的生成器生成主键属性，赋值给入参对象后，按照全量插入处理。
     * 2.3、存在主键，主键属性为空，主键上有AutoIncrement主键，则除了主键属性外，所有的属性均插入数据库，并且返回数据库自动生成的主键值。
     * 2.4、存在主键，主键属性为空，主键上有Sequence主键，则除了主键属性外，所有的属性均插入数据库，并且返回数据库自动生成的主键值。
     * 2.5、抛出异常
     *
     * @param entity
     * @param <T>
     * @return
     */
    public <T> void save(T entity)
    {
        TableEntityInfo entityInfo = TableEntityInfo.parse(entity.getClass());
        if (entityInfo.getPkInfo() == null)
        {
            insert(entity);
            pkReturnType = TableEntityInfo.PkReturnType.NO_RETURN_PK;
        }
        else
        {
            TableEntityInfo.ColumnInfo pkInfo = entityInfo.getPkInfo();
            Object                     pk     = pkInfo.accessor().get(entity);
            if (pk == null)
            {
                if (pkInfo.field().isAnnotationPresent(PkGenerator.class))
                {
                    Object next = entityInfo.getPkGenerator().next();
                    pkInfo.accessor().setObject(entity, next);
                    insert(entity);
                    pkReturnType = TableEntityInfo.PkReturnType.NO_RETURN_PK;
                }
                else if (pkInfo.field().isAnnotationPresent(AutoIncrement.class) || pkInfo.field().isAnnotationPresent(Sequence.class))
                {
                    setInsertInto(new InsertInto(entityInfo.getEntityClass()));
                    entityInfo.getPropertyNameKeyMap().values().stream()//
                              .filter(columnInfo -> columnInfo.field() != pkInfo.field())//
                              .forEach(columnInfo -> insert.add(new Insert(columnInfo.columnName(), columnInfo.accessor().get(entity))));
                    if (pkInfo.field().isAnnotationPresent(Sequence.class))
                    {
                        insert.add(new Insert(pkInfo.columnName(), pkInfo.field().getAnnotation(Sequence.class)));
                    }
                    pkReturnType = entityInfo.getPkReturnType();
                }
                else
                {
                    throw new IllegalArgumentException(pkInfo.field() + "主键没有自动生成，也没有标记自增长或者序列注解，不能在空值情况下执行save操作");
                }
            }
            else
            {
                update(entity);
                pkReturnType = TableEntityInfo.PkReturnType.NO_RETURN_PK;
            }
        }
    }
}







