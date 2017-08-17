package com.jfireframework.sql.parse.lexer.token;

public enum Expression implements TokenType
{
    // 参数变量
    VARIABLE, //
    // 字面值变量
    BRACE, //
    // 常量
    CONSTANT, //
    IF, //
    ENDIF, //
    // 带~的变量
    VARIABLE_WITH_TIDLE, //
}
