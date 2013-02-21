package com.jaeksoft.searchlib.test.rest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.jaeksoft.searchlib.test.DeleteIndexTestCase;
import com.jaeksoft.searchlib.test.WebTemplateTestCase;

@RunWith(Suite.class)
@SuiteClasses({ WebTemplateTestCase.class, RestUpdateTestCase.class,
		DeleteIndexTestCase.class })
public class AllRestAPITests {

}