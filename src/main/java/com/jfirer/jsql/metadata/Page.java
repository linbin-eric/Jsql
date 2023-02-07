package com.jfirer.jsql.metadata;

public class Page
{
    private int     total;
    private int     offset;
    private int     size;
    /**
     * 是否需要查询总数
     */
    private boolean fetchSum = false;

    public int getTotal()
    {
        return total;
    }

    public int getOffset()
    {
        return offset;
    }

    public int getSize()
    {
        return size;
    }

    public void setTotal(int total)
    {
        this.total = total;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public boolean isFetchSum()
    {
        return fetchSum;
    }

    public void setFetchSum(boolean fetchSum)
    {
        this.fetchSum = fetchSum;
    }
}
