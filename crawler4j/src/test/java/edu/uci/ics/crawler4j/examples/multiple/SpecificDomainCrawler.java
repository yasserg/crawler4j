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

package edu.uci.ics.crawler4j.examples.multiple;

import java.util.Set;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.CrawlerConfiguration;
import edu.uci.ics.crawler4j.crawler.*;
import edu.uci.ics.crawler4j.crawler.controller.CrawlController;
import edu.uci.ics.crawler4j.crawler.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.frontier.pageharvests.PageHarvests;
import edu.uci.ics.crawler4j.parser.*;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * @author Yasser Ganjisaffar
 */
public class SpecificDomainCrawler extends DefaultWebCrawler {

    private static final Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g"
            + "|png|tiff?|mid|mp2|mp3|mp4" + "|wav|avi|mov|mpeg|ram|m4v|pdf"
            + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    private final String[] myCrawlDomains;

    public SpecificDomainCrawler(Integer id, CrawlerConfiguration configuration,
            CrawlController controller, PageFetcher pageFetcher, RobotstxtServer robotstxtServer,
            PageHarvests pageHarvests, Frontier frontier, Parser parser, String[] myCrawlDomains) {
        super(id, configuration, controller, pageFetcher, robotstxtServer, pageHarvests, frontier,
                parser);
        this.myCrawlDomains = myCrawlDomains;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        if (FILTERS.matcher(href).matches()) {
            return false;
        }

        for (String crawlDomain : myCrawlDomains) {
            if (href.startsWith(crawlDomain)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void visit(Page page) {
        int docid = page.getWebURL().getId();
        String url = page.getWebURL().getURL();
        int parentDocid = page.getWebURL().getParentId();

        logger.debug("Docid: {}", docid);
        logger.info("URL: {}", url);
        logger.debug("Docid of parent page: {}", parentDocid);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            logger.debug("Text length: {}", text.length());
            logger.debug("Html length: {}", html.length());
            logger.debug("Number of outgoing links: {}", links.size());
        }

        logger.debug("=============");
    }

}