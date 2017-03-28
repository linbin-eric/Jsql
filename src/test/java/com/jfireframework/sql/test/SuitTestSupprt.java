package com.jfireframework.sql.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ConcurrentTest.class, //
        DaoTest.class, //
        FieldTest.class, //
        InterfaceTest.class, //
        TxManagerTest.class, //
        StrategyTest.class, //
        H2Test.class, //
        TxTest.class//
})
public class SuitTestSupprt
{
    
}
