package it;

import com.atlassian.confluence.plugin.functest.AbstractConfluencePluginWebTestCase;
import com.atlassian.confluence.plugin.functest.JWebUnitConfluenceWebTester;
import com.atlassian.confluence.plugin.functest.TesterConfiguration;
import junit.framework.Assert;

import java.io.IOException;
import java.util.Properties;

public class AbstractIntegrationTestCase extends AbstractConfluencePluginWebTestCase
{
    @Override
    protected JWebUnitConfluenceWebTester createConfluenceWebTester()
    {
        Properties props = new Properties();
        props.put("confluence.webapp.protocol", "http");
        props.put("confluence.webapp.host", "localhost");

        // this is deceiving: the func test library checks for the system properties
        // *before* checking in this properties file for these values, so these
        // properties are technically ignored
        props.put("confluence.webapp.port", Integer.parseInt(System.getProperty("http.port")));
        props.put("confluence.webapp.context.path", System.getProperty("context.path"));

        props.put("confluence.auth.admin.username", "admin");
        props.put("confluence.auth.admin.password", "admin");

        TesterConfiguration conf;
        try
        {
            conf = new TesterConfiguration(props);
        }
        catch (IOException ioe)
        {
            Assert.fail("Unable to create tester: " + ioe.getMessage());
            return null;
        }

        JWebUnitConfluenceWebTester tester = new JWebUnitConfluenceWebTester(conf);

        tester.getTestContext().setBaseUrl(tester.getBaseUrl());
        tester.setScriptingEnabled(false);

        return tester;
    }
}
