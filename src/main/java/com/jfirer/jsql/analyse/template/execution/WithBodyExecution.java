package com.jfirer.jsql.analyse.template.execution;

public interface WithBodyExecution extends Execution
{
    void setBody(Execution... executions);

    boolean isBodyNotSet();
}
