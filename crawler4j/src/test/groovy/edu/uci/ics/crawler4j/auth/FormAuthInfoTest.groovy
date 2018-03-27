package edu.uci.ics.crawler4j.auth

import com.github.tomakehurst.wiremock.client.BasicCredentials
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import edu.uci.ics.crawler4j.crawler.CrawlConfig
import edu.uci.ics.crawler4j.crawler.CrawlController
import edu.uci.ics.crawler4j.crawler.WebCrawler
import edu.uci.ics.crawler4j.crawler.authentication.BasicAuthInfo
import edu.uci.ics.crawler4j.crawler.authentication.FormAuthInfo
import edu.uci.ics.crawler4j.fetcher.PageFetcher
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer
import org.apache.http.client.CookieStore
import org.apache.http.client.config.CookieSpecs
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.conn.InMemoryDnsResolver
import org.junit.Assert
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.spockframework.util.Matchers
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.junit.Assert.assertThat

class FormAuthInfoTest extends Specification {

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
                .withHeader("Host", "localhost")
                .withBody(
                $/<html>
                    <head>
                        <meta charset="UTF-8">
                        <title>Home page</title>
                    </head>
                    <body>
                        <h1>Title</h1>
                        <a href="/login.php">Login</a>
                        <a href="/profile.php">Profile</a>
                    </body>
                   </html>/$
        )))

        stubFor(get(urlEqualTo("/login.php"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withHeader("Host", "localhost")
                .withBody(
                $/<html>
                    <head>
                        <meta charset="UTF-8">
                        <title>Landing page</title>
                    </head>
                    <body>
                        <h1>Title</h1>
                        <form action="/login.php" method="POST">
                            <input type="text" title="username" placeholder="username" />
                            <input type="password" title="password" placeholder="password" />
                            <button type="submit" class="btn">Login</button>
                        </form>
                    </body>
                   </html>/$
        )))
        stubFor(post(urlEqualTo("/login.php"))
                .willReturn(
                aResponse()
                .withStatus(200)
                .withHeader("Set-Cookie", "secret=hash; Path=/")
                .withHeader("Host", "localhost")
        ))
        stubFor(get(urlEqualTo("/profile.php"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withHeader("Host", "localhost")
                .withHeader("Cookie", "secret=hash")
                .withBody(
                $/<html>
                    <head>
                        <meta charset="UTF-8">
                        <title>Profile page</title>
                    </head>
                    <body>
                        <h1>Title</h1>
                    </body>
                   </html>/$
        )))

        CrawlConfig c = new CrawlConfig()
        c.setCrawlStorageFolder(temp.newFolder().getAbsolutePath())
        c.setMaxPagesToFetch 10
        c.setPolitenessDelay 150
        FormAuthInfo formAuthInfo = new FormAuthInfo(
                "foofy"
                , "superS3cret"
                , "http://localhost:${wireMockRule.port()}/login.php"
                , "username"
                , "password")
        c.setAuthInfos([formAuthInfo])
        c.setCookieStore(new BasicCookieStore())
        c.setCookiePolicy(CookieSpecs.DEFAULT)

        PageFetcher pageFetcher = new PageFetcher(c)
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig()
        robotstxtConfig.setEnabled false
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher)
        CrawlController controller = new CrawlController(c, pageFetcher, robotstxtServer)

        controller.addSeed("http://localhost:${wireMockRule.port()}/")
        controller.start(WebCrawler.class, 1)

        expect: "POST to credentials"
        verify(exactly(1), getRequestedFor(urlEqualTo("/")))
        verify(exactly(1), getRequestedFor(urlEqualTo("/login.php")))
        verify(exactly(1), postRequestedFor(urlEqualTo("/login.php")))
        verify(exactly(1), getRequestedFor(urlEqualTo("/profile.php"))
            .withCookie("secret", new EqualToPattern("hash"))
        )

        and: "cookie store is like intended"
        Assert.assertEquals 1, c.getCookieStore().getCookies().size()
        Assert.assertEquals "secret", c.getCookieStore().getCookies().get(0).name
        Assert.assertEquals "hash", c.getCookieStore().getCookies().get(0).value
    }

}
