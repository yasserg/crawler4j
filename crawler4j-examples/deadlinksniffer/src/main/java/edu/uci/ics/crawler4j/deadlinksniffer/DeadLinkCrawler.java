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
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.http.Header;

/**
 * TODO: Currently not thread safe!
 *
 * @author Yasser Ganjisaffar
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
public class DeadLinkCrawler extends WebCrawler {

    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(".*\\.(bmp|gif|jpg|png|jpeg|css|js)$");


    private AtomicInteger maxVisits = new AtomicInteger(0);

    private File rootFolder;
    private FileWriter brokenPages;


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
        return ((DeadLinkCrawlConfig) getMyController().getConfig()).getUrlPatterns()
                .stream()
                .anyMatch(pattern -> pattern.matcher(href).matches());
    }

    @Override
    protected boolean shouldFollowLinksIn(WebURL url) {
        int visits = maxVisits.incrementAndGet();
        logger.info("Number of visits so far: {}", visits);
        return  true;
    }

    @Override
    protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
        if (statusCode != 200) {
            logger.info("\n\n FEHLERHAFTE SEITE status {} {} \n\n", statusCode, webUrl.getURL());
            try {
                getBrokenPages().append("" + statusCode + ", " + webUrl.getURL() + ", " + webUrl.getParentUrl() + "\n");
                getBrokenPages().flush();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
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

            storeHtml(url, html);
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

    private void storeHtml(String url, String html) {
        File f = new File(getRootFolder(), url.replace("/", "_"));
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
        closeFile(brokenPages, "errorPages");
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

    public File getRootFolder() {
        if (rootFolder == null) {
            rootFolder = new File(getMyController().getConfig().getCrawlStorageFolder(), "content");
            rootFolder.mkdirs();
        }
        return rootFolder;
    }

    private boolean isImageLink(String href) {
        return IMAGE_EXTENSIONS.matcher(href).matches();
    }


    private FileWriter getBrokenPages() {
        if (brokenPages == null) {
            try {
                brokenPages = new FileWriter(new File(getMyController().getConfig().getCrawlStorageFolder(), "brokenPages.csv"));

            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        return brokenPages;
    }
}
