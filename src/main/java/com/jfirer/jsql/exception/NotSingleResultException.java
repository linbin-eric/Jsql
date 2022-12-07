package com.jfirer.jsql.exception;

public class NotSingleResultException extends RuntimeException
{
    /**
     *
     */
    private static final long serialVersionUID = 5514283876304522093L;

    public NotSingleResultException()
    {
        super("查询实体，存在超过1行的记录");
    }
}
