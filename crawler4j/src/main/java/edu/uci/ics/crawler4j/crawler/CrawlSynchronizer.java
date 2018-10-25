/*
 * Copyright 2018 Paul Galbraith <paul.d.galbraith@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.crawler;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class to help multiple crawler threads know when the entire
 * crawling process is finished.
 * <p>
 * Each crawler should register itself when it starts processing by calling
 * {@link #registerCrawler()}.
 * <p>
 * When a crawler finds new pages for processing, it should call
 * {@link #foundMorePages()} to indicate that there is still more work to do.
 * This will free up any crawlers currently waiting at
 * {@link #awaitCompletion()} to continue processing.
 * <p>
 * When an individual crawler finds itself in the position of not having any
 * more pages to process, it should call {@link #awaitCompletion()}, which will
 * block and eventually return either {@code true} (indicating that the whole
 * process is finished) or {@code false} (indicating that there is still more
 * work to do).
 * <p>
 * There must only be a single instance of this class per crawling process, to
 * be shared amongst all threads.
 *
 * @author Paul Galbraith <paul.d.galbraith@gmail.com>
 */
public final class CrawlSynchronizer {

    private static final Logger logger = LoggerFactory.getLogger(CrawlSynchronizer.class);

    private boolean finished = false;

    private final Set<Thread> workers = new HashSet<>();

    private CrawlConfig config;

    public CrawlSynchronizer(CrawlConfig config) {
        this.config = config;
    }

    /**
     * <p>
     * Wait for an undefined amount of time and then return for one of
     * these reasons:
     * <ol>
     * <li>all work is finished
     * <li>there is more work ready to be done
     * <li>no reason at all (i.e. spurious wakeup or wait timeout)
     * </ol>
     * <p>
     * Callers must test conditions after wakeup to see what the real situation is.
     *
     * @return {@code true} if all crawling is completed, or {@code false} if there
     *         is still more work to do
     * @throws InterruptedException
     */
    public synchronized boolean awaitCompletion() throws InterruptedException {
        assert !finished;

        Thread t = Thread.currentThread();
        boolean worker = workers.remove(t);

        if (worker) {
            logger.debug("worker thread [" + t + "] waiting for completion");
        } else {
            logger.debug("non-worker thread [" + t + "] waiting for completion");
        }

        if (workers.isEmpty()) {
            // no crawlers are working, so all crawling is finished
            logger.info("all crawling is finished");
            if (config.isShutdownOnEmptyQueue()) {
                finished = true;
                notifyAll();
            } else {
                logger.info("not stopping crawlers because CrawlConfig.shutdownOnEmptyQueue is configured false");
            }
        } else {
            wait(3000);
            if (worker) {
                workers.add(t);
            }
        }

        return finished;
    }

    /**
     * All crawler threads should call this method whenever they discover new pages
     * for processing. This helps to inform other threads that there is more work to
     * do. Be sure to call this only <em>after</em> new pages are safely committed
     * to the data store.
     */
    public synchronized void foundMorePages() {
        assert !finished;

        notifyAll();
    }

    /**
     * All crawler threads should call this method to register themselves, as soon
     * as they begin working.
     */
    public synchronized void registerCrawler() {
        assert !finished;

        Thread t = Thread.currentThread();
        logger.debug("registering worker thread [" + t + "]");
        workers.add(t);
    }

    public synchronized boolean isFinished() {
        return finished;
    }

}
