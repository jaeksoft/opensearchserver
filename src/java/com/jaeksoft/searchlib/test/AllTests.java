package com.jaeksoft.searchlib.test;

import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ WebCrawlerTestCase.class, PatternTestCase.class })
public class AllTests {

	public static TestSuite openSearchServerTestSuits() {
		TestSuite allTests = new TestSuite();
		allTests.addTest(WebCrawlerTestCase.suite());
		allTests.addTest(PatternTestCase.suite());
		return allTests;
	}

}
