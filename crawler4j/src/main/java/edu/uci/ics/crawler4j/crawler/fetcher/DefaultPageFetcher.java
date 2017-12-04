/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.crawler.fetcher;

import java.io.IOException;
import java.util.Date;

import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.config.*;
import org.apache.http.conn.socket.*;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.*;

import edu.uci.ics.crawler4j.CrawlerConfiguration;
import edu.uci.ics.crawler4j.crawler.exceptions.PageBiggerThanMaxSizeException;
import edu.uci.ics.crawler4j.url.*;

/**
 * @author Yasser Ganjisaffar
 */
public class DefaultPageFetcher implements PageFetcher {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultPageFetcher.class);

    protected final CrawlerConfiguration configuration;

    protected PoolingHttpClientConnectionManager connectionManager;

    protected CloseableHttpClient httpClient;

    protected long lastFetchTime = 0;

    private final IdleHttpClientConnectionMonitor connectionMonitorThread;

    private final Object mutex = new Object();

    public DefaultPageFetcher(CrawlerConfiguration configuration) {
        super();
        this.configuration = configuration;
        connectionManager = new ServerNamingIndicationPoolingHttpClientConnectionManager(
                connectionRegistry(), configuration.getDnsResolver());
        connectionManager.setMaxTotal(configuration.getMaxTotalConnections());
        connectionManager.setDefaultMaxPerRoute(configuration.getMaxConnectionsPerHost());

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.setDefaultRequestConfig(requestConfig());
        clientBuilder.setConnectionManager(connectionManager);
        clientBuilder.setUserAgent(configuration.getUserAgentString());
        clientBuilder.setDefaultHeaders(configuration.getDefaultHeaders());

        configureProxy(clientBuilder);

        httpClient = configuration.getAuthentication().login(clientBuilder);
        connectionMonitorThread = new IdleHttpClientConnectionMonitor(connectionManager);
        connectionMonitorThread.start();
    }

    private void configureProxy(HttpClientBuilder clientBuilder) {
        if (null != configuration.getProxyHost()) {
            logger.debug("Working through Proxy: {}", configuration.getProxyHost());
            clientBuilder.setProxy(new HttpHost(configuration.getProxyHost(), configuration
                    .getProxyPort()));

            if (null != configuration.getProxyUsername()) {
                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(configuration.getProxyHost(),
                        configuration.getProxyPort()), new UsernamePasswordCredentials(configuration
                                .getProxyUsername(), configuration.getProxyPassword()));
                clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }
    }

    private Registry<ConnectionSocketFactory> connectionRegistry() {
        RegistryBuilder<ConnectionSocketFactory> connectionRegistryBuilder = RegistryBuilder
                .create();
        connectionRegistryBuilder.register("http", PlainConnectionSocketFactory.INSTANCE);
        if (configuration.isIncludeHttpsPages()) {
            try {
                connectionRegistryBuilder.register("https",
                        new ServerNameIndicationSSLConnectionSocketFactory(
                                NoopHostnameVerifier.INSTANCE));
            } catch (Exception e) {
                logger.warn("Exception thrown while trying to register https");
                logger.debug("Stacktrace", e);
            }
        }
        return connectionRegistryBuilder.build();
    }

    private RequestConfig requestConfig() {
        return RequestConfig.custom().setExpectContinueEnabled(false).setCookieSpec(configuration
                .getCookiePolicy()).setRedirectsEnabled(false).setSocketTimeout(configuration
                        .getSocketTimeout()).setConnectTimeout(configuration.getConnectionTimeout())
                .build();
    }

    @Override
    public FetchedPage fetch(WebURL webUrl) throws InterruptedException, IOException,
            PageBiggerThanMaxSizeException {
        FetchedPage fetchedPage = new FetchedPageImpl();
        HttpUriRequest request = null;
        try {
            request = newHttpUriRequest(webUrl.getURL());
            politenessDelay();

            CloseableHttpResponse response = httpClient.execute(request);
            fetchedPage.setResponseHeaders(response.getAllHeaders());
            fetchedPage.setEntity(response.getEntity());

            int statusCode = response.getStatusLine().getStatusCode();
            fetchedPage.setStatusCode(statusCode);

            // If Redirect ( 3xx )
            if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY
                    || statusCode == HttpStatus.SC_MOVED_TEMPORARILY
                    || statusCode == HttpStatus.SC_MULTIPLE_CHOICES
                    || statusCode == HttpStatus.SC_SEE_OTHER
                    || statusCode == HttpStatus.SC_TEMPORARY_REDIRECT || statusCode == 308) {
                // https://issues.apache.org/jira/browse/HTTPCORE-389
                handleRedirect(webUrl, fetchedPage, response);
            } else if (200 <= statusCode && statusCode <= 299) { // is 2XX, everything looks ok
                handleSuccess(webUrl, fetchedPage, request, response);
            }
            return fetchedPage;

        } finally { // occurs also with thrown exceptions
            if (null == fetchedPage.getEntity() && null != request) {
                request.abort();
            }
        }
    }

    private void politenessDelay() throws InterruptedException {
        synchronized (mutex) {
            long now = (new Date()).getTime();
            if ((now - lastFetchTime) < configuration.getPolitenessDelay()) {
                long sleepDelay = (configuration.getPolitenessDelay() - (now - lastFetchTime));
                logger.debug("Sleeping for politeness delay {}", sleepDelay);
                Thread.sleep(sleepDelay);
            }
            lastFetchTime = (new Date()).getTime();
        }
    }

    @Override
    public void handleRedirect(WebURL webUrl, FetchedPage fetchedPage,
            CloseableHttpResponse response) {
        Header header = response.getFirstHeader("Location");
        if (null != header) {
            String movedToUrl = URLCanonicalizer.getCanonicalURL(header.getValue(), webUrl
                    .getURL());
            fetchedPage.setMovedToUrl(movedToUrl);
        }
    }

    @Override
    public void handleSuccess(WebURL webUrl, FetchedPage fetchedPage, HttpUriRequest request,
            CloseableHttpResponse response) throws IOException, PageBiggerThanMaxSizeException {
        fetchedPage.setFetchedUrl(webUrl.getURL());
        String uri = request.getURI().toString();
        if (!uri.equals(webUrl.getURL())) {
            if (!URLCanonicalizer.getCanonicalURL(uri).equals(webUrl.getURL())) {
                fetchedPage.setFetchedUrl(uri);
            }
        }
        validatePageSize(fetchedPage, response);
    }

    private void validatePageSize(FetchedPage fetchResult, CloseableHttpResponse response)
            throws IOException, PageBiggerThanMaxSizeException {
        if (null != fetchResult.getEntity()) {
            if (configuration.getMaxDownloadSize() < pageSize(fetchResult, response)) {
                // fix issue #52 - consume entity
                response.close();
                throw new PageBiggerThanMaxSizeException(pageSize(fetchResult, response));
            }
        }
    }

    private static long pageSize(FetchedPage fetchResult, CloseableHttpResponse response) {
        long size = fetchResult.getEntity().getContentLength();
        if (-1 == size) {
            Header length = contentLengthHeader(response);
            if (null != length) {
                size = Integer.parseInt(length.getValue());
            }
        }
        return size;
    }

    private static Header contentLengthHeader(CloseableHttpResponse response) {
        Header length = response.getLastHeader("Content-Length");
        if (null == length) {
            return response.getLastHeader("Content-length");
        }
        return length;
    }

    @Override
    public synchronized void shutDown() {
        if (null != connectionMonitorThread) {
            connectionManager.shutdown();
            connectionMonitorThread.shutdown();
        }
    }

    @Override
    public HttpUriRequest newHttpUriRequest(String url) {
        return new HttpGet(url);
    }

}
