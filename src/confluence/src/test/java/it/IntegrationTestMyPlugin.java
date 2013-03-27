package it;

public class IntegrationTestMyPlugin extends AbstractIntegrationTestCase
{
	public void testSomething()
	{
        gotoPage("");
        assertTextPresent("Welcome");
	}
}
