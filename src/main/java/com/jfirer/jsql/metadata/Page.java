package com.jfirer.jsql.metadata;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class Page
{
    private int          total;
    private int          offset;
    private int          size;
    private List<Object> result;
    /**
     * 是否需要查询总数
     */
    private boolean      fetchSum = false;
}
