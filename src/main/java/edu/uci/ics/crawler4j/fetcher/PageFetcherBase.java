package edu.uci.ics.crawler4j.fetcher;

import edu.uci.ics.crawler4j.crawler.Configurable;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.exceptions.PageBiggerThanMaxSizeException;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by lost on 9/7/2016.
 */
public class PageFetcherBase extends Configurable {
	protected static final Logger logger = LoggerFactory.getLogger(PageFetcher.class);

	public PageFetcherBase(CrawlConfig config) {
		super(config);
	}

	protected static BasicCredentialsProvider getCredentialsProvider(CrawlConfig config) {
		BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(new AuthScope(config.getProxyHost(), config.getProxyPort()),
				new UsernamePasswordCredentials(config.getProxyUsername(),
						config.getProxyPassword()));
		return credentialsProvider;
	}

	protected static RequestConfig getRequestConfig(CrawlConfig config) {
		return RequestConfig.custom().setExpectContinueEnabled(false).setCookieSpec(CookieSpecs.DEFAULT)
				.setRedirectsEnabled(false).setSocketTimeout(config.getSocketTimeout())
				.setConnectTimeout(config.getConnectionTimeout()).build();
	}

	protected void ParseResponse(PageFetchResult fetchResult, String toFetchURL, HttpUriRequest request, HttpResponse response) throws IOException, PageBiggerThanMaxSizeException {
		fetchResult.setEntity(response.getEntity());
		fetchResult.setResponseHeaders(response.getAllHeaders());

		// Setting HttpStatus
		int statusCode = response.getStatusLine().getStatusCode();

		// If Redirect ( 3xx )
		if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY ||
				statusCode == HttpStatus.SC_MULTIPLE_CHOICES || statusCode == HttpStatus.SC_SEE_OTHER ||
				statusCode == HttpStatus.SC_TEMPORARY_REDIRECT ||
				statusCode == 308) { // todo follow https://issues.apache.org/jira/browse/HTTPCORE-389

			Header header = response.getFirstHeader("Location");
			if (header != null) {
				String movedToUrl = URLCanonicalizer.getCanonicalURL(header.getValue(), toFetchURL);
				fetchResult.setMovedToUrl(movedToUrl);
			}
		} else if (statusCode >= 200 && statusCode <= 299) { // is 2XX, everything looks ok
			fetchResult.setFetchedUrl(toFetchURL);
			String uri = request.getURI().toString();
			if (!uri.equals(toFetchURL)) {
				if (!URLCanonicalizer.getCanonicalURL(uri).equals(toFetchURL)) {
					fetchResult.setFetchedUrl(uri);
				}
			}

			// Checking maximum size
			if (fetchResult.getEntity() != null) {
				long size = fetchResult.getEntity().getContentLength();
				if (size == -1) {
					Header length = response.getLastHeader("Content-Length");
					if (length == null) {
						length = response.getLastHeader("Content-length");
					}
					if (length != null) {
						size = Integer.parseInt(length.getValue());
					}
				}
				if (size > config.getMaxDownloadSize()) {
					throw new PageBiggerThanMaxSizeException(size);
				}
			}
		}

		fetchResult.setStatusCode(statusCode);
	}

	/**
	 * Creates a new HttpUriRequest for the given url. The default is to create a HttpGet without
	 * any further configuration. Subclasses may override this method and provide their own logic.
	 *
	 * @param url the url to be fetched
	 * @return the HttpUriRequest for the given url
	 */
	protected HttpUriRequest newHttpUriRequest(String url) {
		return new HttpGet(url);
	}
}
