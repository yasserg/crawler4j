/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.fetcher.politness;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import edu.uci.ics.crawler4j.crawler.Configurable;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class PolitenessServer extends Configurable {

    private static final Logger logger = LoggerFactory.getLogger(PolitenessServer.class);

    public static int NO_POLITENESS_APPLIED = -1;

    private Cache<String, Date> seenHosts;

    private Object mutex = new Object();

    public PolitenessServer(CrawlConfig config) {
        super(config);

        this.seenHosts = CacheBuilder.newBuilder().maximumSize(config.getPolitenessMaximumHostEntries())
                .expireAfterAccess(config.getPolitenessEntryExpiredDelay(), TimeUnit.MILLISECONDS).build();
    }

    public long applyPoliteness(WebURL url) {
        synchronized (mutex) {
            long politenessDelay = NO_POLITENESS_APPLIED;

            String host = getHost(url);

            if (host != null) {

                Date lastFetchTime = seenHosts.getIfPresent(host);

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
                seenHosts.put(host, new Date());
            }

            return politenessDelay;
        }
    }

    /**
     * This can be used to force cache clean up. Per default it performs small amounts of maintenance during write operations,
     * or during occasional read operations if writes are rare. See https://github.com/google/guava/wiki/CachesExplained
     */
    public void forceCleanUp() {
        this.seenHosts.cleanUp();
    }

    public long getSize() {
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

}
