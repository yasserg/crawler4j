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

class TimeoutTest extends Specification {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder()

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(new WireMockConfiguration().dynamicPort())

    def 'intercept socket timeout exception'() {
        given: "an index page with two links will fail to respond in time"
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
        )
                .withFixedDelay(60 * 1_000)

        ))

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
                , connectionTimeout: 10 * 1_000
        )

        PageFetcher pageFetcher = new PageFetcher(config)
        RobotstxtServer robotstxtServer = new RobotstxtServer(new RobotstxtConfig(), pageFetcher)
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer)
        controller.addSeed "http://localhost:" + wireMockRule.port() + "/some/index.html"

        controller.start(VisitAllCrawler.class, 1)


        then:
        Page p = controller.getCrawlersLocalData().get(0)
        assert 0 == p.getStatusCode()
        assert p.getFetchResponseHeaders().length == 0
    }

    def 'response code and header are present when read time out'() {
        given: "an index page with two links very slow to respond"
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
                        <title>Hello, world!</title>
                    </body>
                   </html>/$
                )
                .withChunkedDribbleDelay(15, 30 * 1_000)

        ))

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
                , connectionTimeout: 20 * 1_000
        )

        PageFetcher pageFetcher = new PageFetcher(config)
        RobotstxtServer robotstxtServer = new RobotstxtServer(new RobotstxtConfig(), pageFetcher)
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer)
        controller.addSeed "http://localhost:" + wireMockRule.port() + "/some/index.html"

        controller.start(VisitAllCrawler.class, 1)


        then:
        Page p = controller.getCrawlersLocalData().get(0)
        assert 200 == p.getStatusCode()
        assert p.getFetchResponseHeaders().length > 1
    }
}

class VisitAllCrawler extends WebCrawler {

    private page

    @Override
    boolean shouldFollowLinksIn(WebURL url) {
        return true
    }

    @Override
    protected void onContentFetchError(Page page) {
        this.page = page;
    }

    @Override
    Object getMyLocalData() {
        return page
    }

}
