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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.crawler4j.url.WebURL;

/**
 * @author Yasser Ganjisaffar
 */
public class HostDirectives {

  // If we fetched the directives for this host more than
  // 24 hours, we have to re-fetch it.
  private static final long EXPIRATION_DELAY = 24 * 60 * 1000L;

  private final RuleSet disallows = new RuleSet();
  private final RuleSet allows = new RuleSet();
  private final Set<WebURL> sitemaps = new HashSet<WebURL>();

  private final long timeFetched;
  private long timeLastAccessed;

  public HostDirectives() {
    timeFetched = System.currentTimeMillis();
  }

  public boolean needsRefetch() {
    return ((System.currentTimeMillis() - timeFetched) > EXPIRATION_DELAY);
  }

  public boolean allows(String path) {
    timeLastAccessed = System.currentTimeMillis();
    return !disallows.containsPrefixOf(path) || allows.containsPrefixOf(path);
  }

  public void addDisallow(String path) {
    disallows.add(path);
  }

  public void addAllow(String path) {
    allows.add(path);
  }

  public long getLastAccessTime() {
    return timeLastAccessed;
  }
  
  public void addSitemap(String url) {
	  WebURL wurl = new WebURL();
	  wurl.setURL(url);
	  sitemaps.add(wurl);
  }
  
  public Collection<WebURL> getSitemaps()
  {
	  return sitemaps;
  }
}