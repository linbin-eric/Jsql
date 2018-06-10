package com.jfireframework.sql.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.TableDef;
import com.jfireframework.sql.util.TableEntityInfo;

public abstract class Model<T>
{
    protected Class<?>     entityClass;
    protected List<String> whereProperties;
    protected boolean      frozen = false;
    protected String       generateSql;
    
    /**
     * 返回一个属性名和字段名的映射
     * 
     * @return
     */
    protected Map<String, String> getColumnNameMap()
    {
        return TableEntityInfo.parse(entityClass).getPropertyNameToColumnNameMap();
    }
    
    protected void check()
    {
        if (frozen)
        {
            throw new IllegalStateException("已经是冻结状态，不允许改动");
        }
    }
    
    protected void generateBefore()
    {
        if (frozen != false)
        {
            throw new IllegalStateException("已经生成过，不能该次修改");
        }
        frozen = true;
    }
    
    public abstract T generate();
    
    /**
     * @param cache
     * @param columnNameMap
     */
    protected void setWhereColumns(StringCache cache, Map<String, String> columnNameMap)
    {
        if (whereProperties != null)
        {
            cache.append("where ");
            for (String each : whereProperties)
            {
                String columnName = columnNameMap.get(each);
                cache.append(columnName).append("=? and ");
            }
            cache.deleteEnds(4);
        }
    }
    
    public String getSql()
    {
        return generateSql;
    }
    
    @SuppressWarnings("unchecked")
    public T from(Class<?> entityClass)
    {
        if (entityClass.isAnnotationPresent(TableDef.class) == false)
        {
            throw new IllegalArgumentException("没有实体类注解");
        }
        check();
        this.entityClass = entityClass;
        return (T) this;
    }
    
    @SuppressWarnings("unchecked")
    public T where(String propertyName)
    {
        check();
        if (whereProperties == null)
        {
            whereProperties = new LinkedList<String>();
        }
        whereProperties.add(propertyName);
        return (T) this;
    }
    
    public Class<?> getEntityClass()
    {
        return entityClass;
    }
    
    public static final DeleteModel delete()
    {
        return new DeleteModel();
    }
    
    public static final QueryModel query()
    {
        return new QueryModel();
    }
    
    public static final UpdateModel update()
    {
        return new UpdateModel();
    }
    
    public static final InsertModel insert()
    {
        return new InsertModel();
    }
    
    public static final CountModel count()
    {
        return new CountModel();
    }
}
