package com.jfireframework.sql.test;

import java.util.List;
import com.jfireframework.sql.annotation.Sql;
import com.jfireframework.sql.page.Page;

public interface UserDAO
{
    @Sql(sql = "select * from User where name= User.xx", paramNames = "")
    public List<User> find1();
    
    @Sql(sql = "select * from User", paramNames = "")
    public List<User> find();
    
    @Sql(sql = "select * from User <if($i>5)>where name ='kx'</if> ", paramNames = "i")
    public List<User> find2(int i);
    
    @Sql(sql = "select * from User <if($i>5)>where name ='kx'</if> ", paramNames = "i")
    public List<User> find2(int i, Page page);
    
    @Sql(sql = "update User set name = $user.name", paramNames = "user")
    public int update(User user);
    
    @Sql(sql = "update User set name = $user.name <if( $user.id > 6 )> where  name ='x' </if> ", paramNames = "user")
    public int update2(User user);
    
    @Sql(sql = "update User set name = $user.name <if( $user.name != null && $user.name==\"ss\" )> where name ='x' </if> ", paramNames = "user")
    public int update3(User user);
    
    @Sql(sql = "select * from User where state = State.off", paramNames = "")
    public User find4();
    
    @Sql(sql = "delete from User where id in $~ids", paramNames = "ids")
    public int delete(String ids);
    
    @Sql(sql = "delete from User where id in $~ids", paramNames = "ids")
    public int delete(Integer[] ids);
}
