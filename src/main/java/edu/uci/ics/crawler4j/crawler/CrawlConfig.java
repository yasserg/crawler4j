/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.crawler;

import edu.uci.ics.crawler4j.crawler.authentication.AuthInfo;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class CrawlConfig {
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
     * https://publicsuffix.org/list/effective_tld_names.dat
     */
    private boolean onlineTldListUpdate = false;

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
     * Validates the configs specified by this instance.
     *
     * @throws Exception on Validation fail
     */
    public void validate() throws Exception {
        if (politenessDelay < 0) {
            throw new Exception("Invalid value for politeness delay: " + politenessDelay);
        }
    }

    /**
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
     * @param politenessDelay the delay in milliseconds.
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
     * @param includeBinaryContentInCrawling Should we fetch binary content such as images,
     *                                       audio, ...?
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
     *                        fetched.
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
     * obtained from https://publicsuffix.org/list/effective_tld_names.dat
     */
    public void setOnlineTldListUpdate(boolean online) {
        onlineTldListUpdate = online;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * @param proxyHost If crawler should run behind a proxy, this parameter can be used for
     *                  specifying the proxy host.
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * @param proxyPort If crawler should run behind a proxy, this parameter can be used for
     *                  specifying the proxy port.
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    /**
     * @param proxyUsername If crawler should run behind a proxy and user/pass is needed for
     *                      authentication in proxy, this parameter can be used for specifying the username.
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

    /**
     * @param authInfos authenticationInformations to set
     */
    public void setAuthInfos(List<AuthInfo> authInfos) {
        this.authInfos = authInfos;
    }

    public void addAuthInfo(AuthInfo authInfo) {
        if (this.authInfos == null) {
            this.authInfos = new ArrayList<AuthInfo>();
        }

        this.authInfos.add(authInfo);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
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
        sb.append("Proxy password: " + getProxyPassword() + "\n");
        sb.append("Thread monitoring delay: " + getThreadMonitoringDelaySeconds() + "\n");
        sb.append("Thread shutdown delay: " + getThreadShutdownDelaySeconds() + "\n");
        sb.append("Cleanup delay: " + getCleanupDelaySeconds() + "\n");
        return sb.toString();
    }
}
