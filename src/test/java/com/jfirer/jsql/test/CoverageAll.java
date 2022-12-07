package com.jfirer.jsql.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuiteClasses({InterfaceGenerateTest.class, SqlLexerTest.class, CURDTest.class, ModelTest.class, MapperTest.class, JpaModeTest.class})
@RunWith(Suite.class)
public class CoverageAll
{}
