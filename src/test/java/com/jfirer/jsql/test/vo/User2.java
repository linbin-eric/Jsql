package com.jfirer.jsql.test.vo;

import com.jfirer.jsql.annotation.AutoIncrement;
import com.jfirer.jsql.annotation.ColumnName;
import com.jfirer.jsql.annotation.Pk;
import com.jfirer.jsql.annotation.TableDef;

@TableDef(value = "user")
public class User2
{
    @Pk
    @AutoIncrement
    private Integer id;
    @ColumnName(value = "name2")
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
