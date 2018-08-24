package edu.uci.ics.crawler4j.crawler

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import edu.uci.ics.crawler4j.fetcher.PageFetcher
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer
import edu.uci.ics.crawler4j.url.WebURL
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*

class NoFollowTest extends Specification {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder()

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(new WireMockConfiguration().dynamicPort())

    def "ignore nofollow links"() {
        given: "an index page with two links"
        stubFor(get(urlEqualTo("/some/index.html"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody(
                $/<html>
                    <head>
                      <meta charset="UTF-8">
                    </head>
                    <body> 
                        <a href="/some/page1.html" rel="nofollow">should not visit this</a>
                        <a href="/some/page2.html">link to a nofollow page</a>
                    </body>
                   </html>/$
        )))
        stubFor(get(urlPathMatching("/some/page(1|3).html"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody(
                $/<html>
                    <head>
                      <meta charset="UTF-8">
                    </head>
                    <body>
                        <h1>title</h1>
                    </body>
                  </html>/$)))
        stubFor(get(urlPathMatching("/some/page2.html"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody(
                $/<html>
                    <head>
                      <meta charset="UTF-8">
                      <meta name="robots" content="nofollow">
                    </head>
                    <body>
                        <a href="/some/page3.html">should not visit this</a>
                    </body>
                  </html>/$)))

        and: "an allow everything robots.txt"
        stubFor(get(urlPathMatching("/robots.txt"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody(
                $/User-agent: * 
                  Allow: /
                /$)))

        when:
        CrawlConfig config = new CrawlConfig(
                crawlStorageFolder: temp.getRoot().getAbsolutePath()
                , politenessDelay: 100
                , maxConnectionsPerHost: 1
                , threadShutdownDelaySeconds: 1
                , threadMonitoringDelaySeconds: 1
                , cleanupDelaySeconds: 1
        )

        PageFetcher pageFetcher = new PageFetcher(config)
        RobotstxtServer robotstxtServer = new RobotstxtServer(new RobotstxtConfig(), pageFetcher)
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer)
        controller.addSeed "http://localhost:" + wireMockRule.port() + "/some/index.html"

        controller.start(WebCrawler.class, 1)

        then: "nofollow links should not be visited"
        verify(exactly(1), getRequestedFor(urlEqualTo("/robots.txt")))
        verify(exactly(0), getRequestedFor(urlEqualTo("/some/page1.html")))
        verify(exactly(1), getRequestedFor(urlEqualTo("/some/page2.html")))
        verify(exactly(0), getRequestedFor(urlEqualTo("/some/page3.html")))
    }
}
