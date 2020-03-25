package com.jfirer.jsql.test.mysqltest;

import com.jfirer.jsql.annotation.Pk;
import com.jfirer.jsql.annotation.StandardColumnDef;
import com.jfirer.jsql.annotation.TableDef;
import com.jfirer.jsql.dbstructure.Constraint;
import com.jfirer.jsql.dbstructure.Constraint.Type;

@TableDef(name = "test_demo")
public class MysqlTable2
{
    @Pk
    @Constraint(type = Type.PRIMARY_KEY)
    @StandardColumnDef(isNullable = false)
    private String id;
    private String name;
}
