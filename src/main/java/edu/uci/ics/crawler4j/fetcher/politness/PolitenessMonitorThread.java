package edu.uci.ics.crawler4j.fetcher.politness;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolitenessMonitorThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(PolitenessMonitorThread.class);

    private final PolitenessServer politenessServer;
    private volatile boolean shutdown;

    public PolitenessMonitorThread(PolitenessServer politenessServer) {
        super("Politeness Monitor");
        this.politenessServer = politenessServer;
    }

    @Override
    public void run() {
        try {
            while (!shutdown) {
                synchronized (this) {
                    wait(politenessServer.getConfig().getPolitnessEntryExpiredWaitMultiplier() * politenessServer.getConfig().getPolitenessEntryExpiredDelay());

                    int expired = politenessServer.removeExpiredEntries();

                    logger.debug("Removed {} expired host entries.", expired);
                }
            }
        } catch (InterruptedException ignored) {
            // nothing to do here
        }
    }

    public void shutdown() {
        shutdown = true;
        synchronized (this) {
            notifyAll();
        }
    }
}