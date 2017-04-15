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

package edu.uci.ics.crawler4j.frontier;

import edu.uci.ics.crawler4j.crawler.Configurable;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author Yasser Ganjisaffar
 */

public class Frontier extends Configurable {
    private static final Logger logger = LoggerFactory.getLogger(Frontier.class);
    private boolean isShutdown = false;
    private WorkQueues workQueues;

    public Frontier(CrawlConfig config) {
        super(config);
        workQueues = new WorkQueues();
    }

    public void scheduleAll(List<WebURL> urls) {

    }

    public void schedule(WebURL url) {
        scheduleAll(Collections.singletonList(url));
    }

    public void getNextURLs(int max, List<WebURL> result) {

    }

    public boolean isShutdown() {
        return isShutdown;
    }

    public void shutdown() {
        workQueues.shutdown();
        isShutdown = true;
    }
}
