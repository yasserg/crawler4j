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

package edu.uci.ics.crawler4j.tests.fetcher;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.http.HttpStatus;
import org.openqa.selenium.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.fetcher.PageFetcherInterface;
import edu.uci.ics.crawler4j.selenium.PageFetchResultSelenium;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 *
 * Simple PageFetcher for Selenium. Does not support custom headers or authentication!
 *
 * It is merely used for testing purposes: This selenium driver is not suitable to crawl
 * everything. Binary files and some css fail to load properly.
 *
 * @author Dario Goikoetxea
 */
public class PageFetcherSeleniumOnly implements PageFetcherInterface {
    protected static final Logger logger = LoggerFactory.getLogger(PageFetcherSeleniumOnly.class);
    protected final Object mutex = new Object();
    /**
     * This field is protected for retro compatibility. Please use the getter method: getConfig() to
     * read this field;
     */
    protected final CrawlConfig config;
    protected final Settings configSelenium;
    protected long lastFetchTime = 0;

    public PageFetcherSeleniumOnly(CrawlConfig config)
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        this.config = config;
        if (config.getSeleniumConfig() == null) {
            configSelenium = Settings.builder().javascript(true).build();
        } else {
            configSelenium = config.getSeleniumConfig();
        }
    }

    public PageFetchResultSelenium fetchPage(WebURL webUrl)
            throws InterruptedException, IOException {
        // Getting URL, setting headers & content
        PageFetchResultSelenium fetchResult = new PageFetchResultSelenium(config.isHaltOnError());
        String toFetchURL = webUrl.getURL();

        JBrowserDriver driver = new JBrowserDriver(configSelenium);
        try {
            if (config.getPolitenessDelay() > 0) {
                // Applying Politeness delay
                synchronized (mutex) {
                    long now = (new Date()).getTime();
                    if ((now - lastFetchTime) < config.getPolitenessDelay()) {
                        Thread.sleep(config.getPolitenessDelay() - (now - lastFetchTime));
                    }
                    lastFetchTime = (new Date()).getTime();
                }
            }
            driver.get(toFetchURL);

            fetchResult.setDriver(driver);
            fetchResult.setFetchedUrl(toFetchURL);
            // Setting HttpStatus
            int statusCode = driver.getStatusCode();

            if (statusCode == 499) {
                // Sometimes JBrowserDriver is not properly detecting status
                try {
                    if (driver.getPageSource() != null) {
                        statusCode = 200;
                    }
                } catch (NoSuchElementException e) {
                    // It is a real 499.
                    e.printStackTrace();
                }
            }

            // If Redirect ( 3xx )
            if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY ||
                    statusCode == HttpStatus.SC_MOVED_TEMPORARILY ||
                    statusCode == HttpStatus.SC_MULTIPLE_CHOICES ||
                    statusCode == HttpStatus.SC_SEE_OTHER ||
                    statusCode == HttpStatus.SC_TEMPORARY_REDIRECT ||
                    statusCode == 308) { // todo follow
                // https://issues.apache.org/jira/browse/HTTPCORE-389

                throw new IOException("Redirection not supported for Selenium. It should follow it automatically");
            } else if (statusCode >= 200 && statusCode <= 299) { // is 2XX, everything looks ok
                fetchResult.setFetchedUrl(toFetchURL);
                String uri = driver.getCurrentUrl();
                if (!uri.equals(toFetchURL)) {
                    if (!URLCanonicalizer.getCanonicalURL(uri).equals(toFetchURL)) {
                        fetchResult.setFetchedUrl(uri);
                    }
                }

            }

            fetchResult.setStatusCode(statusCode);
            return fetchResult;

        } catch (InterruptedException | IOException | RuntimeException e) {
            driver.quit();
            throw e;
        }
    }

    public synchronized void shutDown() {
    }

    protected CrawlConfig getConfig() {
        return config;
    }
}
