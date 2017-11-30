package edu.uci.ics.crawler4j.crawler.controller;

import java.util.List;

import org.slf4j.*;

import edu.uci.ics.crawler4j.CrawlerConfiguration;
import edu.uci.ics.crawler4j.crawler.*;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.frontier.pageharvests.PageHarvests;

public class CrawlControllerMonitor<T extends WebCrawler> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CrawlControllerMonitor.class);

    /**
     * Is the crawling of this session finished?
     */
    private boolean finished = false;

    /**
     * Is the crawling session set to 'shutdown'. Crawler threads monitor this flag and when it is
     * set they will no longer process new pages.
     */
    private boolean shuttingDown = false;

    private final CrawlerConfiguration configuration;

    private final CrawlController controller;

    private final List<Thread> threads;

    private final List<T> crawlers;

    private final WebCrawlerFactory<T> crawlerFactory;

    private final Frontier frontier;

    private final List<Object> crawlerData;

    private final PageFetcher pageFetcher;

    private final PageHarvests pageHarvests;

    private final Object waitingLock;

    public CrawlControllerMonitor(CrawlerConfiguration configuration, CrawlController controller,
            List<Thread> threads, List<T> crawlers, WebCrawlerFactory<T> crawlerFactory,
            Frontier frontier, List<Object> crawlerData, PageFetcher pageFetcher,
            PageHarvests pageHarvests, Object waitingLock) {
        super();
        this.configuration = configuration;
        this.controller = controller;
        this.threads = threads;
        this.crawlers = crawlers;
        this.crawlerFactory = crawlerFactory;
        this.frontier = frontier;
        this.crawlerData = crawlerData;
        this.pageFetcher = pageFetcher;
        this.pageHarvests = pageHarvests;
        this.waitingLock = waitingLock;
    }

    @Override
    public void run() {
        try {
            synchronized (waitingLock) {
                while (true) {
                    sleep(configuration.getThreadMonitoringDelaySeconds());
                    boolean working = false;
                    for (int i = 0; i < threads.size(); i++) {
                        Thread thread = threads.get(i);
                        if (!thread.isAlive()) {
                            if (!shuttingDown) {
                                crawlerFactory.replaceInstance(crawlers.get(i), thread, controller,
                                        threads, crawlers);
                            }
                        } else if (crawlers.get(i).isNotWaitingForNewURLs()) {
                            working = true;
                        }
                    }
                    if (!working && configuration.isShutdownOnEmptyQueue()) {
                        logger.info("It looks like no thread is working, "
                                + "waiting for {} seconds to make sure...", configuration
                                        .getThreadShutdownDelaySeconds());
                        sleep(configuration.getThreadShutdownDelaySeconds());

                        working = false;
                        for (int i = 0; i < threads.size(); i++) {
                            Thread thread = threads.get(i);
                            if (thread.isAlive() && crawlers.get(i).isNotWaitingForNewURLs()) {
                                working = true;
                            }
                        }
                        if (!working) {
                            if (!shuttingDown) {
                                if (0 < frontier.getQueueLength()) {
                                    continue;
                                }
                                logger.info("No thread is working and no more URLs are in queue "
                                        + "waiting for another {} seconds to make sure...",
                                        configuration.getThreadShutdownDelaySeconds());
                                sleep(configuration.getThreadShutdownDelaySeconds());
                                if (0 < frontier.getQueueLength()) {
                                    continue;
                                }
                            }

                            logger.info(
                                    "All of the crawlers are stopped. Finishing the process...");
                            frontier.finish();
                            for (T crawler : crawlers) {
                                crawler.onBeforeExit();
                                crawlerData.add(crawler.getMyLocalData());
                            }

                            logger.info("Waiting for {} seconds before final clean up...",
                                    configuration.getCleanupDelaySeconds());
                            sleep(configuration.getCleanupDelaySeconds());

                            frontier.close();
                            pageHarvests.close();
                            pageFetcher.shutDown();

                            finished = true;
                            waitingLock.notifyAll();
                            configuration.getCrawlPersistentConfiguration().close();

                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected Error", e);
        }
    }

    private static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ignored) {
            // empty
        }
    }

    public void shutDown() {
        shuttingDown = true;
    }

    public boolean isFinished() {
        return finished;
    }

}
