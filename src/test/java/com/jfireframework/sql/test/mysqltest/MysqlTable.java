package com.jfireframework.sql.test.mysqltest;

import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import com.jfireframework.sql.annotation.Pk;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.dbstructure.column.Comment;
import com.jfireframework.sql.pkstrategy.AutoIncrement;

@TableEntity(name = "test_demo")
public class MysqlTable
{
	@Pk
	@AutoIncrement
	@Comment("这是主键")
	private Integer		id;
	@Comment("12123注释")
	private int			col1;
	private long		col2;
	private float		col3;
	@Comment("注释2")
	private double		col4;
	private String		col5;
	private boolean		col6;
	private Date		col7;
	private Calendar	col8;
	@Comment("注释3")
	private Timestamp	col9;
	private Time		col10;
	private byte[]		col11;
	private Clob		col12;
}
