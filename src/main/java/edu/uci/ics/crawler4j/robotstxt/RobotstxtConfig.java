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
   * The maximum number of hosts for which their robots.txt is cached.
   */
  private int cacheSize = 500;

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
}