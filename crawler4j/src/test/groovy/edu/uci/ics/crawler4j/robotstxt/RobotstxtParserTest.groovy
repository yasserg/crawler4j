package edu.uci.ics.crawler4j.robotstxt;

import com.github.tomakehurst.wiremock.junit.WireMockRule
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig
import edu.uci.ics.crawler4j.robotstxt.RobotstxtParser
import org.apache.commons.io.FileUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class RobotstxtParserTest extends Specification {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder()

    @Rule
    public WireMockRule wireMockRule = new WireMockRule()

    def "no null expection when robots.txt ends with User-agent"() {
        given: "a robots.txt that ends wit User-agent"
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig()
        String body = FileUtils.readFileToString(new File(
                RobotstxtParserTest.class.getClassLoader().getResource("robotstxt/he.wikipedia.org_robots.txt").getFile()), "UTF-8")

        when:
        RobotstxtParser.parse(body, robotstxtConfig);

        then:
        notThrown(NullPointerException.class)
    }
}
