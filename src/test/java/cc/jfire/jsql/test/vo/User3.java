package cc.jfire.jsql.test.vo;

import cc.jfire.jsql.annotation.ColumnName;
import cc.jfire.jsql.annotation.Pk;
import cc.jfire.jsql.annotation.PkGenerator;
import cc.jfire.jsql.annotation.TableDef;

@TableDef(value = "user2")
public class User3
{
    @Pk
    @PkGenerator(PkGenerator.UUIDGenerator.class)
    private String id;
    @ColumnName(value = "name2")
    private String name;
    private int    age;

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
