package edu.uci.ics.crawler4j.crawler

import static com.github.tomakehurst.wiremock.client.WireMock.*

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import com.github.tomakehurst.wiremock.junit.WireMockRule

import edu.uci.ics.crawler4j.CrawlerConfiguration
import edu.uci.ics.crawler4j.crawler.controller.*
import edu.uci.ics.crawler4j.robotstxt.*
import edu.uci.ics.crawler4j.tests.TestCrawlerConfiguration
import spock.lang.Specification

class NoFollowTest extends Specification {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder()

    @Rule
    public WireMockRule wireMockRule = new WireMockRule()

    def "ignore nofollow links"() {
        given: "an index page with two links"
        stubFor(get(urlEqualTo("/some/index.html"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody(
                $/<html>
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
        CrawlerConfiguration config = new TestCrawlerConfiguration(temp)
        CrawlController controller = new DefaultCrawlController(config)
        controller.addSeed "http://localhost:8080/some/index.html"
        controller.start()

        then: "nofollow links should not be visited"
        verify(exactly(1), getRequestedFor(urlEqualTo("/robots.txt")))
        verify(exactly(0), getRequestedFor(urlEqualTo("/some/page1.html")))
        verify(exactly(1), getRequestedFor(urlEqualTo("/some/page2.html")))
        verify(exactly(0), getRequestedFor(urlEqualTo("/some/page3.html")))
    }
}
