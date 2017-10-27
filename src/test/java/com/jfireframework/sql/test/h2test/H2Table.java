package com.jfireframework.sql.test.h2test;

import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import com.jfireframework.sql.annotation.NameStrategy;
import com.jfireframework.sql.annotation.Pk;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.pkstrategy.AutoIncrement;

@TableEntity(name = "test_demo")
public class H2Table
{
    @Pk
    @AutoIncrement
    private Integer   id;
    private int       col1;
    private long      col2;
    private float     col3;
    private double    col4;
    private String    col5;
    private boolean   col6;
    private Date      col7;
    private Calendar  col8;
    private Timestamp col9;
    private Time      col10;
    private byte[]    col11;
    private Clob      col12;
}
