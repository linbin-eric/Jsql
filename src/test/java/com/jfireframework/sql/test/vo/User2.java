package com.jfireframework.sql.test.vo;

import com.jfireframework.sql.annotation.Pk;
import com.jfireframework.sql.annotation.StandardColumnDef;
import com.jfireframework.sql.annotation.TableDef;

@TableDef(name = "user", editable = false)
public class User2
{
    @Pk
    private Integer id;
    @StandardColumnDef(columnName = "name2")
    private String name;
    private int age;

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
