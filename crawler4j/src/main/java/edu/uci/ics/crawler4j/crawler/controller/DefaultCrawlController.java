package edu.uci.ics.crawler4j.crawler.controller;

import edu.uci.ics.crawler4j.CrawlerConfiguration;
import edu.uci.ics.crawler4j.crawler.*;

public class DefaultCrawlController<T extends WebCrawler> extends AbstractCrawlController<T> {

    private WebCrawlerFactory<T> crawlerFactory;

    public DefaultCrawlController(CrawlerConfiguration configuration) throws Exception {
        this(configuration, WebCrawler.class);
    }

    public DefaultCrawlController(CrawlerConfiguration configuration,
            Class<? extends WebCrawler> clazz) throws Exception {
        super(configuration);
        this.crawlerFactory = new DefaultWebCrawlerFactory(clazz, configuration, pageFetcher,
                robotstxtServer, pageHarvests, frontier);
    }

    @Override
    protected WebCrawlerFactory<T> crawlerFactory() {
        return crawlerFactory;
    }

}
