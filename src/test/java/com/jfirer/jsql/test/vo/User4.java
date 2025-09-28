package com.jfirer.jsql.test.vo;

import com.jfirer.jsql.annotation.ColumnName;
import com.jfirer.jsql.annotation.TableDef;

@TableDef(value = "user")
public class User4 extends AbstractUser
{
    @ColumnName("name2")
    protected String  name2;
    protected int     age;
    protected Boolean B11 = false;
    protected Double  D11 = 6.32d;

    public String getName2()
    {
        return name2;
    }

    public void setName2(String name2)
    {
        this.name2 = name2;
    }


}
