package edu.uci.ics.crawler4j.crawler;

import java.util.Collection;

public interface WebCrawlerFactory<T extends WebCrawler> {

    T newInstance(Collection<Thread> threads, Collection<T> crawlers) throws Exception;

    T replaceInstance(T existingCrawler, Thread exitingThread, Collection<Thread> threads,
            Collection<T> crawlers) throws Exception;

}