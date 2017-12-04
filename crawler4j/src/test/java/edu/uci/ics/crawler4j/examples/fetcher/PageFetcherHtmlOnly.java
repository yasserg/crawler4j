package edu.uci.ics.crawler4j.examples.fetcher;

import java.io.IOException;
import java.util.Date;

import org.apache.http.client.methods.*;

import edu.uci.ics.crawler4j.CrawlerConfiguration;
import edu.uci.ics.crawler4j.crawler.exceptions.PageBiggerThanMaxSizeException;
import edu.uci.ics.crawler4j.crawler.fetcher.*;
import edu.uci.ics.crawler4j.url.WebURL;

public class PageFetcherHtmlOnly extends DefaultPageFetcher {

    private final Object mutex = new Object();

    public PageFetcherHtmlOnly(CrawlerConfiguration config) {
        super(config);
    }

    @Override
    public FetchedPage fetch(WebURL webUrl) throws InterruptedException, IOException,
            PageBiggerThanMaxSizeException {
        String toFetchURL = webUrl.getURL();

        FetchedPage fetchResult = new FetchedPageImpl();
        HttpHead head = null;
        try {
            head = new HttpHead(toFetchURL);

            synchronized (mutex) {
                long now = new Date().getTime();
                if (now - this.lastFetchTime < configuration.getPolitenessDelay()) {
                    Thread.sleep(configuration.getPolitenessDelay() - (now - this.lastFetchTime));
                }
                this.lastFetchTime = new Date().getTime();
            }

            try (CloseableHttpResponse response = httpClient.execute(head);) {
                fetchResult.setEntity(response.getEntity());
                fetchResult.setResponseHeaders(response.getAllHeaders());
                fetchResult.setFetchedUrl(toFetchURL);
                fetchResult.setStatusCode(response.getStatusLine().getStatusCode());

                String contentType = response.containsHeader("Content-Type") ? response
                        .getFirstHeader("Content-Type").getValue() : null;
                String typeStr = (contentType != null) ? contentType.toLowerCase() : "";

                if (typeStr.equals("") || (typeStr.contains("text") && typeStr.contains("html"))) {
                    return super.fetch(webUrl);
                }
                return fetchResult;
            }
        } finally {
            if (head != null) {
                head.abort();
            }
        }
    }
}
