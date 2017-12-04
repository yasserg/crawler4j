/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.crawler.fetcher;

import java.util.concurrent.TimeUnit;

import org.apache.http.conn.HttpClientConnectionManager;

public class IdleHttpClientConnectionMonitor extends Thread {

    private final HttpClientConnectionManager httpClientConnectionManager;

    private volatile boolean shutdown;

    public IdleHttpClientConnectionMonitor(HttpClientConnectionManager connMgr) {
        super("Connection Manager");
        this.httpClientConnectionManager = connMgr;
    }

    @Override
    public void run() {
        try {
            while (!shutdown) {
                synchronized (this) {
                    wait(5000);
                    httpClientConnectionManager.closeExpiredConnections();
                    httpClientConnectionManager.closeIdleConnections(30, TimeUnit.SECONDS);
                }
            }
        } catch (InterruptedException ignored) {
            // terminate
        }
    }

    public void shutdown() {
        shutdown = true;
        synchronized (this) {
            notifyAll();
        }
    }
}