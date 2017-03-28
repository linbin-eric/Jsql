package com.jfireframework.sql.resultsettransfer;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import com.jfireframework.sql.resultsettransfer.field.MapField;

public class FixBeanTransfer<T> extends AbstractResultsetTransfer<T>
{
    private volatile MapField[] fields;
    
    public FixBeanTransfer(Class<T> type)
    {
        super(type);
    }
    
    @Override
    protected T valueOf(ResultSet resultSet, String sql) throws Exception
    {
        if (fields == null)
        {
            fields = buildFieldsFromMetadata(resultSet.getMetaData());
        }
        T entity = entityClass.newInstance();
        for (MapField each : fields)
        {
            each.setEntityValue(entity, resultSet);
        }
        return entity;
    }
    
    private MapField[] buildFieldsFromMetadata(ResultSetMetaData metaData) throws SQLException
    {
        int colCount = metaData.getColumnCount();
        List<MapField> resultFields = new LinkedList<MapField>();
        for (int i = 0; i < colCount; i++)
        {
            MapField mapField = mapFields.get(metaData.getColumnName(i + 1).toLowerCase());
            if (mapField != null)
            {
                resultFields.add(mapField);
            }
        }
        MapField[] fields = resultFields.toArray(new MapField[resultFields.size()]);
        return fields;
    }
}
