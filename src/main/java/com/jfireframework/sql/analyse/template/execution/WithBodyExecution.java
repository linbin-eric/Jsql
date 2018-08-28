package com.jfireframework.sql.analyse.template.execution;

public interface WithBodyExecution extends Execution
{
    void setBody(Execution... executions);

    boolean isBodyNotSet();
}
