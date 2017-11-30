package edu.uci.ics.crawler4j.crawler;

import java.util.Collection;

import edu.uci.ics.crawler4j.crawler.controller.CrawlController;

public interface WebCrawlerFactory<T extends WebCrawler> {

    T newInstance(CrawlController crawlController, Collection<Thread> threads,
            Collection<T> crawlers) throws Exception;

    T replaceInstance(T existingCrawler, Thread exitingThread, CrawlController crawlController,
            Collection<Thread> threads, Collection<T> crawlers) throws Exception;

}