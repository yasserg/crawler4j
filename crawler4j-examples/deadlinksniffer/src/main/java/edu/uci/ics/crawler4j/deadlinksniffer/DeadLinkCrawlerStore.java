/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.uci.ics.crawler4j.deadlinksniffer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.ImageData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores information about erroneous pages to the disk.
 *
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
public class DeadLinkCrawlerStore {
    private static final Logger logger = LoggerFactory.getLogger(DeadLinkCrawlerStore.class);

    private final DeadLinkCrawlConfig config;

    private FileWriter brokenPages;
    private FileWriter imageWoAlt;


    protected DeadLinkCrawlerStore(DeadLinkCrawlConfig config) {
        this.config = config;
        try {
            brokenPages = new FileWriter(new File(config.getCrawlStorageFolder(), "brokenPages.csv"));
            brokenPages.append("status, url, parent_url\n");
            brokenPages.flush();

            imageWoAlt = new FileWriter(new File(config.getCrawlStorageFolder(), "imageWoAlt.csv"));
            imageWoAlt.append("onPage, imageNr, imgSrc, imgLink\n");
            imageWoAlt.flush();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }




    public synchronized void close() {
        closeFile(brokenPages, "brokenPages");
        closeFile(imageWoAlt, "imageWoAlt");
        brokenPages = null;
        imageWoAlt = null;
    }

    private void closeFile(FileWriter fw, String name) {
        if (fw == null) {
            return;
        }

        try {
            fw.close();
        }
        catch (IOException e) {
            logger.error("problem with closing" + name, e);
        }
    }

    public synchronized void storePageStatus(int statusCode, WebURL webUrlFail) {
        try {
            brokenPages.append("" + statusCode + ", " + webUrlFail.getURL() + ", " + webUrlFail.getParentUrl() + "\n");
            brokenPages.flush();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void storeImageInfo(Page page, int imgNr, ImageData imageData) {
        // log all images with missing alt tag
        if (!imageData.getAttrVals().containsKey("alt") || imageData.getAttrVals().get("alt").isEmpty()) {
            String url = page.getWebURL().getURL();
            logger.info("\n\n IMAGE without 'alt' tag on page {} img: {}", url, imageData.getSrc());
            try {
                String src = imageData.getSrc();
                String imgLink;
                if (src.startsWith("https://") || src.startsWith("http://")) {
                    // absolute image
                    imgLink = src;
                }
                else if (src.startsWith("/")) {
                    // server-root relative image
                    imgLink = page.getWebURL().getRootUrl() + src;
                }
                else {
                    // relative image
                    imgLink = page.getWebURL().getRootUrl() + page.getWebURL().getPath() + "/" + src;
                }
                imageWoAlt.append(url + ", " + imgNr + ", " + src + ", " + imgLink + "\n");
                imageWoAlt.flush();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
