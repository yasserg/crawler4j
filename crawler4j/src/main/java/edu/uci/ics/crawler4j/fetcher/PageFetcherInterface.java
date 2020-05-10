package edu.uci.ics.crawler4j.fetcher;

import java.io.IOException;

import edu.uci.ics.crawler4j.crawler.exceptions.PageBiggerThanMaxSizeException;
import edu.uci.ics.crawler4j.url.WebURL;

public interface PageFetcherInterface {

    PageFetchResultInterface fetchPage(WebURL webUrl) throws InterruptedException, IOException,
                                                                PageBiggerThanMaxSizeException;

    void shutDown();
}