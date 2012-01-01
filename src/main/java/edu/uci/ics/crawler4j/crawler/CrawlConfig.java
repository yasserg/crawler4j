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

package edu.uci.ics.crawler4j.crawler;

public class CrawlConfig {

	private String crawlStorageFolder;

	/**
	 * If this feature is enabled, you would be able to resume a previously
	 * stopped/crashed crawl. However, it makes crawling slightly slower
	 */
	private boolean resumableCrawling = false;

	/**
	 * Maximum depth of crawling For unlimited depth this parameter should be
	 * set to -1
	 */
	private int maxDepthOfCrawling = -1;

	/**
	 * Maximum number of pages to fetch For unlimited number of pages, this
	 * parameter should be set to -1
	 */
	private int maxPagesToFetch = -1;

	/**
	 * user-agent string that is used for representing your crawler to web
	 * servers. See http://en.wikipedia.org/wiki/User_agent for more details
	 */
	private String userAgentString = "crawler4j (http://code.google.com/p/crawler4j/)";

	/**
	 * Politeness delay in milliseconds (delay between sending two requests to
	 * the same host).
	 */
	private int politenessDelay = 200;

	/**
	 * Should we also crawl https pages?
	 */
	private boolean includeHttpsPages = false;

	/**
	 * Should we fetch binary content such as images, audio, ...?
	 */
	private boolean includeBinaryContentInCrawling = false;

	/**
	 * Default encoding of pages
	 */
	private String defaultEncoding = "UTF-8";

	/**
	 * Maximum Connections per host
	 */
	private int maxConnectionsPerHost = 100;

	/**
	 * Maximum total connections
	 */
	private int maxTotalConnections = 100;

	/**
	 * Socket timeout in milliseconds
	 */
	private int socketTimeout = 20000;

	/**
	 * Connection timeout in milliseconds
	 */
	private int connectionTimeout = 30000;

	/**
	 * Max number of outgoing links which are processed from a page
	 */
	private int maxOutgoingLinksToFollow = 5000;

	/**
	 * Max size of page is 1Mb
	 */
	private int maxDownloadSize = 1048576;

	/**
	 * Should we follow redirects?
	 */
	private boolean followRedirects = true;

	/**
	 * Should we log the 404 (Not Found) pages?
	 */
	private boolean show404PagesInLogs = false;

	/**
	 * If crawler should run behind a proxy, this parameter can be used for
	 * specifying the proxy host.
	 */
	private String proxyHost = null;

	/**
	 * If crawler should run behind a proxy, this parameter can be used for
	 * specifying the proxy port.
	 */
	private int proxyPort = 80;

	/**
	 * If crawler should run behind a proxy and user/pass is needed for
	 * authentication in proxy, this parameter can be used for specifying the
	 * username.
	 */
	private String proxyUsername = null;

	/**
	 * If crawler should run behind a proxy and user/pass is needed for
	 * authentication in proxy, this parameter can be used for specifying the
	 * password.
	 */
	private String proxyPassword = null;

	public CrawlConfig() {
	}

	public void validate() throws Exception {
		if (crawlStorageFolder == null) {
			throw new Exception("Crawl storage folder is not set in the CrawlConfig.");
		}
		if (politenessDelay < 0) {
			throw new Exception("Invalid value for politeness delay: " + politenessDelay);
		}
		if (maxDepthOfCrawling < -1) {
			throw new Exception("Maximum crawl depth should be either a positive number or -1 for unlimited depth.");
		}
		if (maxDepthOfCrawling > Short.MAX_VALUE) {
			throw new Exception("Maximum value for crawl depth is " + Short.MAX_VALUE);
		}

	}

	public String getCrawlStorageFolder() {
		return crawlStorageFolder;
	}

	public void setCrawlStorageFolder(String crawlStorageFolder) {
		this.crawlStorageFolder = crawlStorageFolder;
	}

	public boolean isResumableCrawling() {
		return resumableCrawling;
	}

	public void setResumableCrawling(boolean resumableCrawling) {
		this.resumableCrawling = resumableCrawling;
	}

	public int getMaxDepthOfCrawling() {
		return maxDepthOfCrawling;
	}

	public void setMaxDepthOfCrawling(int maxDepthOfCrawling) {
		this.maxDepthOfCrawling = maxDepthOfCrawling;
	}

	public int getMaxPagesToFetch() {
		return maxPagesToFetch;
	}

	public void setMaxPagesToFetch(int maxPagesToFetch) {
		this.maxPagesToFetch = maxPagesToFetch;
	}

	public String getUserAgentString() {
		return userAgentString;
	}

	public void setUserAgentString(String userAgentString) {
		this.userAgentString = userAgentString;
	}

	public int getPolitenessDelay() {
		return politenessDelay;
	}

	public void setPolitenessDelay(int politenessDelay) {
		this.politenessDelay = politenessDelay;
	}

	public boolean isIncludeHttpsPages() {
		return includeHttpsPages;
	}

	public void setIncludeHttpsPages(boolean includeHttpsPages) {
		this.includeHttpsPages = includeHttpsPages;
	}

	public boolean isIncludeBinaryContentInCrawling() {
		return includeBinaryContentInCrawling;
	}

	public void setIncludeBinaryContentInCrawling(boolean includeBinaryContentInCrawling) {
		this.includeBinaryContentInCrawling = includeBinaryContentInCrawling;
	}

	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	public int getMaxConnectionsPerHost() {
		return maxConnectionsPerHost;
	}

	public void setMaxConnectionsPerHost(int maxConnectionsPerHost) {
		this.maxConnectionsPerHost = maxConnectionsPerHost;
	}

	public int getMaxTotalConnections() {
		return maxTotalConnections;
	}

	public void setMaxTotalConnections(int maxTotalConnections) {
		this.maxTotalConnections = maxTotalConnections;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getMaxOutgoingLinksToFollow() {
		return maxOutgoingLinksToFollow;
	}

	public void setMaxOutgoingLinksToFollow(int maxOutgoingLinksToFollow) {
		this.maxOutgoingLinksToFollow = maxOutgoingLinksToFollow;
	}

	public int getMaxDownloadSize() {
		return maxDownloadSize;
	}

	public void setMaxDownloadSize(int maxDownloadSize) {
		this.maxDownloadSize = maxDownloadSize;
	}

	public boolean isFollowRedirects() {
		return followRedirects;
	}

	public void setFollowRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
	}

	public boolean isShow404PagesInLogs() {
		return show404PagesInLogs;
	}

	public void setShow404PagesInLogs(boolean show404PagesInLogs) {
		this.show404PagesInLogs = show404PagesInLogs;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyUsername() {
		return proxyUsername;
	}

	public void setProxyUsername(String proxyUsername) {
		this.proxyUsername = proxyUsername;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

}
