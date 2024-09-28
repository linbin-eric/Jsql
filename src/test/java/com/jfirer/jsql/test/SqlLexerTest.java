package com.jfirer.jsql.test;

import com.jfirer.jsql.analyse.token.SqlLexer;
import com.jfirer.jsql.test.vo.User;
import org.junit.Assert;
import org.junit.Test;

public class SqlLexerTest
{
    @Test
    public void test_1()
    {
        String sql = "select * from user where name = ${name}";
        Assert.assertEquals("select * from user where name = ${name}", SqlLexer.parse(sql, User.class));
        //
        sql = "select sum(age) from user";
        Assert.assertEquals("select sum(age) from user", SqlLexer.parse(sql, User.class));
        //
        sql = "select name from user where name ='sasad'";
        Assert.assertEquals(sql, SqlLexer.parse(sql, User.class));
        //
        sql = "select name as n from user where age>14";
        Assert.assertEquals(sql, SqlLexer.parse(sql, User.class));
        //
        sql = """
                select name,age from user where id!=1 
                and sex=1""";
        Assert.assertEquals(sql, SqlLexer.parse(sql, User.class));
        //
        sql = "select name , age from user where (name='sada' and sex=1) or age >=13";
        Assert.assertEquals(sql, SqlLexer.parse(sql, User.class));
        //
        sql = "select #{myname} from user";
        Assert.assertEquals(sql, SqlLexer.parse(sql, User.class));
        //
        sql = "select name from user where <%if($name != null){%>   name   = 'sasa' <%}%> and age =12";
        Assert.assertEquals("select name from user where <%if($name != null){%> name = 'sasa' <%}%> and age =12", SqlLexer.parse(sql, User.class));
        //
        sql = "select * from user where id in ~{ids}";
        Assert.assertEquals(sql, SqlLexer.parse(sql, User.class));
        //
        sql = "select User.name from User";
        Assert.assertEquals("select user.name2 from user", SqlLexer.parse(sql, User.class));
        //
        sql = "select user.name from user";
        Assert.assertEquals(sql, SqlLexer.parse(sql, User.class));
        //
        sql = "select user.name from user left join room WHERE   user.id = room.id";
        Assert.assertEquals("select user.name from user left join room WHERE user.id = room.id", SqlLexer.parse(sql, User.class));
        //
        sql = "select * from User WHERE name =  ${n.name()}";
        Assert.assertEquals("select * from user WHERE name2 = ${n.name()}", SqlLexer.parse(sql, User.class));
        //
        sql = "update User set name='x' <%if($i>5){%>WHERE name ='kx'<%}%> ";
        Assert.assertEquals("update user set name2 ='x' <%if($i>5){%> WHERE name2 ='kx' <%}%>", SqlLexer.parse(sql, User.class));
        //
        sql = "select * from User where name = '1212'";
        Assert.assertEquals("select * from user where name2 = '1212'", SqlLexer.parse(sql, User.class));
    }

    @Test
    public void test_3()
    {
        String sql = "select name, age FROM User";
        Assert.assertEquals("select name2 , age FROM user", SqlLexer.parse(sql, User.class));
        //
        sql = "select u.name , u.age FROM User as u";
        Assert.assertEquals("select u.name2 , u.age FROM user as u", SqlLexer.parse(sql, User.class));
        //
        sql = "select name FROM User WHERE <%if( $name == 'sasda'){%> name = ${name} <%}%>";
        Assert.assertEquals("select name2 FROM user WHERE <%if( $name == 'sasda'){%> name2 = ${name} <%}%>", SqlLexer.parse(sql, User.class));
        //
        sql = "select age FROM User as u WHERE u.name = ?";
        Assert.assertEquals("select age FROM user as u WHERE u.name2 = ?", SqlLexer.parse(sql, User.class));
        //
        sql = "insert into User (age) values(5)";
        Assert.assertEquals("insert into user ( age ) values(5)", SqlLexer.parse(sql, User.class));
        //
        sql = "insert into User (age) select 16 from dual where not exists (select * from user where age = ${age})";
        Assert.assertEquals("insert into user ( age ) select 16 from dual where not exists (select * from user where age = ${age} )", SqlLexer.parse(sql, User.class));
    }
}
