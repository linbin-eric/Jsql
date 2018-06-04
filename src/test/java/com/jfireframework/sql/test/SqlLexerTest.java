package com.jfireframework.sql.test;

import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.sql.analyse.token.SqlLexer;

public class SqlLexerTest
{
	@Test
	public void test_1()
	{
		
		String sql = "select * from user where name = ${name}";
		Assert.assertEquals("SELECT * FROM user WHERE name = ${name}", new SqlLexer(sql).format());
		//
		sql = "select sum(age) from user";
		Assert.assertEquals("SELECT SUM ( age ) FROM user", new SqlLexer(sql).format());
		//
		sql = "select name from user where name ='sasad'";
		Assert.assertEquals("SELECT name FROM user WHERE name = 'sasad'", new SqlLexer(sql).format());
		//
		sql = "select name as n from user where age>14";
		Assert.assertEquals("SELECT name AS n FROM user WHERE age > 14", new SqlLexer(sql).format());
		//
		sql = "select name,age from user where id!=1 and sex=1";
		Assert.assertEquals("SELECT name , age FROM user WHERE id != 1 AND sex = 1", new SqlLexer(sql).format());
		//
		sql = "select name ,  age from user where (name='sada' and sex=1) or age >=13";
		Assert.assertEquals("SELECT name , age FROM user WHERE ( name = 'sada' AND sex = 1 ) OR age >= 13", new SqlLexer(sql).format());
		//
		sql = "select #{myname}  from  user";
		Assert.assertEquals("SELECT #{myname} FROM user", new SqlLexer(sql).format());
		//
		sql = "select name from user where <%if($name != null){%>   name   = 'sasa' <%}%> and age =12";
		Assert.assertEquals("SELECT name FROM user WHERE <%if($name != null){%> name = 'sasa' <%}%> AND age = 12", new SqlLexer(sql).format());
		//
		sql = "select * from user where id in ~{ids}";
		Assert.assertEquals("SELECT * FROM user WHERE id IN ~{ids}", new SqlLexer(sql).format());
		//
		sql = "select User.name from User";
		Assert.assertEquals("SELECT User.name FROM User", new SqlLexer(sql).format());
		//
		sql = "select user.name from user";
		Assert.assertEquals("SELECT user.name FROM user", new SqlLexer(sql).format());
		//
		sql = "select user.name from user left join room WHERE   user.id = room.id";
		Assert.assertEquals("SELECT user.name FROM user LEFT JOIN room WHERE user.id = room.id", new SqlLexer(sql).format());
		//
		sql = "select * from User WHERE name =  ${n.name()}";
		Assert.assertEquals("SELECT * FROM User WHERE name = ${n.name()}", new SqlLexer(sql).format());
		//
		sql = "update User set name='x' <%if($i>5){%>WHERE name ='kx'<%}%> ";
		Assert.assertEquals("UPDATE User SET name = 'x' <%if($i>5){%> WHERE name = 'kx' <%}%>", new SqlLexer(sql).format());
		//
		sql = "select * from User where name = '1212'";
		Assert.assertEquals("SELECT * FROM User WHERE name = '1212'", new SqlLexer(sql).format());
	}
}
