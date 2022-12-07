package com.jfirer.jsql.test.vo;

import com.jfirer.jsql.annotation.Pk;
import com.jfirer.jsql.annotation.StandardColumnDef;
import com.jfirer.jsql.annotation.TableDef;

@TableDef(name = "user", editable = false)
public class User2
{
    @Pk
    private Integer id;
    @StandardColumnDef(columnName = "name2")
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
