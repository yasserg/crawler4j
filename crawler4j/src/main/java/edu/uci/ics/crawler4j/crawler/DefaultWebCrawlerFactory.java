package edu.uci.ics.crawler4j.crawler;

import java.util.Collection;

import org.slf4j.*;

import edu.uci.ics.crawler4j.CrawlerConfiguration;
import edu.uci.ics.crawler4j.crawler.controller.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.frontier.pageharvests.PageHarvests;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class DefaultWebCrawlerFactory<T extends WebCrawler> implements WebCrawlerFactory<T> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWebCrawlerFactory.class);

    private final Class<T> clazz;

    private final CrawlerConfiguration crawlerConfiguration;

    private final PageFetcher pageFetcher;

    private final RobotstxtServer robotstxtServer;

    private final PageHarvests pageHarvests;

    private final Frontier frontier;

    private int threadId;

    private final Object mutex = new Object();

    public DefaultWebCrawlerFactory(Class<T> clazz, CrawlerConfiguration crawlerConfiguration,
            PageFetcher pageFetcher, RobotstxtServer robotstxtServer, PageHarvests pageHarvests,
            Frontier frontier) {
        this.clazz = clazz;
        this.crawlerConfiguration = crawlerConfiguration;
        this.pageFetcher = pageFetcher;
        this.robotstxtServer = robotstxtServer;
        this.pageHarvests = pageHarvests;
        this.frontier = frontier;
    }

    @Override
    public T newInstance(CrawlController crawlController, Collection<Thread> threads,
            Collection<T> crawlers) throws Exception {
        synchronized (mutex) {
            int id = ++threadId;
            T crawler = newInstance();
            crawler.setId(id);
            crawler.setConfiguration(crawlerConfiguration);
            crawler.setController(crawlController);
            crawler.setParser(new Parser(crawlerConfiguration));

            crawler.setPageFetcher(pageFetcher);
            crawler.setRobotstxtServer(robotstxtServer);
            crawler.setPageHarvests(pageHarvests);
            crawler.setFrontier(frontier);

            Thread thread = new Thread(crawler, "Crawler-" + id);
            crawler.setThread(thread);
            crawlers.add(crawler);
            threads.add(thread);

            thread.start();
            logger.info("Crawler {} started", id);

            return crawler;
        }
    }

    protected T newInstance() throws InstantiationException, IllegalAccessException {
        return clazz.newInstance();
    }

    @Override
    public T replaceInstance(T existingCrawler, Thread existingThread,
            CrawlController crawlController, Collection<Thread> threads, Collection<T> crawlers)
            throws Exception {
        synchronized (mutex) {
            logger.info("Thread {} was dead, I'll recreate it", existingCrawler.getId());
            crawlers.remove(existingCrawler);
            threads.remove(existingThread);

            return newInstance(crawlController, threads, crawlers);
        }
    }
}
