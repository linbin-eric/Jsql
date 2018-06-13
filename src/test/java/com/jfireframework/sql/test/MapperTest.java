package com.jfireframework.sql.test;

import static org.junit.Assert.assertEquals;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import org.h2.Driver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.jfireframework.sql.SessionFactory;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.dialect.impl.H2Dialect;
import com.jfireframework.sql.metadata.Page;
import com.jfireframework.sql.metadata.TableMode;
import com.jfireframework.sql.session.SqlSession;
import com.jfireframework.sql.test.vo.SqlLog;
import com.jfireframework.sql.test.vo.User;
import com.jfireframework.sql.test.vo.User.State;
import com.jfireframework.sql.test.vo.User.StringEnum;
import com.jfireframework.sql.transfer.resultset.ResultMap;
import com.jfireframework.sql.transfer.resultset.impl.EnumOrdinalTransfer;
import com.zaxxer.hikari.HikariDataSource;

public class MapperTest
{
	public static interface TestOp
	{
		@Sql(sql = "select count(*) from #{table} ", paramNames = "table")
		public int count(String table);
		
		@Sql(sql = "select count(*) from #{table} where name2=${name}", paramNames = "table,name")
		public int count(String table, String name);
		
		/** 测试$%%格式 **/
		@Sql(sql = "select count(*) from User where name like ${'%'+name+'%'}", paramNames = "name")
		public int count2(String name);
		
		@Sql(sql = "select count(*) from User where name like ${'%'+name}", paramNames = "name")
		public int count3(String name);
		
		@Sql(sql = "select count(*) from User where name like ${name+'%'}", paramNames = "name")
		public int count4(String name);
		
		/** 测试$%%格式 **/
		
		/** 测试$~ **/
		@Sql(sql = "select count(*) from User where id in ~{ids}", paramNames = "ids")
		public int count5(String ids);
		
		@Sql(sql = "select count(*) from User where id in ~{ids}", paramNames = "ids")
		public int count5(String[] ids);
		
		@Sql(sql = "select count(*) from User where id in ~{ids}", paramNames = "ids")
		public int count5(double[] ids);
		
		@Sql(sql = "select count(*) from User where id in ~{ids}", paramNames = "ids")
		public int count5(char[] ids);
		
		@Sql(sql = "select count(*) from User where id in ~{ids}", paramNames = "ids")
		public int count5(int[] ids);
		
		@Sql(sql = "select count(*) from User where id in ~{ids}", paramNames = "ids")
		public int count5(float[] ids);
		
		@Sql(sql = "select count(*) from User where id in ~{ids}", paramNames = "ids")
		public int count5(Integer[] ids);
		
		@Sql(sql = "select count(*) from User where id in ~{ids}", paramNames = "ids")
		public int count5(byte[] ids);
		
		@Sql(sql = "select count(*) from User where id in ~{ids}", paramNames = "ids")
		public int count5(short[] ids);
		
		@Sql(sql = "select count(*) from User where id in ~{ids}", paramNames = "ids")
		public int count5(long[] ids);
		
		@Sql(sql = "select count(*) from User where id in ~{ids}", paramNames = "ids")
		public int count5(Long[] ids);
		
		@Sql(sql = "select count(*) from User where id in ~{ids}", paramNames = "ids")
		public int count5(List<Integer> ids);
		
		@Sql(sql = "select count(*) from User where b in ~{booleans}", paramNames = "booleans")
		public int count5(boolean[] booleans);
		
		@Sql(sql = "select count(*) from User where id in ~{ids}", paramNames = "ids")
		public int count5_2(List<String> ids);
		
		/** 测试$~ **/
		
		/** 测试as 功能 **/
		@Sql(sql = "select * from User as u where u.name = ${name}", paramNames = "name")
		public User find(String name);
		
		@Sql(sql = "select age from User as u where u.name = ${name}", paramNames = "name")
		public User find2(String name);
		
		@Sql(sql = "select age as a from User as u where u.name = ${name}", paramNames = "name")
		public User find3(String name);
		
		/** 测试as类别名 **/
		/** 测试Enum */
		@Sql(sql = "select * from User where state =${s.ordinal()}", paramNames = "s")
		public User find(State s);
		
		@Sql(sql = "select * from User where stringEnum = ${v.name()}", paramNames = "v")
		User find(StringEnum v);
		
		@ResultMap(EnumOrdinalTransfer.class)
		@Sql(sql = "select state from User where name=${name}", paramNames = "name")
		public State findState(String name);
		
		@ResultMap(EnumOrdinalTransfer.class)
		@Sql(sql = "select state from User where name like ${'%'+name+'%'}", paramNames = "name")
		public List<State> findListState(String name);
		
		/** 测试Enum */
		
		/* 测试对POJO属性的提取 */
		/* 测试page */
		@Sql(sql = "select * from User <%if( name != null) {%> where name like ${'%'+name+'%'} <%}%> ", paramNames = "name")
		public List<User> find(String name, Page page);
		
		@Sql(sql = "select * from User where name like ${'%'+name+'%'}", paramNames = "name")
		public List<User> find2(String name, Page page);
		
		/* 测试page */
		/* 静态常量 */
		@Sql(sql = "select * from User where name = ${T(com.jfireframework.sql.test.vo.User).customName}", paramNames = "")
		User find3();
		/* 静态常量 */
		
	}
	
	public static interface TestOp2
	{
		/** 测试Enum */
		@Sql(sql = "select * from User where state =${s.ordinal()}", paramNames = "s")
		public User find(State s);
	}
	
	private SessionFactory			sessionFactory;
	private SessionfactoryConfig	config;
	private TestOp					testOp;
	
