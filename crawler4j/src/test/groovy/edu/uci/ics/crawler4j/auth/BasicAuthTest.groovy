package edu.uci.ics.crawler4j.auth

import com.github.tomakehurst.wiremock.client.BasicCredentials
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import edu.uci.ics.crawler4j.crawler.CrawlConfig
import edu.uci.ics.crawler4j.crawler.CrawlController
import edu.uci.ics.crawler4j.crawler.WebCrawler
import edu.uci.ics.crawler4j.crawler.authentication.BasicAuthInfo
import edu.uci.ics.crawler4j.fetcher.PageFetcher
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer
import org.apache.http.impl.conn.InMemoryDnsResolver
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*

class BasicAuthTest extends Specification {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder()

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(new WireMockConfiguration().dynamicPort())

    def "http basic auth"() {
        given: "two pages on first.com behind basic auth"
        stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withHeader("Host", "first.com")
                .withBody(
                $/<html>
                    <head>
                        <meta charset="UTF-8">
                        <title>Landing page</title>
                    </head>
                    <body> 
                        <h1>Title</h1>
                        <a href="/some/index.html">a link!</a>
                    </body>
                   </html>/$
        )))
        stubFor(get(urlEqualTo("/some/index.html"))
                .withBasicAuth("user", "pass")
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withHeader("Host", "first.com")
                .withBody(
                $/<html>
                    <head>
                        <meta charset="UTF-8">
                        <title>Hello, world!</title>
                    </head>
                    <body> 
                        <h1>Sub page</h1>
                    </body>
                   </html>/$
        )))

        and: "two pages on second.com are not with basic auth"
        stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withHeader("Host", "first.com")
                .withBody(
                $/<html>
                    <head>
                        <meta charset="UTF-8">
                        <title>Landing page</title>
                    </head>
                    <body> 
                        <h1>Title</h1>
                        <a href="/some/index.html">a link!</a>
                    </body>
                   </html>/$
        )))
        stubFor(get(urlEqualTo("/some/index.html"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withHeader("Host", "first.com")
                .withBody(
                $/<html>
                    <head>
                        <meta charset="UTF-8">
                        <title>Hello, world!</title>
                    </head>
                    <body> 
                        <h1>Sub page</h1>
                    </body>
                   </html>/$
        )))

        when: "just resolve first.com and second.com to localhost"
        final InMemoryDnsResolver inMemDnsResolver = new InMemoryDnsResolver()
        inMemDnsResolver.add("first.com"
                , InetAddress.getByName("127.0.0.1"))
        inMemDnsResolver.add("second.com"
                , InetAddress.getByName("127.0.0.1"))


        CrawlConfig config = new CrawlConfig()
        config.setCrawlStorageFolder(temp.newFolder().getAbsolutePath())
        config.setMaxPagesToFetch 10
        config.setPolitenessDelay 500
        BasicAuthInfo basicAuthInfo = new BasicAuthInfo(
                "user", "pass",
                "http://first.com:${wireMockRule.port()}/"
        )
        config.setDnsResolver(inMemDnsResolver)
        config.setAuthInfos([basicAuthInfo])

        PageFetcher pageFetcher = new PageFetcher(config)
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig()
        robotstxtConfig.setEnabled false
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher)
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer)

        controller.addSeed("http://first.com:${wireMockRule.port()}/")
        controller.addSeed("http://second.com:${wireMockRule.port()}/")
        controller.start(WebCrawler.class, 1)


        then: "first.com will receive credentials"
        verify(exactly(1), getRequestedFor(urlEqualTo("/some/index.html"))
                .withBasicAuth(new BasicCredentials("user", "pass"))
                .withHeader("Host", new EqualToPattern( "first.com:${wireMockRule.port()}"))
        )
        verify(exactly(1), getRequestedFor(urlEqualTo("/"))
                .withBasicAuth(new BasicCredentials("user", "pass"))
                .withHeader("Host", new EqualToPattern( "first.com:${wireMockRule.port()}"))
        )

        and: "second.com won't see secrets"
        verify(exactly(1), getRequestedFor(urlEqualTo("/some/index.html"))
                .withHeader("Host", new EqualToPattern( "second.com:${wireMockRule.port()}"))
        )
        verify(exactly(1), getRequestedFor(urlEqualTo("/"))
                .withHeader("Host", new EqualToPattern( "second.com:${wireMockRule.port()}"))
        )
    }

}
