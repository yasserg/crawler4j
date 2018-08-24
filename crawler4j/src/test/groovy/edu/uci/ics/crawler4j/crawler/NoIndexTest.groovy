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

class NoIndexTest extends Specification {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder()

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(new WireMockConfiguration().dynamicPort())

    def "ignore noindex pages"() {
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

        controller.start(NoIndexWebCrawler.class, 1)

        then: "noindex pages should be ignored"
        Map<String, Page> visitedPages = (Map<String, Page>)controller.getCrawlersLocalData().get(0);
        visitedPages.containsKey("http://localhost:" + wireMockRule.port() + "/some/index.html")
        visitedPages.containsKey("http://localhost:" + wireMockRule.port() + "/some/page1.html")
        !visitedPages.containsKey("http://localhost:" + wireMockRule.port() + "/some/page2.html")
    }
 }
    
class NoIndexWebCrawler extends WebCrawler {

	private Map<String, Page> visitedPages;
	
	public void init(int id, CrawlController crawlController) {
		super.init(id, crawlController);
		visitedPages = new HashMap<String, Page>();
	}

	public void visit(Page page) {
		visitedPages.put(page.getWebURL().toString(), page);
	}
	
	public Object getMyLocalData() {
		return visitedPages;
	}
}
