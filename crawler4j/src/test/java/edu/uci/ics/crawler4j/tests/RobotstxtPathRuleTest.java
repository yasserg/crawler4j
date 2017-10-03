package edu.uci.ics.crawler4j.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import edu.uci.ics.crawler4j.robotstxt.PathRule;

public class RobotstxtPathRuleTest {
    @Test
    public void testPattern() {
        assertFalse(PathRule.matchesRobotsPattern("/", "test.txt"));
        assertEquals(false, PathRule.matchesRobotsPattern("/", "test.xml"));

        assertEquals(true, PathRule.matchesRobotsPattern("/", "/test.txt"));
        assertEquals(true, PathRule.matchesRobotsPattern("/", "/test.xml"));
        assertEquals(true, PathRule.matchesRobotsPattern("/", "/any/path/will/do"));

        assertEquals(true, PathRule.matchesRobotsPattern("/*.txt", "/test.txt"));
        assertEquals(false, PathRule.matchesRobotsPattern("/*.txt", "/test.xml"));

        assertEquals(true, PathRule.matchesRobotsPattern("/abc/def", "/abc/def/"));
        assertEquals(true, PathRule.matchesRobotsPattern("/abc/def", "/abc/def/index.htm"));

        assertEquals(true,
                     PathRule.matchesRobotsPattern("/abc/*/favicon.ico", "/abc/site1/favicon.ico"));
        assertEquals(false, PathRule.matchesRobotsPattern("/abc/*/othericon.ico",
                                                          "/abc/site1/favicon.ico"));

        assertEquals(true, PathRule.matchesRobotsPattern("/private*/", "/privateFiles/secret.txt"));
        assertEquals(true,
                     PathRule.matchesRobotsPattern("/private*/", "/private/images/secret.img"));
        assertEquals(false, PathRule.matchesRobotsPattern("/private*/",
                                                          "/public/private/images/secret.img"));

        assertEquals(true, PathRule.matchesRobotsPattern("/*?", "/index.php?query=true"));
        assertEquals(true, PathRule.matchesRobotsPattern("/*?", "/index.php?"));
        assertEquals(false, PathRule.matchesRobotsPattern("/*?", "/index.php"));

        assertEquals(true, PathRule.matchesRobotsPattern("*.txt$", "/some/path/to/file.txt"));
        assertEquals(false, PathRule.matchesRobotsPattern("*.txt$", "/some/path/to/file.xml"));

        assertEquals(true, PathRule.matchesRobotsPattern("/*?$", "/some/weird/path.php?"));
        assertEquals(false,
                     PathRule.matchesRobotsPattern("/*?$", "/some/weird/path.php?query=true"));

        assertEquals(true, PathRule.matchesRobotsPattern("/*?", "/some/weird/path.php?"));
        assertEquals(true, PathRule.matchesRobotsPattern("/*?", "/some/weird/path.php?query=true"));

        assertEquals(true, PathRule.matchesRobotsPattern("/a\\*/escaped/path", "/a*/escaped/path"));
        assertEquals(false,
                     PathRule.matchesRobotsPattern("/a\\*/escaped/path", "/another/escaped/path"));
    }
}
