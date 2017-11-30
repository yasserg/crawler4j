package edu.uci.ics.crawler4j.config

import static com.github.tomakehurst.wiremock.client.WireMock.*

import org.apache.http.impl.conn.InMemoryDnsResolver
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import com.github.tomakehurst.wiremock.junit.WireMockRule

import edu.uci.ics.crawler4j.*
import edu.uci.ics.crawler4j.crawler.*
import edu.uci.ics.crawler4j.crawler.controller.*
import edu.uci.ics.crawler4j.robotstxt.*
import edu.uci.ics.crawler4j.tests.NoObeyRobotsCrawlController
import spock.lang.Specification

class CustomDnsResolverTest extends Specification {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder()

    @Rule
    public WireMockRule wireMockRule = new WireMockRule()


    def "visit javascript files"() {
        given: "an index page"
        stubFor(get(urlEqualTo("/some/index.html"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody(
                $/<html>
                    <head>
                        <title>Hello, world!</title>
                    </head>
                    <body> 
                        <h1>Title</h1>
                    </body>
                   </html>/$
                )))

        when:
        InMemoryDnsResolver inMemDnsResolver = new InMemoryDnsResolver()
        inMemDnsResolver.add("googhle.com", InetAddress.getByName("127.0.0.1"))

        CrawlerConfiguration config = new CrawlerConfiguration(new SleepyCatCrawlPersistentConfiguration())
        config.crawlPersistentConfiguration.storageFolder = temp.newFolder().getAbsolutePath()
        config.numberOfCrawlers = 1
        config.maxPagesToFetch = 10
        config.politenessDelay = 1000
        config.dnsResolver = inMemDnsResolver

        try {
            CrawlController controller = new NoObeyRobotsCrawlController(config)
            controller.addSeed("http://googhle.com:8080/some/index.html")
            controller.start()
        } catch (Exception e) {
            throw new RuntimeException(e)
        }

        then:
        verify(exactly(1), getRequestedFor(urlEqualTo("/some/index.html")))
    }
}
