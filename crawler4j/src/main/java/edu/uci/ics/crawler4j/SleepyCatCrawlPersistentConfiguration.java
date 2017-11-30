package edu.uci.ics.crawler4j;

import java.io.File;

import com.sleepycat.je.*;

import edu.uci.ics.crawler4j.frontier.pageharvests.*;
import edu.uci.ics.crawler4j.frontier.pagequeue.*;
import edu.uci.ics.crawler4j.frontier.pagestatistics.*;
import edu.uci.ics.crawler4j.util.IO;

public class SleepyCatCrawlPersistentConfiguration implements CrawlPersistentConfiguration {

    /**
     * The folder which will be used by crawler for storing the intermediate crawl data. The content
     * of this folder should not be modified manually.
     */
    private String storageFolder = "/frontier";

    /**
     * If this feature is enabled, you would be able to resume a previously stopped/crashed crawl.
     * However, it makes crawling slightly slower
     */
    private boolean resumableCrawling = false;

    private Environment environment;

    private PageStatistics pageStatistics;

    private PageHarvests pageHarvests;

    private PageQueue pendingPageQueue;

    private PageQueue inprocessPageQueue;

    @Override
    public void initialize() throws Exception {
        if (null == storageFolder) {
            throw new Exception("Crawl storage folder is not set in the CrawlerConfiguration.");
        }
        environment = environment(storageFolder, resumableCrawling);

        if (resumableCrawling) {
            pageStatistics = new SleepyCatPageStatistics(environment);
        } else {
            pageStatistics = new InMemoryPageStatistics();
        }
        pageHarvests = new SleepyCatPageHarvests(environment, resumableCrawling);
        pendingPageQueue = new SleepyCatPageQueue(environment, "PendingPages", resumableCrawling);
        if (resumableCrawling) {
            inprocessPageQueue = new SleepyCatPageQueue(environment, "InprocessPages",
                    resumableCrawling);
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
    public String getStorageFolder() {
        return this.storageFolder;
    }

    @Override
    public void setStorageFolder(String storageFolder) {
        this.storageFolder = storageFolder;
    }

    @Override
    public void setResumableCrawling(boolean resumableCrawling) {
        this.resumableCrawling = resumableCrawling;
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
