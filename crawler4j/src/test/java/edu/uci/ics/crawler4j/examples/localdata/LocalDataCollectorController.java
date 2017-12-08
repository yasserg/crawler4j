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

import java.util.Collection;

import org.slf4j.*;

import edu.uci.ics.crawler4j.*;
import edu.uci.ics.crawler4j.crawler.controller.*;

public class LocalDataCollectorController {
    private static final Logger logger = LoggerFactory.getLogger(
            LocalDataCollectorController.class);

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            logger.info("Needed parameters: ");
            logger.info("\t rootFolder (it will contain intermediate crawl data)");
            logger.info("\t numberOfCralwers (number of concurrent threads)");
            return;
        }

        String rootFolder = args[0];
        int numberOfCrawlers = Integer.parseInt(args[1]);

        CrawlerConfiguration config = new CrawlerConfiguration();
        config.setCrawlPersistentConfiguration(new SleepyCatCrawlPersistentConfiguration(config));
        config.setStorageFolder(rootFolder);
        config.setMaxPagesToFetch(10);
        config.setPolitenessDelay(1000);
        config.setNumberOfCrawlers(numberOfCrawlers);

        CrawlController controller = new DefaultCrawlController(config,
                LocalDataCollectorCrawler.class);
        controller.addSeed("http://www.ics.uci.edu/");

        Collection<Object> crawlersLocalData = controller.getCrawlerData();
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