package edu.uci.ics.crawler4j.example;


import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.SeleniumCrawlConfig;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.fetcher.PageFetcherSelenium;
import edu.uci.ics.crawler4j.parser.ParserSelenium;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.TLDList;

public class SeleniumExample {

	public static void main(String[] args) throws Exception {
		String crawlStorageFolder = "/data/crawl/root";
        int numberOfCrawlers = 1;

        SeleniumCrawlConfig config = new SeleniumCrawlConfig();
        config.setThreadMonitoringDelaySeconds(1);
        config.setCleanupDelaySeconds(1);
        config.setThreadShutdownDelaySeconds(1);
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setDefaultToSelenium(true);
        // Instantiate the controller for this crawl.
        TLDList tldList = new TLDList(config);
        PageFetcher pageFetcher = new PageFetcher(config);
        ParserSelenium parser = new ParserSelenium(config, tldList);
        PageFetcherSelenium pageFetcherSelenium = new PageFetcherSelenium(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcherSelenium, parser, robotstxtServer, tldList);

        // For each crawl, you need to add some seed urls. These are the first
        // URLs that are fetched and then the crawler starts following links
        // which are found in these pages
        controller.addSeed("https://www.google.com");
    	
    	// The factory which creates instances of crawlers.
        CrawlController.WebCrawlerFactory<WebCrawler > factory = WebCrawler::new;
        
        // Start the crawl. This is a blocking operation, meaning that your code
        // will reach the line after this only when crawling is finished.
        controller.start(factory, numberOfCrawlers);
        pageFetcher.shutDown();
	}
}
