/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.url;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Trust the path and query in the URL passed as arguments but transform the #! into _escaped_fragment_ in order to follow links from one page app.
 *
 * @author Bounkong Khamphousone
 */
public class URLEscapedFragment implements URLTransformer {

    /**
     * {@see https://developers.google.com/webmasters/ajax-crawling/docs/specification}
     */
    private static final String ESCAPED_FRAGMENT_PATTERN = "#!";

    private static final String ESCAPED_FRAGMENT_KEY = "_escaped_fragment_";
    public static final String ESCAPED_FRAGMENT_KEY_GET_PATTERN = "[" + Pattern.quote("&") + "|" + Pattern.quote("?") + "]?" + Pattern.quote(ESCAPED_FRAGMENT_KEY) + "[" + Pattern.quote("=") + "]?";

    public String transform(String url) {
        return transform(url, null);
    }

    public String transform(String href, String context) {

        try {
            // Replace _escaped_fragment_ with its pattern before proceding the build of the new URL.
            URL url = new URL(UrlResolver.resolveUrl((context == null) ? "" : context.replaceFirst(ESCAPED_FRAGMENT_KEY_GET_PATTERN, ESCAPED_FRAGMENT_PATTERN), href));

            String host = url.getHost().toLowerCase();
            if (Objects.equals(host, "")) {
                // This is an invalid Url.
                return null;
            }

            final StringBuilder queryString = new StringBuilder();
            if (url.getQuery() != null) {
                queryString.append("?").append(url.getQuery());
            }
            AbstractMap.SimpleImmutableEntry<String, String> escapedFragment = createEscapedFragment(url);
            if (escapedFragment != null) {
                if (queryString.length() == 0) {
                    queryString.append("?");
                } else {
                    queryString.append("&");
                }
                queryString.append(URLUtils.percentEncodeRfc3986(escapedFragment.getKey())).append("=").append(URLUtils.percentEncodeRfc3986(escapedFragment
                        .getValue()));
            }

            //Drop default port: example.com:80 -> example.com
            int port = url.getPort();
            if (port == url.getDefaultPort()) {
                port = -1;
            }

            String protocol = url.getProtocol().toLowerCase();
            String pathAndQueryString = url.getPath() + queryString;

            URL result = new URL(protocol, host, port, pathAndQueryString);
            return result.toExternalForm();

        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * @param url url to extract information from
     * @return Always return at least an empty map and have a maximum of one element which is the escaped fragment entry.
     */
    private static AbstractMap.SimpleImmutableEntry<String, String> createEscapedFragment(final URL url) {
        final AbstractMap.SimpleImmutableEntry<String, String> escapedFragment;
        String urlAsString = url.toString();
        if (urlAsString.contains(ESCAPED_FRAGMENT_PATTERN)) {
            String[] pairs = urlAsString.split(Pattern.quote(ESCAPED_FRAGMENT_PATTERN), 2);
            escapedFragment = new AbstractMap.SimpleImmutableEntry(ESCAPED_FRAGMENT_KEY, pairs[1]);
        } else {
            escapedFragment = null;
        }
        return escapedFragment;
    }

    @Override
    public String toString() {
        return getClass().getName();
    }
}