package com.jfirer.jsql.analyse.token;

public enum TokenType
{
    // 关键字
    KEYWORD, //
    // 数字
    NUMBER, //
    // 执行语句
    EXECUTION, //
    // 表达式
    EXPRESSION, //
    // 模板字符串
    TEMPLATE_CHARACTERS,
    // 自动集合
    AUTO_COLLECTION, //
    // 符号
    SYMBOL, //
    // 标识符
    IDENTIFIER, //
    // 文本
    TEXT, //
    // 表映射类
    TABLE_ENTITY,//
}
