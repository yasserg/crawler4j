package edu.uci.ics.crawler4j.crawler.authentication;

import org.apache.http.impl.client.*;

public interface CrawlerAuthentication {

    void configure(HttpClientBuilder clientBuilder);

    void login(CloseableHttpClient httpClient);

}
