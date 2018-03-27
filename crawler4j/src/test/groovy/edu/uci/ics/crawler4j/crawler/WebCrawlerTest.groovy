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

class WebCrawlerTest extends Specification {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder()

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(new WireMockConfiguration().dynamicPort())

    static String pageWhichLinksMustNotBeVisited = "page2.html"
    def pageUnvisited = "page4.html"

    def "ignore links contained in given page"() {
        given: "an index page with three links"
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
                        <a href="/some/page1.html">a link</a>
                        <a href="/some/\${pageWhichLinksMustNotBeVisited}">ignore links in this page</a>
                        <a href="/some/page3.html">a link</a> 
                    </body>
                   </html>/$
        )))
        stubFor(get(urlPathMatching("/some/page([1,3,4]*).html"))
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
        stubFor(get(urlPathMatching("/some/${pageWhichLinksMustNotBeVisited}"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody(
                $/<html>
                    <head>
                        <meta charset="UTF-8">
                    </head>
                    <body>
                        <a href="/some/\${pageUnvisited}">should not visit this</a>
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

        controller.start(ShouldNotVisitPageWebCrawler.class, 1)

        then: "links in ${pageWhichLinksMustNotBeVisited} should not be visited"
        verify(exactly(1), getRequestedFor(urlEqualTo("/robots.txt")))
        verify(exactly(1), getRequestedFor(urlEqualTo("/some/page1.html")))
        verify(exactly(1), getRequestedFor(urlEqualTo("/some/${pageWhichLinksMustNotBeVisited}")))
        verify(exactly(1), getRequestedFor(urlEqualTo("/some/page3.html")))
        verify(exactly(0), getRequestedFor(urlEqualTo("/some/${pageUnvisited}")))
    }
}

class ShouldNotVisitPageWebCrawler extends WebCrawler {

    @Override
    boolean shouldFollowLinksIn(WebURL url) {
        if ( url.path.endsWith(WebCrawlerTest.pageWhichLinksMustNotBeVisited) ) return false
        return true
    }
}
