package com.jfirer.jsql.test.vo;

import com.jfirer.jsql.annotation.TableDef;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableDef("userdto")
public class UserDTO
{
    private int    age;
    private String name2;
}
