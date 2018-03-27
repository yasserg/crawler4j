/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

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

class CrawlerWithJSTest extends Specification {
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
                        <script src="/js/app.js"></script>
                    </head>
                    <body> 
                        <a href="/some/page1.html">a link</a>
                        <a href="/some/page2.html">a link</a>
                    </body>
                   </html>/$
        )))

        and: "a page with js in the head tag"
        stubFor(get(urlPathMatching("/some/page1.html"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody(
                $/<html>
                    <head>
                        <meta charset="UTF-8">
                        <script src="/js/app.js"></script>
                    </head> 
                    <body>
                        <h1>title</h1>
                    </body>
                  </html>/$)))

        and: "a page with js in the head tag and a script src in the body"
        stubFor(get(urlPathMatching("/some/page2.html"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody(
                $/<html>
                    <head>
                        <meta charset="UTF-8">
                        <script src="/js/app.js"></script>
                    </head> 
                    <body>
                        <h1>title</h1>
                        <script src="/js/module1.js"></script>
                    </body>
                  </html>/$)))
        stubFor(get(urlPathMatching("/js/app.js"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/javascript")
                .withBody(
                $/
                    function greetings() {
                        alert('Hello, world!');
                    }

                    greetings();
                /$)))
        stubFor(get(urlPathMatching("/js/module1.js"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/javascript")
                .withBody(
                $/
                    // This is the source of the module
                /$)))

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

        controller.start(ShouldWebCrawler.class, 1)

        then: "java script files must be visited"
        verify(exactly(1), getRequestedFor(urlEqualTo("/robots.txt")))
        verify(exactly(1), getRequestedFor(urlEqualTo("/js/app.js")))
        verify(exactly(1), getRequestedFor(urlEqualTo("/js/module1.js")))
    }
}

class ShouldWebCrawler extends WebCrawler {

}
