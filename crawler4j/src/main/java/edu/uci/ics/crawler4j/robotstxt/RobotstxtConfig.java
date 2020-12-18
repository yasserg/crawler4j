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

public class RobotstxtConfig {

    /**
     * Should the crawler obey Robots.txt protocol? More info on Robots.txt is
     * available at http://www.robotstxt.org/
     */
    private boolean enabled = true;

    /**
     * user-agent name that will be used to determine whether some servers have
     * specific rules for this agent name.
     */
    private String userAgentName = "crawler4j";

    /**
     * Whether to ignore positive user-agent discrimination. There are websties that use
     * a white-list system where they explicitly allow Googlebot but disallow all other
     * bots by a "User-agent: * Disallow: /" rule. Setting this setting to true
     * will ignore the user-agent and apply the "Allow" rule to all user-agents.
     * This can still be overridden when a robots.txt explicitly disallows the configured
     * User-agent, as such a rule supersedes the generic rule.
     */
    private boolean ignoreUADiscrimination = false;

    /**
     * The maximum number of hosts for which their robots.txt is cached.
     */
    private int cacheSize = 500;

    /**
     * The milliseconds before a regexp timeouts. -1 means no timeout. This will
     * reduce regexp performance.
     */
    private long timeout = -1;

    /**
     * If true, the system will consider that a timed out regexp is a matching one.
     */
    private boolean matchOnTimeout = false;

    /**
     * In order to be able to timeout inside a matcher, the system check's for timeout
     * while reading the CharSecuence. This parameter sets the number of characters that
     * will be read between timeout checking. Higher values means less CPU overhead and
     * less accuracy on timeout. Default value is 30000000. Null means default.
     */
    private Integer checkTimeoutInterval = null;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUserAgentName() {
        return userAgentName;
    }

    public void setUserAgentName(String userAgentName) {
        this.userAgentName = userAgentName;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public void setIgnoreUADiscrimination(boolean ignore) {
        this.ignoreUADiscrimination = ignore;
    }

    public boolean getIgnoreUADiscrimination() {
        return ignoreUADiscrimination;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public boolean isMatchOnTimeout() {
        return matchOnTimeout;
    }

    public void setMatchOnTimeout(boolean matchOnTimeout) {
        this.matchOnTimeout = matchOnTimeout;
    }

    public Integer getCheckTimeoutInterval() {
        return checkTimeoutInterval;
    }

    public void setCheckTimeoutInterval(Integer checkTimeoutInterval) {
        this.checkTimeoutInterval = checkTimeoutInterval;
    }
}
