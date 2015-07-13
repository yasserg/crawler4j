package edu.uci.ics.crawler4j.tests;

import junit.framework.TestCase;
import org.junit.Test;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.ProxyConfig;
import edu.uci.ics.crawler4j.examples.proxies.ProxyCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;


public class ProxyTest extends TestCase {

    @Test
    public void testProxyConfig()
    {
        boolean test;
        try {
            new ProxyConfig("104.43.166.43.52", 3128);
            test = false;
        } catch (IllegalArgumentException e) {
            test = true;
        }
        assertTrue(test);
        
        try {
            new ProxyConfig("104.43.166.43", 3128);
            test = true;
        } catch (IllegalArgumentException e) {
            test = false;
        }
        
        try {
            new ProxyConfig("invalid host", 3128);
            test = false;
        } catch (IllegalArgumentException e) {
            test = true;
        }
        
        try {
            new ProxyConfig("test.com", 3128);
            test = false;
        } catch (IllegalArgumentException e) {
            test = true;
        }
    }
    
    @Test
    public void testWithoutProxy()
    {
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder("frontier");
        config.setIncludeBinaryContentInCrawling(false);
        config.setResumableCrawling(false);

        //fork crawler4j and PageFetcher for proxies
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        robotstxtConfig.setEnabled(false);
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        try {
            CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
            controller.addSeed("http://checkip.amazonaws.com/");
            controller.start(ProxyCrawler.class, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testWithProxy()
    {
        ProxyConfig[] proxies = {
            new ProxyConfig("104.43.166.43", 3128),
            new ProxyConfig("72.181.94.33", 7004),
            new ProxyConfig("104.41.206.249", 3128)
        };
        
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder("frontier");
        config.setIncludeBinaryContentInCrawling(false);
        config.setResumableCrawling(false);
        config.setProxies(proxies);
        config.setSocketTimeout(30000);

        //fork crawler4j and PageFetcher for proxies
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        robotstxtConfig.setEnabled(false);
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        try {
            CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
            controller.addSeed("http://icanhazip.com/");
            controller.addSeed("http://checkip.amazonaws.com/");
            controller.addSeed("https://wtfismyip.com/text");
            controller.start(ProxyCrawler.class, proxies.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}