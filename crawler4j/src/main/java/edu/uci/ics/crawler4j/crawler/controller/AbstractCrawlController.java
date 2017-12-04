package edu.uci.ics.crawler4j.crawler.controller;

import java.util.*;

import org.slf4j.*;

import edu.uci.ics.crawler4j.CrawlerConfiguration;
import edu.uci.ics.crawler4j.crawler.*;
import edu.uci.ics.crawler4j.crawler.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.frontier.pageharvests.PageHarvests;
import edu.uci.ics.crawler4j.robotstxt.*;
import edu.uci.ics.crawler4j.url.*;

public abstract class AbstractCrawlController<T extends WebCrawler> implements CrawlController {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCrawlController.class);

    private final CrawlerConfiguration configuration;

    /**
     * Once the crawling session finishes the controller collects the local data of the crawler
     * threads and stores them in this List.
     */
    private final List<Object> crawlerData;

    protected final PageFetcher pageFetcher;

    protected final RobotstxtServer robotstxtServer;

    protected final Frontier frontier;

    protected final PageHarvests pageHarvests;

    private CrawlControllerMonitor monitor;

    private boolean shuttingDown = false;

    private final Object waitingLock = new Object();

    public AbstractCrawlController(CrawlerConfiguration configuration) throws Exception {
        super();
        this.configuration = configuration;
        TLDList.setUseOnline(configuration.isOnlineTldListUpdate());
        configuration.initialize();
        this.pageHarvests = configuration.getCrawlPersistentConfiguration().getPageHarvests();
        this.frontier = new Frontier(configuration);
        this.pageFetcher = new PageFetcher(configuration);
        this.robotstxtServer = robotstxtServer();
        this.crawlerData = new ArrayList<>();
    }

    protected RobotstxtServer robotstxtServer() {
        return new RobotstxtServer(new RobotstxtConfig(), pageFetcher);
    }

    @Override
    public void addSeed(String pageUrl) {
        addSeed(pageUrl, -1);
    }

    @Override
    public void addSeed(String pageUrl, int docId) {
        String canonicalUrl = URLCanonicalizer.getCanonicalURL(pageUrl);
        if (null == canonicalUrl) {
            logger.error("Invalid seed URL: {}", pageUrl);
        } else {
            if (docId < 0) {
                if (pageHarvests.containsKey(canonicalUrl)) {
                    logger.trace("This URL is already seen.");
                    return;
                }
                pageHarvests.add(canonicalUrl);
            } else {
                try {
                    pageHarvests.put(canonicalUrl, docId);
                } catch (Exception e) {
                    logger.error("Could not add seed: {}", e.getMessage());
                }
            }

            WebURL webUrl = new WebURL();
            webUrl.setURL(canonicalUrl);
            webUrl.setId(pageHarvests.get(canonicalUrl));
            webUrl.setDepth((short) 0);
            if (robotstxtServer.allows(webUrl)) {
                frontier.schedule(webUrl);
            } else {
                logger.warn("Robots.txt does not allow this seed: {}", pageUrl);
            }
        }
    }

    @Override
    public void addSeen(String pageUrl, int docId) {
        String canonicalUrl = URLCanonicalizer.getCanonicalURL(pageUrl);
        if (null == canonicalUrl) {
            logger.error("Invalid Url: {} (can't cannonicalize it!)", pageUrl);
        } else {
            try {
                pageHarvests.put(canonicalUrl, docId);
            } catch (Exception e) {
                logger.error("Could not add seen url: {}", e.getMessage());
            }
        }
    }

    @Override
    public void start() {
        this.start(true);
    }

    private void start(boolean isBlocking) {
        try {
            crawlerData.clear();
            List<Thread> threads = new ArrayList<>();
            List<T> crawlers = new ArrayList<>();

            for (int i = 1; i <= configuration.getNumberOfCrawlers(); i++) {
                crawlerFactory().newInstance(threads, crawlers);
            }

            monitor = new CrawlControllerMonitor<>(configuration, threads, crawlers,
                    crawlerFactory(), frontier, crawlerData, pageFetcher, pageHarvests,
                    waitingLock);
            Thread monitorThread = new Thread(monitor);
            monitorThread.start();

            if (isBlocking) {
                waitUntilFinish();
            }

        } catch (Exception e) {
            logger.error("Error happened", e);
        }
    }

    protected abstract WebCrawlerFactory<T> crawlerFactory();

    @Override
    public void startNonBlocking() {
        start(false);
    }

    @Override
    public void waitUntilFinish() {
        while (!monitor.isFinished()) {
            synchronized (waitingLock) {
                if (monitor.isFinished()) {
                    return;
                }
                try {
                    waitingLock.wait();
                } catch (InterruptedException e) {
                    logger.error("Error occurred", e);
                }
            }
        }
    }

    @Override
    public Collection<Object> getCrawlerData() {
        return crawlerData;
    }

    @Override
    public void shutdown() {
        logger.info("Shutting down...");
        shuttingDown = true;
        monitor.shutDown();
        pageFetcher.shutDown();
        frontier.finish();
    }

    @Override
    public boolean isShuttingDown() {
        return shuttingDown;
    }

    @Override
    public PageFetcher getPageFetcher() {
        return this.pageFetcher;
    }

    @Override
    public RobotstxtServer getRobotstxtServer() {
        return this.robotstxtServer;
    }

    @Override
    public PageHarvests getPageHarvests() {
        return this.pageHarvests;
    }

    @Override
    public Frontier getFrontier() {
        return this.frontier;
    }

}
