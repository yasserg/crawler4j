package edu.uci.ics.crawler4j.tests.fetcher;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.exceptions.PageBiggerThanMaxSizeException;
import edu.uci.ics.crawler4j.fetcher.PageFetchResult;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.url.WebURL;

public class PageFetcherHtmlOnly extends PageFetcher {

    public PageFetcherHtmlOnly(CrawlConfig config)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        super(config);
    }

    @Override
    public PageFetchResult fetchPage(WebURL webUrl)
        throws InterruptedException, IOException, PageBiggerThanMaxSizeException {
        String toFetchURL = webUrl.getURL();

        PageFetchResult fetchResult = new PageFetchResult(config.isHaltOnError());
        HttpHead head = null;
        try {
            head = new HttpHead(toFetchURL);

            synchronized (mutex) {
                long now = new Date().getTime();
                if (now - this.lastFetchTime < getConfig().getPolitenessDelay()) {
                    Thread.sleep(getConfig().getPolitenessDelay() - (now - this.lastFetchTime));
                }
                this.lastFetchTime = new Date().getTime();
            }

            HttpResponse response = httpClient.execute(head);
            fetchResult.setEntity(response.getEntity());
            fetchResult.setResponseHeaders(response.getAllHeaders());
            fetchResult.setFetchedUrl(toFetchURL);
            fetchResult.setStatusCode(response.getStatusLine().getStatusCode());

            String contentType = response.containsHeader("Content-Type") ?
                                 response.getFirstHeader("Content-Type").getValue() : null;
            String typeStr = (contentType != null) ? contentType.toLowerCase() : "";

            if (typeStr.equals("") || (typeStr.contains("text") && typeStr.contains("html"))) {
                return super.fetchPage(webUrl);
            } else {
                return fetchResult;
            }
        } finally {
            if (head != null) {
                head.abort();
            }
        }
    }
}
