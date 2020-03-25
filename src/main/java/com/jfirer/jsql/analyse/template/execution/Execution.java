package com.jfirer.jsql.analyse.template.execution;


import java.util.List;
import java.util.Map;

public interface Execution
{

    /**
     * 语句是否已经执行
     *
     * @param variables
     * @param cache
     * @param params    TODO
     * @return
     */
    boolean execute(Map<String, Object> variables, StringBuilder cache, List<Object> params);

    /**
     * 在所有的执行语句都生成完毕后会执行一次校验
     */
    void check();
}
