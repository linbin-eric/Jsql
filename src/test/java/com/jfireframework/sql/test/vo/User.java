package com.jfireframework.sql.test.vo;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import com.jfireframework.sql.annotation.Column;
import com.jfireframework.sql.annotation.EnumBoundHandler;
import com.jfireframework.sql.annotation.Id;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.util.enumhandler.EnumOrdinalHandler;
import com.jfireframework.sql.util.enumhandler.EnumStringHandler;

@TableEntity(name = "user")
public class User
{
    public static final long now = System.currentTimeMillis();
    
    @EnumBoundHandler(EnumOrdinalHandler.class)
    public static enum State
    {
        off, on;
    }
    
    @EnumBoundHandler(EnumStringHandler.class)
    public static enum StringEnum
    {
        v1, v2
    }
    
    @Id
    private Integer       id;
    @Column(name = "name2")
    private String        name;
    private int           age;
    private State         state;
    @Column(loadIgnore = true)
    private Integer       length;
    @Column(name = "age", saveIgnore = true)
    private int           age2;
    private StringEnum    stringEnum;
    private boolean       b         = false;
    private byte[]        barray    = new byte[] { 1, 2 };
    private Calendar      calendar  = Calendar.getInstance();
    private Date          date      = new Date();
    private double        d1        = 2.53d;
    private float         f1        = 5.36f;
    private long          l1        = 23l;
    private java.sql.Date sqlDate   = new java.sql.Date(now);
    private Time          time      = new Time(now);
    private Timestamp     timestamp = new Timestamp(now);
    private Boolean       B11       = false;
    private Double        D11       = 6.32d;
    private Float         F11       = 5.69f;
    private Long          L11       = 5625l;
    
    public double getD1()
    {
        return d1;
    }
    
    public void setD1(double d1)
    {
        this.d1 = d1;
    }
    
    public float getF1()
    {
        return f1;
    }
    
    public void setF1(float f1)
    {
        this.f1 = f1;
    }
    
    public long getL1()
    {
        return l1;
    }
    
    public void setL1(long l1)
    {
        this.l1 = l1;
    }
    
    public java.sql.Date getSqlDate()
    {
        return sqlDate;
    }
    
    public void setSqlDate(java.sql.Date sqlDate)
    {
        this.sqlDate = sqlDate;
    }
    
    public Time getTime()
    {
        return time;
    }
    
    public void setTime(Time time)
    {
        this.time = time;
    }
    
    public Boolean getB11()
    {
        return B11;
    }
    
    public void setB11(Boolean b11)
    {
        B11 = b11;
    }
    
    public Double getD11()
    {
        return D11;
    }
    
    public void setD11(Double d11)
    {
        D11 = d11;
    }
    
    public Float getF11()
    {
        return F11;
    }
    
    public void setF11(Float f11)
    {
        F11 = f11;
    }
    
    public Long getL11()
    {
        return L11;
    }
    
    public void setL11(Long l11)
    {
        L11 = l11;
    }
    
    public Timestamp getTimestamp()
    {
        return timestamp;
    }
    
    public boolean isB()
    {
        return b;
    }
    
    public void setB(boolean b)
    {
        this.b = b;
    }
    
    public byte[] getBarray()
    {
        return barray;
    }
    
    public void setBarray(byte[] barray)
    {
        this.barray = barray;
    }
    
    public Calendar getCalendar()
    {
        return calendar;
    }
    
    public void setCalendar(Calendar calendar)
    {
        this.calendar = calendar;
    }
    
    public Date getDate()
    {
        return date;
    }
    
    public void setDate(Date date)
    {
        this.date = date;
    }
    
    public void setTimestamp(Timestamp timestamp)
    {
        this.timestamp = timestamp;
    }
    
    public StringEnum getStringEnum()
    {
        return stringEnum;
    }
    
    public void setStringEnum(StringEnum stringEnum)
    {
        this.stringEnum = stringEnum;
    }
    
    public int getAge2()
    {
        return age2;
    }
    
    public void setAge2(int age2)
    {
        this.age2 = age2;
    }
    
    public Integer getLength()
    {
        return length;
    }
    
    public void setLength(Integer length)
    {
        this.length = length;
    }
    
    public int getAge()
    {
        return age;
    }
    
    public void setAge(int age)
    {
        this.age = age;
    }
    
    public State getState()
    {
        return state;
    }
    
    public void setState(State state)
    {
        this.state = state;
    }
    
    public static String xx = "ssss";
    
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
