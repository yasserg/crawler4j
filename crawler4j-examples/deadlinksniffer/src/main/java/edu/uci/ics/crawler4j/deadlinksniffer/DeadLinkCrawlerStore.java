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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores information about erroneous pages to the disk.
 *
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
public class DeadLinkCrawlerStore {
    private static final Logger logger = LoggerFactory.getLogger(DeadLinkCrawlerStore.class);

    private final DeadLinkCrawlConfig config;

    private FileWriter brokenPages;


    protected DeadLinkCrawlerStore(DeadLinkCrawlConfig config) {
        this.config = config;
        try {
            brokenPages = new FileWriter(new File(config.getCrawlStorageFolder(), "brokenPages.csv"));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }




    public synchronized void close() {
        closeFile(brokenPages, "errorPages");
        brokenPages = null;
    }

    private void closeFile(FileWriter fw, String name) {
        if (fw == null) {
            return;
        }

        try {
            fw.close();
        }
        catch (IOException e) {
            logger.error("problem with closing" + name, e);
        }
    }

    public synchronized void storePageStatus(int statusCode, WebURL webUrlFail) {
        try {
            brokenPages.append("" + statusCode + ", " + webUrlFail.getURL() + ", " + webUrlFail.getParentUrl() + "\n");
            brokenPages.flush();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
