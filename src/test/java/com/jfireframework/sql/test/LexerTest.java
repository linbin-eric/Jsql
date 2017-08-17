package com.jfireframework.sql.test;

import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.parse.lexer.Lexer;
import com.jfireframework.sql.test.vo.User;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public class LexerTest
{
    @Test
    public void test_1()
    {
        String sql = "select * from user where name = $name";
        Assert.assertEquals("select * from user where name = $name", new Lexer(sql).toString());
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
        sql = "select User.name from User";
        Assert.assertEquals("select User.name from User", new Lexer(sql).toString());
        sql = "select user.name from user";
        Assert.assertEquals("select user.name from user", new Lexer(sql).toString());
        sql = "select user.name from user left join room where   user.id = room.id";
        Assert.assertEquals("select user.name from user left join room where user.id = room.id", new Lexer(sql).toString());
    }
    
    @Test
    public void test_2()
    {
        Set<Class<?>> set = new HashSet<Class<?>>();
        set.add(User.class);
        MetaContext metaContext = new MetaContext(set, new JdbcTypeDictionary.MysqlJdbcTypes());
        String sql = "select name from User";
        Assert.assertEquals("select name from user", new Lexer(sql).parseEntity(metaContext).toString());
    }
    
    @Test
    public void test_3()
    {
        Set<Class<?>> set = new HashSet<Class<?>>();
        set.add(User.class);
        MetaContext metaContext = new MetaContext(set, new JdbcTypeDictionary.MysqlJdbcTypes());
        String sql = "select name from User";
        Assert.assertEquals("select user.name2 from user", new Lexer(sql).parseEntity(metaContext).parseEntityAlias(metaContext).parseFieldName().toString());
        sql = "select u.name from User as u";
        Assert.assertEquals("select u.name2 from user as u", new Lexer(sql).parseEntity(metaContext).parseEntityAlias(metaContext).parseFieldName().toString());
    }
}
