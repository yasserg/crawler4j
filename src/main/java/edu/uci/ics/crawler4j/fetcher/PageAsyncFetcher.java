package edu.uci.ics.crawler4j.fetcher;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.exceptions.PageBiggerThanMaxSizeException;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by lost on 9/7/2016.
 */
public class PageAsyncFetcher extends PageFetcherBase {
	protected final CloseableHttpAsyncClient httpClient;
	protected final PoolingNHttpClientConnectionManager connectionManager;
	private final AtomicLong nextCleanupTimeMs = new AtomicLong();

	public PageAsyncFetcher(CrawlConfig config) {
		super(config);

		if ((config.getAuthInfos() != null) && !config.getAuthInfos().isEmpty()) {
			throw new UnsupportedOperationException("Authentication is not implemented");
		}

		if (config.getPolitenessDelay() > 0) {
			throw new UnsupportedOperationException("PolitenessDelay is not implemented");
		}

		RequestConfig requestConfig = getRequestConfig(config);

		SchemeIOSessionStrategy sslSessionStrategy = config.getSkipSSLVerification()
				? buildNoVerificationSSLSessionStrategy()
				: SSLIOSessionStrategy.getSystemDefaultStrategy();
		Registry<SchemeIOSessionStrategy> uriSchemeSessionRegistry = RegistryBuilder.<SchemeIOSessionStrategy>create()
				.register("https", sslSessionStrategy)
				.register("http", NoopIOSessionStrategy.INSTANCE)
				.build();
		try {
			connectionManager = new PoolingNHttpClientConnectionManager(new DefaultConnectingIOReactor(IOReactorConfig.DEFAULT), uriSchemeSessionRegistry);
		} catch (IOReactorException e) {
			throw new RuntimeException(e);
		}
		connectionManager.setMaxTotal(config.getMaxTotalConnections());
		connectionManager.setDefaultMaxPerRoute(config.getMaxConnectionsPerHost());

		HttpAsyncClientBuilder clientBuilder = HttpAsyncClientBuilder.create();
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
		nextCleanupTimeMs.set(new Date().getTime());
		updateLastCleanup();
	}

	private boolean updateLastCleanup() {
		long now = new Date().getTime();
		long scheduledCleanup = nextCleanupTimeMs.get();
		if (scheduledCleanup < now) {
			return nextCleanupTimeMs.compareAndSet(scheduledCleanup, now);
		}

		return false;
	}

	public void fetchPage(WebURL webUrl, final FutureCallback<PageFetchResult> callback)
			throws InterruptedException, IOException, PageBiggerThanMaxSizeException {
		if (webUrl == null)
			throw new IllegalArgumentException("Must supply webUrl");
		if (callback == null)
			throw new IllegalArgumentException("Must supply callback");

		cleanupIfNecessary();

		// Getting URL, setting headers & content
		final PageFetchResult fetchResult = new PageFetchResult();
		final String toFetchURL = webUrl.getURL();
		final HttpUriRequest request = newHttpUriRequest(toFetchURL);

		try {
			// TODO Applying Politeness delay
			httpClient.execute(request, new FutureCallback<HttpResponse>() {
				@Override
				public void completed(HttpResponse response) {
					try {
						ParseResponse(fetchResult, toFetchURL, request, response);
					} catch (Exception e) {
						callback.failed(e);
					}
					callback.completed(fetchResult);
				}

				@Override
				public void failed(Exception e) {
					callback.failed(e);
				}

				@Override
				public void cancelled() {
					callback.cancelled();
				}
			});
		} finally { // occurs also with thrown exceptions
			if ((fetchResult.getEntity() == null) && (request != null)) {
				request.abort();
			}
		}
	}

	public void shutdown() throws IOException {
		connectionManager.shutdown();
	}

	private void cleanupIfNecessary() {
		if (!updateLastCleanup())
			return;

		cleanupConnections();
	}

	protected void cleanupConnections() {
		connectionManager.closeExpiredConnections();
		connectionManager.closeIdleConnections(30, TimeUnit.SECONDS);
	}

	private static SSLIOSessionStrategy buildNoVerificationSSLSessionStrategy() {
		try {
			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
				@Override
				public boolean isTrusted(final X509Certificate[] chain, String authType) {
					return true;
				}
			}).build();
			return new SSLIOSessionStrategy(sslContext, NoopHostnameVerifier.INSTANCE);
		} catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
			throw new RuntimeException(e);
		}
	}
}
