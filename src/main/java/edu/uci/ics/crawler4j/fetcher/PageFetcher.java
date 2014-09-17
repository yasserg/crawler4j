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

package edu.uci.ics.crawler4j.fetcher;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.Configurable;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class PageFetcher extends Configurable {

  protected static final Logger logger = LoggerFactory.getLogger(PageFetcher.class);

  protected PoolingHttpClientConnectionManager connectionManager;

  protected CloseableHttpClient httpClient;

  protected final Object mutex = new Object();

  protected long lastFetchTime = 0;

  protected IdleConnectionMonitorThread connectionMonitorThread = null;

  public PageFetcher(CrawlConfig config) {
    super(config);

    RequestConfig requestConfig = RequestConfig.custom()
        .setExpectContinueEnabled(false)
        .setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
        .setRedirectsEnabled(false)
        .setSocketTimeout(config.getSocketTimeout())
        .setConnectTimeout(config.getConnectionTimeout())
        .build();

    RegistryBuilder<ConnectionSocketFactory> connRegistryBuilder = RegistryBuilder.create();
    connRegistryBuilder.register("http", PlainConnectionSocketFactory.INSTANCE);
    if (config.isIncludeHttpsPages()) {
      try { // Fixing: https://code.google.com/p/crawler4j/issues/detail?id=174
        // By always trusting the ssl certificate
        SSLContext sslContext = SSLContexts.custom()
            .loadTrustMaterial(null, new TrustStrategy() {
              @Override
              public boolean isTrusted(final X509Certificate[] chain, String authType) {
                return true;
              }
            }).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        connRegistryBuilder.register("https", sslsf);
      } catch (Exception e) {
        logger.debug("Exception thrown while trying to register https:", e);
      }
    }

    Registry<ConnectionSocketFactory> connRegistry = connRegistryBuilder.build();
    connectionManager = new PoolingHttpClientConnectionManager(connRegistry);
    connectionManager.setMaxTotal(config.getMaxTotalConnections());
    connectionManager.setDefaultMaxPerRoute(config.getMaxConnectionsPerHost());

    HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    clientBuilder.setDefaultRequestConfig(requestConfig);
    clientBuilder.setConnectionManager(connectionManager);
    clientBuilder.setUserAgent(config.getUserAgentString());
    if (config.getProxyHost() != null) {

      if (config.getProxyUsername() != null) {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
            new AuthScope(config.getProxyHost(), config.getProxyPort()),
            new UsernamePasswordCredentials(config.getProxyUsername(), config.getProxyPassword()));
        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
      }

      HttpHost proxy = new HttpHost(config.getProxyHost(), config.getProxyPort());
      clientBuilder.setProxy(proxy);
    }
    clientBuilder.addInterceptorLast(new HttpResponseInterceptor() {
      @Override
      public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
        HttpEntity entity = response.getEntity();
        Header contentEncoding = entity.getContentEncoding();
        if (contentEncoding != null) {
          HeaderElement[] codecs = contentEncoding.getElements();
          for (HeaderElement codec : codecs) {
            if (codec.getName().equalsIgnoreCase("gzip")) {
              response.setEntity(new GzipDecompressingEntity(response.getEntity()));
              return;
            }
          }
        }
      }
    });

    httpClient = clientBuilder.build();

    if (connectionMonitorThread == null) {
      connectionMonitorThread = new IdleConnectionMonitorThread(connectionManager);
    }
    connectionMonitorThread.start();
  }

  public PageFetchResult fetchHeader(WebURL webUrl) {
    PageFetchResult fetchResult = new PageFetchResult();
    String toFetchURL = webUrl.getURL();
    HttpGet get = null;
    try {
      get = new HttpGet(toFetchURL);
      synchronized (mutex) {
        long now = (new Date()).getTime();
        if (now - lastFetchTime < config.getPolitenessDelay()) {
          Thread.sleep(config.getPolitenessDelay() - (now - lastFetchTime));
        }
        lastFetchTime = (new Date()).getTime();
      }

      HttpResponse response = httpClient.execute(get);
      fetchResult.setEntity(response.getEntity());
      fetchResult.setResponseHeaders(response.getAllHeaders());

      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode != HttpStatus.SC_OK) {
        if (statusCode != HttpStatus.SC_NOT_FOUND) {
          if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY
              || statusCode == HttpStatus.SC_MULTIPLE_CHOICES || statusCode == HttpStatus.SC_SEE_OTHER
              || statusCode == HttpStatus.SC_TEMPORARY_REDIRECT || statusCode == CustomFetchStatus.SC_PERMANENT_REDIRECT) {
            Header header = response.getFirstHeader("Location");
            if (header != null) {
              String movedToUrl = header.getValue();
              movedToUrl = URLCanonicalizer.getCanonicalURL(movedToUrl, toFetchURL);
              fetchResult.setMovedToUrl(movedToUrl);
            }
            fetchResult.setStatusCode(statusCode);
            return fetchResult;
          }
          logger.info("Failed: {}, while fetching {}", response.getStatusLine().toString(), toFetchURL);
        }
        fetchResult.setStatusCode(response.getStatusLine().getStatusCode());
        return fetchResult;
      }

      fetchResult.setFetchedUrl(toFetchURL);
      String uri = get.getURI().toString();
      if (!uri.equals(toFetchURL)) {
        if (!URLCanonicalizer.getCanonicalURL(uri).equals(toFetchURL)) {
          fetchResult.setFetchedUrl(uri);
        }
      }

      if (fetchResult.getEntity() != null) {
        long size = fetchResult.getEntity().getContentLength();
        if (size == -1) {
          Header length = response.getLastHeader("Content-Length");
          if (length == null) {
            length = response.getLastHeader("Content-length");
          }
          if (length != null) {
            size = Integer.parseInt(length.getValue());
          } else {
            size = -1;
          }
        }
        if (size > config.getMaxDownloadSize()) {
          fetchResult.setStatusCode(CustomFetchStatus.PageTooBig);
          get.abort();
          logger.warn("Failed: Page Size ({}) exceeded max-download-size ({}), at URL: {}",
              size, config.getMaxDownloadSize(), webUrl.getURL());
          return fetchResult;
        }

        fetchResult.setStatusCode(HttpStatus.SC_OK);
        return fetchResult;
      }

      get.abort();

    } catch (IOException e) {
      if (toFetchURL.toLowerCase().endsWith("robots.txt")) {
        // Ignoring this Exception as it just means that we tried to parse a robots.txt file which this site doesn't have
        // Which is ok, so no exception should be thrown
      } else {
        logger.error("Fatal transport error: {} while fetching {} (link found in doc #{})",
            e.getMessage() != null ? e.getMessage() : e.getCause(), toFetchURL, webUrl.getParentDocid());
        logger.debug("Stacktrace: ", e);
        fetchResult.setStatusCode(CustomFetchStatus.FatalTransportError);
        return fetchResult;
      }
    } catch (IllegalStateException e) {
      // ignoring exceptions that occur because of not registering https
      // and other schemes
    } catch (Exception e) {
      logger.error("{} Error while fetching {}", e.getMessage() != null ? e.getMessage() : e.getCause(), webUrl.getURL());
      logger.debug("Stacktrace:", e);
    } finally {
      try {
        if (fetchResult.getEntity() == null && get != null) {
          get.abort();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    fetchResult.setStatusCode(CustomFetchStatus.UnknownError);
    logger.error("Failed: Unknown error occurred while fetching {}", webUrl.getURL());
    return fetchResult;
  }

  public synchronized void shutDown() {
    if (connectionMonitorThread != null) {
      connectionManager.shutdown();
      connectionMonitorThread.shutdown();
    }
  }

  public HttpClient getHttpClient() {
    return httpClient;
  }


  private static class GzipDecompressingEntity extends HttpEntityWrapper {

    public GzipDecompressingEntity(final HttpEntity entity) {
      super(entity);
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {

      // the wrapped entity's getContent() decides about repeatability
      InputStream wrappedin = wrappedEntity.getContent();

      return new GZIPInputStream(wrappedin);
    }

    @Override
    public long getContentLength() {
      // length of ungzipped content is not known
      return -1;
    }
  }
}