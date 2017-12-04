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

package edu.uci.ics.crawler4j.crawler.fetcher;

import java.io.IOException;

import org.apache.http.*;
import org.apache.http.util.EntityUtils;
import org.slf4j.*;

import edu.uci.ics.crawler4j.crawler.Page;

/**
 * @author Yasser Ganjisaffar
 */
public class FetchedPageImpl implements FetchedPage {

    protected static final Logger logger = LoggerFactory.getLogger(FetchedPageImpl.class);

    private int statusCode;

    private String fetchedUrl;

    private String movedToUrl;

    private Header[] responseHeaders;

    private HttpEntity entity;

    @Override
    public boolean fetchContent(Page page, int maxBytes) {
        try {
            page.load(entity, maxBytes);
            page.setFetchResponseHeaders(responseHeaders);
            return true;
        } catch (Exception e) {
            logger.info("Exception while fetching content for: {} [{}]", page.getWebURL().getURL(),
                    e.getMessage());
        }
        return false;
    }

    @Override
    public void discardContentIfNotConsumed() {
        try {
            if (entity != null) {
                EntityUtils.consume(entity);
            }
        } catch (IOException ignored) {
            // We can EOFException (extends IOException) exception. It can happen on compressed
            // streams which are not
            // repeatable
            // We can ignore this exception. It can happen if the stream is closed.
        } catch (Exception e) {
            logger.warn("Unexpected error occurred while trying to discard content", e);
        }
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
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
    public String getMovedToUrl() {
        return movedToUrl;
    }

    @Override
    public void setMovedToUrl(String movedToUrl) {
        this.movedToUrl = movedToUrl;
    }

    @Override
    public Header[] getResponseHeaders() {
        return responseHeaders;
    }

    @Override
    public void setResponseHeaders(Header[] responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    @Override
    public HttpEntity getEntity() {
        return entity;
    }

    @Override
    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }

}
