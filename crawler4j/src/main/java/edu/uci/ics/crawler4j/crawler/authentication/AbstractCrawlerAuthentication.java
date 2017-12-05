package edu.uci.ics.crawler4j.crawler.authentication;

import java.net.*;

import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.*;
import org.slf4j.*;

public abstract class AbstractCrawlerAuthentication implements CrawlerAuthentication {

    protected static final Logger logger = LoggerFactory.getLogger(
            AbstractCrawlerAuthentication.class);

    protected final URL uRL;

    protected final HttpHost targetHost;

    protected final String protocol;

    protected final String host;

    protected final int port;

    protected final String file;

    protected final String username;

    protected final String password;

    public AbstractCrawlerAuthentication(String url, String username, String password)
            throws MalformedURLException {
        super();
        this.uRL = new URL(url);
        this.targetHost = new HttpHost(uRL.getHost(), uRL.getPort(), uRL.getProtocol());
        this.protocol = uRL.getProtocol();
        this.host = uRL.getHost();
        this.port = uRL.getDefaultPort();
        this.file = uRL.getFile();
        this.username = username;
        this.password = password;
    }

    @Override
    public void configure(HttpClientBuilder clientBuilder) {
        logger.info("Configuring http client for: " + targetHost);
        clientBuilder.setDefaultCredentialsProvider(credentialsProvider());
    }

    @SuppressWarnings("static-method")
    protected CredentialsProvider credentialsProvider() {
        return new BasicCredentialsProvider();
    }

    @Override
    public void login(CloseableHttpClient httpClient) {
        // empty
    }

}
