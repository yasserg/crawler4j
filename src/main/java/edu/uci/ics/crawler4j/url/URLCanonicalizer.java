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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * See http://en.wikipedia.org/wiki/URL_normalization for a reference
 * Note: some parts of the code are adapted from: http://stackoverflow.com/a/4057470/405418
 * 
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class URLCanonicalizer {

	public static String getCanonicalURL(String url) {
		URL canonicalURL = getCanonicalURL(url, null);
		if (canonicalURL != null) {
			return canonicalURL.toExternalForm();
		}
		return null;
	}

	public static URL getCanonicalURL(String href, String context) {

		/*
		 * Lower case the URL
		 */
		href = href.toLowerCase();

		try {
			
			URL canonicalURL;
			if (context == null) {
				canonicalURL = new URL(href);
			} else {
				canonicalURL = new URL(new URL(context), href);
			}

			String path = canonicalURL.getPath();

			/*
			 * Normalize: no empty segments (i.e., "//"), no segments equal to
			 * ".", and no segments equal to ".." that are preceded by a segment
			 * not equal to "..".
			 */
			path = new URI(path).normalize().toString();

			/*
			 * Convert '//' -> '/'
			 */
			int idx = path.indexOf("//");
			while (idx >= 0) {
				path = path.replace("//", "/");
				idx = path.indexOf("//");
			}

			/*
			 * Drop starting '/../'
			 */
			while (path.startsWith("/../")) {
				path = path.substring(3);
			}

			/*
			 * Trim
			 */
			path = path.trim();

			final SortedMap<String, String> params = createParameterMap(canonicalURL.getQuery());
			final String queryString;

			if (params != null && params.size() > 0) {
				for (final Map.Entry<String, String> entry : params.entrySet()) {
					final String key = entry.getKey();
					if (key.contains("session")) {
						params.remove(key);
					}
				}
				queryString = "?" + canonicalize(params);
			} else {
				queryString = "";
			}

			/*
			 * Fix '?' and '&' problems
			 */
			/*int index = path.lastIndexOf('?');
			if (index > 0) {
				if (index == (path.length() - 1)) {
					// '?' is the last char. Drop it.
					path = path.substring(0, path.length() - 1);
				} else if (path.charAt(index + 1) == '&') {
					// Next char is '&'. Strip it.
					if (path.length() == (index + 2)) {
						// Then url ends with '?&'. Strip them.
						path = path.substring(0, path.length() - 2);
					} else {
						// The '&' is redundant. Strip it.
						path = path.substring(0, index + 1) + path.substring(index + 2);
					}
				} else if (path.charAt(path.length() - 1) == '&') {
					path = path.substring(0, path.length() - 1);
				}
			}*/

			/*
			 * Add starting slash if needed
			 */
			if (path.length() == 0) {
				path = "/" + path;
			}

			/*
			 * Drop default port: example.com:80 -> example.com
			 */
			int port = canonicalURL.getPort();
			if (port == canonicalURL.getDefaultPort()) {
				port = -1;
			}

			return new URL(canonicalURL.getProtocol(), canonicalURL.getHost(), port, path + queryString);

		} catch (MalformedURLException ex) {
			return null;
		} catch (URISyntaxException ex) {
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
		if (queryString == null || queryString.isEmpty()) {
			return null;
		}

		final String[] pairs = queryString.split("&");
		final Map<String, String> params = new HashMap<String, String>(pairs.length);

		for (final String pair : pairs) {
			if (pair.length() < 1) {
				continue;
			}

			String[] tokens = pair.split("=", 2);
			for (int j = 0; j < tokens.length; j++) {
				try {
					tokens[j] = URLDecoder.decode(tokens[j], "UTF-8");
				} catch (UnsupportedEncodingException ex) {
					ex.printStackTrace();
				}
			}
			switch (tokens.length) {
			case 1: {
				if (pair.charAt(0) == '=') {
					params.put("", tokens[0]);
				} else {
					params.put(tokens[0], "");
				}
				break;
			}
			case 2: {
				params.put(tokens[0], tokens[1]);
				break;
			}
			}
		}
		return new TreeMap<String, String>(params);
	}

	/**
	 * Canonicalize the query string.
	 * 
	 * @param sortedParamMap
	 *            Parameter name-value pairs in lexicographical order.
	 * @return Canonical form of query string.
	 */
	private static String canonicalize(final SortedMap<String, String> sortedParamMap) {
		if (sortedParamMap == null || sortedParamMap.isEmpty()) {
			return "";
		}

		final StringBuffer sb = new StringBuffer(350);
		final Iterator<Map.Entry<String, String>> iter = sortedParamMap.entrySet().iterator();

		while (iter.hasNext()) {
			final Map.Entry<String, String> pair = iter.next();
			sb.append(percentEncodeRfc3986(pair.getKey()));
			sb.append('=');
			sb.append(percentEncodeRfc3986(pair.getValue()));
			if (iter.hasNext()) {
				sb.append('&');
			}
		}

		return sb.toString();
	}

	/**
	 * Percent-encode values according the RFC 3986. The built-in Java
	 * URLEncoder does not encode according to the RFC, so we make the extra
	 * replacements.
	 * 
	 * @param string
	 *            Decoded string.
	 * @return Encoded string per RFC 3986.
	 */
	private static String percentEncodeRfc3986(final String string) {
		try {
			return URLEncoder.encode(string, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
		} catch (UnsupportedEncodingException e) {
			return string;
		}
	}
}
