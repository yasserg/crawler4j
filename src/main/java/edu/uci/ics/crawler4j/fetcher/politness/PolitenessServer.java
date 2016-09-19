package edu.uci.ics.crawler4j.fetcher.politness;

import edu.uci.ics.crawler4j.crawler.Configurable;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zowallar on 19.09.2016.
 */
public class PolitenessServer extends Configurable {

    private static final Logger logger = LoggerFactory.getLogger(PolitenessServer.class);

    public static int NO_POLITENESS_APPLIED = -1;
    private PolitenessMonitorThread politenessMonitorThread;

    private Map<String, Date> seenHosts;

    public PolitenessServer(CrawlConfig config) {
        super(config);
        this.seenHosts = new ConcurrentHashMap<>();

        if (politenessMonitorThread == null) {
            politenessMonitorThread = new PolitenessMonitorThread(this);
        }
        politenessMonitorThread.start();
    }

    public long applyPoliteness(WebURL url) {

        long politenessDelay = NO_POLITENESS_APPLIED;

        String host = getHost(url);

        if (host != null) {
            if (seenHosts.containsKey(host)) {

                Date lastFetchTime = seenHosts.get(host);

                if (lastFetchTime != null) {
                    long now = (new Date()).getTime();
                    long diff = (now - lastFetchTime.getTime());

                    if (diff < config.getPolitenessDelay()) {
                        politenessDelay = config.getPolitenessDelay() - diff;

                        logger.debug("Applying politeness delay of {} ms for host {}", politenessDelay, host);
                    } else {
                        //nothing to do here
                    }
                }

            }
            seenHosts.put(host, new Date());
            return politenessDelay;
        } else {
            logger.warn("Could not determine host for: " + url.getURL());
            return politenessDelay;
        }

    }

    public int removeExpiredEntries() {

        int expired = 0;

        long now = (new Date()).getTime();

        for (Map.Entry<String, Date> host : seenHosts.entrySet()) {

            long diff = (now - host.getValue().getTime());

            if (diff < config.getPolitenessEntryExpiredDelay()) {
                //entry is not expired
            } else {
                seenHosts.remove(host.getKey());
                expired++;
            }

        }

        return expired;
    }

    public int getSize() {
        return seenHosts.size();
    }

    private String getHost(WebURL webURL) {
        String host = null;
        try {
            URL url = new URL(webURL.getURL());
            host = url.getHost().toLowerCase();

        } catch (MalformedURLException e) {
            logger.error("Could not determine host for: " + webURL.getURL(), e);
        }
        return host;
    }

    public synchronized void shutdown() {
        if (politenessMonitorThread != null) {
            politenessMonitorThread.shutdown();
        }
    }

}
