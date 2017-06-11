package com.jfireframework.sql.test.vo;

import com.jfireframework.sql.annotation.Column;
import com.jfireframework.sql.annotation.Id;
import com.jfireframework.sql.annotation.TableEntity;

@TableEntity(name = "user", editable = false)
public class User2
{
    @Id
    private Integer id;
    @Column(name = "name2")
    private String  name;
    private int     age;
    
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
    
    public int getAge()
    {
        return age;
    }
    
    public void setAge(int age)
    {
        this.age = age;
    }
    
}
