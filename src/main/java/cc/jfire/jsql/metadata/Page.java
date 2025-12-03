package cc.jfire.jsql.metadata;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class Page<T>
{
    private int     total;
    private int     offset;
    private int     size;
    private List<T> result;
    /**
     * 是否需要查询总数
     */
    private boolean fetchSum = false;
}
