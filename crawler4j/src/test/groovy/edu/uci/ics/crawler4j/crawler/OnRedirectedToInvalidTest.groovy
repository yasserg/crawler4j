package edu.uci.ics.crawler4j.crawler

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import edu.uci.ics.crawler4j.fetcher.PageFetcher
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*

class OnRedirectedToInvalidTest extends Specification {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder()

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(new WireMockConfiguration().dynamicPort())


    def "intercept redirect to invalid url"() {
        given: "an index page with links to a redirect"

        String redirectToNothing = "asd://-invalid-/varybadlocation"

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
                        <a href="/some/redirect.html">link to a redirected page to nothing</a>
                    </body>
                   </html>/$
        )))

        when: "the redirect point to an invalid url"
        stubFor(get(urlPathMatching("/some/redirect.html"))
            .willReturn(aResponse()
            .withStatus(redirectHttpCode)
            .withHeader("Content-Type", "text/html")
            .withHeader("Location", redirectToNothing)
            .withBody(
            $/<html>
                <head>
                    <title>Moved</title>
                </head>
                <body>
                    <h1>Moved</h1>
                    <p>This page has moved to <a href="${redirectToNothing}">Some invalid location</a>.</p>
                </body>
                </html>/$)))

        and:
        CrawlConfig config = new CrawlConfig(
            crawlStorageFolder: temp.getRoot().getAbsolutePath()
            , politenessDelay: 100
            , maxConnectionsPerHost: 1
            , threadShutdownDelaySeconds: 1
            , threadMonitoringDelaySeconds: 1
            , cleanupDelaySeconds: 1
        )

        PageFetcher pageFetcher = new PageFetcher(config)
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig()
        robotstxtConfig.setEnabled false
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher)
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer)
        controller.addSeed "http://localhost:" + wireMockRule.port() + "/some/index.html"

        HandleInvalidRedirectWebCrawler crawler = new HandleInvalidRedirectWebCrawler()
        controller.start(crawler)

        then: "the right event must triggered"
        crawler.invalidLocation.equals("/some/redirect.html")

        where:
        redirectHttpCode | _
        300 | _
        301 | _
        302 | _
        303 | _
        307 | _
        308 | _
    }
}

class HandleInvalidRedirectWebCrawler extends WebCrawler {

    String invalidLocation

    @Override
    void onRedirectedToInvalidUrl(Page page) {
        super.onRedirectedToInvalidUrl(page)
        invalidLocation = page.getWebURL().getPath()
    }

}
