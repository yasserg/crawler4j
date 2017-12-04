package edu.uci.ics.crawler4j.crawler.fetcher;

import org.apache.http.*;

import edu.uci.ics.crawler4j.crawler.Page;

public interface FetchedPage {

    boolean fetchContent(Page page, int maxBytes);

    void discardContentIfNotConsumed();

    int getStatusCode();

    void setStatusCode(int statusCode);

    String getFetchedUrl();

    void setFetchedUrl(String fetchedUrl);

    String getMovedToUrl();

    void setMovedToUrl(String movedToUrl);

    Header[] getResponseHeaders();

    void setResponseHeaders(Header[] responseHeaders);

    HttpEntity getEntity();

    void setEntity(HttpEntity entity);

}
