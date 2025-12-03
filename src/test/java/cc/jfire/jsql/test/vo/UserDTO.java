package cc.jfire.jsql.test.vo;

import cc.jfire.jsql.annotation.ColumnName;
import cc.jfire.jsql.annotation.TableDef;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableDef("user")
public class UserDTO
{
    private int    age;
    @ColumnName("name2")
    private String name2;
}
