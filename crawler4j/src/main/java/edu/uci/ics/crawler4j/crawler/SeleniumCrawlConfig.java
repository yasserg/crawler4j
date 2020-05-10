package edu.uci.ics.crawler4j.crawler;

import java.util.List;

public class SeleniumCrawlConfig extends CrawlConfig {

    /**
     * If true, selenium will be used when an URL does not match inclussion / exclussion patterns
     */
    private boolean defaultToSelenium = false;

    /**
     * List of regular expressions. If a URL matches any of them, it will never be processed by selenium
     */
    private List<String> seleniumExcludes;

    /**
     * List of regular expressions. If a URL matches any of them and doesn't match any exclussion,
     * it will be processed with Selenium
     */
    private List<String> seleniumIncludes;

    private boolean cookiesSelemiun;

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
        sb.append("Default to selenium: " + isDefaultToSelenium() + "\n");
        sb.append("Cookies to selenium: " + isCookiesSelemiun() + "\n");
        sb.append("Selenium inclussion patterns: " + getSeleniumIncludes() + "\n");
        sb.append("Selenium exclussion patterns: " + getSeleniumExcludes() + "\n");
        return sb.toString();
    }

    public boolean isDefaultToSelenium() {
        return defaultToSelenium;
    }

    public void setDefaultToSelenium(boolean defaultToSelenium) {
        this.defaultToSelenium = defaultToSelenium;
    }

    public List<String> getSeleniumExcludes() {
        return seleniumExcludes;
    }

    public void setSeleniumExcludes(List<String> seleniumExcludes) {
        this.seleniumExcludes = seleniumExcludes;
    }

    public List<String> getSeleniumIncludes() {
        return seleniumIncludes;
    }

    public void setSeleniumIncludes(List<String> seleniumIncludes) {
        this.seleniumIncludes = seleniumIncludes;
    }

    public boolean isCookiesSelemiun() {
        return cookiesSelemiun;
    }

    public void setCookiesSelemiun(boolean cookiesSelemiun) {
        this.cookiesSelemiun = cookiesSelemiun;
    }
}
