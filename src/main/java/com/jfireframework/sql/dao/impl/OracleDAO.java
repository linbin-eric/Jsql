package com.jfireframework.sql.dao.impl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.annotation.pkstrategy.GenerateStringPk;
import com.jfireframework.sql.annotation.pkstrategy.Sequence;
import com.jfireframework.sql.annotation.pkstrategy.GenerateStringPk.StringGenerator;
import com.jfireframework.sql.dbstructure.column.MapColumn;
import com.jfireframework.sql.session.ExecSqlTemplate;
import sun.misc.Unsafe;

public class OracleDAO<T> extends BaseDAO<T>
{
	private static final Unsafe		unsafe	= ReflectUtil.getUnsafe();
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
		if (Number.class.isAssignableFrom(field.getType()) && field.isAnnotationPresent(Sequence.class))
		{
			generatePkStrategy = GeneratePkStrategy.GENERATE_BY_DATABASE;
			StringCache cache = new StringCache();
			cache.append("INSERT INTO ").append(tableName).append('(').append(pkColumn.getColName()).appendComma();
			for (MapColumn column : valueColumns)
			{
				cache.append(column.getColName()).appendComma();
			}
			cache.deleteLast().append(") VALUES(");
			String sequenceName = field.getAnnotation(Sequence.class).value();
			cache.append(sequenceName).append(".NEXTVAL,");
			List<FieldValueFetcher> generateByDatebaseFetchers = new ArrayList<BaseDAO<T>.FieldValueFetcher>();
			for (int i = 0; i < valueColumns.length; i++)
			{
				cache.append("?").appendComma();
				generateByDatebaseFetchers.add(new FieldValueFetcher(valueColumns[i].getField()));
			}
			cache.deleteLast().append(")");
			generateByDatebaseSql = cache.toString();
			this.generateByDatebaseFetchers = generateByDatebaseFetchers.toArray(new BaseDAO.FieldValueFetcher[generateByDatebaseFetchers.size()]);
		}
		else if (field.getType() == String.class && field.isAnnotationPresent(GenerateStringPk.class))
		{
			generatePkStrategy = GeneratePkStrategy.GENERATE_BY_APPLICATION;
			StringCache cache = new StringCache();
			List<FieldValueFetcher> generateByAppFetchers = new ArrayList<FieldValueFetcher>();
			cache.append(pkColumn.getFieldName()).appendComma();
			for (MapColumn column : valueColumns)
			{
				cache.append(column.getFieldName()).appendComma();
			}
			cache.deleteLast();
			generateByAppSql = cache.toString();
			try
			{
				final StringGenerator stringGenerator = field.getAnnotation(GenerateStringPk.class).value().newInstance();
				generateByAppFetchers.add(new FieldValueFetcher(pkColumn.getField()) {
					Object fieldValue(Object entity)
					{
						try
						{
							String next = stringGenerator.next();
							field.set(entity, next);
							return next;
						}
						catch (Exception e)
						{
							throw new JustThrowException(e);
						}
					}
				});
			}
			catch (Exception e)
			{
				throw new JustThrowException(e);
			}
			for (MapColumn column : valueColumns)
			{
				generateByAppFetchers.add(new FieldValueFetcher(column.getField()));
			}
			this.generateByAppFetchers = generateByAppFetchers.toArray(new BaseDAO.FieldValueFetcher[generateByAppFetchers.size()]);
		}
	}
	
	@Override
	protected void autoGeneratePkInsert(Object entity, Connection connection)
	{
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
	
	enum GeneratePkStrategy
	{
		/**
		 * 主键由程序自动生成
		 */
		GENERATE_BY_APPLICATION, //
		/**
		 * 主键由数据库生成
		 */
		GENERATE_BY_DATABASE
	}
}
