package com.jfireframework.sql.mapper;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.smc.compiler.JavaStringCompiler;
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.baseutil.smc.model.FieldModel;
import com.jfireframework.baseutil.smc.model.MethodModel;
import com.jfireframework.sql.SqlSession;
import com.jfireframework.sql.analyse.template.Template;
import com.jfireframework.sql.analyse.token.SqlLexer;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.transfer.resultset.ResultMap;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;
import com.jfireframework.sql.transfer.resultset.impl.BeanTransfer;
import com.jfireframework.sql.util.Page;
import com.jfireframework.sql.util.TableEntityInfo;

public class MapperGenerator
{
	private static final AtomicInteger count = new AtomicInteger(0);
	
	public static Class<?> generate(Class<?> ckass, Map<String, TableEntityInfo> tableEntityInfos, JavaStringCompiler compiler)
	{
		Method[] methods = ckass.getMethods();
		for (Method method : methods)
		{
			if (method.isAnnotationPresent(Sql.class) == false)
			{
				throw new IllegalArgumentException("类:" + method.getDeclaringClass().getName() + "有方法没有打@Sql注解");
			}
		}
		ClassModel classModel = new ClassModel(ckass.getName() + "$Mapper$" + count.getAndIncrement(), Mapper.class, ckass);
		classModel.addImport(Mapper.class, Template.class, Map.class, HashMap.class, String.class, List.class, BeanTransfer.class, SqlSession.class);
		AtomicInteger fieldNameCount = new AtomicInteger(0);
		for (Method method : methods)
		{
			StringCache cache = new StringCache();
			cache.append("SqlSession session = sessionFactory.getCurrentSession();\r\n");
			cache.append("if(session==null){throw new NullPointerException(\"当前没有session\");\r\n}");
			cache.append("Map<String,Object> variables = cachedVariables.get();\r\n");
			cache.append("List<Object> params = cachedParams.get();\r\n");
			MethodModel methodModel = new MethodModel(method);
			Sql annotation = method.getAnnotation(Sql.class);
			String formatSql = generateSqlAndTemplateField(tableEntityInfos, classModel, fieldNameCount, method, cache, annotation);
			if (formatSql.startsWith("SELECT"))
			{
				String transferFieldName = "transfer_" + (fieldNameCount.getAndIncrement());
				if (List.class.isAssignableFrom(method.getReturnType()))
				{
					Class<?> componentClass = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
					classModel.addImport(componentClass);
					addResultSetTransferField(classModel, method, transferFieldName, componentClass);
					cache.append("List result = session.queryList(").append(transferFieldName).append(",sql,params);\r\n");
				}
				else
				{
					addResultSetTransferField(classModel, method, transferFieldName, method.getReturnType());
					cache.append(method.getReturnType().getName()).append(" result = session.query(").append(transferFieldName).append(",sql,params);\r\n");
				}
			}
			else
			{
				cache.append("int result = session.update(sql,params);\r\n");
			}
			cache.append("params.clear();\r\n");
			cache.append("variables.clear();\r\n");
			cache.append("return result;\r\n");
			methodModel.setBody(cache.toString());
			classModel.putMethodModel(methodModel);
		}
		Thread.currentThread().getContextClassLoader();
		try
		{
			Class<?> compile = compiler.compile(classModel);
			return compile;
		}
		catch (Exception e)
		{
			throw new JustThrowException(e);
		}
	}
	
	/**
	 * 生成ResultSetTransferField字段，并且添加到ClassModel中。
	 * 
	 * @param classModel
	 * @param method
	 * @param transferFieldName
	 * @param itemType 返回参数的类型。如果方法返回是List，则取其泛型参数的类型
	 */
	private static void addResultSetTransferField(ClassModel classModel, Method method, String transferFieldName, Class<?> itemType)
	{
		Class<? extends ResultSetTransfer> resultSetTransferClass = method.isAnnotationPresent(ResultMap.class) ? method.getAnnotation(ResultMap.class).value() : BeanTransfer.class;
		if (resultSetTransferClass != BeanTransfer.class)
		{
			classModel.addImport(resultSetTransferClass);
		}
		FieldModel transferField = new FieldModel(transferFieldName, ResultSetTransfer.class, "new BeanTransfer().initialize(" + itemType.getName() + ".class)");
		classModel.addField(transferField);
	}
	
	/**
	 * 生成并添加模板字段，并且生成解析格式化Sql的代码。最终返回格式化的sql
	 * 
	 * @param tableEntityInfos
	 * @param classModel
	 * @param fieldNameCount
	 * @param method
	 * @param cache
	 * @param annotation
	 * @return
	 */
	private static String generateSqlAndTemplateField(Map<String, TableEntityInfo> tableEntityInfos, ClassModel classModel, AtomicInteger fieldNameCount, Method method, StringCache cache, Sql annotation)
	{
		String formatSql = SqlLexer.parse(annotation.sql()).transfer(tableEntityInfos).format();
		String templateFieldName = "template_" + (fieldNameCount.getAndIncrement());
		FieldModel fieldModel = new FieldModel(templateFieldName, Template.class, "Template.parse(\"" + formatSql + "\")");
		classModel.addField(fieldModel);
		Class<?>[] parameterTypes = method.getParameterTypes();
		String paramNames = annotation.paramNames();
		if (parameterTypes.length != 0)
		{
			String[] names = paramNames.split(",");
			int index = 0;
			for (String each : names)
			{
				cache.append("variables.put(\"").append(each).append("\",$").append(index).append(");\r\n");
			}
		}
		cache.append("String sql =").append(templateFieldName).append(".render(variables,params);\r\n");
		if (parameterTypes.length != 0 && parameterTypes[parameterTypes.length - 1] == Page.class)
		{
			cache.append("params.add($").append(parameterTypes.length - 1).append(");\r\n");
		}
		return formatSql;
	}
}
