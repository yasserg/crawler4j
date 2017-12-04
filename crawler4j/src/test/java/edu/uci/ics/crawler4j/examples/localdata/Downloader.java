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

package edu.uci.ics.crawler4j.examples.localdata;

import org.apache.http.HttpStatus;
import org.slf4j.*;

import edu.uci.ics.crawler4j.*;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.fetcher.*;
import edu.uci.ics.crawler4j.parser.*;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * This class is a demonstration of how crawler4j can be used to download a single page and extract
 * its title and text.
 */
public class Downloader {

    private static final Logger logger = LoggerFactory.getLogger(Downloader.class);

    private final CrawlerConfiguration configuration;

    private final Parser parser;

    private final PageFetcher pageFetcher;

    public Downloader() throws InstantiationException, IllegalAccessException {
        configuration = new CrawlerConfiguration(new SleepyCatCrawlPersistentConfiguration());
        parser = new Parser(configuration);
        pageFetcher = new DefaultPageFetcher(configuration);
    }

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        Downloader downloader = new Downloader();
        downloader.processUrl("http://en.wikipedia.org/wiki/Main_Page/");
        downloader.processUrl("http://www.yahoo.com/");
    }

    public void processUrl(String url) {
        logger.debug("Processing: {}", url);
        Page page = download(url);
        if (page != null) {
            ParseData parseData = page.getParseData();
            if (parseData != null) {
                if (parseData instanceof HtmlParseData) {
                    HtmlParseData htmlParseData = (HtmlParseData) parseData;
                    logger.debug("Title: {}", htmlParseData.getTitle());
                    logger.debug("Text length: {}", htmlParseData.getText().length());
                    logger.debug("Html length: {}", htmlParseData.getHtml().length());
                }
            } else {
                logger.warn("Couldn't parse the content of the page.");
            }
        } else {
            logger.warn("Couldn't fetch the content of the page.");
        }
        logger.debug("==============");
    }

    private Page download(String url) {
        WebURL curURL = new WebURL();
        curURL.setURL(url);
        FetchedPage fetchResult = null;
        try {
            fetchResult = pageFetcher.fetch(curURL);
            if (fetchResult.getStatusCode() == HttpStatus.SC_OK) {
                Page page = new Page(curURL);
                fetchResult.fetchContent(page, configuration.getMaxDownloadSize());
                parser.parse(page, curURL.getURL());
                return page;
            }
        } catch (Exception e) {
            logger.error("Error occurred while fetching url: " + curURL.getURL(), e);
        } finally {
            if (fetchResult != null) {
                fetchResult.discardContentIfNotConsumed();
            }
        }
        return null;
    }
}
