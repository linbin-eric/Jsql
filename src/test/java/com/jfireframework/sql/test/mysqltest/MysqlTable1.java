package com.jfireframework.sql.test.mysqltest;

import com.jfireframework.sql.annotation.Pk;
import com.jfireframework.sql.annotation.StandardColumnDef;
import com.jfireframework.sql.annotation.TableDef;
import com.jfireframework.sql.annotation.pkstrategy.AutoIncrement;
import com.jfireframework.sql.dbstructure.Constraint;
import com.jfireframework.sql.dbstructure.Constraint.Type;
import com.jfireframework.sql.dbstructure.Index;

import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

@TableDef(name = "test_demo")
public class MysqlTable1
{
    @Pk
    @AutoIncrement
    @Constraint(type = Type.PRIMARY_KEY)
    @StandardColumnDef(isNullable = false, comment = "这是主键")
    private Integer id;
    @StandardColumnDef(comment = "12123注释")
    private int col1;
    @Index
    private long col2;
    @Index(unique = true)
    private float col3;
    @StandardColumnDef(comment = "注释2")
    private double col4;
    private String col5;
    private boolean col6;
    private Date col7;
    private Calendar col8;
    @StandardColumnDef(comment = "注释3")
    private Timestamp col9;
    private Time col10;
    private byte[] col11;
    private Clob col12;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public int getCol1()
    {
        return col1;
    }

    public void setCol1(int col1)
    {
        this.col1 = col1;
    }

    public long getCol2()
    {
        return col2;
    }

    public void setCol2(long col2)
    {
        this.col2 = col2;
    }

    public float getCol3()
    {
        return col3;
    }

    public void setCol3(float col3)
    {
        this.col3 = col3;
    }

    public double getCol4()
    {
        return col4;
    }

    public void setCol4(double col4)
    {
        this.col4 = col4;
    }

    public String getCol5()
    {
        return col5;
    }

    public void setCol5(String col5)
    {
        this.col5 = col5;
    }

    public boolean isCol6()
    {
        return col6;
    }

    public void setCol6(boolean col6)
    {
        this.col6 = col6;
    }

    public Date getCol7()
    {
        return col7;
    }

    public void setCol7(Date col7)
    {
        this.col7 = col7;
    }

    public Calendar getCol8()
    {
        return col8;
    }

    public void setCol8(Calendar col8)
    {
        this.col8 = col8;
    }

    public Timestamp getCol9()
    {
        return col9;
    }

    public void setCol9(Timestamp col9)
    {
        this.col9 = col9;
    }

    public Time getCol10()
    {
        return col10;
    }

    public void setCol10(Time col10)
    {
        this.col10 = col10;
    }

    public byte[] getCol11()
    {
        return col11;
    }

    public void setCol11(byte[] col11)
    {
        this.col11 = col11;
    }

    public Clob getCol12()
    {
        return col12;
    }

    public void setCol12(Clob col12)
    {
        this.col12 = col12;
    }

}
