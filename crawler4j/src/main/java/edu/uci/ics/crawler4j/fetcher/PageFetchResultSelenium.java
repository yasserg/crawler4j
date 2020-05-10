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

package edu.uci.ics.crawler4j.fetcher;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;

import edu.uci.ics.crawler4j.crawler.Page;

/**
 * @author Dario Goikoetxea
 */
public class PageFetchResultSelenium implements PageFetchResultInterface {

    protected static final Logger logger = LoggerFactory.getLogger(PageFetchResultSelenium.class);

    private boolean haltOnError;
    protected int statusCode;
    protected JBrowserDriver driver = null;
    protected String fetchedUrl = null;
    protected String movedToUrl = null;

    public PageFetchResultSelenium(boolean haltOnError) {
        this.haltOnError = haltOnError;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public JBrowserDriver getDriver() {
        return driver;
    }

    public void setDriver(JBrowserDriver driver) {
        this.driver = driver;
    }

    @Override
    public String getFetchedUrl() {
        return fetchedUrl;
    }

    @Override
    public void setFetchedUrl(String fetchedUrl) {
        this.fetchedUrl = fetchedUrl;
    }

    @Override
    public boolean fetchContent(Page page, int maxBytes) throws SocketTimeoutException, IOException {
        try {
            page.load(driver);
            return true;
        } catch (RuntimeException e) {
            if (haltOnError) {
                throw e;
            } else {
                logger.info("Exception while fetching content for: {} [{}]", page.getWebURL().getURL(),
                            e.getMessage());
            }
        }
        return false;
    }

    @Override
    public void discardContentIfNotConsumed() {
        try {
            if (driver != null) {
                driver.quit();
            }
        } catch (RuntimeException e) {
            if (haltOnError) {
                throw e;
            } else {
                logger.warn("Unexpected error occurred while closing Selenium WebDriver", e);
            }
        }
    }

    @Override
    public String getMovedToUrl() {
        return movedToUrl;
    }

    @Override
    public void setMovedToUrl(String movedToUrl) {
        this.movedToUrl = movedToUrl;
    }

    /**
     * This does not use entities.
     */
    @Override
    public HttpEntity getEntity() {
        return null;
    }

    /**
     * Selenium does not easilly support obtaining response headers
     *
     */
    @Override
    public Header[] getResponseHeaders() {
        return null;
    }
}
