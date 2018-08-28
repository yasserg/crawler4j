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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlMapper;
//import edu.uci.ics.crawler4j.parser.tagsoup.HtmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.exceptions.ParseException;
import edu.uci.ics.crawler4j.util.Net;
import edu.uci.ics.crawler4j.util.Util;

/**
 * @author Yasser Ganjisaffar
 */
public class Parser {

    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private final CrawlConfig config;

    private final HtmlParser htmlContentParser;

    public Parser(CrawlConfig config) throws IllegalAccessException, InstantiationException {
        this.config = config;
        this.htmlContentParser = new TikaHtmlParser(config);
    }

    public Parser(CrawlConfig config, HtmlParser htmlParser) {
        this.config = config;
        this.htmlContentParser = htmlParser;
    }

    public void parse(Page page, String contextURL)
        throws NotAllowedContentException, ParseException {
        if (Util.hasBinaryContent(page.getContentType())) { // BINARY
            BinaryParseData parseData = new BinaryParseData();
            if (config.isIncludeBinaryContentInCrawling()) {
                if (config.isProcessBinaryContentInCrawling()) {
                    parseData.setBinaryContent(page.getContentData());
                } else {
                    parseData.setHtml("<html></html>");
                }
                page.setParseData(parseData);
                if (parseData.getHtml() == null) {
                    throw new ParseException();
                }
                parseData.setOutgoingUrls(Net.extractUrls(parseData.getHtml()));
            } else {
                throw new NotAllowedContentException();
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
                parseData.setOutgoingUrls(Net.extractUrls(parseData.getTextContent()));
                page.setParseData(parseData);
            } catch (Exception e) {
                logger.error("{}, while parsing: {}", e.getMessage(), page.getWebURL().getURL());
                throw new ParseException();
            }
        } else { // isHTML

            HtmlParseData parsedData = this.htmlContentParser.parse(page, contextURL);

            if (page.getContentCharset() == null) {
                page.setContentCharset(parsedData.getContentCharset());
            }

            // Please note that identifying language takes less than 10 milliseconds
            LanguageIdentifier languageIdentifier = new LanguageIdentifier(parsedData.getText());
            page.setLanguage(languageIdentifier.getLanguage());
            logger.debug("The parser has identified html page language as: " + page.getLanguage());

            page.setParseData(parsedData);
            
            // write page content data to a file for testing purposes
            // ********************************************************************
			File saveBinaryFile = null;
			FileOutputStream fileOutputStream = null;
			String binaryFileName = "testFileContent";
			String pathToTestFile = "/Users/saleemhalipoto/development/crawler4j/crawler4j/src/test/resources";
			byte [] contentData = page.getContentData();

				try {
					// Extract and process the filename information
					//StringBuffer binaryFileNamePath = new StringBuffer(sfWithUrl.getUrlString());
					//String binaryFileName = binaryFileNamePath.substring(binaryFileNamePath.lastIndexOf("/"));
					
					
					// generate filename with directory as parent					
					saveBinaryFile = new File(pathToTestFile + "/" + binaryFileName);
					
					// Create the empty file with filename generated as above
					fileOutputStream = new FileOutputStream(new File(saveBinaryFile.getPath()));
		            logger.debug("Created empty file: " + saveBinaryFile.getPath());
		            
			        /*
			         * Writes a byte object to a file
			         */
					//ObjectOutputStream objStream = new ObjectOutputStream(fileOutputStream); 
		            fileOutputStream.write(contentData);
		            logger.debug("Saved binary contents to file: " + saveBinaryFile.getPath());
		            
		            // Add the file to the global list of urls paired with filenames to fix broken links
		            //addFileToUrlFilenameSet(sfWithUrl.getUrlString(), saveBinaryFile.getPath());
				} catch (IOException e) {
		            logger.debug("Error saving binary contents to file: " + binaryFileName);
		            logger.debug("This file also was not added to the global set: setOfAllFilesWithUrls");
					e.printStackTrace();			
				} finally {
					try {
						if (fileOutputStream != null) fileOutputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
        //***********************************************************************************************

        }
    }
