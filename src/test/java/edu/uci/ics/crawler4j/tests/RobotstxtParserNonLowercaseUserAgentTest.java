package edu.uci.ics.crawler4j.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import edu.uci.ics.crawler4j.robotstxt.HostDirectives;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtParser;

public class RobotstxtParserNonLowercaseUserAgentTest {

    @Test
    public void testParseWithNonLowercaseUserAgent() {
        String userAgent = "testAgent";
        String content = "User-agent: " + userAgent + '\n' + "Disallow: /test/path/\n";

        final RobotstxtConfig robotsConfig = new RobotstxtConfig();
        robotsConfig.setUserAgentName(userAgent);

        HostDirectives hostDirectives = RobotstxtParser.parse(content, robotsConfig);
        assertNotNull("parsed HostDirectives is null", hostDirectives);
        assertFalse("HostDirectives should not allow path: '/test/path/'",
                    hostDirectives.allows("/test/path/"));
    }

}
