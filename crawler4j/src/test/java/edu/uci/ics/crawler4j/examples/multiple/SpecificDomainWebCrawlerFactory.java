package edu.uci.ics.crawler4j.examples.multiple;

import edu.uci.ics.crawler4j.CrawlerConfiguration;
import edu.uci.ics.crawler4j.crawler.DefaultWebCrawlerFactory;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.frontier.pageharvests.PageHarvests;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class SpecificDomainWebCrawlerFactory extends
        DefaultWebCrawlerFactory<SpecificDomainCrawler> {

    private final String[] myCrawlDomains;

    public SpecificDomainWebCrawlerFactory(String[] myCrawlDomains,
            CrawlerConfiguration crawlerConfiguration, PageFetcher pageFetcher,
            RobotstxtServer robotstxtServer, PageHarvests pageHarvests, Frontier frontier) {
        super(SpecificDomainCrawler.class, crawlerConfiguration, pageFetcher, robotstxtServer,
                pageHarvests, frontier);
        this.myCrawlDomains = myCrawlDomains;
    }

    @Override
    protected SpecificDomainCrawler newInstance() {
        return new SpecificDomainCrawler(myCrawlDomains);
    }

}
