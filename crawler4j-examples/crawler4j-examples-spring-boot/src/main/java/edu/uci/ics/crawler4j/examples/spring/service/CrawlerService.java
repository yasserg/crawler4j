/*
 * Copyright 2018 Federico Tolomei <mail@s17t.net>
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

package edu.uci.ics.crawler4j.examples.spring.service;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.examples.spring.model.CrawlerRequestModel;
import edu.uci.ics.crawler4j.examples.spring.repo.CrawlerRequestRepository;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
public class CrawlerService {

    @Autowired
    CrawlConfig config;

    @Autowired
    CrawlerRequestRepository crawlerRequestRepository;

    @Autowired
    ModelMapper modelMapper;

    @Async
    public void crawl(CrawlerRequestModel request, int numCrawlers) throws Exception {
        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);

        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        // If you want ignore robots.txt
        robotstxtConfig.setEnabled(false);
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
        controller.addSeed(request.getUrl());

        CrawlerFactory factory = new CrawlerFactory(request, crawlerRequestRepository, modelMapper);

        controller.startNonBlocking(factory, numCrawlers);

        while (!controller.isFinished()) {
            Thread.sleep(10_000);

            log.info("Waiting for crawler request {} to finish."
                , request.getId());
        }

        log.info("Crawling done for {}", 10);

        if ( ! deleteQuietly(new File(config.getCrawlStorageFolder())) ) {
            log.warn("Something wrong deleting {}" + config.getCrawlStorageFolder());
        }
    }
}
