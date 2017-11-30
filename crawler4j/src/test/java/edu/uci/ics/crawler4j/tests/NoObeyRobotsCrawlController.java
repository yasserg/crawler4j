package edu.uci.ics.crawler4j.tests;

import edu.uci.ics.crawler4j.CrawlerConfiguration;
import edu.uci.ics.crawler4j.crawler.controller.DefaultCrawlController;
import edu.uci.ics.crawler4j.robotstxt.*;

public class NoObeyRobotsCrawlController extends DefaultCrawlController {

    NoObeyRobotsCrawlController(CrawlerConfiguration configuration) throws Exception {
        super(configuration);
    }

    @Override
    protected RobotstxtServer robotstxtServer() {
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        robotstxtConfig.setEnabled(false);
        return new RobotstxtServer(robotstxtConfig, pageFetcher);
    }
}
