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

import edu.uci.ics.crawler4j.fetcher.PageFetchResult;
import edu.uci.ics.crawler4j.fetcher.CustomFetchStatus;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.DocIDServer;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.NotAllowedContentException;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * WebCrawler class in the Runnable class that is executed by each crawler
 * thread.
 *
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class WebCrawler implements Runnable {

  protected static final Logger logger = LoggerFactory.getLogger(WebCrawler.class);

  /**
   * The id associated to the crawler thread running this instance
   */
  protected int myId;

  /**
   * The controller instance that has created this crawler thread. This
   * reference to the controller can be used for getting configurations of the
   * current crawl or adding new seeds during runtime.
   */
  protected CrawlController myController;

  /**
   * The thread within which this crawler instance is running.
   */
  private Thread myThread;

  /**
   * The parser that is used by this crawler instance to parse the content of
   * the fetched pages.
   */
  private Parser parser;

  /**
   * The fetcher that is used by this crawler instance to fetch the content of
   * pages from the web.
   */
  private PageFetcher pageFetcher;

  /**
   * The RobotstxtServer instance that is used by this crawler instance to
   * determine whether the crawler is allowed to crawl the content of each
   * page.
   */
  private RobotstxtServer robotstxtServer;

  /**
   * The DocIDServer that is used by this crawler instance to map each URL to
   * a unique docid.
   */
  private DocIDServer docIdServer;

  /**
   * The Frontier object that manages the crawl queue.
   */
  private Frontier frontier;

  /**
   * Is the current crawler instance waiting for new URLs? This field is
   * mainly used by the controller to detect whether all of the crawler
   * instances are waiting for new URLs and therefore there is no more work
   * and crawling can be stopped.
   */
  private boolean isWaitingForNewURLs;

  /**
   * Initializes the current instance of the crawler
   *
   * @param id
   *            the id of this crawler instance
   * @param crawlController
   *            the controller that manages this crawling session
   */
  public void init(int id, CrawlController crawlController) {
    this.myId = id;
    this.pageFetcher = crawlController.getPageFetcher();
    this.robotstxtServer = crawlController.getRobotstxtServer();
    this.docIdServer = crawlController.getDocIdServer();
    this.frontier = crawlController.getFrontier();
    this.parser = new Parser(crawlController.getConfig());
    this.myController = crawlController;
    this.isWaitingForNewURLs = false;
  }

  /**
   * Get the id of the current crawler instance
   *
   * @return the id of the current crawler instance
   */
  public int getMyId() {
    return myId;
  }

  public CrawlController getMyController() {
    return myController;
  }

  /**
   * This function is called just before starting the crawl by this crawler
   * instance. It can be used for setting up the data structures or
   * initializations needed by this crawler instance.
   */
  public void onStart() {
    // Do nothing by default
    // Sub-classed can override this to add their custom functionality
  }

  /**
   * This function is called just before the termination of the current
   * crawler instance. It can be used for persisting in-memory data or other
   * finalization tasks.
   */
  public void onBeforeExit() {
    // Do nothing by default
    // Sub-classed can override this to add their custom functionality
  }

  /**
   * This function is called once the header of a page is fetched. It can be
   * overridden by sub-classes to perform custom logic for different status
   * codes. For example, 404 pages can be logged, etc.
   *
   * @param webUrl
   * @param statusCode
   * @param statusDescription
   */
  protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
    // Do nothing by default
    // Sub-classed can override this to add their custom functionality
  }

    /**
     * This function is called before processing of the page's URL
     * It can be overridden by subclasses for tweaking of the url before processing it.
     * For example, http://abc.com/def?a=123 >> http://abc.com/def
     *
     * @param curURL current URL which can be tweaked before processing
     * @return tweaked WebURL
     */
    protected WebURL handleUrlBeforeProcess(WebURL curURL) {
      return curURL;
    }

    /**
     * This function is called if the content of a url is bigger than allowed size.
     *
     * @param urlStr - The URL which it's content is bigger than allowed size
     */
    protected void onPageBiggerThanMaxSize(String urlStr) {
      logger.warn("Skipping a page which was bigger than max allowed size: {}", urlStr);
    }

    /**
     * This function is called if the crawler encountered an unexpected error while crawling this url
     *
     * @param urlStr - The URL in which an unexpected error was encountered while crawling
     */
    protected void onUnexpectedError(String urlStr, int statusCode, String contentType, String description) {
      logger.warn("Skipping URL: {}, StatusCode: {}, {}, {}", urlStr, statusCode, contentType, description);
      // Do nothing by default (except basic logging)
      // Sub-classed can override this to add their custom functionality
    }

  /**
   * This function is called if the content of a url could not be fetched.
   *
   * @param webUrl
   */
  protected void onContentFetchError(WebURL webUrl) {
    logger.warn("Can't fetch content of: {}", webUrl.getURL());
    // Do nothing by default (except basic logging)
    // Sub-classed can override this to add their custom functionality
  }

  /**
   * This function is called if there has been an error in parsing the
   * content.
   *
   * @param webUrl
   */
  protected void onParseError(WebURL webUrl) {
    logger.warn("Parsing error of: {}", webUrl.getURL());
    // Do nothing by default (Except logging)
    // Sub-classed can override this to add their custom functionality
  }

  /**
   * The CrawlController instance that has created this crawler instance will
   * call this function just before terminating this crawler thread. Classes
   * that extend WebCrawler can override this function to pass their local
   * data to their controller. The controller then puts these local data in a
   * List that can then be used for processing the local data of crawlers (if
   * needed).
   */
  public Object getMyLocalData() {
    return null;
  }

  public void run() {
    onStart();
    while (true) {
      List<WebURL> assignedURLs = new ArrayList<>(50);
      isWaitingForNewURLs = true;
      frontier.getNextURLs(50, assignedURLs);
      isWaitingForNewURLs = false;
      if (assignedURLs.size() == 0) {
        if (frontier.isFinished()) {
          return;
        }
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      } else {
        for (WebURL curURL : assignedURLs) {
          if (curURL != null) {
            curURL = handleUrlBeforeProcess(curURL);
            processPage(curURL);
            frontier.setProcessed(curURL);
          }
          if (myController.isShuttingDown()) {
            logger.info("Exiting because of controller shutdown.");
            return;
          }
        }
      }
    }
  }

  /**
  * Classes that extends WebCrawler can overwrite this function to tell the
  * crawler whether the given url should be crawled or not. The following
  * implementation indicates that all urls should be included in the crawl.
  *
  * @param url
  *            the url which we are interested to know whether it should be
  *            included in the crawl or not.
  * @param page
  *           Page context from which this URL was scraped
  * @return if the url should be included in the crawl it returns true,
  *         otherwise false is returned.
  */
  public boolean shouldVisit(Page page, WebURL url) {
    return true;
  }

  /**
   * Classes that extends WebCrawler can overwrite this function to process
   * the content of the fetched and parsed page.
   *
   * @param page
   *            the page object that is just fetched and parsed.
   */
  public void visit(Page page) {
    // Do nothing by default
    // Sub-classed can override this to add their custom functionality
  }

  private void processPage(WebURL curURL) {
    if (curURL == null) {
      return;
    }
    PageFetchResult fetchResult = null;
    try {
      fetchResult = pageFetcher.fetchHeader(curURL);
      int statusCode = fetchResult.getStatusCode();
      handlePageStatusCode(curURL, statusCode, CustomFetchStatus.getStatusDescription(statusCode));

      Page page = new Page(curURL);
      page.setFetchResponseHeaders(fetchResult.getResponseHeaders());
      page.setStatusCode(statusCode);
      if (statusCode != HttpStatus.SC_OK) {
        if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY
            || statusCode == HttpStatus.SC_MULTIPLE_CHOICES || statusCode == HttpStatus.SC_SEE_OTHER
            || statusCode == HttpStatus.SC_TEMPORARY_REDIRECT || statusCode == CustomFetchStatus.SC_PERMANENT_REDIRECT) {

          page.setRedirect(true);
          if (myController.getConfig().isFollowRedirects()) {
            String movedToUrl = fetchResult.getMovedToUrl();
            if (movedToUrl == null) {
              logger.warn("Unexpected error, URL: {} is redirected to NOTHING", curURL);
              return;
            }
            page.setRedirectedToUrl(movedToUrl);

            int newDocId = docIdServer.getDocId(movedToUrl);
            if (newDocId > 0) {
              logger.debug("Redirect page: {} is already seen", curURL);
              return;
            }

            WebURL webURL = new WebURL();
            webURL.setURL(movedToUrl);
            webURL.setParentDocid(curURL.getParentDocid());
            webURL.setParentUrl(curURL.getParentUrl());
            webURL.setDepth(curURL.getDepth());
            webURL.setDocid(-1);
            webURL.setAnchor(curURL.getAnchor());
            if (shouldVisit(page, webURL) && robotstxtServer.allows(webURL)) {
              webURL.setDocid(docIdServer.getNewDocID(movedToUrl));
              frontier.schedule(webURL);
            } else {
              logger.debug("Not visiting: {} as per your \"shouldVisit\" policy", webURL.getURL());
            }
          }
        } else if (fetchResult.getStatusCode() == CustomFetchStatus.PageTooBig) {
          onPageBiggerThanMaxSize(curURL.getURL());
        } else {
          String description = CustomFetchStatus.getStatusDescription(statusCode);
          String contentType = fetchResult.getEntity() == null ? "" : fetchResult.getEntity().getContentType().getValue();
          onUnexpectedError(curURL.getURL(), fetchResult.getStatusCode(), contentType, description);
        }
        return;
      }

      if (!curURL.getURL().equals(fetchResult.getFetchedUrl())) {
        if (docIdServer.isSeenBefore(fetchResult.getFetchedUrl())) {
          logger.debug("Redirect page: {} has already been seen", curURL);
          return;
        }
        curURL.setURL(fetchResult.getFetchedUrl());
        curURL.setDocid(docIdServer.getNewDocID(fetchResult.getFetchedUrl()));
      }

      if (!fetchResult.fetchContent(page)) {
        onContentFetchError(curURL);
        return;
      }

      if (!parser.parse(page, curURL.getURL())) {
        onParseError(curURL);
        return;
      }

      ParseData parseData = page.getParseData();
      List<WebURL> toSchedule = new ArrayList<>();
      int maxCrawlDepth = myController.getConfig().getMaxDepthOfCrawling();
      for (WebURL webURL : parseData.getOutgoingUrls()) {
        webURL.setParentDocid(curURL.getDocid());
        webURL.setParentUrl(curURL.getURL());
        int newdocid = docIdServer.getDocId(webURL.getURL());
        if (newdocid > 0) {
          // This is not the first time that this Url is
          // visited. So, we set the depth to a negative number.
          webURL.setDepth((short) -1);
          webURL.setDocid(newdocid);
        } else {
          webURL.setDocid(-1);
          webURL.setDepth((short) (curURL.getDepth() + 1));
          if (maxCrawlDepth == -1 || curURL.getDepth() < maxCrawlDepth) {
            if (shouldVisit(page, webURL) && robotstxtServer.allows(webURL)) {
                webURL.setDocid(docIdServer.getNewDocID(webURL.getURL()));
                toSchedule.add(webURL);
            }
          }
        }
      }
      frontier.scheduleAll(toSchedule);

      try {
        visit(page);
      } catch (Exception e) {
        logger.error("Exception while running the visit method. Stacktrace: ", e);
      }
    } catch (NotAllowedContentException nace) {
      logger.debug("Skipping: {} as it contains binary content which you configured not to crawl", curURL.getURL());
    } catch (Exception e) {
      logger.error("{}, while processing: {}", e.getMessage(), curURL.getURL());
    } finally {
      if (fetchResult != null) {
        fetchResult.discardContentIfNotConsumed();
      }
    }
  }

    public Thread getThread() {
    return myThread;
  }

  public void setThread(Thread myThread) {
    this.myThread = myThread;
  }

  public boolean isNotWaitingForNewURLs() {
    return !isWaitingForNewURLs;
  }
}