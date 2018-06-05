package com.jfireframework.sql.test;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.sql.analyse.token.SqlLexer;
import com.jfireframework.sql.test.vo.User;
import com.jfireframework.sql.util.TableEntityInfo;

public class SqlLexerTest
{
	@Test
	public void test_1()
	{
		
		String sql = "select * from user where name = ${name}";
		Assert.assertEquals("SELECT * FROM user WHERE name = ${name}", SqlLexer.parse(sql).format());
		//
		sql = "select sum(age) from user";
		Assert.assertEquals("SELECT SUM ( age ) FROM user", SqlLexer.parse(sql).format());
		//
		sql = "select name from user where name ='sasad'";
		Assert.assertEquals("SELECT name FROM user WHERE name = 'sasad'", SqlLexer.parse(sql).format());
		//
		sql = "select name as n from user where age>14";
		Assert.assertEquals("SELECT name AS n FROM user WHERE age > 14", SqlLexer.parse(sql).format());
		//
		sql = "select name,age from user where id!=1 and sex=1";
		Assert.assertEquals("SELECT name , age FROM user WHERE id != 1 AND sex = 1", SqlLexer.parse(sql).format());
		//
		sql = "select name ,  age from user where (name='sada' and sex=1) or age >=13";
		Assert.assertEquals("SELECT name , age FROM user WHERE ( name = 'sada' AND sex = 1 ) OR age >= 13", SqlLexer.parse(sql).format());
		//
		sql = "select #{myname}  from  user";
		Assert.assertEquals("SELECT #{myname} FROM user", SqlLexer.parse(sql).format());
		//
		sql = "select name from user where <%if($name != null){%>   name   = 'sasa' <%}%> and age =12";
		Assert.assertEquals("SELECT name FROM user WHERE <%if($name != null){%> name = 'sasa' <%}%> AND age = 12", SqlLexer.parse(sql).format());
		//
		sql = "select * from user where id in ~{ids}";
		Assert.assertEquals("SELECT * FROM user WHERE id IN ~{ids}", SqlLexer.parse(sql).format());
		//
		sql = "select User.name from User";
		Assert.assertEquals("SELECT User.name FROM User", SqlLexer.parse(sql).format());
		//
		sql = "select user.name from user";
		Assert.assertEquals("SELECT user.name FROM user", SqlLexer.parse(sql).format());
		//
		sql = "select user.name from user left join room WHERE   user.id = room.id";
		Assert.assertEquals("SELECT user.name FROM user LEFT JOIN room WHERE user.id = room.id", SqlLexer.parse(sql).format());
		//
		sql = "select * from User WHERE name =  ${n.name()}";
		Assert.assertEquals("SELECT * FROM User WHERE name = ${n.name()}", SqlLexer.parse(sql).format());
		//
		sql = "update User set name='x' <%if($i>5){%>WHERE name ='kx'<%}%> ";
		Assert.assertEquals("UPDATE User SET name = 'x' <%if($i>5){%> WHERE name = 'kx' <%}%>", SqlLexer.parse(sql).format());
		//
		sql = "select * from User where name = '1212'";
		Assert.assertEquals("SELECT * FROM User WHERE name = '1212'", SqlLexer.parse(sql).format());
	}
	
	@Test
	public void test_2()
	{
		TableEntityInfo tableTransfer = TableEntityInfo.parse(User.class);
		String sql = "select name FROM User";
		Map<String, TableEntityInfo> map = new HashMap<String, TableEntityInfo>();
		map.put("User", tableTransfer);
		Assert.assertEquals("SELECT name2 FROM user", SqlLexer.parse(sql).transfer(map).format());
	}
	
	@Test
	public void test_3()
	{
		TableEntityInfo tableTransfer = TableEntityInfo.parse(User.class);
		Map<String, TableEntityInfo> map = new HashMap<String, TableEntityInfo>();
		map.put("User", tableTransfer);
		String sql = "select name,age FROM User";
		Assert.assertEquals("SELECT name2 , age FROM user", SqlLexer.parse(sql).transfer(map).format());
		//
		sql = "select u.name,u.age FROM User as u";
		Assert.assertEquals("SELECT u.name2 , u.age FROM user AS u", SqlLexer.parse(sql).transfer(map).format());
		//
		sql = "select name FROM User WHERE <%if( $name == 'sasda'){%> name = ${name} <%}%>";
		Assert.assertEquals("SELECT name2 FROM user WHERE <%if( $name == 'sasda'){%> name2 = ${name} <%}%>", SqlLexer.parse(sql).transfer(map).format());
		//
		sql = "select age FROM User as u WHERE u.name = ?";
		Assert.assertEquals("SELECT age FROM user AS u WHERE u.name2 = ?", SqlLexer.parse(sql).transfer(map).format());
		//
		sql = "insert into User (age) values(5)";
		Assert.assertEquals("INSERT INTO user ( age ) VALUES ( 5 )", SqlLexer.parse(sql).transfer(map).format());
		//
		sql = "insert into User (age) select 16 from dual where not exists (select * from user where age = ${age})";
		Assert.assertEquals("INSERT INTO user ( age ) SELECT 16 FROM dual WHERE NOT EXISTS ( SELECT * FROM user WHERE age = ${age} )", SqlLexer.parse(sql).transfer(map).format());
	}
	
}
