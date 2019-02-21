package edu.uci.ics.crawler4j.examples.crawler;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.examples.db.impl.PostgresDBServiceImpl;

public class PostgresCrawlerFactory implements CrawlController.WebCrawlerFactory<PostgresWebCrawler> {

    private ComboPooledDataSource comboPooledDataSource;

    public PostgresCrawlerFactory(ComboPooledDataSource comboPooledDataSource) {
        this.comboPooledDataSource = comboPooledDataSource;
    }

    public PostgresWebCrawler newInstance() throws Exception {
        return new PostgresWebCrawler(new PostgresDBServiceImpl(comboPooledDataSource));
    }
}
