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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.ByteArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * This class contains the data for a fetched and parsed page.
 *
 * @author Yasser Ganjisaffar
 */
public class Page {

    protected final Logger logger = LoggerFactory.getLogger(Page.class);

    /**
     * The URL of this page.
     */
    protected WebURL url;

    /**
     * Redirection flag
     */
    protected boolean redirect;

    /**
     * The URL to which this page will be redirected to
     */
    protected String redirectedToUrl;

    /**
     * Status of the page
     */
    protected int statusCode;

    /**
     * The content of this page in binary format.
     */
    protected byte[] contentData;

    /**
     * The ContentType of this page.
     * For example: "text/html; charset=UTF-8"
     */
    protected String contentType;

    /**
     * The encoding of the content.
     * For example: "gzip"
     */
    protected String contentEncoding;

    /**
     * The charset of the content.
     * For example: "UTF-8"
     */
    protected String contentCharset;

    /**
     * Language of the Content.
     */
    private String language;

    /**
     * Headers which were present in the response of the fetch request
     */
    protected Header[] fetchResponseHeaders = new Header[0];

    /**
     * The parsed data populated by parsers
     */
    protected ParseData parseData;

    /**
     * Whether the content was truncated because the received data exceeded the imposed maximum
     */
    protected boolean truncated = false;

    public Page(WebURL url) {
        this.url = url;
    }

    /**
     * Read contents from an entity, with a specified maximum. This is a replacement of
     * EntityUtils.toByteArray because that function does not impose a maximum size.
     *
     * @param entity The entity from which to read
     * @param maxBytes The maximum number of bytes to read
     * @return A byte array containing maxBytes or fewer bytes read from the entity
     *
     * @throws IOException Thrown when reading fails for any reason
     */
    protected byte[] toByteArray(HttpEntity entity, int maxBytes) throws IOException {
        if (entity == null) {
            return new byte[0];
        }
        try (InputStream is = entity.getContent()) {
            int size = (int) entity.getContentLength();
            int readBufferLength = size;

            if (readBufferLength <= 0) {
                readBufferLength = 4096;
            }
            // in case when the maxBytes is less than the actual page size
            readBufferLength = Math.min(readBufferLength, maxBytes);

            // We allocate the buffer with either the actual size of the entity (if available)
            // or with the default 4KiB if the server did not return a value to avoid allocating
            // the full maxBytes (for the cases when the actual size will be smaller than maxBytes).
            ByteArrayBuffer buffer = new ByteArrayBuffer(readBufferLength);

            byte[] tmpBuff = new byte[4096];
            int dataLength;

            while ((dataLength = is.read(tmpBuff)) != -1) {
                if (maxBytes > 0 && (buffer.length() + dataLength) > maxBytes) {
                    truncated = true;
                    dataLength = maxBytes - buffer.length();
                }
                buffer.append(tmpBuff, 0, dataLength);
                if (truncated) {
                    break;
                }
            }
            return buffer.toByteArray();
        }
    }

    /**
     * Loads the content of this page from a fetched HttpEntity.
     *
     * @param entity HttpEntity
     * @param maxBytes The maximum number of bytes to read
     * @throws IOException when load fails
     */
    public void load(HttpEntity entity, int maxBytes) throws IOException {

        contentType = null;
        Header type = entity.getContentType();
        if (type != null) {
            contentType = type.getValue();
        }

        contentEncoding = null;
        Header encoding = entity.getContentEncoding();
        if (encoding != null) {
            contentEncoding = encoding.getValue();
        }

        Charset charset;
        try {
            charset = ContentType.getOrDefault(entity).getCharset();
        } catch (Exception e) {
            logger.warn("parse charset failed: {}", e.getMessage());
            charset = Charset.forName("UTF-8");
        }

        if (charset != null) {
            contentCharset = charset.displayName();
        }

        contentData = toByteArray(entity, maxBytes);
    }

    public WebURL getWebURL() {
        return url;
    }

    public void setWebURL(WebURL url) {
        this.url = url;
    }

    public boolean isRedirect() {
        return redirect;
    }

    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

    public String getRedirectedToUrl() {
        return redirectedToUrl;
    }

    public void setRedirectedToUrl(String redirectedToUrl) {
        this.redirectedToUrl = redirectedToUrl;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Returns headers which were present in the response of the fetch request
     *
     * @return Header Array, the response headers
     */
    public Header[] getFetchResponseHeaders() {
        return fetchResponseHeaders;
    }

    public void setFetchResponseHeaders(Header[] headers) {
        fetchResponseHeaders = headers;
    }

    /**
     * @return parsed data generated for this page by parsers
     */
    public ParseData getParseData() {
        return parseData;
    }

    public void setParseData(ParseData parseData) {
        this.parseData = parseData;
    }

    /**
     * @return content of this page in binary format.
     */
    public byte[] getContentData() {
        return contentData;
    }

    public void setContentData(byte[] contentData) {
        this.contentData = contentData;
    }

    /**
     * @return ContentType of this page.
     * For example: "text/html; charset=UTF-8"
     */
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @return encoding of the content.
     * For example: "gzip"
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /**
     * @return charset of the content.
     * For example: "UTF-8"
     */
    public String getContentCharset() {
        return contentCharset;
    }

    public void setContentCharset(String contentCharset) {
        this.contentCharset = contentCharset;
    }

    /**
     * @return Language
     */
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isTruncated() {
        return truncated;
    }
}
