package com.jaeksoft.searchlib.test.rest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ RestIndexTestCase.class, RestUpdateTestCase.class,
		RestAutocompletionTestCase.class, RestDeleteTestCase.class })
public class AllRestAPITests {

}