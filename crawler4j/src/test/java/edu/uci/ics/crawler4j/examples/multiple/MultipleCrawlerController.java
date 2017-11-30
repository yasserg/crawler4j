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

package edu.uci.ics.crawler4j.examples.multiple;

import org.slf4j.*;

import edu.uci.ics.crawler4j.*;
import edu.uci.ics.crawler4j.crawler.controller.CrawlController;

/**
 * @author Yasser Ganjisaffar
 */

public class MultipleCrawlerController {
    private static final Logger logger = LoggerFactory.getLogger(MultipleCrawlerController.class);

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            logger.info("Needed parameter: ");
            logger.info("\t rootFolder (it will contain intermediate crawl data)");
            return;
        }

        /*
         * crawlStorageFolder is a folder where intermediate crawl data is stored.
         */
        String crawlStorageFolder = args[0];

        CrawlerConfiguration config1 = new CrawlerConfiguration(
                new SleepyCatCrawlPersistentConfiguration());
        CrawlerConfiguration config2 = new CrawlerConfiguration(
                new SleepyCatCrawlPersistentConfiguration());

        /*
         * The two crawlers should have different storage folders for their intermediate data
         */
        config1.getCrawlPersistentConfiguration().setStorageFolder(crawlStorageFolder
                + "/crawler1");
        config2.getCrawlPersistentConfiguration().setStorageFolder(crawlStorageFolder
                + "/crawler2");

        config1.setPolitenessDelay(1000);
        config2.setPolitenessDelay(2000);

        config1.setMaxPagesToFetch(50);
        config2.setMaxPagesToFetch(100);

        config1.setNumberOfCrawlers(5);
        config2.setNumberOfCrawlers(7);

        String[] crawler1Domains = new String[] {"http://www.ics.uci.edu/", "http://www.cnn.com/" };
        String[] crawler2Domains = new String[] {"http://en.wikipedia.org/" };

        CrawlController controller1 = new SpecificDomainCrawlController(crawler1Domains, config1);
        CrawlController controller2 = new SpecificDomainCrawlController(crawler2Domains, config2);

        controller1.addSeed("http://www.ics.uci.edu/");
        controller1.addSeed("http://www.cnn.com/");
        controller1.addSeed("http://www.ics.uci.edu/~lopes/");
        controller1.addSeed("http://www.cnn.com/POLITICS/");

        controller2.addSeed("http://en.wikipedia.org/wiki/Main_Page");
        controller2.addSeed("http://en.wikipedia.org/wiki/Obama");
        controller2.addSeed("http://en.wikipedia.org/wiki/Bing");

        controller1.startNonBlocking();
        controller2.startNonBlocking();

        controller1.waitUntilFinish();
        logger.info("Crawler 1 is finished.");

        controller2.waitUntilFinish();
        logger.info("Crawler 2 is finished.");
    }
}