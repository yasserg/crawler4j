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

package edu.uci.ics.crawler4j.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.crawler4j.util.Net;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;

import edu.uci.ics.crawler4j.crawler.Configurable;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class Parser extends Configurable {

  protected static final Logger logger = LoggerFactory.getLogger(Parser.class);

  private HtmlParser htmlParser;
  private ParseContext parseContext;

  public Parser(CrawlConfig config) {
    super(config);
    htmlParser = new HtmlParser();
    parseContext = new ParseContext();
  }

  public boolean parse(Page page, String contextURL) throws NotAllowedContentException {

    if (Util.hasBinaryContent(page.getContentType())) {
      BinaryParseData parseData = new BinaryParseData();
      if (config.isIncludeBinaryContentInCrawling()) {
        parseData.setBinaryContent(page.getContentData());
        page.setParseData(parseData);
      } else {
        throw new NotAllowedContentException();
      }

      parseData.setOutgoingUrls(Net.extractUrls(parseData.getHtml()));
      return parseData.getHtml() != null;
    } else if (Util.hasPlainTextContent(page.getContentType())) {
      try {
        TextParseData parseData = new TextParseData();
        if (page.getContentCharset() == null) {
          parseData.setTextContent(new String(page.getContentData()));
        } else {
          parseData.setTextContent(new String(page.getContentData(), page.getContentCharset()));
        }
        parseData.setOutgoingUrls(Net.extractUrls(parseData.getTextContent()));
        page.setParseData(parseData);

        return true;
      } catch (Exception e) {
        logger.error("{}, while parsing: {}", e.getMessage(), page.getWebURL().getURL());
        return false;
      }
    }

    Metadata metadata = new Metadata();
    HtmlContentHandler contentHandler = new HtmlContentHandler();
    InputStream inputStream = null;
    try {
      inputStream = new ByteArrayInputStream(page.getContentData());
      htmlParser.parse(inputStream, contentHandler, metadata, parseContext);
    } catch (Exception e) {
      logger.error("{}, while parsing: {}", e.getMessage(), page.getWebURL().getURL());
    } finally {
      try {
        if (inputStream != null) {
          inputStream.close();
        }
      } catch (IOException e) {
        logger.error("{}, while parsing: {}", e.getMessage(), page.getWebURL().getURL());
      }
    }

    if (page.getContentCharset() == null) {
      page.setContentCharset(metadata.get("Content-Encoding"));
    }

    HtmlParseData parseData = new HtmlParseData();
    parseData.setText(contentHandler.getBodyText().trim());
    parseData.setTitle(metadata.get(DublinCore.TITLE));
    parseData.setMetaTags(contentHandler.getMetaTags());
    LanguageIdentifier languageIdentifier = new LanguageIdentifier(parseData.getText());
    page.setLanguage(languageIdentifier.getLanguage());

    Set<WebURL> outgoingUrls = new HashSet<>();

    String baseURL = contentHandler.getBaseUrl();
    if (baseURL != null) {
      contextURL = baseURL;
    }

    int urlCount = 0;
    for (ExtractedUrlAnchorPair urlAnchorPair : contentHandler.getOutgoingUrls()) {
      String href = urlAnchorPair.getHref().trim();
      if (href.length() == 0) {
        continue;
      }

      String hrefLoweredCase = href.toLowerCase();
      if (!hrefLoweredCase.contains("javascript:") && !hrefLoweredCase.contains("mailto:") && !hrefLoweredCase.contains("@")) {
        String url = URLCanonicalizer.getCanonicalURL(href, contextURL);
        if (url != null) {
          WebURL webURL = new WebURL();
          webURL.setURL(url);
          webURL.setTag(urlAnchorPair.getTag());
          webURL.setAnchor(urlAnchorPair.getAnchor());
          outgoingUrls.add(webURL);
          urlCount++;
          if (urlCount > config.getMaxOutgoingLinksToFollow()) {
            break;
          }
        }
      }
    }

    parseData.setOutgoingUrls(outgoingUrls);

    try {
      if (page.getContentCharset() == null) {
        parseData.setHtml(new String(page.getContentData()));
      } else {
        parseData.setHtml(new String(page.getContentData(), page.getContentCharset()));
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return false;
    }

    page.setParseData(parseData);
    return true;
  }
}