	@Before
	public void before() throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		config = new SessionfactoryConfig();
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:h2:mem:orderdb");
		dataSource.setDriverClassName(Driver.class.getName());
		dataSource.setUsername("sa");
		dataSource.setPassword("");
		config.setDataSource(dataSource);
		config.setClassLoader(MapperTest.class.getClassLoader());
		config.setTableMode(TableMode.CREATE);
		config.setDialect(new H2Dialect() {
			protected void setUnDefinedType(PreparedStatement preparedStatement, int i, Object value) throws SQLException
			{
				if (value instanceof StringEnum)
				{
					StringEnum stringEnum = (StringEnum) value;
					preparedStatement.setString(i, stringEnum.name());
				}
				else if (value instanceof Enum<?>)
				{
					Enum<?> enum1 = (Enum<?>) value;
					preparedStatement.setInt(i, enum1.ordinal());
				}
				else
				{
					preparedStatement.setObject(i, value);
				}
			}
		});
		config.setScanPackage("com.jfireframework.sql.test:in~*$TestOp;com.jfireframework.sql.test.vo");
		config.addSqlExecutor(new SqlLog());
		sessionFactory = config.build();
		SqlSession session = sessionFactory.openSession();
		User user = new User();
		user.setAge(12);
		user.setName("lin");
		user.setLength(18);
		user.setState(State.off);
		user.setStringEnum(StringEnum.v1);
		session.save(user);
		user.setId(null);
		user.setState(null);
		user.setName("linbin");
		user.setStringEnum(StringEnum.v2);
		session.save(user);
		session.close();
		testOp = sessionFactory.getMapper(TestOp.class);
		sessionFactory.getOrCreateCurrentSession();
	}
	
	/**
	 * 测试{}符号
	 */
	@Test
	public void test_1()
	{
		Assert.assertEquals(2, testOp.count("user"));
		Assert.assertEquals(1, testOp.count("user", "linbin"));
		sessionFactory.getOrCreateCurrentSession().close();
	}
	
	/**
	 * 测试$%%的格式
	 */
	@Test
	public void test_2()
	{
		Assert.assertEquals(2, testOp.count2("in"));
		Assert.assertEquals(2, testOp.count3("in"));
		Assert.assertEquals(1, testOp.count4("linb"));
		sessionFactory.getOrCreateCurrentSession().close();
	}
	
	/**
	 * 测试$~的格式
	 */
	@Test
	public void test_3()
	{
		Assert.assertEquals(2, testOp.count5("1,2"));
		Assert.assertEquals(2, testOp.count5(new String[] { "1", "2" }));
		Assert.assertEquals(2, testOp.count5(new char[] { '1', '2' }));
		Assert.assertEquals(2, testOp.count5("1,2,"));
		Assert.assertEquals(2, testOp.count5(new int[] { 1, 2 }));
		Assert.assertEquals(2, testOp.count5(new Integer[] { 1, 2 }));
		Assert.assertEquals(2, testOp.count5(new byte[] { 1, 2 }));
		Assert.assertEquals(2, testOp.count5(new short[] { 1, 2 }));
		Assert.assertEquals(2, testOp.count5(new double[] { 1, 2 }));
		Assert.assertEquals(2, testOp.count5(new Integer[] { 1, 2 }));
		Assert.assertEquals(2, testOp.count5(new long[] { 1, 2 }));
		Assert.assertEquals(2, testOp.count5(new Long[] { 1l, 2l }));
		Assert.assertEquals(2, testOp.count5(new float[] { 1, 2 }));
		assertEquals(2, testOp.count5(new boolean[] { true, false }));
		List<Integer> ids = new LinkedList<Integer>();
		ids.add(1);
		ids.add(2);
		Assert.assertEquals(2, testOp.count5(ids));
		List<String> ids2 = new LinkedList<String>();
		ids2.add("1");
		ids2.add("2");
		Assert.assertEquals(2, testOp.count5_2(ids2));
	}
	
	/**
	 * 测试类的as别名功能
	 */
	@Test
	public void test_4()
	{
		Assert.assertEquals(12, testOp.find("lin").getAge());
		Assert.assertEquals(12, testOp.find2("lin").getAge());
		/**
		 * 因为有了别名，所以实际上是无法映射的
		 */
		Assert.assertEquals(12, testOp.find3("lin").getAge());
		sessionFactory.getCurrentSession().close();
	}
	
	/**
	 * 测试enum
	 */
	@Test
	public void test_5()
	{
		Assert.assertNotNull(testOp.find(State.off));
		Assert.assertEquals(State.off, testOp.findState("lin"));
		List<State> list = testOp.findListState("lin");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals(State.off, list.get(0));
		Assert.assertEquals("lin", testOp.find(StringEnum.v1).getName());
		sessionFactory.getCurrentSession().close();
	}
	
	/**
	 * 测试page功能
	 */
	@Test
	public void test_7()
	{
		Page page = new Page();
		page.setOffset(0);
		page.setSize(1);
		page.setFetchSum(true);
		List<User> users = testOp.find("linb", page);
		Assert.assertEquals(1, page.getTotal());
		Assert.assertEquals(1, users.size());
		users = testOp.find2("lin", page);
		Assert.assertEquals(2, page.getTotal());
		Assert.assertEquals(1, users.size());
		sessionFactory.getCurrentSession().close();
	}
	
	/**
	 * 测试静态常量
	 */
	@Test
	public void test_8()
	{
		User user = new User();
		user.setName(User.customName);
		sessionFactory.getCurrentSession().save(user);
		Assert.assertEquals(user.getId(), testOp.find3().getId());
	}
}
