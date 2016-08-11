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

package edu.uci.ics.crawler4j.frontier;


import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class maintains the list of pages which are
 * assigned to crawlers but are not yet processed.
 * It is used for resuming a previous crawl.
 *
 * @author Yasser Ganjisaffar
 */
public class InProcessPagesDB extends WorkQueues {
  private static final Logger logger = LoggerFactory.getLogger(InProcessPagesDB.class);

  public static final int DATABASE_INDEX = 3;

  public InProcessPagesDB(CrawlConfig crawlConfig) {
    super(DATABASE_INDEX,crawlConfig);
    long docCount = getLength();
    if (docCount > 0) {
      logger.info("Loaded {} URLs that have been in process in the previous crawl.", docCount);
    }
  }

  public boolean removeURL(WebURL webUrl) {
    synchronized (mutex) {
      String key = getDatabaseEntryKey(webUrl);
      getUrlsDB().del(key);
      getUrlsDB().zrem(ALL_URLS, key);
    }
    return true;
  }
}