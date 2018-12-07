/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.uci.ics.crawler4j.deadlinksniffer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;

/**
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
public class DeadLinkCrawlConfig extends CrawlConfig {
    private List<Pattern> urlPatterns = new ArrayList<>();
    private volatile DeadLinkCrawlerStore crawlerStore;
    private List<Pattern> excludePatterns = new ArrayList<>();

    public List<Pattern> getUrlPatterns() {
        return urlPatterns;
    }

    public List<Pattern> getExcludePatterns() {
        return excludePatterns;
    }

    /**
     * Add a regular expression for URLs which should be followed
     * by the crawler.
     */
    public void addUrlPattern(String urlPattern) {
        this.urlPatterns.add(Pattern.compile(urlPattern));
    }

    /**
     * Add a regular expression for URLs which should be excluded from scanning.
     * This is effectively a stop-criterium and will get evaluated
     * after all the patterns added via {@link #addUrlPattern(String)}.
     */
    public void addExcludePattern(String excludePattern) {
        this.excludePatterns.add(Pattern.compile(excludePattern));
    }

    public DeadLinkCrawlerStore getCrawlerStore() {
        if (crawlerStore == null) {
            synchronized (this) {
                if (crawlerStore == null) {
                    crawlerStore = new DeadLinkCrawlerStore(this);
                }
            }
        }

        return crawlerStore;
    }
}
