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

package edu.uci.ics.crawler4j.robotstxt;

import java.util.StringTokenizer;

/**
 * @author Yasser Ganjisaffar
 */


public class RobotstxtParser {

  private static final String PATTERNS_USERAGENT = "(?i)^User-agent:.*";
  private static final String PATTERNS_DISALLOW = "(?i)Disallow:.*";
  private static final String PATTERNS_ALLOW = "(?i)Allow:.*";

  private static final int PATTERNS_USERAGENT_LENGTH = 11;
  private static final int PATTERNS_DISALLOW_LENGTH = 9;
  private static final int PATTERNS_ALLOW_LENGTH = 6;

  public static HostDirectives parse(String content, String myUserAgent) {

    HostDirectives directives = null;
    boolean inMatchingUserAgent = false;

    StringTokenizer st = new StringTokenizer(content, "\n\r");
    while (st.hasMoreTokens()) {
      String line = st.nextToken();

      int commentIndex = line.indexOf('#');
      if (commentIndex > -1) {
        line = line.substring(0, commentIndex);
      }

      // remove any html markup
      line = line.replaceAll("<[^>]+>", "");

      line = line.trim();

      if (line.isEmpty()) {
        continue;
      }

      if (line.matches(PATTERNS_USERAGENT)) {
        String ua = line.substring(PATTERNS_USERAGENT_LENGTH).trim().toLowerCase();
        inMatchingUserAgent = "*".equals(ua) || ua.contains(myUserAgent);
      } else if (line.matches(PATTERNS_DISALLOW)) {
        if (!inMatchingUserAgent) {
          continue;
        }
        String path = line.substring(PATTERNS_DISALLOW_LENGTH).trim();
        if (path.endsWith("*")) {
          path = path.substring(0, path.length() - 1);
        }
        path = path.trim();
        if (!path.isEmpty()) {
          if (directives == null) {
            directives = new HostDirectives();
          }
          directives.addDisallow(path);
        }
      } else if (line.matches(PATTERNS_ALLOW)) {
        if (!inMatchingUserAgent) {
          continue;
        }
        String path = line.substring(PATTERNS_ALLOW_LENGTH).trim();
        if (path.endsWith("*")) {
          path = path.substring(0, path.length() - 1);
        }
        path = path.trim();
        if (directives == null) {
          directives = new HostDirectives();
        }
        directives.addAllow(path);
      }
    }

    return directives;
  }
}