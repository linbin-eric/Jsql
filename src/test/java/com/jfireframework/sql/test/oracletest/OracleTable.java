package com.jfireframework.sql.test.oracletest;

import com.jfireframework.sql.annotation.Id;
import com.jfireframework.sql.annotation.TableEntity;

@TableEntity(name = "TEST_DEMO")
public class OracleTable
{
	@Id
	private Integer	id;
	private String	name;
	private int		age3;
//	private int		age;
	
	public Integer getId()
	{
		return id;
	}
	
	public void setId(Integer id)
	{
		this.id = id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
}
