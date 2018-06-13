package com.jfireframework.sql.mapper;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.compiler.JavaStringCompiler;
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.baseutil.smc.model.FieldModel;
import com.jfireframework.baseutil.smc.model.MethodModel;
import com.jfireframework.sql.analyse.template.Template;
import com.jfireframework.sql.analyse.token.SqlLexer;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.metadata.Page;
import com.jfireframework.sql.metadata.TableEntityInfo;
import com.jfireframework.sql.session.SqlSession;
import com.jfireframework.sql.transfer.resultset.ResultMap;
import com.jfireframework.sql.transfer.resultset.ResultSetTransfer;
import com.jfireframework.sql.transfer.resultset.impl.BeanTransfer;
import com.jfireframework.sql.transfer.resultset.impl.BooleanTransfer;
import com.jfireframework.sql.transfer.resultset.impl.DoubleTransfer;
import com.jfireframework.sql.transfer.resultset.impl.EnumNameTransfer;
import com.jfireframework.sql.transfer.resultset.impl.FloatTransfer;
import com.jfireframework.sql.transfer.resultset.impl.IntegerTransfer;
import com.jfireframework.sql.transfer.resultset.impl.LongTransfer;
import com.jfireframework.sql.transfer.resultset.impl.ShortTransfer;
import com.jfireframework.sql.transfer.resultset.impl.SqlDateTransfer;
import com.jfireframework.sql.transfer.resultset.impl.StringTransfer;
import com.jfireframework.sql.transfer.resultset.impl.TimeStampTransfer;
import com.jfireframework.sql.transfer.resultset.impl.TimeTransfer;
import com.jfireframework.sql.transfer.resultset.impl.UtilDateTransfer;

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
		ClassModel classModel = new ClassModel(ckass.getSimpleName() + "$Mapper$" + count.getAndIncrement(), Mapper.class, ckass);
		classModel.addImport(Mapper.class);
		classModel.addImport(Template.class);
		classModel.addImport(Map.class);
		classModel.addImport(HashMap.class);
		classModel.addImport(String.class);
		classModel.addImport(BeanTransfer.class);
		classModel.addImport(SqlSession.class);
		classModel.addImport(List.class);
		AtomicInteger fieldNameCount = new AtomicInteger(0);
		for (Method method : methods)
		{
			StringCache cache = new StringCache();
			cache.append("SqlSession session = sessionFactory.getCurrentSession();\r\n");
			cache.append("if(session==null){throw new NullPointerException(\"当前没有session\");}");
			cache.append("Map<String,Object> variables = cachedVariables.get();\r\n");
			cache.append("List<Object> params = cachedParams.get();\r\n");
			MethodModel methodModel = new MethodModel(method, classModel);
			Sql annotation = method.getAnnotation(Sql.class);
			String formatSql = generateSqlAndTemplateField(tableEntityInfos, classModel, fieldNameCount, method, cache, annotation);
			if (formatSql.startsWith("SELECT"))
			{
				String transferFieldName = "transfer_" + (fieldNameCount.getAndIncrement());
				if (List.class.isAssignableFrom(method.getReturnType()))
				{
					Class<?> componentClass = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
					addResultSetTransferField(classModel, method, transferFieldName, componentClass);
					cache.append("List result = session.queryList(").append(transferFieldName).append(",sql,params);\r\n");
				}
				else
				{
					addResultSetTransferField(classModel, method, transferFieldName, method.getReturnType());
					String returnTypeName = method.getReturnType().isPrimitive() ? ReflectUtil.wrapPrimitive(method.getReturnType()).getName() : SmcHelper.getReferenceName(method.getReturnType(), classModel);
					cache.append(returnTypeName).append(" result = session.query(").append(transferFieldName).append(",sql,params);\r\n");
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
		Class<? extends ResultSetTransfer> ckass = null;
		if (method.isAnnotationPresent(ResultMap.class))
		{
			ckass = method.getAnnotation(ResultMap.class).value();
		}
		else if (itemType == String.class)
		{
			ckass = StringTransfer.class;
		}
		else if (Enum.class.isAssignableFrom(itemType))
		{
			ckass = EnumNameTransfer.class;
		}
		else if (itemType == Date.class)
		{
			ckass = SqlDateTransfer.class;
		}
		else if (itemType == java.util.Date.class)
		{
			ckass = UtilDateTransfer.class;
		}
		else if (itemType == Timestamp.class)
		{
			ckass = TimeStampTransfer.class;
		}
		else if (itemType == Time.class)
		{
			ckass = TimeTransfer.class;
		}
		else if (itemType.isPrimitive())
		{
			itemType = ReflectUtil.wrapPrimitive(itemType);
			if (itemType == Integer.class)
			{
				ckass = IntegerTransfer.class;
			}
			else if (itemType == Long.class)
			{
				ckass = LongTransfer.class;
			}
			else if (itemType == Short.class)
			{
				ckass = ShortTransfer.class;
			}
			else if (itemType == Float.class)
			{
				ckass = FloatTransfer.class;
			}
			else if (itemType == Double.class)
			{
				ckass = DoubleTransfer.class;
			}
			else if (itemType == Boolean.class)
			{
				ckass = BooleanTransfer.class;
			}
			else
			{
				throw new UnsupportedOperationException("不支持的单类型转换:" + itemType.getName());
			}
		}
		else
		{
			ckass = BeanTransfer.class;
		}
		classModel.addImport(ckass);
		FieldModel fieldModel = new FieldModel(transferFieldName, ResultSetTransfer.class, "new " + SmcHelper.getReferenceName(ckass, classModel) + "().initialize(" + SmcHelper.getReferenceName(itemType, classModel) + ".class)", classModel);
		classModel.addField(fieldModel);
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
		FieldModel fieldModel = new FieldModel(templateFieldName, Template.class, "Template.parse(\"" + formatSql + "\")", classModel);
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
				index++;
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
