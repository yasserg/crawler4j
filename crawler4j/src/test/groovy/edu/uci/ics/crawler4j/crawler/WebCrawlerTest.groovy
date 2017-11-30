package edu.uci.ics.crawler4j.crawler

import static com.github.tomakehurst.wiremock.client.WireMock.*

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import com.github.tomakehurst.wiremock.junit.WireMockRule

import edu.uci.ics.crawler4j.CrawlerConfiguration
import edu.uci.ics.crawler4j.crawler.controller.*
import edu.uci.ics.crawler4j.robotstxt.*
import edu.uci.ics.crawler4j.tests.TestCrawlerConfiguration
import edu.uci.ics.crawler4j.url.WebURL
import spock.lang.Specification

class WebCrawlerTest extends Specification {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder()

    @Rule
    public WireMockRule wireMockRule = new WireMockRule()

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
        CrawlerConfiguration config = new TestCrawlerConfiguration(temp)
        CrawlController controller = new DefaultCrawlController(config, ShouldNotVisitPageWebCrawler.class)
        controller.addSeed "http://localhost:8080/some/index.html"

        controller.start()

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
