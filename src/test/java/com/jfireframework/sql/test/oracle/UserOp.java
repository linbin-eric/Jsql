package com.jfireframework.sql.test.oracle;

import java.util.List;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.jfirecontext.MapperOp;
import com.jfireframework.sql.page.Page;

@MapperOp
public interface UserOp
{
    @Sql(sql = "select * from User", paramNames = "")
    public List<User12> find(Page page);
}
