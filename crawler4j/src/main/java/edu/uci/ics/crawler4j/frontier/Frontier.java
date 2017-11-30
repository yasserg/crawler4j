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

import java.util.*;

import org.slf4j.*;

import com.sleepycat.je.DatabaseException;

import edu.uci.ics.crawler4j.CrawlerConfiguration;
import edu.uci.ics.crawler4j.frontier.pagequeue.PageQueue;
import edu.uci.ics.crawler4j.frontier.pagestatistics.*;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * @author Yasser Ganjisaffar
 */

public class Frontier {

    private static final Logger logger = LoggerFactory.getLogger(Frontier.class);

    private static final int IN_PROCESS_RESCHEDULE_BATCH_SIZE = 100;

    private final CrawlerConfiguration config;

    private final PageStatistics pageStatistics;

    private final PageQueue pendingPageQueue;

    private final PageQueue inprocessPageQueue;

    private final Object mutex = new Object();

    private final Object waitingList = new Object();

    private long scheduledPages;

    private boolean isFinished = false;

    public Frontier(CrawlerConfiguration config) {
        super();
        this.config = config;
        this.pageStatistics = config.getCrawlPersistentConfiguration().getPageStatistics();
        this.pendingPageQueue = config.getCrawlPersistentConfiguration().getPendingPageQueue();
        this.inprocessPageQueue = config.getCrawlPersistentConfiguration().getInprocessPageQueue();

        this.scheduledPages = pageStatistics.get(PageStatisticsType.SCHEDULED_PAGES);

        if (0 < inprocessPageQueue.size()) {
            logger.info("Rescheduling {} URLs from previous crawl.", inprocessPageQueue.size());
            scheduledPages -= inprocessPageQueue.size();

            Collection<WebURL> urls = inprocessPageQueue.nextRecords(
                    IN_PROCESS_RESCHEDULE_BATCH_SIZE);
            while (!urls.isEmpty()) {
                scheduleAll(urls);
                inprocessPageQueue.deleteNextRecords(urls.size());
                urls = inprocessPageQueue.nextRecords(IN_PROCESS_RESCHEDULE_BATCH_SIZE);
            }
        }
    }

    public void scheduleAll(Collection<WebURL> urls) {
        int maxPagesToFetch = config.getMaxPagesToFetch();
        synchronized (mutex) {
            int newScheduledPage = 0;
            for (WebURL url : urls) {
                if (0 < maxPagesToFetch && maxPagesToFetch <= (scheduledPages + newScheduledPage)) {
                    break;
                }
                try {
                    pendingPageQueue.put(url);
                    newScheduledPage++;
                } catch (DatabaseException e) {
                    logger.error("Error while putting the url in the work queue", e);
                }
            }
            if (0 < newScheduledPage) {
                scheduledPages += newScheduledPage;
                pageStatistics.increment(PageStatisticsType.SCHEDULED_PAGES, newScheduledPage);
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
                    pendingPageQueue.put(url);
                    scheduledPages++;
                    pageStatistics.increment(PageStatisticsType.SCHEDULED_PAGES);
                }
            } catch (DatabaseException e) {
                logger.error("Error while putting the url in the work queue", e);
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
                    Collection<WebURL> results = pendingPageQueue.nextRecords(max);
                    pendingPageQueue.deleteNextRecords(results.size());
                    if (null != inprocessPageQueue) {
                        for (WebURL r : results) {
                            inprocessPageQueue.put(r);
                        }
                    }
                    result.addAll(results);
                } catch (DatabaseException e) {
                    logger.error("Error while getting next urls", e);
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
        pageStatistics.increment(PageStatisticsType.PROCESSED_PAGES);
        if (null != inprocessPageQueue) {
            if (!inprocessPageQueue.deleteRecord(webURL)) {
                logger.warn("Could not remove: {} from list of in process pages.", webURL.getURL());
            }
        }
    }

    public long getQueueLength() {
        return pendingPageQueue.size();
    }

    public long getNumberOfAssignedPages() {
        return inprocessPageQueue.size();
    }

    public long getNumberOfProcessedPages() {
        return pageStatistics.get(PageStatisticsType.PROCESSED_PAGES);
    }

    public long getNumberOfScheduledPages() {
        return pageStatistics.get(PageStatisticsType.SCHEDULED_PAGES);
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void close() {
        pageStatistics.close();
        pendingPageQueue.close();
        if (null != inprocessPageQueue) {
            inprocessPageQueue.close();
        }
    }

    public void finish() {
        isFinished = true;
        synchronized (waitingList) {
            waitingList.notifyAll();
        }
    }
}
