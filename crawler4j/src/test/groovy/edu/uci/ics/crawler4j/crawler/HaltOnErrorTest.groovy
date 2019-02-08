/*
 * Copyright 2018 Paul Galbraith <paul.d.galbraith@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.junit.WireMockRule
import edu.uci.ics.crawler4j.fetcher.PageFetcher
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static edu.uci.ics.crawler4j.crawler.CrawlController.WebCrawlerFactory

/**
 * @author Paul Galbraith <paul.d.galbraith@gmail.com>
 *
 */
class HaltOnErrorTest extends Specification {
    
    @Shared numThreads = 10
    @Shared activeThreads = Collections.synchronizedSet(new HashSet())
    @Shared transformer = new ResponseTransformer() {
        def page = 0
        String getName() { return "infinite-website" }
        Response transform(Request request, Response response, FileSource files, Parameters parameters) {
            return Response.Builder.like(response).but().body(
                "Link 1: http://localhost:8080/page/" + page++ + "\n" +
                "Link 2: http://localhost:8080/page/" + page++ + "\n"
            ).build()
        }
    }
        
    @Rule TemporaryFolder folder = new TemporaryFolder()
    @Rule WireMockRule wireMock = new WireMockRule(new WireMockConfiguration().extensions(transformer))
    
    def "multiple threads start and run until an exception is thrown"() {
        
        given: "basic crawler setup that throws exception once all the threads have started up"
        def config = new CrawlConfig(crawlStorageFolder: folder.root, haltOnError: true, allowSingleLevelDomain: true, politenessDelay: 0)
        def fetcher = new PageFetcher(config)
        def robots = new RobotstxtServer(new RobotstxtConfig(enabled: false), fetcher)
        def controller = new CrawlController(config, fetcher, robots)
        def aborted = false
        def factory = new WebCrawlerFactory() {
            WebCrawler newInstance() {
                return new WebCrawler() {
                    void visit(Page page) {
                        activeThreads.add(thread)
                        logger.info(page.webURL.url)
                        if (activeThreads.size() == numThreads) {
                            synchronized(this) {
                                if (!aborted) {
                                    aborted = true
                                    throw new RuntimeException("aborting since all threads have started")
                                }
                            }
                        }
                    }
                }
            }
        }
            
        and: "mock website with infinite pages"
        givenThat(get(urlMatching("/.*")).willReturn(aResponse().withHeader("Content-Type", "text/plain; charset=utf-8")))

        when: "start crawl task and timeout after 30 seconds if not complete"
        def executorService = Executors.newSingleThreadExecutor()
        executorService.submit(new Runnable() {
            void run() {
                controller.addSeed("http://localhost:8080/page/0")
                controller.start(factory, numThreads)
            }
        })
        executorService.shutdown()
        def timedOut = !executorService.awaitTermination(30, TimeUnit.SECONDS)

        then: "did not timeout"
        assert(!timedOut)
            
    }

}
