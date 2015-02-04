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

package edu.uci.ics.crawler4j.examples.localdata;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class LocalDataCollectorController {
  private static final Logger logger = LoggerFactory.getLogger(LocalDataCollectorController.class);

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      logger.info("Needed parameters: ");
      logger.info("\t rootFolder (it will contain intermediate crawl data)");
      logger.info("\t numberOfCralwers (number of concurrent threads)");
      return;
    }

    String rootFolder = args[0];
    int numberOfCrawlers = Integer.parseInt(args[1]);

    CrawlConfig config = new CrawlConfig();
    config.setCrawlStorageFolder(rootFolder);
    config.setMaxPagesToFetch(10);
    config.setPolitenessDelay(1000);

    PageFetcher pageFetcher = new PageFetcher(config);
    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
    CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

    controller.addSeed("http://www.ics.uci.edu/");
    controller.start(LocalDataCollectorCrawler.class, numberOfCrawlers);

    List<Object> crawlersLocalData = controller.getCrawlersLocalData();
    long totalLinks = 0;
    long totalTextSize = 0;
    int totalProcessedPages = 0;
    for (Object localData : crawlersLocalData) {
      CrawlStat stat = (CrawlStat) localData;
      totalLinks += stat.getTotalLinks();
      totalTextSize += stat.getTotalTextSize();
      totalProcessedPages += stat.getTotalProcessedPages();
    }

    logger.info("Aggregated Statistics:");
    logger.info("\tProcessed Pages: {}", totalProcessedPages);
    logger.info("\tTotal Links found: {}", totalLinks);
    logger.info("\tTotal Text Size: {}", totalTextSize);
  }
}