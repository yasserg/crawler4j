package edu.uci.ics.crawler4j.tests;

import org.junit.rules.TemporaryFolder;

import edu.uci.ics.crawler4j.*;

public class TestCrawlerConfiguration extends CrawlerConfiguration {

    public TestCrawlerConfiguration(TemporaryFolder temp) {
        super();
        setCrawlPersistentConfiguration(new SleepyCatCrawlPersistentConfiguration(this));
        setStorageFolder(temp.getRoot().getAbsolutePath());

        setNumberOfCrawlers(1);
        setPolitenessDelay(100);
        setMaxConnectionsPerHost(1);
        setThreadShutdownDelaySeconds(1);
        setThreadMonitoringDelaySeconds(1);
        setCleanupDelaySeconds(1);

    }

}
