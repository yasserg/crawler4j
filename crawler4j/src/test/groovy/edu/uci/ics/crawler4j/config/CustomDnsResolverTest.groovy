package edu.uci.ics.crawler4j.config

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import edu.uci.ics.crawler4j.crawler.CrawlConfig
import edu.uci.ics.crawler4j.crawler.CrawlController
import edu.uci.ics.crawler4j.crawler.WebCrawler
import edu.uci.ics.crawler4j.fetcher.PageFetcher
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer
import org.apache.http.impl.conn.InMemoryDnsResolver
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*

class CustomDnsResolverTest extends Specification {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder()

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(new WireMockConfiguration().dynamicPort())


    def "visit javascript files"() {
        given: "an index page"
        stubFor(get(urlEqualTo("/some/index.html"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody(
                $/<html>
                    <head>
                        <meta charset="UTF-8">
                        <title>Hello, world!</title>
                    </head>
                    <body> 
                        <h1>Title</h1>
                    </body>
                   </html>/$
        )))

        when:
        final InMemoryDnsResolver inMemDnsResolver = new InMemoryDnsResolver()
        inMemDnsResolver.add("googhle.com"
                , InetAddress.getByName("127.0.0.1"))

        CrawlConfig config = new CrawlConfig()
        config.setCrawlStorageFolder(temp.newFolder().getAbsolutePath())
        config.setMaxPagesToFetch(10)
        config.setPolitenessDelay(1000)
        config.setDnsResolver(inMemDnsResolver)

        PageFetcher pageFetcher = new PageFetcher(config)
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig()
        robotstxtConfig.setEnabled false
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher)
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer)

        controller.addSeed("http://googhle.com:" + wireMockRule.port() + "/some/index.html")
        controller.start(WebCrawler.class, 1)


        then:
        verify(exactly(1), getRequestedFor(urlEqualTo("/some/index.html")))
    }

}
