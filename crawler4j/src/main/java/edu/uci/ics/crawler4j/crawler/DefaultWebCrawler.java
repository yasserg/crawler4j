package edu.uci.ics.crawler4j.crawler;

import java.util.*;

import org.apache.http.HttpStatus;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.slf4j.*;

import edu.uci.ics.crawler4j.CrawlerConfiguration;
import edu.uci.ics.crawler4j.crawler.controller.CrawlController;
import edu.uci.ics.crawler4j.crawler.exceptions.*;
import edu.uci.ics.crawler4j.fetcher.*;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.frontier.pageharvests.PageHarvests;
import edu.uci.ics.crawler4j.parser.*;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;

public class DefaultWebCrawler implements WebCrawler {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultWebCrawler.class);

    /**
     * The id associated to the crawler thread running this instance
     */
    private final int id;

    private final CrawlerConfiguration configuration;

    /**
     * The controller instance that has created this crawler thread. This reference to the
     * controller can be used for getting configurations of the current crawl or adding new seeds
     * during runtime.
     */
    private final CrawlController controller;

    /**
     * The fetcher that is used by this crawler instance to fetch the content of pages from the web.
     */
    private final PageFetcher pageFetcher;

    /**
     * The RobotstxtServer instance that is used by this crawler instance to determine whether the
     * crawler is allowed to crawl the content of each page.
     */
    private final RobotstxtServer robotstxtServer;

    /**
     * The PageHarvests that is used by this crawler instance to map each URL to a unique id.
     */
    private final PageHarvests pageHarvests;

    /**
     * The Frontier object that manages the crawl queue.
     */
    private final Frontier frontier;

    /**
     * The parser that is used by this crawler instance to parse the content of the fetched pages.
     */
    private final Parser parser;

    /**
     * Is the current crawler instance waiting for new URLs? This field is mainly used by the
     * controller to detect whether all of the crawler instances are waiting for new URLs and
     * therefore there is no more work and crawling can be stopped.
     */
    private boolean waitingForNewURLs = false;

    public DefaultWebCrawler(Integer id, CrawlerConfiguration configuration,
            CrawlController controller, PageFetcher pageFetcher, RobotstxtServer robotstxtServer,
            PageHarvests pageHarvests, Frontier frontier, Parser parser) {
        super();
        this.id = id;
        this.configuration = configuration;
        this.controller = controller;
        this.pageFetcher = pageFetcher;
        this.robotstxtServer = robotstxtServer;
        this.pageHarvests = pageHarvests;
        this.frontier = frontier;
        this.parser = parser;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public CrawlController getController() {
        return controller;
    }

    @Override
    public void onStart() {
        // empty
    }

    @Override
    public void pageFetched(WebURL webUrl, int statusCode, String statusDescription) {
        // empty
    }

    @Override
    public WebURL onProcessPage(WebURL url) {
        return url;
    }

    @Override
    public void onRedirectedStatusCode(Page page) {
        // empty
    }

    @Override
    public void handleUnexpectedStatusCode(String urlStr, int statusCode, String contentType,
            String description) {
        logger.warn("Skipping URL: {}, StatusCode: {}, {}, {}", urlStr, statusCode, contentType,
                description);
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        if (configuration.isRespectNoFollow()) {
            return !((referringPage != null && referringPage.getContentType() != null
                    && referringPage.getContentType().contains("html")
                    && ((HtmlParseData) referringPage.getParseData()).getMetaTagValue("robots")
                            .contains("nofollow")) || url.getAttribute("rel").contains("nofollow"));
        }
        return true;
    }

    @Override
    public boolean shouldFollowLinksIn(WebURL url) {
        return true;
    }

    @Override
    public void visit(Page page) {
        // empty
    }

    @Override
    public void handlePageSizeBiggerThanMaxSize(String url, long pageSize) {
        logger.warn("Skipping a URL: {} which was bigger ( {} ) than max allowed size", url,
                pageSize);
    }

    @Override
    public void handleParseError(WebURL webUrl) {
        logger.warn("Parsing error of: {}", webUrl.getURL());
    }

    @Override
    public void handleContentFetchError(WebURL webUrl) {
        logger.warn("Can't fetch content of: {}", webUrl.getURL());
    }

    @Override
    public void handleUnhandledException(WebURL webUrl, Throwable e) {
        String urlStr = (null == webUrl ? "NULL" : webUrl.getURL());
        logger.warn("Unhandled exception while fetching {}: {}", urlStr, e.getMessage());
        logger.info("Stacktrace: ", e);
    }

    @Override
    public boolean isWaitingForNewURLs() {
        return waitingForNewURLs;
    }

    @Override
    public void onStop() {
        // empty
    }

    @Override
    public Object getData() {
        return null;
    }

    @Override
    public void run() {
        onStart();
        while (true) {
            List<WebURL> urls = new ArrayList<>(50);
            waitingForNewURLs = true;
            frontier.getNextURLs(50, urls);
            waitingForNewURLs = false;
            if (urls.isEmpty()) {
                if (frontier.isFinished()) {
                    return;
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    logger.error("Error occurred", e);
                }
            } else {
                for (WebURL url : urls) {
                    if (controller.isShuttingDown()) {
                        logger.info("Exiting because of controller shutdown.");
                        return;
                    }
                    if (null != url) {
                        url = onProcessPage(url);
                        processPage(url);
                        frontier.setProcessed(url);
                    }
                }
            }
        }
    }

    private void processPage(WebURL url) {
        PageFetchResult fetchResult = null;
        try {
            if (null == url) {
                return;
            }
            fetchResult = pageFetcher.fetchPage(url);
            int statusCode = fetchResult.getStatusCode();
            pageFetched(url, statusCode, EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode,
                    Locale.ENGLISH));

            Page page = new Page(url);
            page.setFetchResponseHeaders(fetchResult.getResponseHeaders());
            page.setStatusCode(statusCode);
            if (statusCode < 200 || 299 < statusCode) {
                if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY
                        || statusCode == HttpStatus.SC_MOVED_TEMPORARILY
                        || statusCode == HttpStatus.SC_MULTIPLE_CHOICES
                        || statusCode == HttpStatus.SC_SEE_OTHER
                        || statusCode == HttpStatus.SC_TEMPORARY_REDIRECT || statusCode == 308) {
                    handleRedirect(url, fetchResult, page);
                } else {
                    handleUnexpectedStatusCode(url.getURL(), fetchResult.getStatusCode(),
                            contentType(fetchResult), EnglishReasonPhraseCatalog.INSTANCE.getReason(
                                    fetchResult.getStatusCode(), Locale.ENGLISH));
                }
            } else {
                handleSuccess(url, fetchResult, page);
            }
        } catch (PageBiggerThanMaxSizeException e) {
            handlePageSizeBiggerThanMaxSize(url.getURL(), e.getPageSize());
        } catch (ParseException pe) {
            handleParseError(url);
        } catch (ContentFetchException cfe) {
            handleContentFetchError(url);
        } catch (NotAllowedContentException nace) {
            logger.debug(
                    "Skipping: {} as it contains binary content which you configured not to crawl",
                    url.getURL());
        } catch (Exception e) {
            handleUnhandledException(url, e);
        } finally {
            if (null != fetchResult) {
                fetchResult.discardContentIfNotConsumed();
            }
        }
    }

    private static String contentType(PageFetchResult fetchResult) {
        if (null == fetchResult.getEntity() || null == fetchResult.getEntity().getContentType()) {
            return "";
        }
        return fetchResult.getEntity().getContentType().getValue();
    }

    @Override
    public void handleRedirect(WebURL url, PageFetchResult fetchResult, Page page) {
        page.setRedirect(true);
        String movedToUrl = fetchResult.getMovedToUrl();
        if (null == movedToUrl) {
            logger.warn("Unexpected error, URL: {} is redirected to NOTHING", url);
            return;
        }
        page.setRedirectedToUrl(movedToUrl);
        onRedirectedStatusCode(page);

        if (configuration.isFollowRedirects()) {
            if (pageHarvests.containsKey(movedToUrl)) {
                logger.debug("Redirect page: {} is already seen", url);
                return;
            }

            WebURL webURL = new WebURL();
            webURL.setURL(movedToUrl);
            webURL.setParentDocid(url.getParentDocid());
            webURL.setParentUrl(url.getParentUrl());
            webURL.setDepth(url.getDepth());
            webURL.setDocid(-1);
            webURL.setAnchor(url.getAnchor());
            if (shouldVisit(page, webURL)) {
                if (!shouldFollowLinksIn(webURL) || robotstxtServer.allows(webURL)) {
                    webURL.setDocid(pageHarvests.add(movedToUrl));
                    frontier.schedule(webURL);
                } else {
                    logger.debug("Not visiting: {} as per the server's " + "\"robots.txt\" policy",
                            webURL.getURL());
                }
            } else {
                logger.debug("Not visiting: {} as per your \"shouldVisit\" policy", webURL
                        .getURL());
            }
        }
    }

    @Override
    public void handleSuccess(WebURL url, PageFetchResult fetchResult, Page page)
            throws ContentFetchException, NotAllowedContentException, ParseException {
        if (!url.getURL().equals(fetchResult.getFetchedUrl())) {
            if (pageHarvests.containsKey(fetchResult.getFetchedUrl())) {
                logger.debug("Redirect page: {} has already been seen", url);
                return;
            }
            url.setURL(fetchResult.getFetchedUrl());
            url.setDocid(pageHarvests.add(fetchResult.getFetchedUrl()));
        }

        if (!fetchResult.fetchContent(page, configuration.getMaxDownloadSize())) {
            throw new ContentFetchException();
        }

        if (page.isTruncated()) {
            logger.warn("Warning: unknown page size exceeded max-download-size, truncated to: "
                    + "({}), at URL: {}", configuration.getMaxDownloadSize(), url.getURL());
        }

        parser.parse(page, url.getURL());

        if (shouldFollowLinksIn(page.getWebURL())) {
            ParseData parseData = page.getParseData();
            List<WebURL> toSchedule = new ArrayList<>();
            for (WebURL webURL : parseData.getOutgoingUrls()) {
                webURL.setParentDocid(url.getDocid());
                webURL.setParentUrl(url.getURL());
                Integer newdocid = pageHarvests.get(webURL.getURL());
                if (null != newdocid && 0 < newdocid) {
                    // This is not the first time that this Url is visited. So, we set the
                    // depth to a negative number.
                    webURL.setDepth((short) -1);
                    webURL.setDocid(newdocid);
                } else {
                    webURL.setDocid(-1);
                    webURL.setDepth((short) (url.getDepth() + 1));
                    if (-1 != configuration.getMaxDepthOfCrawling() && configuration
                            .getMaxDepthOfCrawling() <= url.getDepth()) {
                        logger.debug("Not visiting: {} as url depth {} has been exceeded", webURL
                                .getURL(), url.getDepth());
                    } else {
                        if (!shouldVisit(page, webURL)) {
                            logger.debug("Not visiting: {} as per your \"shouldVisit\" policy",
                                    webURL.getURL());
                        } else {
                            if (!robotstxtServer.allows(webURL)) {
                                logger.debug("Not visiting: {} as per the server's "
                                        + "\"robots.txt\" policy", webURL.getURL());
                            } else {
                                webURL.setDocid(pageHarvests.add(webURL.getURL()));
                                toSchedule.add(webURL);
                            }
                        }
                    }
                }
            }
            frontier.scheduleAll(toSchedule);
        } else {
            logger.debug("Not looking for links in page {}, "
                    + "as per your \"shouldFollowLinksInPage\" policy", page.getWebURL().getURL());
        }

        if (noIndexMetaTag(page)) {
            logger.debug("Not visiting page {}, " + "as per your \"respectNoIndex\" policy", page
                    .getWebURL().getURL());
        } else {
            visit(page);
        }
    }

    private boolean noIndexMetaTag(Page page) {
        if (configuration.isRespectNoIndex()) {
            if (null != page.getContentType() && page.getContentType().contains("html")) {
                return ((HtmlParseData) page.getParseData()).getMetaTagValue("robots").contains(
                        "noindex");
            }
        }
        return false;
    }

}
