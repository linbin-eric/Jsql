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
        sql = "select name from user where <if($name != null)>   name   = 'sasa' </if> and age =12";
        Assert.assertEquals("select name from user where <if($name != null)> name = 'sasa' </if> and age = 12", new Lexer(sql).toString());
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
        sql = "select * from User where name =  @com.jfireframework.context.test.function.validate.User.Constant";
        Assert.assertEquals("select * from User where name = @com.jfireframework.context.test.function.validate.User.Constant", new Lexer(sql).toString());
        sql = "select * from User where name =  $n.name()";
        Assert.assertEquals("select * from User where name = $n.name()", new Lexer(sql).toString());
        sql = "upadte User set name='x' <if($i>5)>where name ='kx'</if> ";
        Assert.assertEquals("upadte User set name = 'x' <if($i>5)> where name = 'kx' </if>", new Lexer(sql).toString());
    }
    
    @Test
    public void test_2()
    {
        Set<Class<?>> set = new HashSet<Class<?>>();
        set.add(User.class);
        MetaContext metaContext = new MetaContext(set, new JdbcTypeDictionary.StandandTypes());
        String sql = "select name from User";
        Assert.assertEquals("select name from user", new Lexer(sql).parseEntity(metaContext).toString());
    }
    
    @Test
    public void test_3()
    {
        Set<Class<?>> set = new HashSet<Class<?>>();
        set.add(User.class);
        MetaContext metaContext = new MetaContext(set, new JdbcTypeDictionary.StandandTypes());
        String sql = "select name,age from User";
        Assert.assertEquals("select user.name2 , user.age from user", new Lexer(sql).parse(metaContext).toString());
        sql = "select u.name,u.age from User as u";
        Assert.assertEquals("select u.name2 , u.age from user as u", new Lexer(sql).parse(metaContext).toString());
        sql = "select name from User where <if( $name == 'sasda')> name = $name </if>";
        Assert.assertEquals("select user.name2 from user where <if( $name == 'sasda')> user.name2 = $name </if>", new Lexer(sql).parse(metaContext).toString());
        sql = "select age from User as u where u.name = ?";
        Assert.assertEquals("select u.age from user as u where u.name2 = ?", new Lexer(sql).parse(metaContext).toString());
    }
}
