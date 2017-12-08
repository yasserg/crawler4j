package edu.uci.ics.crawler4j;

import java.io.File;

import com.sleepycat.je.*;

import edu.uci.ics.crawler4j.frontier.pageharvests.*;
import edu.uci.ics.crawler4j.frontier.pagequeue.*;
import edu.uci.ics.crawler4j.frontier.pagestatistics.*;
import edu.uci.ics.crawler4j.util.IO;

public class SleepyCatCrawlPersistentConfiguration implements CrawlPersistentConfiguration {

    private final CrawlerConfiguration configuration;

    private Environment environment;

    private PageStatistics pageStatistics;

    private PageHarvests pageHarvests;

    private PageQueue pendingPageQueue;

    private PageQueue inprocessPageQueue;

    public SleepyCatCrawlPersistentConfiguration(CrawlerConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    public void initialize() throws Exception {
        environment = environment(configuration.getStorageFolder(), configuration
                .isResumableCrawling());

        if (configuration.isResumableCrawling()) {
            pageStatistics = new SleepyCatPageStatistics(environment);
        } else {
            pageStatistics = new InMemoryPageStatistics();
        }
        pageHarvests = new SleepyCatPageHarvests(environment, configuration.isResumableCrawling());
        pendingPageQueue = new SleepyCatPageQueue(environment, "PendingPages", configuration
                .isResumableCrawling());
        if (configuration.isResumableCrawling()) {
            inprocessPageQueue = new SleepyCatPageQueue(environment, "InprocessPages", configuration
                    .isResumableCrawling());
        } else {
            inprocessPageQueue = new DefaultPageQueue();
        }
    }

    private static Environment environment(String storageFolder, boolean resumableCrawling)
            throws Exception {
        EnvironmentConfig configuration = new EnvironmentConfig();
        configuration.setAllowCreate(true);
        configuration.setTransactional(resumableCrawling);
        configuration.setLocking(resumableCrawling);

        File storage = new File(new File(storageFolder), "frontier");
        if (!storage.exists()) {
            if (!storage.mkdir()) {
                throw new Exception("Failed creating the frontier folder: " + storage
                        .getAbsolutePath());
            }
        }
        if (!resumableCrawling) {
            IO.deleteFolderContents(storage);
        }
        return new Environment(storage, configuration);
    }

    @Override
    public void close() throws Exception {
        if (null != environment) {
            environment.close();
        }
    }

    @Override
    public PageStatistics getPageStatistics() {
        return pageStatistics;
    }

    @Override
    public PageHarvests getPageHarvests() {
        return pageHarvests;
    }

    @Override
    public PageQueue getPendingPageQueue() {
        return pendingPageQueue;
    }

    @Override
    public PageQueue getInprocessPageQueue() {
        return inprocessPageQueue;

    }

}
