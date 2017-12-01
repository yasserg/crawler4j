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

import static com.github.tomakehurst.wiremock.client.WireMock.*

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import com.github.tomakehurst.wiremock.junit.WireMockRule

import edu.uci.ics.crawler4j.CrawlerConfiguration
import edu.uci.ics.crawler4j.crawler.controller.*
import edu.uci.ics.crawler4j.fetcher.PageFetcher
import edu.uci.ics.crawler4j.frontier.Frontier
import edu.uci.ics.crawler4j.frontier.pageharvests.PageHarvests
import edu.uci.ics.crawler4j.parser.Parser
import edu.uci.ics.crawler4j.robotstxt.*
import edu.uci.ics.crawler4j.tests.TestCrawlerConfiguration
import spock.lang.Specification

class RedirectHandlerTest extends Specification {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder()

    @Rule
    public WireMockRule wireMockRule = new WireMockRule()

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
                    <body>
                        <h1>Redirected here.</h1>
                    </body>
                  </html>/$)))

        when:
        CrawlerConfiguration config = new TestCrawlerConfiguration(temp)

        and: "and allow everything robots.txt"
        stubFor(get(urlPathMatching("/robots.txt"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody(
                $/User-agent: * 
                  Allow: /
                /$)))

        CrawlController controller = new DefaultCrawlController(config, HandleRedirectWebCrawler.class)
        controller.addSeed "http://localhost:8080/some/index.html"
        controller.start()

        then: "envent in WebCrawler will trigger"
        List<Object> crawlerData = controller.crawlerData.get(0)
        assert crawlerData.get(0) == 1
        assert crawlerData.get(1) == "http://localhost:8080/another/index.html"

        verify(exactly(1), getRequestedFor(urlEqualTo("/some/index.html")))
        verify(exactly(1), getRequestedFor(urlEqualTo("/another/index.html")))

        where:
        redirectStatus | _
        301 | _
        302 | _
    }
}


class HandleRedirectWebCrawler extends DefaultWebCrawler {

    int onRedirectedCounter = 0
    List<Object> data = []

    HandleRedirectWebCrawler(Integer id, CrawlerConfiguration configuration,
    CrawlController controller, PageFetcher pageFetcher, RobotstxtServer robotstxtServer,
    PageHarvests pageHarvests, Frontier frontier, Parser parser){
        super(id, configuration,controller, pageFetcher, robotstxtServer,
        pageHarvests, frontier, parser);
    }

    @Override
    void onRedirectedStatusCode(Page page) {
        data.add(0, ++onRedirectedCounter)
        data.add(1, page.getRedirectedToUrl())
    }

    @Override
    Object getData() {
        return data
    }
}

