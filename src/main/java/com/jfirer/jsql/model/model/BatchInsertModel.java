package com.jfirer.jsql.model.model;

import com.jfirer.baseutil.reflect.ValueAccessor;
import com.jfirer.jsql.annotation.AutoIncrement;
import com.jfirer.jsql.annotation.PkGenerator;
import com.jfirer.jsql.annotation.Sequence;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.BaseModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BatchInsertModel extends BaseModel
{
    private String sql;

    record Insert(String columnName, Object value)
    {
    }

    public BatchInsertModel(List<Object> list)
    {
        this.type = ModelType.batchInsert;
        StringBuilder   builder      = new StringBuilder();
        List<Insert>    inserts      = new ArrayList<>();
        Object          firstForMode = list.get(0);
        TableEntityInfo entityInfo   = TableEntityInfo.parse((Class<?>) firstForMode.getClass());
        builder.append("insert into ").append(entityInfo.getTableName()).append(" ");
        pkReturnType = TableEntityInfo.PkReturnType.NO_RETURN_PK;
        if (entityInfo.getPkInfo() == null)
        {
            for (TableEntityInfo.ColumnInfo columnInfo : entityInfo.getPropertyNameKeyMap().values())
            {
                inserts.add(new Insert(columnInfo.columnName(), columnInfo.accessor()));
            }
            processBatchValues(list, builder, inserts);
        }
        else
        {
            TableEntityInfo.ColumnInfo pkInfo = entityInfo.getPkInfo();
            Object                     pk     = pkInfo.accessor().get(firstForMode);
            if (pk != null)
            {
                for (TableEntityInfo.ColumnInfo columnInfo : entityInfo.getPropertyNameKeyMap().values())
                {
                    inserts.add(new Insert(columnInfo.columnName(), columnInfo.accessor()));
                }
                processBatchValues(list, builder, inserts);
            }
            else
            {
                if (pkInfo.field().isAnnotationPresent(PkGenerator.class))
                {
                    list.stream().forEach(v -> pkInfo.accessor().setObject(v, entityInfo.getPkGenerator().next()));
                    for (TableEntityInfo.ColumnInfo columnInfo : entityInfo.getPropertyNameKeyMap().values())
                    {
                        inserts.add(new Insert(columnInfo.columnName(), columnInfo.accessor()));
                    }
                    processBatchValues(list, builder, inserts);
                }
                else if (pkInfo.field().isAnnotationPresent(AutoIncrement.class) || pkInfo.field().isAnnotationPresent(Sequence.class))
                {
                    entityInfo.getPropertyNameKeyMap().values().stream()//
                              .filter(columnInfo -> columnInfo.field() != pkInfo.field())//
                              .forEach(columnInfo -> inserts.add(new Insert(columnInfo.columnName(), columnInfo.accessor())));
                    if (pkInfo.field().isAnnotationPresent(Sequence.class))
                    {
                        inserts.add(new Insert(pkInfo.columnName(), pkInfo.field().getAnnotation(Sequence.class)));
                        builder.append(" ( ").append(inserts.stream().map(data -> data.columnName()).collect(Collectors.joining(","))).append(") values  ");
                        for (Object obj : list)
                        {
                            builder.append("(");
                            int last = inserts.size() - 1;
                            for (int i = 0; i < inserts.size(); i++)
                            {
                                if (i != last)
                                {
                                    builder.append("?,");
                                    paramValues.add(((ValueAccessor) inserts.get(i).value()).get(obj));
                                }
                                else
                                {
                                    builder.append(((Sequence) inserts.get(i).value()).value()).append(".NEXTVAL),");
                                }
                            }
                        }
                        builder.setLength(builder.length() - 1);
                    }
                    else
                    {
                        processBatchValues(list, builder, inserts);
                    }
                }
                else
                {
                    throw new IllegalArgumentException(pkInfo.field() + "主键没有自动生成，也没有标记自增长或者序列注解，不能在空值情况下执行batchInsert操作");
                }
            }
            sql = builder.toString();
        }
    }

    @Override
    public String getSql()
    {
        return sql;
    }

    private void processBatchValues(List<Object> list, StringBuilder builder, List<Insert> inserts)
    {
        builder.append(" ( ").append(inserts.stream().map(data -> data.columnName()).collect(Collectors.joining(","))).append(") values  ");
        for (Object obj : list)
        {
            builder.append("(");
            for (int i = 0; i < inserts.size(); i++)
            {
                builder.append("?,");
                paramValues.add(((ValueAccessor) inserts.get(i).value()).get(obj));
            }
            builder.setLength(builder.length() - 1);
            builder.append("),");
        }
        builder.setLength(builder.length() - 1);
    }
}
