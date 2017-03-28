package com.jfireframework.sql.test.entity;

import com.jfireframework.sql.annotation.Column;
import com.jfireframework.sql.annotation.FindBy;
import com.jfireframework.sql.annotation.Id;
import com.jfireframework.sql.annotation.TableEntity;

@TableEntity(name = "user12")
public class UidUser
{
    @Id(useUid = true)
    @Column(name = "id")
    private Long   id;
    @FindBy
    private String username;
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
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
