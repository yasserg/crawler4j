package edu.uci.ics.crawler4j.crawler

import static com.github.tomakehurst.wiremock.client.WireMock.*

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import com.github.tomakehurst.wiremock.junit.WireMockRule

import edu.uci.ics.crawler4j.CrawlerConfiguration
import edu.uci.ics.crawler4j.crawler.controller.*
import edu.uci.ics.crawler4j.crawler.fetcher.PageFetcher
import edu.uci.ics.crawler4j.frontier.Frontier
import edu.uci.ics.crawler4j.frontier.pageharvests.PageHarvests
import edu.uci.ics.crawler4j.parser.Parser
import edu.uci.ics.crawler4j.robotstxt.*
import edu.uci.ics.crawler4j.tests.TestCrawlerConfiguration
import spock.lang.Specification

class NoIndexTest extends Specification {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder()

    @Rule
    public WireMockRule wireMockRule = new WireMockRule()

    def "ignore noindex pages"() {
        given: "an index page with two links"
        stubFor(get(urlEqualTo("/some/index.html"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody(
                $/<html>
                    <body> 
                        <a href="/some/page1.html">link to normal page</a>
                        <a href="/some/page2.html">link to noindex page</a>
                    </body>
                   </html>/$
                )))
        stubFor(get(urlPathMatching("/some/page1.html"))
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
                      <meta name="robots" content="noindex, nofollow">
                    </head>
                    <body>
                        <p>This is a paragraph.</p>
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
        CrawlController controller = new DefaultCrawlController(config, NoIndexWebCrawler.class)
        controller.addSeed "http://localhost:8080/some/index.html"
        controller.start()

        then: "noindex pages should be ignored"
        Map<String, Page> visitedPages = (Map<String, Page>)controller.crawlerData.get(0);
        visitedPages.containsKey("http://localhost:8080/some/index.html")
        visitedPages.containsKey("http://localhost:8080/some/page1.html")
        !visitedPages.containsKey("http://localhost:8080/some/page2.html")
    }
}

class NoIndexWebCrawler extends DefaultWebCrawler {

    private Map<String, Page> visitedPages = new HashMap<>()

    NoIndexWebCrawler(Integer id, CrawlerConfiguration configuration,
    CrawlController controller, PageFetcher pageFetcher, RobotstxtServer robotstxtServer,
    PageHarvests pageHarvests, Frontier frontier, Parser parser){
        super(id, configuration,controller, pageFetcher, robotstxtServer,
        pageHarvests, frontier, parser);
    }

    @Override
    void visit(Page page) {
        visitedPages.put(page.getWebURL().toString(), page)
    }

    @Override
    Object getData() {
        return visitedPages;
    }
}
