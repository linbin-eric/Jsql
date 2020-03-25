package com.jfirer.jsql.test.oracletest;

import com.jfirer.jsql.annotation.Pk;
import com.jfirer.jsql.annotation.StandardColumnDef;
import com.jfirer.jsql.annotation.TableDef;
import com.jfirer.jsql.dbstructure.Index;

import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

@TableDef(name = "TEST_DEMO")
public class OracleTable
{
    @Pk
    @StandardColumnDef(comment = "这是主键")
    private Integer id;
    private int col1;
    private long col2;
    private float col3;
    @Index
    private double col4;
    private String col5;
    private boolean col6;
    private Date col7;
    private Calendar col8;
    private Timestamp col9;
    private Time col10;
    private byte[] col11;
    private Clob col12;
}
