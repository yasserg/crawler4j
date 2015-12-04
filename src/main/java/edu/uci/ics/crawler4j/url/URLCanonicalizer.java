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

package edu.uci.ics.crawler4j.url;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * See http://en.wikipedia.org/wiki/URL_normalization for a reference Note: some
 * parts of the code are adapted from: http://stackoverflow.com/a/4057470/405418
 *
 * @author Yasser Ganjisaffar
 */
public class URLCanonicalizer implements URLTransformer{

  public String transform(String url) {
    return transform(url, null);
  }

  public String transform(String href, String context) {

    try {
      URL canonicalURL = new URL(UrlResolver.resolveUrl((context == null) ? "" : context, href));

      String host = canonicalURL.getHost().toLowerCase();
      if (Objects.equals(host, "")) {
        // This is an invalid Url.
        return null;
      }

      String path = canonicalURL.getPath();

      /*
       * Normalize: no empty segments (i.e., "//"), no segments equal to
       * ".", and no segments equal to ".." that are preceded by a segment
       * not equal to "..".
       */
      path = new URI(path.replace("\\", "/")).normalize().toString();

      int idx = path.indexOf("//");
      while (idx >= 0) {
        path = path.replace("//", "/");
        idx = path.indexOf("//");
      }

      while (path.startsWith("/../")) {
        path = path.substring(3);
      }

      path = path.trim();

      final SortedMap<String, String> params = createParameterMap(canonicalURL.getQuery());
      final String queryString;
      if ((params != null) && !params.isEmpty()) {
        String canonicalParams = canonicalize(params);
        queryString = (canonicalParams.isEmpty() ? "" : ("?" + canonicalParams));
      } else {
        queryString = "";
      }

      if (path.isEmpty()) {
        path = "/";
      }

      //Drop default port: example.com:80 -> example.com
      int port = canonicalURL.getPort();
      if (port == canonicalURL.getDefaultPort()) {
        port = -1;
      }

      String protocol = canonicalURL.getProtocol().toLowerCase();
      String pathAndQueryString = URLUtils.normalizePath(path) + queryString;

      URL result = new URL(protocol, host, port, pathAndQueryString);
      return result.toExternalForm();

    } catch (MalformedURLException | URISyntaxException ex) {
      return null;
    }
  }

  /**
   * Takes a query string, separates the constituent name-value pairs, and
   * stores them in a SortedMap ordered by lexicographical order.
   *
   * @return Null if there is no query string.
   */
  private static SortedMap<String, String> createParameterMap(final String queryString) {
    if ((queryString == null) || queryString.isEmpty()) {
      return null;
    }

    final String[] pairs = queryString.split("&");
    final Map<String, String> params = new HashMap<>(pairs.length);

    for (final String pair : pairs) {
      if (pair.isEmpty()) {
        continue;
      }

      String[] tokens = pair.split("=", 2);
      switch (tokens.length) {
        case 1:
          if (pair.charAt(0) == '=') {
            params.put("", tokens[0]);
          } else {
            params.put(tokens[0], "");
          }
          break;
        case 2:
          params.put(tokens[0], tokens[1]);
          break;
      }
    }
    return new TreeMap<>(params);
  }

  /**
   * Canonicalize the query string.
   *
   * @param sortedParamMap
   *            Parameter name-value pairs in lexicographical order.
   * @return Canonical form of query string.
   */
  private static String canonicalize(final SortedMap<String, String> sortedParamMap) {
    if ((sortedParamMap == null) || sortedParamMap.isEmpty()) {
      return "";
    }

    final StringBuilder sb = new StringBuilder(100);
    for (Map.Entry<String, String> pair : sortedParamMap.entrySet()) {
      final String key = pair.getKey().toLowerCase();
      if ("jsessionid".equals(key) || "phpsessid".equals(key) || "aspsessionid".equals(key)) {
        continue;
      }
      if (sb.length() > 0) {
        sb.append('&');
      }
      sb.append(URLUtils.percentEncodeRfc3986(pair.getKey()));
      if (!pair.getValue().isEmpty()) {
        sb.append('=');
        sb.append(URLUtils.percentEncodeRfc3986(pair.getValue()));
      }
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return getClass().getName();
  }
}