package com.jaeksoft.searchlib.test;

import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ WebTemplateTestCase.class, PatternTestCase.class,
		WebCrawlerTestCase.class })
public class AllTests {

	public static TestSuite openSearchServerTestSuits()
			throws InterruptedException {
		TestSuite allTests = new TestSuite();
		allTests.addTest(WebTemplateTestCase.suite());
		allTests.addTest(PatternTestCase.suite());
		allTests.addTest(WebCrawlerTestCase.suite());
		return allTests;
	}

}
