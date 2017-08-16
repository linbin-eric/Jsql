package com.jfireframework.sql.parse.lexer.token;

public enum Expression implements TokenType
{
    // 参数变量
    VARIABLE, //
    // 字面值变量
    LITERALS, //
    IF, //
    // 带~的变量
    VARIABLE_WITH_VARIABLE,
}
