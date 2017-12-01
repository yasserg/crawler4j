package edu.uci.ics.crawler4j.examples.multiple;

import edu.uci.ics.crawler4j.CrawlerConfiguration;
import edu.uci.ics.crawler4j.crawler.controller.AbstractCrawlController;

public class SpecificDomainCrawlController extends AbstractCrawlController<SpecificDomainCrawler> {

    private SpecificDomainWebCrawlerFactory factory;

    public SpecificDomainCrawlController(String[] specificDomains,
            CrawlerConfiguration configuration) throws Exception {
        super(configuration);
        this.factory = new SpecificDomainWebCrawlerFactory(specificDomains, configuration, this);
    }

    @Override
    protected SpecificDomainWebCrawlerFactory crawlerFactory() {
        return factory;
    }

}
