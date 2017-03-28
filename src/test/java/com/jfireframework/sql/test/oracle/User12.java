package com.jfireframework.sql.test.oracle;

import com.jfireframework.sql.annotation.Id;
import com.jfireframework.sql.annotation.SeqId;
import com.jfireframework.sql.annotation.TableEntity;

@TableEntity(name = "user12")
public class User12
{
    @Id
    @SeqId("testseq")
    private Integer id;
    private String  username;
    
    public Integer getId()
    {
        return id;
    }
    
    public void setId(Integer id)
    {
        this.id = id;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public void setUsername(String username)
    {
        this.username = username;
    }
    
}
