package com.jfireframework.sql.test;

import java.sql.PreparedStatement;
import org.junit.Test;
import com.jfireframework.sql.resultsettransfer.IntegerTransfer;
import com.jfireframework.sql.test.entity.UidUser;

public class UidTest extends BaseTestSupport
{
    @Test
    public void test()
    {
        UidUser user = new UidUser();
        user.setUsername("test");
        session.save(user);
    }
    
    @Test
    public void test2()
    {
        // UidUser user = session.findBy(UidUser.class, "username", "test");
        // System.out.println(user.getId());
        int value = session.query(new IntegerTransfer(), "select SEQ_TICKET.nextval from dual");
        System.out.println(value);
    }
    
}
