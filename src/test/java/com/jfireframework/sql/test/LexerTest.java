package com.jfireframework.sql.test;

import java.util.HashSet;
import java.util.Set;
import org.h2.util.New;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.dbstructure.column.impl.MysqlColumnTypeDictionary;
import com.jfireframework.sql.mapfield.FieldOperatorDictionary;
import com.jfireframework.sql.metadata.MetaContext;
import com.jfireframework.sql.parse.lexer.Lexer;
import com.jfireframework.sql.test.vo.User;

public class LexerTest
{
	@Test
	public void test_1()
	{
		
		String sql = "select * from user where name = $name";
		Assert.assertEquals("SELECT * FROM user WHERE name = $name", new Lexer(sql).toString());
		//
		sql = "select sum(age) from user";
		Assert.assertEquals("SELECT SUM ( age ) FROM user", new Lexer(sql).toString());
		//
		sql = "select name from user where name ='sasad'";
		Assert.assertEquals("SELECT name FROM user WHERE name = 'sasad'", new Lexer(sql).toString());
		//
		sql = "select name as n from user where age>14";
		Assert.assertEquals("SELECT name AS n FROM user WHERE age > 14", new Lexer(sql).toString());
		//
		sql = "select name,age from user where id!=1 and sex=1";
		Assert.assertEquals("SELECT name , age FROM user WHERE id != 1 AND sex = 1", new Lexer(sql).toString());
		//
		sql = "select name ,  age from user where (name='sada' and sex=1) or age >=13";
		Assert.assertEquals("SELECT name , age FROM user WHERE ( name = 'sada' AND sex = 1 ) OR age >= 13", new Lexer(sql).toString());
		//
		sql = "select {myname}  from  user";
		Assert.assertEquals("SELECT {myname} FROM user", new Lexer(sql).toString());
		//
		sql = "select name from user where <if($name != null)>   name   = 'sasa' </if> and age =12";
		Assert.assertEquals("SELECT name FROM user WHERE <if($name != null)> name = 'sasa' </if> AND age = 12", new Lexer(sql).toString());
		//
		sql = "select * from user where id in $~ids";
		Assert.assertEquals("SELECT * FROM user WHERE id IN $~ids", new Lexer(sql).toString());
		//
		sql = "select * from user where name = $%name%";
		Assert.assertEquals("SELECT * FROM user WHERE name = $%name%", new Lexer(sql).toString());
		//
		sql = "select User.name from User";
		Assert.assertEquals("SELECT User.name FROM User", new Lexer(sql).toString());
		//
		sql = "select user.name from user";
		Assert.assertEquals("SELECT user.name FROM user", new Lexer(sql).toString());
		//
		sql = "select user.name from user left join room WHERE   user.id = room.id";
		Assert.assertEquals("SELECT user.name FROM user LEFT JOIN room WHERE user.id = room.id", new Lexer(sql).toString());
		//
		sql = "select * from User WHERE name =  @com.jfireframework.context.test.function.validate.User.Constant";
		Assert.assertEquals("SELECT * FROM User WHERE name = @com.jfireframework.context.test.function.validate.User.Constant", new Lexer(sql).toString());
		//
		sql = "select * from User WHERE name =  $n.name()";
		Assert.assertEquals("SELECT * FROM User WHERE name = $n.name()", new Lexer(sql).toString());
		//
		sql = "update User set name='x' <if($i>5)>WHERE name ='kx'</if> ";
		Assert.assertEquals("UPDATE User SET name = 'x' <if($i>5)> WHERE name = 'kx' </if>", new Lexer(sql).toString());
	}
	
	@Test
	public void test_2()
	{
		Set<Class<?>> set = new HashSet<Class<?>>();
		set.add(User.class);
		SessionfactoryConfig config = new SessionfactoryConfig();
		config.setJdbcTypeDictionary(new MysqlColumnTypeDictionary());
		config.setFieldOperatorDictionary(new FieldOperatorDictionary.BuildInFieldOperatorDictionary());
		MetaContext metaContext = new MetaContext(set, config);
		String sql = "select name FROM User";
		Assert.assertEquals("SELECT name FROM user", new Lexer(sql).parseEntity(metaContext).toString());
	}
	
	@Test
	public void test_3()
	{
		Set<Class<?>> set = new HashSet<Class<?>>();
		set.add(User.class);
		SessionfactoryConfig config = new SessionfactoryConfig();
		config.setJdbcTypeDictionary(new MysqlColumnTypeDictionary());
		config.setFieldOperatorDictionary(new FieldOperatorDictionary.BuildInFieldOperatorDictionary());
		MetaContext metaContext = new MetaContext(set, config);
		String sql = "select name,age FROM User";
		Assert.assertEquals("SELECT user.name2 , user.age FROM user", new Lexer(sql).parse(metaContext).toString());
		//
		sql = "select u.name,u.age FROM User as u";
		Assert.assertEquals("SELECT u.name2 , u.age FROM user AS u", new Lexer(sql).parse(metaContext).toString());
		//
		sql = "select name FROM User WHERE <if( $name == 'sasda')> name = $name </if>";
		Assert.assertEquals("SELECT user.name2 FROM user WHERE <if( $name == 'sasda')> user.name2 = $name </if>", new Lexer(sql).parse(metaContext).toString());
		//
		sql = "select age FROM User as u WHERE u.name = ?";
		Assert.assertEquals("SELECT u.age FROM user AS u WHERE u.name2 = ?", new Lexer(sql).parse(metaContext).toString());
	}
}
