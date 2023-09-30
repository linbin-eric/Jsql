package com.jfirer.jsql.model.model;

import com.jfirer.jsql.annotation.AutoIncrement;
import com.jfirer.jsql.annotation.PkGenerator;
import com.jfirer.jsql.annotation.Sequence;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.BaseModel;

public class InsertEntityModel extends BaseModel
{
    private String sql;

    public InsertEntityModel(Object entity)
    {
        type = ModelType.insert;
        StringBuilder   builder    = new StringBuilder();
        TableEntityInfo entityInfo = TableEntityInfo.parse(entity.getClass());
        builder.append("insert into ").append(entityInfo.getTableName()).append(" (");
        if (entityInfo.getPkInfo() == null)
        {
            makeSql(entity, entityInfo, builder);
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
                    pkInfo.accessor().setObject(entity, entityInfo.getPkGenerator().next());
                    makeSql(entity, entityInfo, builder);
                    pkReturnType = TableEntityInfo.PkReturnType.NO_RETURN_PK;
                }
                else if (pkInfo.field().isAnnotationPresent(AutoIncrement.class) || pkInfo.field().isAnnotationPresent(Sequence.class))
                {
                    entityInfo.getPropertyNameKeyMap().values().stream()//
                              .filter(columnInfo -> columnInfo.field() != pkInfo.field())//
                              .forEach(columnInfo -> {
                                  builder.append(columnInfo.columnName()).append(",");
                                  paramValues.add(columnInfo.accessor().get(entity));
                              });
                    if (pkInfo.field().isAnnotationPresent(Sequence.class))
                    {
                        builder.append(pkInfo.columnName()).append(") values (");
                        int count = paramValues.size();
                        for (int i = 0; i < count; i++)
                        {
                            builder.append("?,");
                        }
                        builder.append(pkInfo.field().getAnnotation(Sequence.class).value()).append(".NEXTVAL)");
                    }
                    else
                    {
                        builder.setLength(builder.length() - 1);
                        builder.append(") values (");
                        int count = paramValues.size();
                        for (int i = 0; i < count; i++)
                        {
                            builder.append("?,");
                        }
                        builder.setLength(builder.length() - 1);
                        builder.append(")");
                    }
                    pkReturnType = entityInfo.getPkReturnType();
                }
                else
                {
                    throw new IllegalArgumentException(pkInfo.field() + "主键没有自动生成，也没有标记自增长或者序列注解，不能在空值情况下执行insert操作");
                }
            }
            else
            {
                makeSql(entity, entityInfo, builder);
                pkReturnType = TableEntityInfo.PkReturnType.NO_RETURN_PK;
            }
        }
        sql = builder.toString();
    }

    private void makeSql(Object entity, TableEntityInfo entityInfo, StringBuilder builder)
    {
        for (TableEntityInfo.ColumnInfo columnInfo : entityInfo.getPropertyNameKeyMap().values())
        {
            builder.append(columnInfo.columnName()).append(",");
            paramValues.add(columnInfo.accessor().get(entity));
        }
        builder.setLength(builder.length() - 1);
        builder.append(") values (");
        int count = paramValues.size();
        for (int i = 0; i < count; i++)
        {
            builder.append("?,");
        }
        builder.setLength(builder.length() - 1);
        builder.append(")");
    }

    @Override
    public String getSql()
    {
        return sql;
    }
}
