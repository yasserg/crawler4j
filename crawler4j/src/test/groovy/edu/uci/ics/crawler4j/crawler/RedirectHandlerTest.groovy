/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

class RedirectHandlerTest extends Specification {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder()

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(new WireMockConfiguration().dynamicPort())

    def "follow redirects"(int redirectStatus) {
        given: "an index page with a ${redirectStatus}"
        stubFor(get(urlEqualTo("/some/index.html"))
                .willReturn(aResponse()
                .withStatus(redirectStatus)
                .withHeader("Location", "/another/index.html")))

        stubFor(get(urlPathMatching("/another/index.html"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withBody(
                $/<html>
                    <head>
                        <meta charset="UTF-8">
                    </head>
                    <body>
                        <h1>Redirected here.</h1>
                    </body>
                  </html>/$)))

        when:
        CrawlConfig config = new CrawlConfig(
                crawlStorageFolder: temp.getRoot().getAbsolutePath()
                , politenessDelay: 100
                , maxConnectionsPerHost: 1
                , threadShutdownDelaySeconds: 1
                , threadMonitoringDelaySeconds: 1
                , cleanupDelaySeconds: 1
        )

        and: "and allow everything robots.txt"
        stubFor(get(urlPathMatching("/robots.txt"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody(
                $/User-agent: * 
                  Allow: /
                /$)))

        PageFetcher pageFetcher = new PageFetcher(config)
        RobotstxtServer robotstxtServer = new RobotstxtServer(new RobotstxtConfig(), pageFetcher)
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer)
        controller.addSeed "http://localhost:" + wireMockRule.port() + "/some/index.html"

        controller.start(HandleRedirectWebCrawler.class, 1)

        then: "envent in WebCrawler will trigger"
        List<Object> crawlerData = controller.getCrawlersLocalData().get(0)
        assert crawlerData.get(0) == 1
        assert crawlerData.get(1) == "http://localhost:" + wireMockRule.port() + "/another/index.html"

        verify(exactly(1), getRequestedFor(urlEqualTo("/some/index.html")))
        verify(exactly(1), getRequestedFor(urlEqualTo("/another/index.html")))

        where:
        redirectStatus | _
        301 | _
        302 | _
    }
}


class HandleRedirectWebCrawler extends WebCrawler {

    int onRedirectedCounter = 0
    List<Object> data = []

    @Override
    void onRedirectedStatusCode(Page page) {
        data.add(0, ++onRedirectedCounter)
        data.add(1, page.getRedirectedToUrl())
    }

    @Override
    Object getMyLocalData() {
        return data
    }
}

