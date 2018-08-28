package com.jfireframework.sql.test.vo;

import com.jfireframework.sql.annotation.Pk;
import com.jfireframework.sql.annotation.StandardColumnDef;
import com.jfireframework.sql.annotation.TableDef;
import com.jfireframework.sql.annotation.pkstrategy.PkGenerator;

@TableDef(name = "user2")
public class User3
{
    @Pk
    @PkGenerator(PkGenerator.UUIDGenerator.class)
    private String id;
    @StandardColumnDef(columnName = "name2")
    private String name;
    private int age;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
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
