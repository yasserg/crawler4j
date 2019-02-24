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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.conn.DnsResolver;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.message.BasicHeader;

import edu.uci.ics.crawler4j.crawler.authentication.AuthInfo;

public class CrawlConfig {

    /**
     * The folder which will be used by crawler for storing the intermediate
     * crawl data. The content of this folder should not be modified manually.
     */
    private String crawlStorageFolder;

    /**
     * If this feature is enabled, you would be able to resume a previously
     * stopped/crashed crawl. However, it makes crawling slightly slower
     */
    private boolean resumableCrawling = false;

    /**
     * The lock timeout for the underlying sleepycat DB, in milliseconds
     */
    private long dbLockTimeout = 500;

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
    private String userAgentString = "crawler4j (https://github.com/yasserg/crawler4j/)";

    /**
     * Default request header values.
     */
    private Collection<BasicHeader> defaultHeaders = new HashSet<BasicHeader>();

    /**
     * Politeness delay in milliseconds (delay between sending two requests to
     * the same host).
     */
    private int politenessDelay = 200;

    /**
     * Should we also crawl https pages?
     */
    private boolean includeHttpsPages = true;

    /**
     * Should we fetch binary content such as images, audio, ...?
     */
    private boolean includeBinaryContentInCrawling = false;

    /**
     * Should we process binary content such as image, audio, ... using TIKA?
     */
    private boolean processBinaryContentInCrawling = false;

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
     * Max allowed size of a page. Pages larger than this size will not be
     * fetched.
     */
    private int maxDownloadSize = 1048576;

    /**
     * Should we follow redirects?
     */
    private boolean followRedirects = true;

    /**
     * Should the TLD list be updated automatically on each run? Alternatively,
     * it can be loaded from the embedded tld-names.zip file that was obtained from
     * https://publicsuffix.org/list/public_suffix_list.dat
     */
    private boolean onlineTldListUpdate = false;

    private String publicSuffixSourceUrl = "https://publicsuffix.org/list/public_suffix_list.dat";

    private String publicSuffixLocalFile = null;

    /**
     * Should the crawler stop running when the queue is empty?
     */
    private boolean shutdownOnEmptyQueue = true;

    /**
     * Wait this long before checking the status of the worker threads.
     */
    private int threadMonitoringDelaySeconds = 10;

    /**
     * Wait this long to verify the craweler threads are finished working.
     */
    private int threadShutdownDelaySeconds = 10;

    /**
     * Wait this long in seconds before launching cleanup.
     */
    private int cleanupDelaySeconds = 10;

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

    /**
     * List of possible authentications needed by crawler
     */
    private List<AuthInfo> authInfos;

    /**
     * Cookie policy
     */
    private String cookiePolicy = CookieSpecs.STANDARD;

    /**
     * Whether to honor "nofollow" flag
     */
    private boolean respectNoFollow = true;

    /**
     * Whether to honor "noindex" flag
     */
    private boolean respectNoIndex = true;

    /**
     * The {@link CookieStore} to use to store and retrieve cookies <br />
     * useful for passing initial cookies to the crawler.
     */
    private CookieStore cookieStore;

    /**
     * DNS resolver to use, {@link SystemDefaultDnsResolver} is default.
     */
    public void setDnsResolver(final DnsResolver dnsResolver) {
        this.dnsResolver = dnsResolver;
    }

    public DnsResolver getDnsResolver() {
        return dnsResolver;
    }

    private DnsResolver dnsResolver = new SystemDefaultDnsResolver();

    private boolean haltOnError = false;

    private boolean allowSingleLevelDomain = false;

    /*
     * number of pages to fetch/process from the database in a single read
     */
    private int batchReadSize = 50;

    /**
     * Validates the configs specified by this instance.
     *
     * @throws Exception on Validation fail
     */
    public void validate() throws Exception {
        if (crawlStorageFolder == null) {
            throw new Exception("Crawl storage folder is not set in the CrawlConfig.");
        }
        if (politenessDelay < 0) {
            throw new Exception("Invalid value for politeness delay: " + politenessDelay);
        }
        if (maxDepthOfCrawling < -1) {
            throw new Exception(
                "Maximum crawl depth should be either a positive number or -1 for unlimited depth" +
                ".");
        }
        if (maxDepthOfCrawling > Short.MAX_VALUE) {
            throw new Exception("Maximum value for crawl depth is " + Short.MAX_VALUE);
        }
    }

    public String getCrawlStorageFolder() {
        return crawlStorageFolder;
    }

