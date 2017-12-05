package edu.uci.ics.crawler4j.crawler.authentication;

import org.apache.http.impl.client.*;

public class DefaultCrawlerAuthentication implements CrawlerAuthentication {

    @Override
    public void configure(HttpClientBuilder clientBuilder) {
        // empty
    }

    @Override
    public void login(CloseableHttpClient httpClient) {
        // empty
    }

}
