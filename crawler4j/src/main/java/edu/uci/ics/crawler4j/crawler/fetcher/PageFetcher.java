package edu.uci.ics.crawler4j.crawler.fetcher;

import java.io.IOException;

import org.apache.http.client.methods.*;

import edu.uci.ics.crawler4j.crawler.exceptions.CrawlerException;
import edu.uci.ics.crawler4j.url.WebURL;

public interface PageFetcher {

    FetchedPage fetch(WebURL webUrl) throws InterruptedException, IOException, CrawlerException;

    void handleRedirect(WebURL webUrl, FetchedPage fetchedPage, CloseableHttpResponse response);

    void handleSuccess(WebURL webUrl, FetchedPage fetchedPage, HttpUriRequest request,
            CloseableHttpResponse response) throws IOException, CrawlerException;

    void shutDown();

    /**
     * Creates a new HttpUriRequest for the given url. The default is to create a HttpGet without
     * any further configuration. Subclasses may override this method and provide their own logic.
     *
     * @param url
     *            the url to be fetched
     * @return the HttpUriRequest for the given url
     */
    HttpUriRequest newHttpUriRequest(String url);

}
