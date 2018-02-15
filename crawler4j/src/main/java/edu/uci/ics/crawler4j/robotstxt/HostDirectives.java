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

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * @author Yasser Ganjisaffar
 */
public class HostDirectives {
    // If we fetched the directives for this host more than
    // 24 hours, we have to re-fetch it.
    private static final long EXPIRATION_DELAY = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);

    public static final int ALLOWED = 1;
    public static final int DISALLOWED = 2;
    public static final int UNDEFINED = 3;

    /** A list of rule sets, sorted on match with the configured user agent */
    private Set<UserAgentDirectives> rules;

    private final long timeFetched;
    private long timeLastAccessed;
    private RobotstxtConfig config;
    private String userAgent;

    public HostDirectives(RobotstxtConfig configuration) {
        timeFetched = System.currentTimeMillis();
        config = configuration;
        userAgent = config.getUserAgentName().toLowerCase();
        rules = new TreeSet<UserAgentDirectives>(
            new UserAgentDirectives.UserAgentComparator(userAgent));
    }

    public boolean needsRefetch() {
        return ((System.currentTimeMillis() - timeFetched) > EXPIRATION_DELAY);
    }

    /**
     * Check if the host directives allows visiting path.
     *
     * @param path The path to check
     * @return True if the path is not disallowed, false if it is
     */
    public boolean allows(String path) {
        return checkAccess(path) != DISALLOWED;
    }

    /**
     * Change the user agent string used to crawl after initialization. This will
     * reorder (recreate) the list of user agent directives for this host.
     *
     * @param userAgent The new user agent to use.
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent.toLowerCase();

        // Re-order the set
        Set<UserAgentDirectives> replace = new TreeSet<UserAgentDirectives>(
            new UserAgentDirectives.UserAgentComparator(this.userAgent));
        replace.addAll(rules);
        rules = replace;
    }

    /**
     * Check if the host directives explicitly disallow visiting path.
     *
     * @param path The path to check
     * @return True if the path is explicity disallowed, false otherwise
     */
    public boolean disallows(String path) {
        return checkAccess(path) == DISALLOWED;
    }

    /**
     * Check if any of the rules say anything about the specified path
     *
     * @param path The path to check
     * @return One of ALLOWED, DISALLOWED or UNDEFINED
     */
    public int checkAccess(String path) {
        timeLastAccessed = System.currentTimeMillis();
        int result = UNDEFINED;
        String myUA = config.getUserAgentName();
        boolean ignoreUADisc = config.getIgnoreUADiscrimination();

        // When checking rules, the list of rules is already ordered based on the
        // match of the user-agent of the clause with the user-agent of the crawler.
        // The most specific match should come first.
        //
        // Only the most specific match is obeyed, unless ignoreUADiscrimination is
        // enabled. In that case, any matching non-wildcard clause that explicitly
        // disallows the path is obeyed. If no such rule exists and any UA in the list
        // is allowed access, that rule is obeyed.
        for (UserAgentDirectives ua : rules) {
            int score = ua.match(myUA);

            // If ignoreUADisc is disabled and the current UA doesn't match,
            // the rest will not match so we are done here.
            if (score == 0 && !ignoreUADisc) {
                break;
            }

            // Match the rule to the path
            result = ua.checkAccess(path, userAgent);

            // If the result is ALLOWED or UNDEFINED, or if
            // this is a wildcard rule and ignoreUADisc is disabled,
            // this is the final verdict.
            if (result != DISALLOWED || (!ua.isWildcard() || !ignoreUADisc)) {
                break;
            }

            // This is a wildcard rule that disallows access. The verdict is stored,
            // but the other rules will also be checked to see if any specific UA is allowed
            // access to this path. If so, that positive UA discrimination is ignored
            // and we crawl the page anyway.
        }
        return result;
    }

    /**
     * Store set of directives
     *
     * @param directives The set of directives to add to this host
     */
    public void addDirectives(UserAgentDirectives directives) {
        rules.add(directives);
    }

    public long getLastAccessTime() {
        return timeLastAccessed;
    }
}
