package com.jfirer.jsql.model;

import com.jfirer.jsql.annotation.AutoIncrement;
import com.jfirer.jsql.annotation.PkGenerator;
import com.jfirer.jsql.annotation.Sequence;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.impl.SpecialPkEqParam;
import com.jfirer.jsql.model.support.LockMode;
import com.jfirer.jsql.model.support.SFunction;

import java.lang.reflect.Field;
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
        insert
    }

    public record ModelResult(String sql, Class<?> returnType, List<Object> paramValues, TableEntityInfo.PkReturnType pkReturnType) {}

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
                    return "left join " + segment + " ";
                case "right join":
                    return "right join " + segment + " ";
                case "inner join":
                    return "inner join " + segment + " ";
                case "full join":
                    return "full join " + segment + " ";
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
            builder.append("on ");
            ((InternalParam) param).renderSql(model, builder, params);
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

    class Select
    {
        final String content;
        String          className;
        String          fieldName;
        //两种不同的模式
        SFunction<?, ?> fn;
        String          function;
        String          asName;
        BaseModel       model;

        public Select(SFunction<?, ?> fn, String function, String asName, BaseModel model)
        {
            this.fn = fn;
            this.function = function;
            this.asName = asName;
            this.model = model;
            this.content = null;
        }

        public Select(String content)
        {
            this.content = content;
        }

        public Select(String content, String className, String fieldName)
        {
            this.content = content;
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

    List<Record>          from    = new LinkedList<>();
    List<Select>          select  = new LinkedList<>();
    List<SFunction<?, ?>> exclude = new LinkedList<>();
    List<Record>          set     = new LinkedList<>();
    List<Record>          orderBy = new LinkedList<>();
    List<Record>          groupBy = new LinkedList<>();
    List<Record>          insert  = new LinkedList<>();
    private final ModelType                    type;
    private       Update                       update;
    private       Delete                       delete;
    private       InsertInto                   insertInto;
    private       Param                        param;
    private       Class<?>                     returnType;
    private       Page                         page;
    private       TableEntityInfo.PkReturnType pkReturnType;
    private       LockMode                     lockMode;
    private final List<Object>                 paramValues = new LinkedList<>();

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
        param = new SpecialPkEqParam(entity, entityInfo.getPkInfo());
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
                                    .filter(record -> ((Table) record).tableClass().getName().equals(implClass))//
                                    .map(record -> ((Table) record))//
                                    .findAny().orElseThrow();
                return tableAs.asName + "." + TableEntityInfo.parse(tableAs.tableClass).getPropertyNameKeyMap().get(fn.resolveFieldName()).columnName();
            }
            case delete ->
            {
                TableEntityInfo tableEntityInfo = TableEntityInfo.parse(delete.ckass);
                return tableEntityInfo.getTableName() + "." + tableEntityInfo.getPropertyNameKeyMap().get(fn.resolveFieldName()).columnName();
            }
            case update ->
            {
                TableEntityInfo tableEntityInfo = TableEntityInfo.parse(update.ckass);
                return tableEntityInfo.getTableName() + "." + tableEntityInfo.getPropertyNameKeyMap().get(fn.resolveFieldName()).columnName();
            }
            case insert ->
            {
                TableEntityInfo tableEntityInfo = TableEntityInfo.parse(insertInto.ckass);
                return tableEntityInfo.getTableName() + "." + tableEntityInfo.getPropertyNameKeyMap().get(fn.resolveFieldName()).columnName();
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

    private Class<?> getReturnType()
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
            case delete ->
            {
                builder.append(delete.toString());
                if (param != null)
                {
                    builder.append(" where ");
                    ((InternalParam) param).renderSql(this, builder, paramValues);
                }
                else
                {
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
                    ((InternalParam) param).renderSql(this, builder, paramValues);
                }
                else
                {
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







