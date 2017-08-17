package com.jfireframework.sql.test;

import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.sql.parse.lexer.Lexer;

public class LexerTest
{
    @Test
    public void test_1()
    {
        String sql = "select * from user";
        Assert.assertEquals("select * from user", new Lexer(sql).toString());
        sql = "select name from user where name ='sasad'";
        Assert.assertEquals("select name from user where name = 'sasad'", new Lexer(sql).toString());
        sql = "select name as n from user where age>14";
        Assert.assertEquals("select name as n from user where age > 14", new Lexer(sql).toString());
        sql = "select name,age from user where id!=1 and sex=1";
        Assert.assertEquals("select name , age from user where id != 1 and sex = 1", new Lexer(sql).toString());
        sql = "select name ,  age from user where (name='sada' and sex=1) or age >=13";
        Assert.assertEquals("select name , age from user where ( name = 'sada' and sex = 1 ) or age >= 13", new Lexer(sql).toString());
        sql = "select {myname}  from  user";
        Assert.assertEquals("select {myname} from user", new Lexer(sql).toString());
        sql = "select name from user where <if($name != null)> name = 'sasa' </if>";
        Assert.assertEquals("select name from user where <if($name != null)> name = 'sasa' </if>", new Lexer(sql).toString());
        sql = "select * from user where id in $~ids";
        Assert.assertEquals("select * from user where id in $~ids", new Lexer(sql).toString());
        sql = "select * from user where name = $%name%";
        Assert.assertEquals("select * from user where name = $%name%", new Lexer(sql).toString());
    }
}
