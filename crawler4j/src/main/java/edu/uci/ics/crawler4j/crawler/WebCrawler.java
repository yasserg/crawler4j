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

import edu.uci.ics.crawler4j.crawler.controller.CrawlController;
import edu.uci.ics.crawler4j.crawler.exceptions.*;
import edu.uci.ics.crawler4j.crawler.fetcher.FetchedPage;
import edu.uci.ics.crawler4j.parser.NotAllowedContentException;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * WebCrawler class in the Runnable class that is executed by each crawler thread.
 *
 * @author Yasser Ganjisaffar
 */
public interface WebCrawler extends Runnable {

    int getId();

    CrawlController getController();

    /**
     * This function is called just before starting the crawl by this crawler instance. It can be
     * used for setting up the data structures or initializations needed by this crawler instance.
     */
    void onStart();

    /**
     * This function is called once the header of a page is fetched. It can be overridden by
     * sub-classes to perform custom logic for different status codes. For example, 404 pages can be
     * logged, etc.
     *
     * @param webUrl
     *            WebUrl containing the statusCode
     * @param statusCode
     *            Html Status Code number
     * @param statusDescription
     *            Html Status COde description
     */
    void pageFetched(WebURL webUrl, int statusCode, String statusDescription);

    /**
     * This function is called before processing of the page's URL It can be overridden by
     * subclasses for tweaking of the url before processing it. For example,
     * http://abc.com/def?a=123 - http://abc.com/def
     *
     * @param curURL
     *            current URL which can be tweaked before processing
     * @return tweaked WebURL
     */
    WebURL onProcessPage(WebURL curURL);

    /**
     * This function is called if the crawler encounters a page with a 3xx status code
     *
     * @param page
     *            Partial page object
     */
    void onRedirectedStatusCode(Page page);

    /**
     * This function is called if the crawler encountered an unexpected http status code ( a status
     * code other than 3xx)
     *
     * @param urlStr
     *            URL in which an unexpected error was encountered while crawling
     * @param statusCode
     *            Html StatusCode
     * @param contentType
     *            Type of Content
     * @param description
     *            Error Description
     */
    void handleUnexpectedStatusCode(String urlStr, int statusCode, String contentType,
            String description);

    /**
     * Classes that extends WebCrawler should overwrite this function to tell the crawler whether
     * the given url should be crawled or not. The following default implementation indicates that
     * all urls should be included in the crawl except those with a nofollow flag.
     *
     * @param url
     *            the url which we are interested to know whether it should be included in the crawl
     *            or not.
     * @param referringPage
     *            The Page in which this url was found.
     * @return if the url should be included in the crawl it returns true, otherwise false is
     *         returned.
     */
    boolean shouldVisit(Page referringPage, WebURL url);

    /**
     * Determine whether links found at the given URL should be added to the queue for crawling. By
     * default this method returns true always, but classes that extend WebCrawler can override it
     * in order to implement particular policies about which pages should be mined for outgoing
     * links and which should not.
     *
     * If links from the URL are not being followed, then we are not operating as a web crawler and
     * need not check robots.txt before fetching the single URL. (see definition at
     * http://www.robotstxt.org/faq/what.html). Thus URLs that return false from this method will
     * not be subject to robots.txt filtering.
     *
     * @param url
     *            the URL of the page under consideration
     * @return true if outgoing links from this page should be added to the queue.
     */
    boolean shouldFollowLinksIn(WebURL url);

    /**
     * Classes that extends WebCrawler should overwrite this function to process the content of the
     * fetched and parsed page.
     *
     * @param page
     *            the page object that is just fetched and parsed.
     */
    void visit(Page page);

    void handleRedirect(WebURL url, FetchedPage fetchResult, Page page);

    void handleSuccess(WebURL url, FetchedPage fetchResult, Page page) throws ContentFetchException,
            NotAllowedContentException, ParseException;

    /**
     * This function is called if the content of a url is bigger than allowed size.
     *
     * @param url
     *            - The URL which it's content is bigger than allowed size
     */
    void handlePageSizeBiggerThanMaxSize(String url, long pageSize);

    /**
     * This function is called if there has been an error in parsing the content.
     *
     * @param webUrl
     *            URL which failed on parsing
     */
    void handleParseError(WebURL webUrl);

    /**
     * This function is called if the content of a url could not be fetched.
     *
     * @param webUrl
     *            URL which content failed to be fetched
     */
    void handleContentFetchError(WebURL webUrl);

    /**
     * This function is called when a unhandled exception was encountered during fetching
     *
     * @param webUrl
     *            URL where a unhandled exception occured
     */
    void handleUnhandledException(WebURL webUrl, Throwable e);

    boolean isWaitingForNewURLs();

    /**
     * This function is called just before the termination of the current crawler instance. It can
     * be used for persisting in-memory data or other finalization tasks.
     */
    void onStop();

    /**
     * The CrawlController instance that has created this crawler instance will call this function
     * just before terminating this crawler thread. Classes that extend WebCrawler can override this
     * function to pass their local data to their controller. The controller then puts these local
     * data in a List that can then be used for processing the local data of crawlers (if needed).
     *
     * @return currently NULL
     */
    Object getData();

}