    /**
     * Sets the folder which will be used by crawler for storing the
     * intermediate crawl data (e.g. list of urls that are extracted
     * from previously fetched pages and need to be crawled later).
     * Content of this folder should not be modified manually.
     */
    public void setCrawlStorageFolder(String crawlStorageFolder) {
        this.crawlStorageFolder = crawlStorageFolder;
    }

    public boolean isResumableCrawling() {
        return resumableCrawling;
    }

    /**
     * If this feature is enabled, you would be able to resume a previously
     * stopped/crashed crawl. However, it makes crawling slightly slower
     *
     * @param resumableCrawling Should crawling be resumable between runs ?
     */
    public void setResumableCrawling(boolean resumableCrawling) {
        this.resumableCrawling = resumableCrawling;
    }

    /**
     * Set the lock timeout for the underlying sleepycat DB, in milliseconds. Default is 500.
     *
     * @see com.sleepycat.je.EnvironmentConfig#setLockTimeout(long, java.util.concurrent.TimeUnit)
     * @param dbLockTimeout
     */
    public void setDbLockTimeout(long dbLockTimeout) {
        this.dbLockTimeout = dbLockTimeout;
    }

    public long getDbLockTimeout() {
        return this.dbLockTimeout;
    }

    public int getMaxDepthOfCrawling() {
        return maxDepthOfCrawling;
    }

    /**
     * Maximum depth of crawling For unlimited depth this parameter should be set to -1
     *
     * @param maxDepthOfCrawling Depth of crawling (all links on current page = depth of 1)
     */
    public void setMaxDepthOfCrawling(int maxDepthOfCrawling) {
        this.maxDepthOfCrawling = maxDepthOfCrawling;
    }

    public int getMaxPagesToFetch() {
        return maxPagesToFetch;
    }

    /**
     * Maximum number of pages to fetch For unlimited number of pages, this parameter should be
     * set to -1
     *
     * @param maxPagesToFetch How many pages to fetch from all threads together ?
     */
    public void setMaxPagesToFetch(int maxPagesToFetch) {
        this.maxPagesToFetch = maxPagesToFetch;
    }

    /**
     *
     * @return userAgentString
     */
    public String getUserAgentString() {
        return userAgentString;
    }

    /**
     * user-agent string that is used for representing your crawler to web
     * servers. See http://en.wikipedia.org/wiki/User_agent for more details
     *
     * @param userAgentString Custom userAgent string to use as your crawler's identifier
     */
    public void setUserAgentString(String userAgentString) {
        this.userAgentString = userAgentString;
    }

    /**
     * Return a copy of the default header collection.
     */
    public Collection<BasicHeader> getDefaultHeaders() {
        return new HashSet<>(defaultHeaders);
    }

    /**
     * Set the default header collection (creating copies of the provided headers).
     */
    public void setDefaultHeaders(Collection<? extends Header> defaultHeaders) {
        Collection<BasicHeader> copiedHeaders = new HashSet<>();
        for (Header header : defaultHeaders) {
            copiedHeaders.add(new BasicHeader(header.getName(), header.getValue()));
        }
        this.defaultHeaders = copiedHeaders;
    }

    public int getPolitenessDelay() {
        return politenessDelay;
    }

    /**
     * Politeness delay in milliseconds (delay between sending two requests to
     * the same host).
     *
     * @param politenessDelay
     *            the delay in milliseconds.
     */
    public void setPolitenessDelay(int politenessDelay) {
        this.politenessDelay = politenessDelay;
    }

    public boolean isIncludeHttpsPages() {
        return includeHttpsPages;
    }

    /**
     * @param includeHttpsPages Should we crawl https pages?
     */
    public void setIncludeHttpsPages(boolean includeHttpsPages) {
        this.includeHttpsPages = includeHttpsPages;
    }

    public boolean isIncludeBinaryContentInCrawling() {
        return includeBinaryContentInCrawling;
    }

    /**
     *
     * @param includeBinaryContentInCrawling Should we fetch binary content such as images,
     * audio, ...?
     */
    public void setIncludeBinaryContentInCrawling(boolean includeBinaryContentInCrawling) {
        this.includeBinaryContentInCrawling = includeBinaryContentInCrawling;
    }

    public boolean isProcessBinaryContentInCrawling() {
        return processBinaryContentInCrawling;
    }

    /**
     * Should we process binary content such as images, audio, ... using TIKA?
     */
    public void setProcessBinaryContentInCrawling(boolean processBinaryContentInCrawling) {
        this.processBinaryContentInCrawling = processBinaryContentInCrawling;
    }

    public int getMaxConnectionsPerHost() {
        return maxConnectionsPerHost;
    }

