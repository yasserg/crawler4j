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