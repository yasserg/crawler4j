package edu.uci.ics.crawler4j.examples.multiple;

import edu.uci.ics.crawler4j.CrawlerConfiguration;
import edu.uci.ics.crawler4j.crawler.DefaultWebCrawlerFactory;
import edu.uci.ics.crawler4j.crawler.controller.CrawlController;
import edu.uci.ics.crawler4j.parser.Parser;

public class SpecificDomainWebCrawlerFactory extends
        DefaultWebCrawlerFactory<SpecificDomainCrawler> {

    private final String[] myCrawlDomains;

    public SpecificDomainWebCrawlerFactory(String[] myCrawlDomains,
            CrawlerConfiguration crawlerConfiguration, CrawlController crawlController) {
        super(SpecificDomainCrawler.class, crawlerConfiguration, crawlController);
        this.myCrawlDomains = myCrawlDomains;
    }

    @Override
    protected SpecificDomainCrawler newInstance(Integer id) throws InstantiationException,
            IllegalAccessException {
        return new SpecificDomainCrawler(id, crawlerConfiguration, crawlController, crawlController
                .getPageFetcher(), crawlController.getRobotstxtServer(), crawlController
                        .getPageHarvests(), crawlController.getFrontier(), new Parser(
                                crawlerConfiguration), myCrawlDomains);
    }

}
