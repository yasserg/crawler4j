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

package edu.uci.ics.crawler4j.deadlinksniffer;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scan given web pages (seed) for dead links.
 *
 * @author Yasser Ganjisaffar
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
public class DeadLinkCrawlController {
    private static final Logger logger = LoggerFactory.getLogger(DeadLinkCrawlController.class);

    public static void main(String[] args) throws Exception {

        Options options = new Options();

        options.addRequiredOption("s", "seed", true,
                "Seeding page where the crawling should get started from.");

        options.addOption("u", "url", true,
                "Url Regular Expressions for pages which should get crawled. " +
                "If not given the seed will act as a starting point");

        options.addOption("?", "help", false,
                "Print detailed infos about the usage.");

        options.addOption("t", "threads", true,
                "Number of Threads to use for crawling. Defaults to 1.");

        options.addOption("o", "outDir", true,
                "output Directory to store the downloaded pages and information. Defaults to ./crawl");

        options.addOption("d", "delay", true,
                "time delay between requests in ms. Defaults to 1000 (1 second).");

        options.addOption("m", "maxdepth", true,
                "Maximum Depth of Crawling. Defaults to 3.");

        options.addOption("p", "pages", true,
                "Maximum number of pages to fetch. Defaults to 2000.");

        CommandLine cmd = null;
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);
        }
        catch (ParseException pe) {
            logger.info(pe.getMessage());
            printHelpAndExit(options);
        }

        if (cmd.hasOption("?")) {
            printHelpAndExit(options);
        }

        DeadLinkCrawlConfig config = new DeadLinkCrawlConfig();

        /*
         * crawlStorageFolder is a folder where intermediate crawl data is
         * stored.
         */
        String crawlStorageFolder =
                cmd.hasOption("o")
                ? cmd.getOptionValue("o")
                : "crawl";
        config.setCrawlStorageFolder(crawlStorageFolder);


        /*
         * Be polite: Make sure that we don't send more than 1 request per
         * second (1000 milliseconds between requests).
         */
        int delay  =
                cmd.hasOption("d")
                ? Integer.parseInt(cmd.getOptionValue("d"))
                : 1000;
        config.setPolitenessDelay(delay);

        /*
         * You can set the maximum crawl depth here. The default value is -1 for
         * unlimited depth
         */
        int maxDepth  =
                cmd.hasOption("m")
                ? Integer.parseInt(cmd.getOptionValue("m"))
                : 3;
        config.setMaxDepthOfCrawling(maxDepth);

        /*
         * You can set the maximum number of pages to crawl. The default value
         * is -1 for unlimited number of pages
         */
        int pages  =
                cmd.hasOption("p")
                ? Integer.parseInt(cmd.getOptionValue("p"))
                : 2000;
        config.setMaxPagesToFetch(pages);


        /*
         * numberOfCrawlers shows the number of concurrent threads that should
         * be initiated for crawling.
         */
        int numberOfCrawlers =
                cmd.hasOption("t")
                        ? Integer.parseInt(cmd.getOptionValue("t"))
                        : 1;

        if (cmd.hasOption("u")) {
            String[] urlPatterns = cmd.getOptionValues("u");

            for (String urlPattern : urlPatterns) {
                config.addUrlPattern(urlPattern);
            }
        }

        /**
         * Do you want crawler4j to crawl also binary data ?
         * example: the contents of pdf, or the metadata of images etc
         */
        config.setIncludeBinaryContentInCrawling(false);

        /*
         * Do you need to set a proxy? If so, you can use:
         * config.setProxyHost("proxyserver.example.com");
         * config.setProxyPort(8080);
         *
         * If your proxy also needs authentication:
         * config.setProxyUsername(username); config.getProxyPassword(password);
         */

        /*
         * This config parameter can be used to set your crawl to be resumable
         * (meaning that you can resume the crawl from a previously
         * interrupted/crashed crawl). Note: if you enable resuming feature and
         * want to start a fresh crawl, you need to delete the contents of
         * rootFolder manually.
         */
        config.setResumableCrawling(false);


        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        boolean addSeedsAsUrls = config.getUrlPatterns().isEmpty();
        String[] seeds = cmd.getOptionValues("s");
        for(String seed : seeds) {
            controller.addSeed(seed);
            if (addSeedsAsUrls) {
                config.addUrlPattern("^" + seed + ".*");
            }
        }


        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        controller.start(DeadLinkCrawler.class, numberOfCrawlers);
    }

    private static void printHelpAndExit(Options options) {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("\n\tDeadLinkSniffer -?                     - for help"+
                     "\n\tDeadLinkSniffer -s=http://mypage.org   - for scanning this page" +
                     "\n\tDeadLinkSniffer -s=http://mypage.org -u=\"https://.*mypage.org.*\"  - for scanning this page with all subdomains, etc"
                     , options);

        System.exit(-1);
    }


}