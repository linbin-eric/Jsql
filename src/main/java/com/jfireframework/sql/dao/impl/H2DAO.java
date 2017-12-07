package com.jfireframework.sql.dao.impl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.sql.annotation.pkstrategy.AutoIncrement;
import com.jfireframework.sql.annotation.pkstrategy.GenerateStringPk;
import com.jfireframework.sql.annotation.pkstrategy.GenerateStringPk.StringGenerator;
import com.jfireframework.sql.dao.impl.MysqlDAO.GeneratePkStrategy;
import com.jfireframework.sql.dbstructure.column.MapColumn;
import com.jfireframework.sql.session.ExecSqlTemplate;

public class H2DAO<T> extends BaseDAO<T>
{
	protected GeneratePkStrategy	generatePkStrategy;
	private String					generateByAppSql;
	private FieldValueFetcher[]		generateByAppFetchers;
	private String					generateByDatebaseSql;
	private FieldValueFetcher[]		generateByDatebaseFetchers;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void setAutoGeneratePkInsertInfo()
	{
		Field field = pkColumn.getField();
		if (Number.class.isAssignableFrom(field.getType()) && field.isAnnotationPresent(AutoIncrement.class))
		{
			StringCache cache = new StringCache();
			cache.append("INSERT INTO ").append(tableName).append('(').append(pkColumn.getColName()).appendComma();
			List<FieldValueFetcher> generateByDatebaseFetchers = new ArrayList<FieldValueFetcher>();
			for (MapColumn column : valueColumns)
			{
				cache.append(column.getColName()).appendComma();
				generateByDatebaseFetchers.add(new FieldValueFetcher(column.getField()));
			}
			cache.deleteLast().append(") VALUES(NULL,");
			for (int i = 0; i < valueColumns.length; i++)
			{
				cache.append("?").appendComma();
			}
			cache.deleteLast().append(")");
			generateByDatebaseSql = cache.toString();
			this.generateByDatebaseFetchers = generateByDatebaseFetchers.toArray(new BaseDAO.FieldValueFetcher[generateByDatebaseFetchers.size()]);
			generatePkStrategy = GeneratePkStrategy.GENERATE_BY_DATABASE;
		}
		else if (field.getType() == String.class && field.isAnnotationPresent(GenerateStringPk.class))
		{
			StringCache cache = new StringCache();
			cache.append(pkColumn.getFieldName()).appendComma();
			for (MapColumn column : valueColumns)
			{
				cache.append(column.getFieldName()).appendComma();
			}
			cache.deleteLast();
			try
			{
				final StringGenerator stringGenerator = field.getAnnotation(GenerateStringPk.class).value().newInstance();
				List<FieldValueFetcher> generateByAppFetchers = new ArrayList<FieldValueFetcher>();
				generateByAppFetchers.add(new FieldValueFetcher(pkColumn.getField()) {
					Object fieldValue(Object entity)
					{
						String next = stringGenerator.next();
						try
						{
							field.set(entity, next);
							return next;
						}
						catch (Exception e)
						{
							throw new JustThrowException(e);
						}
					}
				});
				for (MapColumn each : valueColumns)
				{
					generateByAppFetchers.add(new FieldValueFetcher(each.getField()));
				}
				generateByAppSql = cache.toString();
				this.generateByAppFetchers = generateByAppFetchers.toArray(new BaseDAO.FieldValueFetcher[generateByAppFetchers.size()]);
			}
			catch (Exception e)
			{
				throw new JustThrowException(e);
			}
			generatePkStrategy = GeneratePkStrategy.GENERATE_BY_APPLICATION;
		}
	}
	
	@Override
	protected void autoGeneratePkInsert(Object entity, Connection connection)
	{
		Verify.notNull(generatePkStrategy, "generatePkStrategy为空时无法执行生成主键并插入。请检查{}", entityClass.getName());
		switch (generatePkStrategy)
		{
			case GENERATE_BY_APPLICATION:
				strategyOperation.insert(connection, generateByAppSql, parseParams(entity, generateByAppFetchers));
				break;
			case GENERATE_BY_DATABASE:
				Object pk = ExecSqlTemplate.databasePkGenerateInsert(dialect, pkType, pkName, sqlInterceptors, connection, generateByDatebaseSql, parseParams(entity, generateByDatebaseFetchers));
				unsafe.putObject(entity, pkColumnOffset, pk);
				break;
			default:
				break;
		}
	}
	
}
