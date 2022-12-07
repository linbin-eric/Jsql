package com.jfirer.jsql.model;

import com.jfirer.jsql.annotation.TableDef;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.transfer.impl.BeanTransfer;

import java.util.LinkedList;
import java.util.List;

public class QueryModel extends Model
{
    private List<String> selectProperties;
    private          List<OrderByEntry> orderByProperties;
    private volatile BeanTransfer       beanTransfer;
    private          Page               page;

    class OrderByEntry
    {
        final String orderPropertyName;
        final boolean desc;

        OrderByEntry(String orderPropertyName, boolean desc)
        {
            this.orderPropertyName = orderPropertyName;
            this.desc = desc;
        }

    }

    public QueryModel select(String propertyName)
    {
        if ( selectProperties == null )
        {
            selectProperties = new LinkedList<String>();
        }
        selectProperties.add(propertyName);
        return this;
    }

    public QueryModel orderBy(String orderPropertyName, boolean desc)
    {
        if ( orderByProperties == null )
        {
            orderByProperties = new LinkedList<OrderByEntry>();
        }
        orderByProperties.add(new OrderByEntry(orderPropertyName, desc));
        return this;
    }

    @Override
    public BeanTransfer getBeanTransfer()
    {
        if ( selectProperties == null )
        {
            selectProperties = new LinkedList<String>();
            for (TableEntityInfo.ColumnInfo columnInfo : TableEntityInfo.parse(entityClass).getPropertyNameKeyMap().values())
            {
                selectProperties.add(columnInfo.getPropertyName());
            }
        }
        if ( beanTransfer == null )
        {
            synchronized (this)
            {
                if ( beanTransfer == null )
                {
                    beanTransfer = (BeanTransfer) new BeanTransfer().awareType(entityClass);
                }
            }
        }
        return beanTransfer;
    }

    @Override
    public String _getSql()
    {
        StringBuilder cache = new StringBuilder();
        cache.append("select ");
        if ( selectProperties == null )
        {
            selectProperties = new LinkedList<String>();
            for (TableEntityInfo.ColumnInfo columnInfo : TableEntityInfo.parse(entityClass).getPropertyNameKeyMap().values())
            {
                selectProperties.add(columnInfo.getPropertyName());
            }
        }
        TableEntityInfo tableEntityInfo = TableEntityInfo.parse(entityClass);
        for (String each : selectProperties)
        {
            String columnName = tableEntityInfo.getPropertyNameKeyMap().get(each).getColumnName();
            cache.append(columnName).append(',');
        }
        cache.setLength(cache.length()-1);
        cache.append(' ');
        cache.append("from ");
        String tableName = entityClass.getAnnotation(TableDef.class).name();
        cache.append(tableName);
        setWhereColumns(cache);
        if ( orderByProperties != null )
        {
            cache.append(" order by ");
            for (OrderByEntry each : orderByProperties)
            {
                String columnName = tableEntityInfo.getPropertyNameKeyMap().get(each.orderPropertyName).getColumnName();
                cache.append(columnName).append(each.desc ? " desc" : " asc");
                cache.append(',');
            }
            cache.setLength(cache.length()-1);
        }
        return cache.toString();
    }

    @Override
    public List<Object> getParams()
    {
        List<Object> params = new LinkedList<Object>();
        if ( whereEntries != null )
        {
            for (WhereEntry whereEntry : whereEntries)
            {
                params.add(whereEntry.value);
            }
        }
        if ( page != null )
        {
            params.add(page);
        }
        return params;
    }

    @Override
    public Model setPage(Page page)
    {
        this.page = page;
        return this;
    }
}
