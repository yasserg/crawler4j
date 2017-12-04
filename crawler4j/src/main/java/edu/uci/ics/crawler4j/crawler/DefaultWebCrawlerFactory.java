package edu.uci.ics.crawler4j.crawler;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.slf4j.*;

import edu.uci.ics.crawler4j.CrawlerConfiguration;
import edu.uci.ics.crawler4j.crawler.controller.CrawlController;
import edu.uci.ics.crawler4j.crawler.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.frontier.pageharvests.PageHarvests;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class DefaultWebCrawlerFactory<T extends WebCrawler> implements WebCrawlerFactory<T> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWebCrawlerFactory.class);

    private final Class<T> clazz;

    protected final CrawlerConfiguration crawlerConfiguration;

    protected final CrawlController crawlController;

    private int threadId;

    private final Object mutex = new Object();

    public DefaultWebCrawlerFactory(Class<T> clazz, CrawlerConfiguration crawlerConfiguration,
            CrawlController crawlController) {
        this.clazz = clazz;
        this.crawlerConfiguration = crawlerConfiguration;
        this.crawlController = crawlController;
    }

    @Override
    public T newInstance(Collection<Thread> threads, Collection<T> crawlers) throws Exception {
        synchronized (mutex) {
            int id = ++threadId;
            T crawler = newInstance(id);

            Thread thread = new Thread(crawler, "Crawler-" + id);
            crawlers.add(crawler);
            threads.add(thread);

            thread.start();
            logger.info("Crawler {} started", id);

            return crawler;
        }
    }

    protected T newInstance(Integer id) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        return clazz.getConstructor(Integer.class, CrawlerConfiguration.class,
                CrawlController.class, PageFetcher.class, RobotstxtServer.class, PageHarvests.class,
                Frontier.class, Parser.class).newInstance(id, crawlerConfiguration, crawlController,
                        crawlController.getPageFetcher(), crawlController.getRobotstxtServer(),
                        crawlController.getPageHarvests(), crawlController.getFrontier(),
                        new Parser(crawlerConfiguration));
    }

    @Override
    public T replaceInstance(T existingCrawler, Thread existingThread, Collection<Thread> threads,
            Collection<T> crawlers) throws Exception {
        synchronized (mutex) {
            logger.info("Thread {} was dead, I'll recreate it", existingCrawler.getId());
            crawlers.remove(existingCrawler);
            threads.remove(existingThread);

            return newInstance(threads, crawlers);
        }
    }
}
