/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.parser;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;

public class CssParseData extends TextParseData {

    private Set<WebURL> parseOutgoingUrls(WebURL referringPage) throws UnsupportedEncodingException {

        Set<String> extractedUrls = extractUrlInCssText(this.getTextContent());

        final String pagePath = referringPage.getPath();
        final String pageUrl = referringPage.getURL();

        Set<WebURL> outgoingUrls = new HashSet<>();
        for (String url : extractedUrls) {

            String relative = getLinkRelativeTo(pagePath, url);
            String absolute = getAbsoluteUrlFrom(URLCanonicalizer.getCanonicalURL(pageUrl), relative);

            WebURL webURL = new WebURL();
            webURL.setURL(absolute);
            outgoingUrls.add(webURL);

        }
        return outgoingUrls;
    }

    public void setOutgoingUrls(WebURL referringPage) throws UnsupportedEncodingException {

        Set<WebURL> outgoingUrls = parseOutgoingUrls(referringPage);
        this.setOutgoingUrls(outgoingUrls);
    }

    private static Set<String> extractUrlInCssText(String input) {

        Set<String> extractedUrls = new HashSet<>();
        if (input == null || input.isEmpty()) {
            return extractedUrls;
        }

        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String url = matcher.group(1);
            if (url == null) {
                url = matcher.group(2);
            }
            if (url == null) {
                url = matcher.group(3);
            }
            if (url == null || url.startsWith("data:")) {
                continue;
            }
            extractedUrls.add(url);
        }
        return extractedUrls;
    }

    private static final Pattern pattern = initializePattern();

    private static Pattern initializePattern() {
        return Pattern.compile("url\\(\\s*'([^\\)]+)'\\s*\\)" +     // url('...')
                "|url\\(\\s*\"([^\\)]+)\"\\s*\\)" +                  // url("...")
                "|url\\(\\s*([^\\)]+)\\s*\\)" +                       // url(...)
                "|\\/\\*(\\*(?!\\/)|[^*])*\\*\\/");                 // ignore comments
    }

    private static String getAbsoluteUrlFrom(String pageUrl, String linkPath) {

        String domainUrl = getFullDomainFromUrl(pageUrl);
        if (linkPath.startsWith("/")) {
            return domainUrl + linkPath;
        }
        return domainUrl + "/" + linkPath;
    }

    private static String getLinkRelativeTo(String pagePath, String linkUrl) {

        if (linkUrl.startsWith("/") && !linkUrl.startsWith("//")) {
            return linkUrl;
        }

        if (linkUrl.startsWith("//")) {
            linkUrl = "http" + linkUrl;
        }

        if (linkUrl.startsWith("http")) {
            String domainUrl = getPathFromUrl(linkUrl);
            return domainUrl;
        }

        if (linkUrl.startsWith("../")) {

            String[] parts = pagePath.split("/");

            int pos = linkUrl.lastIndexOf("../") + 3;
            int parents = pos / 3;
            long diff = parts.length - parents - 1;

            String absolute = "";
            for (int i = 0; i < diff; i++) {
                String dir = parts[i];
                if (!dir.isEmpty()) {
                    absolute = absolute + "/" + dir;
                }
            }
            return absolute + "/" + linkUrl.substring(pos);
        }

        String root = getDirsFromUrl(pagePath);
        return root + linkUrl;
    }

    private static String getDirsFromUrl(String urlPath) {

        int pos = urlPath.lastIndexOf("/") + 1;
        String root = urlPath.substring(0, pos);
        return root;
    }

    private static String getPathFromUrl(String url) {

        int pos1 = url.indexOf("//") + 2;              // http://subdomain.domain:port/dir/page.ext
        int pos2 = url.indexOf("/", pos1);
        String path = url.substring(pos2);
        return path;
    }

    private static String getFullDomainFromUrl(String url) {

        int pos1 = url.indexOf("//") + 2;              // http://subdomain.domain:port/dir/page.ext
        int pos2 = url.indexOf("/", pos1);
        String path = url.substring(0, pos2);
        return path;
    }

}