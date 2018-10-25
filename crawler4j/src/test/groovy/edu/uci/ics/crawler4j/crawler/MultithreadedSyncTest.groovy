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

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static edu.uci.ics.crawler4j.crawler.CrawlController.WebCrawlerFactory

import spock.lang.*

import org.junit.*
import org.junit.rules.*

import com.github.tomakehurst.wiremock.common.*
import com.github.tomakehurst.wiremock.core.*
import com.github.tomakehurst.wiremock.extension.*
import com.github.tomakehurst.wiremock.http.*
import com.github.tomakehurst.wiremock.junit.*

import edu.uci.ics.crawler4j.fetcher.*
import edu.uci.ics.crawler4j.robotstxt.*

/**
 * @author Paul Galbraith <paul.d.galbraith@gmail.com>
 *
 */
class MultithreadedSyncTest extends Specification {
    
    @Shared numThreads = 10
    @Shared startedThreads = Collections.synchronizedSet(new HashSet())
    @Shared initializingThreads = Collections.synchronizedSet(new HashSet())
    @Shared participatedThreads = Collections.synchronizedSet(new HashSet())
    @Shared transformer = new ResponseTransformer() {
        def page = 0
        public String getName() { return "infinite-website" }
        public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
            if (startedThreads.size() < numThreads || initializingThreads.size() > 0) {
                return Response.Builder.like(response).but().body(
                    "Link 1: http://localhost:8080/page/" + page++ + "\n" +
                    "Link 2: http://localhost:8080/page/" + page++ + "\n"
                ).build()
            } else {
                return Response.Builder.like(response).but().body("all thread initialized\n").build()
            }
        }
    }
        
    @Rule TemporaryFolder folder = new TemporaryFolder()
    @Rule WireMockRule wireMock = new WireMockRule(new WireMockConfiguration().extensions(transformer))
    
    def "multiple threads start and run to completion"() {
        
        given: "basic crawler setup that throws all unexpected exceptions (haltOnError)"
            def config = new CrawlConfig(crawlStorageFolder: folder.root, haltOnError: true, allowSingleLevelDomain: true, politenessDelay: 0)
            def fetcher = new PageFetcher(config)
            def robots = new RobotstxtServer(new RobotstxtConfig(enabled: false), fetcher)
            def controller = new CrawlController(config, fetcher, robots)
            def factory = new WebCrawlerFactory() {
                @Override public WebCrawler newInstance() {
                    return new WebCrawler() {
                        @Override public void onStart() { 
                            startedThreads.add(thread) 
                            initializingThreads.add(thread) 
                        }
                        @Override public void visit(Page page) { 
                            initializingThreads.remove(thread)
                            participatedThreads.add(thread)
                            logger.info(page.webURL.url)
                        }
                        @Override public void onBeforeExit() {
                            initializingThreads.remove(thread)
                        }
                    }
                }
            }
            
        and: "mock website that runs out of pages once all threads are running"
            givenThat(get(urlMatching("/.*")).willReturn(aResponse().withHeader("Content-Type", "text/plain; charset=utf-8")))
            
        when: "start crawl and wait until finished"
            controller.addSeed("http://localhost:8080/page/0")
            controller.start(factory, numThreads)
        
        then: "all threads participated"
            // make sure threads didn't just start and immediately finish without participating
            assert(participatedThreads.size() == numThreads)
            
    }

}
