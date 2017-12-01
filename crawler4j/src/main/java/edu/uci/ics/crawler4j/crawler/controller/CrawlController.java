package edu.uci.ics.crawler4j.crawler.controller;

import java.util.Collection;

import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.frontier.pageharvests.PageHarvests;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public interface CrawlController {

    /**
     * Adds a new seed URL. A seed URL is a URL that is fetched by the crawler to extract new URLs
     * in it and follow them for crawling.
     *
     * @param pageUrl
     *            the URL of the seed
     */
    void addSeed(String pageUrl);

    /**
     * Adds a new seed URL. A seed URL is a URL that is fetched by the crawler to extract new URLs
     * in it and follow them for crawling. You can also specify a specific document id to be
     * assigned to this seed URL. This document id needs to be unique. Also, note that if you add
     * three seeds with document ids 1,2, and 7. Then the next URL that is found during the crawl
     * will get a doc id of 8. Also you need to ensure to add seeds in increasing order of document
     * ids.
     *
     * Specifying doc ids is mainly useful when you have had a previous crawl and have stored the
     * results and want to start a new crawl with seeds which get the same document ids as the
     * previous crawl.
     *
     * @param pageUrl
     *            the URL of the seed
     * @param docId
     *            the document id that you want to be assigned to this seed URL.
     *
     */
    void addSeed(String pageUrl, int docId);

    /**
     * This function can called to assign a specific document id to a url. This feature is useful
     * when you have had a previous crawl and have stored the Urls and their associated document ids
     * and want to have a new crawl which is aware of the previously seen Urls and won't re-crawl
     * them.
     *
     * Note that if you add three seen Urls with document ids 1,2, and 7. Then the next URL that is
     * found during the crawl will get a doc id of 8. Also you need to ensure to add seen Urls in
     * increasing order of document ids.
     *
     * @param pageUrl
     *            the URL of the page
     * @param docId
     *            the document id that you want to be assigned to this URL.
     *
     */
    void addSeen(String pageUrl, int docId);

    /**
     * Start the crawling session and wait for it to finish. This method utilizes default crawler
     * factory that creates new crawler using Java reflection
     *
     * @param <T>
     *            Your class extending WebCrawler
     */
    <T extends WebCrawler> void start();

    /**
     * Start the crawling session and return immediately.
     *
     * @param <T>
     *            Your class extending WebCrawler
     */
    <T extends WebCrawler> void startNonBlocking();

    /**
     * Wait until this crawling session finishes.
     */
    void waitUntilFinish();

    /**
     * Once the crawling session finishes the controller collects the local data of the crawler
     * threads and stores them in a List. This function returns the reference to this list.
     *
     * @return Collection of objects from each crawlers local data
     */
    Collection<Object> getCrawlerData();

    /**
     * Set the current crawling session set to 'shutdown'. Crawler threads monitor the shutdown flag
     * and when it is set to true, they will no longer process new pages.
     */
    void shutdown();

    boolean isShuttingDown();

    PageFetcher getPageFetcher();

    RobotstxtServer getRobotstxtServer();

    PageHarvests getPageHarvests();

    Frontier getFrontier();

}
