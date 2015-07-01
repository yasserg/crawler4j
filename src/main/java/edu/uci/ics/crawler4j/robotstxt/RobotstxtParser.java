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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yasser Ganjisaffar
 */
public class RobotstxtParser {
  private static final Logger logger = LoggerFactory.getLogger(RobotstxtParser.class);
  private static final Pattern RULE_PATTERN = Pattern.compile("(?i)^([A-Za-z\\-]+):(.*)");
  private static final HashSet<String> VALID_RULES = new HashSet<String>(Arrays.asList("allow", "disallow", "user-agent", "crawl-delay", "host", "sitemap"));  

  public static HostDirectives parse(String content, RobotstxtConfig config) {
    HostDirectives directives = new HostDirectives(config);
    StringTokenizer st = new StringTokenizer(content, "\n\r");

    Set<String> userAgents = new HashSet<String>();
    UserAgentDirectives ua_directives = null;
    
    while (st.hasMoreTokens()) {
      String line = st.nextToken();

      // Strip comments
      int commentIndex = line.indexOf('#');
      if (commentIndex > -1) {
        line = line.substring(0, commentIndex);
      }

      // remove any html markup
      line = line.replaceAll("<[^>]+>", "").trim();
      if (line.isEmpty()) {
        continue;
      }
      
      Matcher m = RULE_PATTERN.matcher(line);
      if (m.matches()) {
        String rule = m.group(1).toLowerCase();
        String value = m.group(2).trim();
        
        if (VALID_RULES.contains(rule)) {
          if (rule.equals("user-agent")) {
            String currentUserAgent = value.toLowerCase();
            if (ua_directives != null) {
              // If ua_directives is not null, this means that one or
              // more rules followed the User-agent: definition list
              // In that case, it's not allowed to add more user-agents,
              // so this is an entirely new set of directives.
              userAgents = new HashSet<String>();
              directives.addDirectives(ua_directives);
              ua_directives = null;
            }
            userAgents.add(currentUserAgent);
          } else {
            if (ua_directives == null) {
              if (userAgents.isEmpty()) // No "User-agent": clause defaults to wildcard UA
                userAgents.add("*");
              ua_directives = new UserAgentDirectives(userAgents);
            }
            ua_directives.add(rule,  value);
          }
        } else {
          logger.info("Unrecognized rule in robots.txt: {}", rule);
        }
      } else {
        logger.debug("Unrecognized line in robots.txt: {}", line);
      }
    }

    return directives;
  }
}
