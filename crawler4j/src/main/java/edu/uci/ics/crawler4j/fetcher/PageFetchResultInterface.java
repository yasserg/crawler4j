package edu.uci.ics.crawler4j.fetcher;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import edu.uci.ics.crawler4j.crawler.Page;

public interface PageFetchResultInterface {

    int getStatusCode();

    void setStatusCode(int statusCode);

    String getFetchedUrl();

    void setFetchedUrl(String fetchedUrl);

    boolean fetchContent(Page page, int maxBytes) throws SocketTimeoutException, IOException;

    void discardContentIfNotConsumed();

    String getMovedToUrl();

    void setMovedToUrl(String movedToUrl);

    HttpEntity getEntity();

    Header[] getResponseHeaders();

}