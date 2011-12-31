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

import edu.uci.ics.crawler4j.crawler.Configurable;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.GZIPInputStream;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */

public class PageFetcher extends Configurable {

	protected static final Logger logger = Logger.getLogger(PageFetcher.class);

	protected ThreadSafeClientConnManager connectionManager;

	protected DefaultHttpClient httpClient;

	protected final Object mutex = new Object();

	protected long lastFetchTime = 0;

	protected IdleConnectionMonitorThread connectionMonitorThread = null;

	protected HttpEntity entity = null;
	protected String fetchedUrl = null;

	public PageFetcher(CrawlConfig config) {
		super(config);

		HttpParams params = new BasicHttpParams();
		HttpProtocolParamBean paramsBean = new HttpProtocolParamBean(params);
		paramsBean.setVersion(HttpVersion.HTTP_1_1);
		paramsBean.setContentCharset("UTF-8");
		paramsBean.setUseExpectContinue(false);

		params.setParameter(CoreProtocolPNames.USER_AGENT, config.getUserAgentString());
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, config.getSocketTimeout());
		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, config.getConnectionTimeout());

		params.setBooleanParameter("http.protocol.handle-redirects", false);

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

		if (config.isIncludeHttpsPages()) {
			schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
		}

		connectionManager = new ThreadSafeClientConnManager(schemeRegistry);
		connectionManager.setMaxTotal(config.getMaxTotalConnections());
		connectionManager.setDefaultMaxPerRoute(config.getMaxConnectionsPerHost());
		logger.setLevel(Level.INFO);
		httpClient = new DefaultHttpClient(connectionManager, params);

		if (config.getProxyHost() != null) {

			if (config.getProxyUsername() != null) {
				httpClient.getCredentialsProvider().setCredentials(
						new AuthScope(config.getProxyHost(), config.getProxyPort()),
						new UsernamePasswordCredentials(config.getProxyUsername(), config.getProxyPassword()));
			}

			HttpHost proxy = new HttpHost(config.getProxyHost(), config.getProxyPort());
			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }

        httpClient.addResponseInterceptor(new HttpResponseInterceptor() {

            @Override
            public void process(final HttpResponse response, final HttpContext context) throws HttpException,
                    IOException {
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

		if (connectionMonitorThread == null) {
			connectionMonitorThread = new IdleConnectionMonitorThread(connectionManager);
		}
		connectionMonitorThread.start();

	}

	public int fetchHeader(WebURL webUrl) {
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
			get.addHeader("Accept-Encoding", "gzip");
			HttpResponse response = httpClient.execute(get);
			entity = response.getEntity();

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				if (statusCode != HttpStatus.SC_NOT_FOUND) {
					if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
						Header header = response.getFirstHeader("Location");
						if (header != null) {
							String movedToUrl = header.getValue();
							movedToUrl = URLCanonicalizer.getCanonicalURL(movedToUrl, toFetchURL).toExternalForm();
							webUrl.setURL(movedToUrl);
						} else {
							webUrl.setURL(null);
						}
						return PageFetchStatus.Moved;
					}
					logger.info("Failed: " + response.getStatusLine().toString() + ", while fetching " + toFetchURL);
				} else if (config.isShow404PagesInLogs()) {
					logger.info("Not Found: " + toFetchURL + " (Link found in doc#: " + webUrl.getParentDocid() + ")");
				}
				return response.getStatusLine().getStatusCode();
			}

			fetchedUrl = toFetchURL;
			String uri = get.getURI().toString();
			if (!uri.equals(toFetchURL)) {
				if (!URLCanonicalizer.getCanonicalURL(uri).equals(toFetchURL)) {
					fetchedUrl = uri;
				}
			}

			if (entity != null) {
				long size = entity.getContentLength();
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
					return PageFetchStatus.PageTooBig;
				}

				return PageFetchStatus.OK;

			} else {
				get.abort();
			}
		} catch (IOException e) {
			logger.error("Fatal transport error: " + e.getMessage() + " while fetching " + toFetchURL
					+ " (link found in doc #" + webUrl.getParentDocid() + ")");
			return PageFetchStatus.FatalTransportError;
		} catch (IllegalStateException e) {
			// ignoring exceptions that occur because of not registering https
			// and other schemes
		} catch (Exception e) {
			if (e.getMessage() == null) {
				logger.error("Error while fetching " + webUrl.getURL());
			} else {
				logger.error(e.getMessage() + " while fetching " + webUrl.getURL());
			}
		} finally {
			try {
				if (entity == null && get != null) {
					get.abort();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return PageFetchStatus.UnknownError;
	}

	public boolean fetchContent(Page page) {
		try {
			page.load(entity);
			return true;
		} catch (Exception e) {
			logger.info("Exception while fetching content for: " + page.getWebURL().getURL() + " [" + e.getMessage()
					+ "]");
		}
		return false;
	}

	public void discardContentIfNotConsumed() {
		try {
			if (entity != null) {
				EntityUtils.consume(entity);
			}
		} catch (EOFException e) {
			// We can ignore this exception. It can happen on compressed streams which are not
			// repeatable
		} catch (IOException e) {
			// We can ignore this exception. It can happen if the stream is closed.
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void shutDown() {
		if (connectionMonitorThread != null) {
			connectionManager.shutdown();
			connectionMonitorThread.shutdown();
		}
	}

	public String getFetchedUrl() {
		return fetchedUrl;
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
