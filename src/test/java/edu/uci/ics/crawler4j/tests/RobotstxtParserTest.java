package edu.uci.ics.crawler4j.tests;

import edu.uci.ics.crawler4j.robotstxt.HostDirectives;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtParser;
import org.junit.Test;

import static junit.framework.TestCase.*;

/**
 * Created by rz on 06.05.2016.
 */
public class RobotstxtParserTest {


    public static String DEMO_ROBOTS_TXT_CONTENT = "User-agent: *\n" +
            "Disallow: /*.pdf\n" +
            "Disallow: /css/\n" +
            "Disallow: /js/\n" +
            "Disallow: /media/\n" +
            "Disallow: /wildcard/*\n" +
            "\n" +
            "User-agent: notMyAgent\n" +
            "Allow: /js/\n" +
            "\n" +
            "User-agent: myAgent\n" +
            "Disallow: /myAgent/\n" +
            "Disallow: /myAgentWithWildcard/*.ptf\n" +
            "Allow: /myAgent2/\n" +
            "Allow: /media/\n" +
            "Disallow: /*.pxf\n" ;


    @Test
    public void testRobotsTxtParser() {

        HostDirectives directives = RobotstxtParser.parse(DEMO_ROBOTS_TXT_CONTENT, "myAgent");

        assertNotNull(directives);

        //not allowed (explicit match for our user-agent)
        assertFalse(directives.allows("/myAgent/"));

        //allowed (explicit match four our user-agent)
        assertTrue(directives.allows("/myAgent2/"));
        //should be allowed for our user-agent (but is disallowed for all others)
        assertTrue(directives.allows("/media/"));

        //disallowed for all crawlers:
        assertFalse(directives.allows("/css/"));
        assertFalse(directives.allows("/js/"));

        //wildcard expressions
        assertFalse(directives.allows("/wildcard/"));
        assertFalse(directives.allows("/wildcard/1"));
        assertFalse(directives.allows("/wildcard/1/2"));

        // wildcard expression... should be disallowed as well
        assertFalse(directives.allows("/dummy.pdf"));
        assertFalse(directives.allows("/34.pdf"));

        assertFalse(directives.allows("/dummy.pxf"));
        assertFalse(directives.allows("/34.pxf"));

        assertFalse(directives.allows("/Safas/34.pxf"));
        assertFalse(directives.allows("/Safas/34.pdf"));

        //wildcard expression for specific user-agent
        assertFalse(directives.allows("/myAgentWithWildcard/*.ptf"));
        assertFalse(directives.allows("/myAgentWithWildcard/gf.ptf"));

    }

}
