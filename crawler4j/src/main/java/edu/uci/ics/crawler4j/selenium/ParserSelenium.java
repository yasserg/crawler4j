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

package edu.uci.ics.crawler4j.selenium;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.tika.language.LanguageIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.exceptions.ParseException;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.CssParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.HtmlParser;
import edu.uci.ics.crawler4j.parser.NotAllowedContentException;
import edu.uci.ics.crawler4j.parser.ParserInterface;
import edu.uci.ics.crawler4j.parser.TextParseData;
import edu.uci.ics.crawler4j.parser.TikaHtmlParser;
import edu.uci.ics.crawler4j.url.TLDList;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.util.Net;
import edu.uci.ics.crawler4j.util.Util;

/**
 * Simple parser. Allows to set regular expressions for inclussion / exclussion for selenium driver.
 *
 *
 *
 * @author Dario Goikoetxea
 */
public class ParserSelenium implements ParserInterface {

    private static final Logger logger = LoggerFactory.getLogger(ParserSelenium.class);

    private final SeleniumCrawlConfig config;

    private final HtmlParser htmlContentParser;

    private final Net net;

    private List<Pattern> exclussions;

    private List<Pattern> includes;

    public ParserSelenium(SeleniumCrawlConfig config, TLDList tldList) throws IllegalAccessException,
            InstantiationException, PatternSyntaxException {
        this(config, new TikaHtmlParser(config, tldList), tldList);
    }

    public ParserSelenium(SeleniumCrawlConfig config, HtmlParser htmlParser, TLDList tldList)
            throws PatternSyntaxException {
        this.config = config;
        this.htmlContentParser = htmlParser;
        this.net = new Net(config, tldList);
        includes = new ArrayList<Pattern>();
        exclussions = new ArrayList<Pattern>();
        if (config.getSeleniumExcludes() != null) {
            for (String pattern : config.getSeleniumExcludes()) {
                exclussions.add(Pattern.compile(pattern));
            }
        }
        if (config.getSeleniumIncludes() != null) {
            for (String pattern : config.getSeleniumIncludes()) {
                includes.add(Pattern.compile(pattern));
            }
        }
    }

    @Override
    public void parse(Page page, String contextURL) throws NotAllowedContentException, ParseException {
        if (Util.hasBinaryContent(page.getContentType())) { // BINARY
            BinaryParseData parseData = new BinaryParseData();
            if (config.isIncludeBinaryContentInCrawling()) {
                if (config.isProcessBinaryContentInCrawling()) {
                    try {
                        parseData.setBinaryContent(page.getContentData());
                    } catch (Exception e) {
                        if (config.isHaltOnError()) {
                            throw new ParseException(e);
                        } else {
                            logger.error("Error parsing file", e);
                        }
                    }
                } else {
                    parseData.setHtml("<html></html>");
                }
                page.setParseData(parseData);
                if (parseData.getHtml() == null) {
                    throw new ParseException();
                }
                parseData.setOutgoingUrls(processSelenium(net.extractUrls(parseData.getHtml())));
            } else {
                throw new NotAllowedContentException();
            }
        } else if (Util.hasCssTextContent(page.getContentType())) { // text/css
            try {
                CssParseData parseData = new CssParseData();
                if (page.getContentCharset() == null) {
                    parseData.setTextContent(new String(page.getContentData()));
                } else {
                    parseData.setTextContent(
                        new String(page.getContentData(), page.getContentCharset()));
                }
                parseData.setOutgoingUrls(page.getWebURL());
                parseData.setOutgoingUrls(processSelenium(parseData.getOutgoingUrls()));
                page.setParseData(parseData);
            } catch (Exception e) {
                logger.error("{}, while parsing css: {}", e.getMessage(), page.getWebURL().getURL());
                throw new ParseException();
            }
        } else if (Util.hasPlainTextContent(page.getContentType())) { // plain Text
            try {
                TextParseData parseData = new TextParseData();
                if (page.getContentCharset() == null) {
                    parseData.setTextContent(new String(page.getContentData()));
                } else {
                    parseData.setTextContent(
                        new String(page.getContentData(), page.getContentCharset()));
                }
                parseData.setOutgoingUrls(processSelenium(net.extractUrls(parseData.getTextContent())));
                page.setParseData(parseData);
            } catch (Exception e) {
                logger.error("{}, while parsing: {}", e.getMessage(), page.getWebURL().getURL());
                throw new ParseException(e);
            }
        } else { // isHTML

            HtmlParseData parsedData = this.htmlContentParser.parse(page, contextURL);
            parsedData.setOutgoingUrls(processSelenium(parsedData.getOutgoingUrls()));
            if (page.getContentCharset() == null) {
                page.setContentCharset(parsedData.getContentCharset());
            }

            // Please note that identifying language takes less than 10 milliseconds
            LanguageIdentifier languageIdentifier = new LanguageIdentifier(parsedData.getText());
            page.setLanguage(languageIdentifier.getLanguage());

            page.setParseData(parsedData);

        }
    }

    private Set<WebURL> processSelenium(Set<WebURL> urls) {
        if (urls == null) {
            return null;
        }
        outer_loop: for (WebURL url : urls) {
            if (config.isDefaultToSelenium()) {
                url.setSelenium(true);
            } else {
                for (Pattern pattern : includes) {
                    if (pattern.matcher(url.getURL()).matches()) {
                        url.setSelenium(true);
                        continue outer_loop;
                    }
                }
            }

        }
        outer_loop: for (WebURL current : urls) {
            if (!current.isSelenium()) {
                continue;
            }
            for (Pattern pattern : exclussions) {
                if (pattern.matcher(current.getURL()).matches()) {
                    current.setSelenium(false);
                    continue outer_loop;
                }
            }
        }
        return urls;
    }
}
