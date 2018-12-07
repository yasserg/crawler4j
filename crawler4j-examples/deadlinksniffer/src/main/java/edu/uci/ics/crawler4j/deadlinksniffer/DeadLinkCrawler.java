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

package edu.uci.ics.crawler4j.deadlinksniffer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.ImageData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.http.Header;
import org.apache.http.HttpStatus;

/**
 * TODO: Currently not thread safe!
 *
 * @author Yasser Ganjisaffar
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
public class DeadLinkCrawler extends WebCrawler {

    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(".*\\.(bmp|gif|jpg|png|jpeg|css|js|pdf)$");


    private AtomicInteger maxVisits = new AtomicInteger(0);

    private File rootFolder;

    /**
     * contains all broken Urls detected in {@link #handlePageStatusCode(WebURL, int, String)}
     */
    private ConcurrentMap<String, Integer> brokenUrls = new ConcurrentHashMap();


    /**
     * You should implement this function to specify whether the given url
     * should be crawled or not (based on your crawling logic).
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        // Ignore the url if it has an extension that matches our defined set of image extensions.
        if (isImageLink(href)) {
            return false;
        }

        // Only accept the url if it is in the requested url domains.
        if (!((DeadLinkCrawlConfig) getMyController().getConfig()).getUrlPatterns()
                .stream()
                .anyMatch(pattern -> pattern.matcher(href).matches())) {
            return false;
        }

        // and also only if the url is not explicitly excluded
        if (((DeadLinkCrawlConfig) getMyController().getConfig()).getExcludePatterns()
                .stream()
                .anyMatch(pattern -> pattern.matcher(href).matches())) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean shouldFollowLinksIn(WebURL url) {
        int visits = maxVisits.incrementAndGet();
        logger.info("Number of visits so far: {}", visits);
        return  true;
    }

    @Override
    protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
        if (statusCode != HttpStatus.SC_OK &&
            statusCode != HttpStatus.SC_TEMPORARY_REDIRECT &&
            statusCode != HttpStatus.SC_MOVED_TEMPORARILY &&
            statusCode != HttpStatus.SC_MOVED_PERMANENTLY) {
            logger.info("\n\n FEHLERHAFTE SEITE status {} {} \n\n", statusCode, webUrl.getURL());
            brokenUrls.put(webUrl.getURL(), statusCode);
            getConfig().getCrawlerStore().storePageStatus(statusCode, webUrl);
        }
    }

    /**
     * This function is called when a page is fetched and ready to be processed
     * by your program.
     */
    @Override
    public void visit(Page page) {
        int docid = page.getWebURL().getDocid();
        String url = page.getWebURL().getURL();
        String domain = page.getWebURL().getDomain();
        String path = page.getWebURL().getPath();
        String subDomain = page.getWebURL().getSubDomain();
        String parentUrl = page.getWebURL().getParentUrl();
        String anchor = page.getWebURL().getAnchor();

        logger.debug("Docid: {}", docid);
        logger.info("URL: {}", url);
        logger.debug("Domain: '{}'", domain);
        logger.debug("Sub-domain: '{}'", subDomain);
        logger.debug("Path: '{}'", path);
        logger.debug("Parent page: {}", parentUrl);
        logger.debug("Anchor text: {}", anchor);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            logger.debug("Text length: {}", text.length());
            logger.debug("Html length: {}", html.length());
            logger.debug("Number of outgoing links: {}", links.size());

            for (WebURL link : links) {
                if (brokenUrls.keySet().contains(link.getURL())) {
                    getConfig().getCrawlerStore().storePageStatus(brokenUrls.get(link.getURL()), link);
                }
            }

            storeHtml(page.getWebURL(), html);


            List<ImageData> imageDatas = htmlParseData.getImageData();
            int imgNr = 0;
            for (ImageData imageData : imageDatas) {
                imgNr++;
                getConfig().getCrawlerStore().storeImageInfo(page, imgNr, imageData);
            }
        }

        Header[] responseHeaders = page.getFetchResponseHeaders();
        if (responseHeaders != null) {
            logger.debug("Response heade rs:");
            for (Header header : responseHeaders) {
                logger.debug("\t{}: {}", header.getName(), header.getValue());
            }
        }

        logger.debug("=============");
    }

    private void storeHtml(WebURL webURL, String html) {
        String rootUrl = webURL.getRootUrl();
        File rootUrlDir = new File(getRootFolder(), rootUrl.replace("/", "_"));
        if (!rootUrlDir.exists()) {
            rootUrlDir.mkdir();
        }


        File f = new File(rootUrlDir, webURL.getURL().replace("/", "_"));
        if (f.exists()) {
            return;
        }
        try (FileWriter fw = new FileWriter(f)) {
            fw.write(html);
        }
        catch (IOException e) {
            logger.error("could not store file " + f.toString(), e);
        }
    }

    @Override
    public void onBeforeExit() {
        getConfig().getCrawlerStore().close();
    }

    private DeadLinkCrawlConfig getConfig() {
        return (DeadLinkCrawlConfig) getMyController().getConfig();
    }

    public File getRootFolder() {
        if (rootFolder == null) {
            rootFolder = new File(getConfig().getCrawlStorageFolder(), "content");
            rootFolder.mkdirs();
        }
        return rootFolder;
    }

    private boolean isImageLink(String href) {
        return IMAGE_EXTENSIONS.matcher(href).matches();
    }


}
