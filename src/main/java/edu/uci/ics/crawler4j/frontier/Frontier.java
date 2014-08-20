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

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import edu.uci.ics.crawler4j.crawler.Configurable;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.frontier.Counters.ReservedCounterNames;
import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */

public class Frontier extends Configurable {

  protected static final Logger logger = LoggerFactory.getLogger(Frontier.class);

  protected WorkQueues workQueues;

  protected InProcessPagesDB inProcessPages;

  protected final Object mutex = new Object();
  protected final Object waitingList = new Object();

  protected boolean isFinished = false;

  protected long scheduledPages;

  protected DocIDServer docIdServer;

  protected Counters counters;

  public Frontier(Environment env, CrawlConfig config, DocIDServer docIdServer) {
    super(config);
    this.counters = new Counters(env, config);
    this.docIdServer = docIdServer;
    try {
      workQueues = new WorkQueues(env, "PendingURLsDB", config.isResumableCrawling());
      if (config.isResumableCrawling()) {
        scheduledPages = counters.getValue(ReservedCounterNames.SCHEDULED_PAGES);
        inProcessPages = new InProcessPagesDB(env);
        long numPreviouslyInProcessPages = inProcessPages.getLength();
        if (numPreviouslyInProcessPages > 0) {
          logger.info("Rescheduling {} URLs from previous crawl.", numPreviouslyInProcessPages);
          scheduledPages -= numPreviouslyInProcessPages;
          while (true) {
            List<WebURL> urls = inProcessPages.get(100);
            if (urls.size() == 0) {
              break;
            }
            scheduleAll(urls);
            inProcessPages.delete(urls.size());
          }
        }
      } else {
        inProcessPages = null;
        scheduledPages = 0;
      }
    } catch (DatabaseException e) {
      logger.error("Error while initializing the Frontier: {}", e.getMessage());
      workQueues = null;
    }
  }

  public void scheduleAll(List<WebURL> urls) {
    int maxPagesToFetch = config.getMaxPagesToFetch();
    synchronized (mutex) {
      int newScheduledPage = 0;
      for (WebURL url : urls) {
        if (maxPagesToFetch > 0 && (scheduledPages + newScheduledPage) >= maxPagesToFetch) {
          break;
        }
        try {
          workQueues.put(url);
          newScheduledPage++;
        } catch (DatabaseException e) {
          logger.error("Error while putting the url in the work queue.");
        }
      }
      if (newScheduledPage > 0) {
        scheduledPages += newScheduledPage;
        counters.increment(Counters.ReservedCounterNames.SCHEDULED_PAGES, newScheduledPage);
      }
      synchronized (waitingList) {
        waitingList.notifyAll();
      }
    }
  }

  public void schedule(WebURL url) {
    int maxPagesToFetch = config.getMaxPagesToFetch();
    synchronized (mutex) {
      try {
        if (maxPagesToFetch < 0 || scheduledPages < maxPagesToFetch) {
          workQueues.put(url);
          scheduledPages++;
          counters.increment(Counters.ReservedCounterNames.SCHEDULED_PAGES);
        }
      } catch (DatabaseException e) {
        logger.error("Error while putting the url in the work queue.");
      }
    }
  }

  public void getNextURLs(int max, List<WebURL> result) {
    while (true) {
      synchronized (mutex) {
        if (isFinished) {
          return;
        }
        try {
          List<WebURL> curResults = workQueues.get(max);
          workQueues.delete(curResults.size());
          if (inProcessPages != null) {
            for (WebURL curPage : curResults) {
              inProcessPages.put(curPage);
            }
          }
          result.addAll(curResults);
        } catch (DatabaseException e) {
          logger.error("Error while getting next urls: {}", e.getMessage());
          e.printStackTrace();
        }
        if (result.size() > 0) {
          return;
        }
      }
      try {
        synchronized (waitingList) {
          waitingList.wait();
        }
      } catch (InterruptedException ignored) {
        // Do nothing
      }
      if (isFinished) {
        return;
      }
    }
  }

  public void setProcessed(WebURL webURL) {
    counters.increment(ReservedCounterNames.PROCESSED_PAGES);
    if (inProcessPages != null) {
      if (!inProcessPages.removeURL(webURL)) {
        logger.warn("Could not remove: {} from list of processed pages.", webURL.getURL());
      }
    }
  }

  public long getQueueLength() {
    return workQueues.getLength();
  }

  public long getNumberOfAssignedPages() {
    return inProcessPages.getLength();
  }

  public long getNumberOfProcessedPages() {
    return counters.getValue(ReservedCounterNames.PROCESSED_PAGES);
  }

  public void sync() {
    workQueues.sync();
    docIdServer.sync();
    counters.sync();
  }

  public boolean isFinished() {
    return isFinished;
  }

  public void close() {
    sync();
    workQueues.close();
    counters.close();
    if (inProcessPages != null) {
      inProcessPages.close();
    }
  }

  public void finish() {
    isFinished = true;
    synchronized (waitingList) {
      waitingList.notifyAll();
    }
  }
}