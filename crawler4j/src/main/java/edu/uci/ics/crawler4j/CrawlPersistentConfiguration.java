package edu.uci.ics.crawler4j;

import edu.uci.ics.crawler4j.frontier.pageharvests.PageHarvests;
import edu.uci.ics.crawler4j.frontier.pagequeue.PageQueue;
import edu.uci.ics.crawler4j.frontier.pagestatistics.PageStatistics;

public interface CrawlPersistentConfiguration {

    void initialize() throws Exception;

    void close() throws Exception;

    String getStorageFolder();

    void setStorageFolder(String storageFolder);

    void setResumableCrawling(boolean resumableCrawling);

    PageStatistics getPageStatistics();

    PageHarvests getPageHarvests();

    PageQueue getPendingPageQueue();

    PageQueue getInprocessPageQueue();

}
