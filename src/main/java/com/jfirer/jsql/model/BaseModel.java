package com.jfirer.jsql.model;

import com.jfirer.jsql.annotation.AutoIncrement;
import com.jfirer.jsql.annotation.PkGenerator;
import com.jfirer.jsql.annotation.Sequence;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.impl.SpecialPkEqParam;
import com.jfirer.jsql.model.support.LockMode;
import com.jfirer.jsql.model.support.SFunction;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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

    public record ModelResult(String sql, Class<?> returnType, List<Object> paramValues, TableEntityInfo.PkReturnType pkReturnType) {}

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

    record UpdateWithObject(Object value) {}

    record InsertIntoWithObject(Object value) {}

    record SaveWithObject(Object value) {}

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

    record SelectAs(SFunction<?, ?> fn, String asName, BaseModel model)
    {
        @Override
        public String toString()
        {
            return model.findColumnName(fn) + " as " + asName;
        }
    }

    record Select(SFunction<?, ?> fn, BaseModel model)
    {
        @Override
        public String toString()
        {
            return model.findColumnName(fn);
        }
    }

    record SelectWithName(String name)
    {
        @Override
        public String toString()
        {
            return name;
        }
    }

    record Count(SFunction<?, ?> fn, BaseModel model)
    {
        @Override
        public String toString()
        {
            return "count( " + model.findColumnName(fn) + " )";
        }
    }

    record set(String columnName, Object value) {}

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
    private List<Object>                 paramValues = new LinkedList<>();

    public BaseModel(Delete delete)
    {
        this.delete = delete;
        type = ModelType.delete;
    }

    public BaseModel(Update update)
    {
        this.update = update;
        type = ModelType.update;
    }

    public BaseModel(InsertInto insertInto)
    {
        this.insertInto = insertInto;
        type = ModelType.insert;
    }

    public BaseModel()
    {
        type = ModelType.query;
    }

    public BaseModel(InsertIntoWithObject insert)
    {
        this(new InsertInto(insert.value.getClass()));
        insert(insert.value);
    }

    private void insert(Object entity)
    {
        TableEntityInfo entityInfo = TableEntityInfo.parse(entity.getClass());
        for (TableEntityInfo.ColumnInfo columnInfo : entityInfo.getPropertyNameKeyMap().values())
        {
            this.insert.add(new Insert(columnInfo.columnName(), columnInfo.accessor().get(entity)));
        }
    }

    public BaseModel(UpdateWithObject update)
    {
        this(new Update(update.value.getClass()));
        updateByPk(update.value);
    }

    private void updateByPk(Object entity)
    {
        TableEntityInfo entityInfo = TableEntityInfo.parse(entity.getClass());
        for (TableEntityInfo.ColumnInfo columnInfo : entityInfo.getPropertyNameKeyMap().values())
        {
            set.add(new set(columnInfo.columnName(), columnInfo.accessor().get(entity)));
        }
        param = new SpecialPkEqParam(entity);
    }

    @Override
    public Model fromAs(Class<?> ckass, String asName)
    {
        from.add(new FromAs(ckass, asName));
        return this;
    }

    @Override
    public <T> Model select(SFunction<T, ?>... fns)
    {
        for (SFunction<T, ?> fn : fns)
        {
            select.add(new Select(fn, this));
        }
        return this;
    }

    @Override
    public Model selectAll(Class<?> ckass)
    {
        from.stream()//
            .filter(record -> FromAs.class.isInstance(record) ? ((FromAs) record).tableClass == ckass : false)//
            .findAny()//
            .ifPresent(record -> {
                FromAs fromAs = (FromAs) record;
                TableEntityInfo.parse(fromAs.tableClass).getPropertyNameKeyMap().values().forEach(columnInfo -> select.add(new SelectWithName(fromAs.asName() + "." + columnInfo.columnName())));
            });
        return this;
    }

    public String findColumnName(SFunction<?, ?> fn)
    {
        String implClass = fn.getImplClass();
        FromAs fromAs = from.stream()//
                            .filter(record -> FromAs.class.isInstance(record) ? ((FromAs) record).tableClass.getName().equals(implClass) : false) //
                            .map(record -> ((FromAs) record))//
                            .findAny().orElseThrow();
        return fromAs.asName + "." + TableEntityInfo.parse(fromAs.tableClass).getPropertyNameKeyMap().get(fn.resolveFieldName()).columnName();
    }

    @Override
    public <T> Model selectAs(SFunction<T, ?> fn, String asName)
    {
        select.add(new SelectAs(fn, asName, this));
        return this;
    }

    @Override
    public <T> Model selectCount(SFunction<T, ?> fn)
    {
        select.add(new Count(fn, this));
        return this;
    }

    @Override
    public Model selectCount()
    {
        select.add(new SelectWithName("count(*)"));
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
        String        columnName1 = findColumnName(fn1);
        String        columnName2 = findColumnName(fn2);
        StringBuilder builder     = new StringBuilder().append("on ").append(columnName1).append(" = ").append(columnName2);
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
                    selectAll(((FromAs) from.get(0)).tableClass);
                }
                String segment = select.stream().map(select -> select.toString()).collect(Collectors.joining(","));
                builder.append(segment);
                builder.append(" from ");
                String fromSegment = from.stream().map(fromAs -> fromAs.toString()).collect(Collectors.joining(" "));
                builder.append(fromSegment);
                if (param != null)
                {
                    builder.append(" where ");
                    ((InternalParam) param).renderSql(this, builder, paramValues);
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

    /**
     * 保存一个对象到数据库。会根据该对象的主键属性是否为空进行不同的行为。
     * 1、不存在主键的，则按照全量插入处理。
     * 2.1、存在主键，且主键属性有值，按照全量插入处理。
     * 2.2、存在主键，主键属性为空，主键有PkGenerator注解，则使用对应的生成器生成主键属性，赋值给入参对象后，按照全量插入处理。
     * 2.3、存在主键，主键属性为空，主键上有AutoIncrement主键，则除了主键属性外，所有的属性均插入数据库，并且返回数据库自动生成的主键值。
     * 2.4、存在主键，主键属性为空，主键上有Sequence主键，则除了主键属性外，所有的属性均插入数据库，并且返回数据库自动生成的主键值。
     * 2.5、抛出异常
     *
     * @return
     */
    public BaseModel(SaveWithObject save)
    {
        TableEntityInfo entityInfo = TableEntityInfo.parse(save.value.getClass());
        if (entityInfo.getPkInfo() == null)
        {
            type = ModelType.insert;
            insertInto = new InsertInto(entityInfo.getEntityClass());
            insert(save.value);
            pkReturnType = TableEntityInfo.PkReturnType.NO_RETURN_PK;
        }
        else
        {
            TableEntityInfo.ColumnInfo pkInfo = entityInfo.getPkInfo();
            Object                     pk     = pkInfo.accessor().get(save.value);
            if (pk == null)
            {
                type = ModelType.insert;
                insertInto = new InsertInto(entityInfo.getEntityClass());
                if (pkInfo.field().isAnnotationPresent(PkGenerator.class))
                {
                    Object next = entityInfo.getPkGenerator().next();
                    pkInfo.accessor().setObject(save.value, next);
                    insert(save.value);
                    pkReturnType = TableEntityInfo.PkReturnType.NO_RETURN_PK;
                }
                else if (pkInfo.field().isAnnotationPresent(AutoIncrement.class) || pkInfo.field().isAnnotationPresent(Sequence.class))
                {
                    entityInfo.getPropertyNameKeyMap().values().stream()//
                              .filter(columnInfo -> columnInfo.field() != pkInfo.field())//
                              .forEach(columnInfo -> insert.add(new Insert(columnInfo.columnName(), columnInfo.accessor().get(save.value))));
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
                type = ModelType.update;
                update = new Update(save.value.getClass());
                updateByPk(save.value);
                pkReturnType = TableEntityInfo.PkReturnType.NO_RETURN_PK;
            }
        }
    }
}