    /**
     * @param maxConnectionsPerHost Maximum Connections per host
     */
    public void setMaxConnectionsPerHost(int maxConnectionsPerHost) {
        this.maxConnectionsPerHost = maxConnectionsPerHost;
    }

    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }

    /**
     * @param maxTotalConnections Maximum total connections
     */
    public void setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * @param socketTimeout Socket timeout in milliseconds
     */
    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * @param connectionTimeout Connection timeout in milliseconds
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getMaxOutgoingLinksToFollow() {
        return maxOutgoingLinksToFollow;
    }

    /**
     * @param maxOutgoingLinksToFollow Max number of outgoing links which are processed from a page
     */
    public void setMaxOutgoingLinksToFollow(int maxOutgoingLinksToFollow) {
        this.maxOutgoingLinksToFollow = maxOutgoingLinksToFollow;
    }

    public int getMaxDownloadSize() {
        return maxDownloadSize;
    }

    /**
     * @param maxDownloadSize Max allowed size of a page. Pages larger than this size will not be
     * fetched.
     */
    public void setMaxDownloadSize(int maxDownloadSize) {
        this.maxDownloadSize = maxDownloadSize;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    /**
     * @param followRedirects Should we follow redirects?
     */
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public boolean isShutdownOnEmptyQueue() {
        return shutdownOnEmptyQueue;
    }

    /**
     * Should the crawler stop running when the queue is empty?
     */
    public void setShutdownOnEmptyQueue(boolean shutdown) {
        shutdownOnEmptyQueue = shutdown;
    }

    public boolean isOnlineTldListUpdate() {
        return onlineTldListUpdate;
    }

    /**
     * Should the TLD list be updated automatically on each run? Alternatively,
     * it can be loaded from the embedded tld-names.txt resource file that was
     * obtained from https://publicsuffix.org/list/public_suffix_list.dat
     */
    public void setOnlineTldListUpdate(boolean online) {
        onlineTldListUpdate = online;
    }

    public String getPublicSuffixSourceUrl() {
        return publicSuffixSourceUrl;
    }

    /**
     * URL from which the public suffix list is obtained.  By default
     * this is https://publicsuffix.org/list/public_suffix_list.dat
     */
    public void setPublicSuffixSourceUrl(String publicSuffixSourceUrl) {
        this.publicSuffixSourceUrl = publicSuffixSourceUrl;
    }

    public String getPublicSuffixLocalFile() {
        return publicSuffixLocalFile;
    }

    /**
     * Only used if {@link #setOnlineTldListUpdate(boolean)} is {@code true}. If
     * this property is not null then it overrides
     * {@link #setPublicSuffixSourceUrl(String)}
     *
     * @param publicSuffixLocalFile local filename of public suffix list
     */
    public void setPublicSuffixLocalFile(String publicSuffixLocalFile) {
        this.publicSuffixLocalFile = publicSuffixLocalFile;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * @param proxyHost If crawler should run behind a proxy, this parameter can be used for
     * specifying the proxy host.
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * @param proxyPort If crawler should run behind a proxy, this parameter can be used for
     * specifying the proxy port.
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    /**
     * @param proxyUsername
     *        If crawler should run behind a proxy and user/pass is needed for
     *        authentication in proxy, this parameter can be used for specifying the username.
     */
    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * If crawler should run behind a proxy and user/pass is needed for
     * authentication in proxy, this parameter can be used for specifying the password.
     *
     * @param proxyPassword String Password
     */
    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    /**
     * @return the authentications Information
     */
    public List<AuthInfo> getAuthInfos() {
        return authInfos;
    }

    public void addAuthInfo(AuthInfo authInfo) {
        if (this.authInfos == null) {
            this.authInfos = new ArrayList<AuthInfo>();
        }

        this.authInfos.add(authInfo);
    }

    /**
     * @param authInfos authenticationInformations to set
     */
    public void setAuthInfos(List<AuthInfo> authInfos) {
        this.authInfos = authInfos;
    }

    public int getThreadMonitoringDelaySeconds() {
        return threadMonitoringDelaySeconds;
    }

    public void setThreadMonitoringDelaySeconds(int delay) {
        this.threadMonitoringDelaySeconds = delay;
    }

    public int getThreadShutdownDelaySeconds() {
        return threadShutdownDelaySeconds;
    }

    public void setThreadShutdownDelaySeconds(int delay) {
        this.threadShutdownDelaySeconds = delay;
    }

    public int getCleanupDelaySeconds() {
        return cleanupDelaySeconds;
    }

    public void setCleanupDelaySeconds(int delay) {
        this.cleanupDelaySeconds = delay;
    }

    public String getCookiePolicy() {
        return cookiePolicy;
    }

    public void setCookiePolicy(String cookiePolicy) {
        this.cookiePolicy = cookiePolicy;
    }

    /**
     * Gets the configured {@link CookieStore} or null if none is set
     * @return the {@link CookieStore}
     */

    public CookieStore getCookieStore() {
        return cookieStore;
    }

    /**
     * Sets the {@link CookieStore to be used}
     * @param cookieStore the {@link CookieStore}
     */
    public void setCookieStore(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    /**
     * Gets the current {@link CookieStore} used
     * @return the {@link CookieStore}
     */
    public boolean isRespectNoFollow() {
        return respectNoFollow;
    }

    public void setRespectNoFollow(boolean respectNoFollow) {
        this.respectNoFollow = respectNoFollow;
    }

    public boolean isRespectNoIndex() {
        return respectNoIndex;
    }

    public void setRespectNoIndex(boolean respectNoIndex) {
        this.respectNoIndex = respectNoIndex;
    }

    /**
     * Indicates if all crawling will stop if an unexpected error occurs.
     */
    public boolean isHaltOnError() {
        return haltOnError;
    }

    /**
     * Should all crawling stop if an unexpected error occurs? Default is
     * {@code false}.
     *
     * @param haltOnError
     *            {@code true} if all crawling should be halted
     */
    public void setHaltOnError(boolean haltOnError) {
        this.haltOnError = haltOnError;
    }

    /**
     * Are single level domains (e.g. http://localhost) considered valid?
     */
    public boolean isAllowSingleLevelDomain() {
        return allowSingleLevelDomain;
    }

    /**
     * Allow single level domains (e.g. http://localhost). This is very useful for
     * testing especially when you may be using localhost.
     *
     * @param allowSingleLevelDomain
     *            {@code true} if single level domain should be considered valid
     */
    public void setAllowSingleLevelDomain(boolean allowSingleLevelDomain) {
        this.allowSingleLevelDomain = allowSingleLevelDomain;
    }

    /**
     * Number of pages to fetch/process from the database in a single read transaction.
     *
     * @return the batch read size
     */
    public int getBatchReadSize() {
        return batchReadSize;
    }

    public void setBatchReadSize(int batchReadSize) {
        this.batchReadSize = batchReadSize;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Crawl storage folder: " + getCrawlStorageFolder() + "\n");
        sb.append("Resumable crawling: " + isResumableCrawling() + "\n");
        sb.append("Max depth of crawl: " + getMaxDepthOfCrawling() + "\n");
        sb.append("Max pages to fetch: " + getMaxPagesToFetch() + "\n");
        sb.append("User agent string: " + getUserAgentString() + "\n");
        sb.append("Include https pages: " + isIncludeHttpsPages() + "\n");
        sb.append("Include binary content: " + isIncludeBinaryContentInCrawling() + "\n");
        sb.append("Max connections per host: " + getMaxConnectionsPerHost() + "\n");
        sb.append("Max total connections: " + getMaxTotalConnections() + "\n");
        sb.append("Socket timeout: " + getSocketTimeout() + "\n");
        sb.append("Max total connections: " + getMaxTotalConnections() + "\n");
        sb.append("Max outgoing links to follow: " + getMaxOutgoingLinksToFollow() + "\n");
        sb.append("Max download size: " + getMaxDownloadSize() + "\n");
        sb.append("Should follow redirects?: " + isFollowRedirects() + "\n");
        sb.append("Proxy host: " + getProxyHost() + "\n");
        sb.append("Proxy port: " + getProxyPort() + "\n");
        sb.append("Proxy username: " + getProxyUsername() + "\n");
        sb.append("Thread monitoring delay: " + getThreadMonitoringDelaySeconds() + "\n");
        sb.append("Thread shutdown delay: " + getThreadShutdownDelaySeconds() + "\n");
        sb.append("Cleanup delay: " + getCleanupDelaySeconds() + "\n");
        sb.append("Cookie policy: " + getCookiePolicy() + "\n");
        sb.append("Respect nofollow: " + isRespectNoFollow() + "\n");
        sb.append("Respect noindex: " + isRespectNoIndex() + "\n");
        sb.append("Halt on error: " + isHaltOnError() + "\n");
        sb.append("Allow single level domain:" + isAllowSingleLevelDomain() + "\n");
        sb.append("Batch read size: " + getBatchReadSize() + "\n");
        return sb.toString();
    }
}
