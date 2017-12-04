package edu.uci.ics.crawler4j.crawler.authentication;

import org.apache.http.impl.client.*;

public interface CrawlerAuthentication {

    CloseableHttpClient login(HttpClientBuilder clientBuilder);

}
