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

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.authentication.AuthInfo;
import edu.uci.ics.crawler4j.crawler.authentication.BasicAuthInfo;
import edu.uci.ics.crawler4j.crawler.authentication.FormAuthInfo;
import edu.uci.ics.crawler4j.crawler.authentication.NtAuthInfo;
import edu.uci.ics.crawler4j.crawler.exceptions.PageBiggerThanMaxSizeException;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Yasser Ganjisaffar
 */
public class PageFetcher extends PageFetcherBase {

  protected PoolingHttpClientConnectionManager connectionManager;
  protected CloseableHttpClient httpClient;
  protected final Object mutex = new Object();
  protected long lastFetchTime = 0;
  protected IdleConnectionMonitorThread connectionMonitorThread = null;
  protected final Registry<ConnectionSocketFactory> connRegistry;

  public PageFetcher(CrawlConfig config) {
    super(config);

    RequestConfig requestConfig = getRequestConfig(config);

    RegistryBuilder<ConnectionSocketFactory> connRegistryBuilder = RegistryBuilder.create();
    connRegistryBuilder.register("http", PlainConnectionSocketFactory.INSTANCE);
    if (config.isIncludeHttpsPages()) {
      if (config.getSkipSSLVerification()) {
        try { // Fixing: https://code.google.com/p/crawler4j/issues/detail?id=174
          // By always trusting the ssl certificate
          SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
            @Override
            public boolean isTrusted(final X509Certificate[] chain, String authType) {
              return true;
            }
          }).build();
          SSLConnectionSocketFactory sslsf =
                  new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
          connRegistryBuilder.register("https", sslsf);
        } catch (Exception e) {
          logger.warn("Exception thrown while trying to register https");
          logger.debug("Stacktrace", e);
        }
      } else {
        connRegistryBuilder.register("https", SSLConnectionSocketFactory.getSystemSocketFactory());
      }
    }
    connRegistry = connRegistryBuilder.build();

    connectionManager = new PoolingHttpClientConnectionManager(connRegistry);
    connectionManager.setMaxTotal(config.getMaxTotalConnections());
    connectionManager.setDefaultMaxPerRoute(config.getMaxConnectionsPerHost());

    HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    clientBuilder.setDefaultRequestConfig(requestConfig);
    clientBuilder.setConnectionManager(connectionManager);
    clientBuilder.setUserAgent(config.getUserAgentString());
    clientBuilder.setDefaultHeaders(config.getDefaultHeaders());

    if (config.getProxyHost() != null) {
      if (config.getProxyUsername() != null) {
        BasicCredentialsProvider credentialsProvider = getCredentialsProvider(config);
        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
      }

      HttpHost proxy = new HttpHost(config.getProxyHost(), config.getProxyPort());
      clientBuilder.setProxy(proxy);
      logger.debug("Working through Proxy: {}", proxy.getHostName());
    }

    httpClient = clientBuilder.build();
    if ((config.getAuthInfos() != null) && !config.getAuthInfos().isEmpty()) {
      doAuthetication(config.getAuthInfos());
    }

    if (connectionMonitorThread == null) {
      connectionMonitorThread = new IdleConnectionMonitorThread(connectionManager);
    }
    connectionMonitorThread.start();
  }

  private void doAuthetication(List<AuthInfo> authInfos) {
    for (AuthInfo authInfo : authInfos) {
      if (authInfo.getAuthenticationType() == AuthInfo.AuthenticationType.BASIC_AUTHENTICATION) {
        doBasicLogin((BasicAuthInfo) authInfo);
      } else if (authInfo.getAuthenticationType() == AuthInfo.AuthenticationType.NT_AUTHENTICATION) {
        doNtLogin((NtAuthInfo) authInfo);
      } else {
        doFormLogin((FormAuthInfo) authInfo);
      }
    }
  }

  /**
   * BASIC authentication<br/>
   * Official Example: https://hc.apache.org/httpcomponents-client-ga/httpclient/examples/org/apache/http/examples
   * /client/ClientAuthentication.java
   * */
  private void doBasicLogin(BasicAuthInfo authInfo) {
    logger.info("BASIC authentication for: " + authInfo.getLoginTarget());
    HttpHost targetHost = new HttpHost(authInfo.getHost(), authInfo.getPort(), authInfo.getProtocol());
    CredentialsProvider credsProvider = new BasicCredentialsProvider();
    credsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                                 new UsernamePasswordCredentials(authInfo.getUsername(), authInfo.getPassword()));
    httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
  }

  /**
   * Do NT auth for Microsoft AD sites.
   */
  private void doNtLogin(NtAuthInfo authInfo) {
    logger.info("NT authentication for: " + authInfo.getLoginTarget());
    HttpHost targetHost = new HttpHost(authInfo.getHost(), authInfo.getPort(), authInfo.getProtocol());
    CredentialsProvider credsProvider = new BasicCredentialsProvider();
    try {
      credsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()),
              new NTCredentials(authInfo.getUsername(), authInfo.getPassword(),
                      InetAddress.getLocalHost().getHostName(), authInfo.getDomain()));
    } catch (UnknownHostException e) {
      logger.error("Error creating NT credentials", e);
    }
    httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
  }

  /**
   * FORM authentication<br/>
   * Official Example:
   *  https://hc.apache.org/httpcomponents-client-ga/httpclient/examples/org/apache/http/examples/client/ClientFormLogin.java
   */
  private void doFormLogin(FormAuthInfo authInfo) {
    logger.info("FORM authentication for: " + authInfo.getLoginTarget());
    String fullUri =
        authInfo.getProtocol() + "://" + authInfo.getHost() + ":" + authInfo.getPort() + authInfo.getLoginTarget();
    HttpPost httpPost = new HttpPost(fullUri);
    List<NameValuePair> formParams = new ArrayList<>();
    formParams.add(new BasicNameValuePair(authInfo.getUsernameFormStr(), authInfo.getUsername()));
    formParams.add(new BasicNameValuePair(authInfo.getPasswordFormStr(), authInfo.getPassword()));

    try {
      UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, "UTF-8");
      httpPost.setEntity(entity);
      httpClient.execute(httpPost);
      logger.debug("Successfully Logged in with user: " + authInfo.getUsername() + " to: " + authInfo.getHost());
    } catch (UnsupportedEncodingException e) {
      logger.error("Encountered a non supported encoding while trying to login to: " + authInfo.getHost(), e);
    } catch (ClientProtocolException e) {
      logger.error("While trying to login to: " + authInfo.getHost() + " - Client protocol not supported", e);
    } catch (IOException e) {
      logger.error("While trying to login to: " + authInfo.getHost() + " - Error making request", e);
    }
  }

  public PageFetchResult fetchPage(WebURL webUrl)
      throws InterruptedException, IOException, PageBiggerThanMaxSizeException {
    // Getting URL, setting headers & content
    PageFetchResult fetchResult = new PageFetchResult();
    String toFetchURL = webUrl.getURL();
    HttpUriRequest request = null;
    try {
      request = newHttpUriRequest(toFetchURL);
      // Applying Politeness delay
      synchronized (mutex) {
        long now = (new Date()).getTime();
        if ((now - lastFetchTime) < config.getPolitenessDelay()) {
          Thread.sleep(config.getPolitenessDelay() - (now - lastFetchTime));
        }
        lastFetchTime = (new Date()).getTime();
      }

      CloseableHttpResponse response = httpClient.execute(request);
      try {
        ParseResponse(fetchResult, toFetchURL, request, response);
      } finally {
        response.close();
      }
      return fetchResult;

    } finally { // occurs also with thrown exceptions
      if ((fetchResult.getEntity() == null) && (request != null)) {
        request.abort();
      }
    }
  }

  public synchronized void shutDown() {
    if (connectionMonitorThread != null) {
      connectionManager.shutdown();
      connectionMonitorThread.shutdown();
    }
  }

}
