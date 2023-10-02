package com.jfirer.jsql.model.model;

import com.jfirer.jsql.annotation.AutoIncrement;
import com.jfirer.jsql.annotation.PkGenerator;
import com.jfirer.jsql.annotation.Sequence;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BatchInsertModel implements Model
{
    private final   String       sql;
    protected final List<Object> paramValues = new ArrayList<>();

    public BatchInsertModel(List<Object> list)
    {
        StringBuilder   builder      = new StringBuilder();
        Object          firstForMode = list.get(0);
        TableEntityInfo entityInfo   = TableEntityInfo.parse((Class<?>) firstForMode.getClass());
        builder.append("insert into ").append(entityInfo.getTableName()).append(" ");
        if (entityInfo.getPkInfo() == null)
        {
            processBatchValues(list, builder, entityInfo.getAllColumnInfos());
        }
        else
        {
            TableEntityInfo.ColumnInfo pkInfo = entityInfo.getPkInfo();
            Object                     pk     = pkInfo.accessor().get(firstForMode);
            if (pk != null)
            {
                processBatchValues(list, builder, entityInfo.getAllColumnInfos());
            }
            else
            {
                if (pkInfo.field().isAnnotationPresent(PkGenerator.class))
                {
                    list.stream().forEach(v -> pkInfo.accessor().setObject(v, entityInfo.getPkGenerator().next()));
                    processBatchValues(list, builder, entityInfo.getAllColumnInfos());
                }
                else if (pkInfo.field().isAnnotationPresent(AutoIncrement.class) || pkInfo.field().isAnnotationPresent(Sequence.class))
                {
                    if (pkInfo.field().isAnnotationPresent(Sequence.class))
                    {
                        builder.append(" ( ");
                        TableEntityInfo.ColumnInfo[] allColumnInfosExcludePk = entityInfo.getAllColumnInfosExcludePk();
                        for (TableEntityInfo.ColumnInfo columnInfo : allColumnInfosExcludePk)
                        {
                            builder.append(columnInfo.columnName()).append(",");
                        }
                        builder.append(pkInfo.columnName()).append(") values ");
                        StringBuilder segment = new StringBuilder("(");
                        for (TableEntityInfo.ColumnInfo columnInfo : allColumnInfosExcludePk)
                        {
                            segment.append(",");
                        }
                        segment.append(pkInfo.field().getAnnotation(Sequence.class).value()).append(".NEXTVAL),");
                        String _segment = segment.toString();
                        for (Object obj : list)
                        {
                            builder.append(_segment);
                            for (TableEntityInfo.ColumnInfo each : allColumnInfosExcludePk)
                            {
                                paramValues.add(each.accessor().get(obj));
                            }
                        }
                        builder.setLength(builder.length() - 1);
                    }
                    else
                    {
                        processBatchValues(list, builder, entityInfo.getAllColumnInfosExcludePk());
                    }
                }
                else
                {
                    throw new IllegalArgumentException(pkInfo.field() + "主键没有自动生成，也没有标记自增长或者序列注解，不能在空值情况下执行batchInsert操作");
                }
            }
        }
        sql = builder.toString();
    }

    @Override
    public ModelResult getResult()
    {
        return new ModelResult(sql, paramValues);
    }

    private void processBatchValues(List<Object> list, StringBuilder builder, TableEntityInfo.ColumnInfo[] inserts)
    {
        builder.append(" ( ").append(Arrays.stream(inserts).map(data -> data.columnName()).collect(Collectors.joining(","))).append(") values  ");
        String segment = "(" + Arrays.stream(inserts).map(insert -> "?").collect(Collectors.joining(",")).toString() + "),";
        for (Object obj : list)
        {
            builder.append(segment);
            for (TableEntityInfo.ColumnInfo insert : inserts)
            {
                paramValues.add(insert.accessor().get(obj));
            }
        }
        builder.setLength(builder.length() - 1);
    }
}
