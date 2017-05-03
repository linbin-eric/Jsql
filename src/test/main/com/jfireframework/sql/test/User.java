package com.jfireframework.sql.test;

import com.jfireframework.sql.annotation.Column;
import com.jfireframework.sql.annotation.EnumBoundHandler;
import com.jfireframework.sql.annotation.Id;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.util.enumhandler.EnumOrdinalHandler;

@TableEntity(name = "user")
public class User
{
    @EnumBoundHandler(EnumOrdinalHandler.class)
    public static enum State
    {
        off, on;
    }
    @Id
    private Integer id;
    @Column(name = "name2")
    private String  name;
    private State   state;
    
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
