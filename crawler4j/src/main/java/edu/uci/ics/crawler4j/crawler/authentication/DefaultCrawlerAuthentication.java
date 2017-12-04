package edu.uci.ics.crawler4j.crawler.authentication;

import org.apache.http.impl.client.*;

public class DefaultCrawlerAuthentication implements CrawlerAuthentication {

    @Override
    public CloseableHttpClient login(HttpClientBuilder clientBuilder) {
        return clientBuilder.build();
    }

}
