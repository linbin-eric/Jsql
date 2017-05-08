package com.jfireframework.sql.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuiteClasses({ InterfaceGenerateTest.class, DbCreateTest.class, CURDTest.class, StrategyTest.class })
@RunWith(Suite.class)
public class SuiteTest
{
    
}
