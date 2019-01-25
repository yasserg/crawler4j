/*
 * Copyright 2018 Paul Galbraith <paul.d.galbraith@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.frontier;

import java.util.List;

import edu.uci.ics.crawler4j.url.WebURL;

/**
 * @author Paul Galbraith <paul.d.galbraith@gmail.com>
 *
 */
public interface Frontier {

    /**
     * @param url
     * @param docId
     */
    void addUrlAndDocId(String url, int docId);

    void close();

    /**
     * Returns the docid of an already seen url.
     *
     * @param url the URL for which the docid is returned.
     * @return the docid of the url if it is seen before. Otherwise -1 is returned.
     */
    int getDocId(String url);

    /**
     * @param url
     * @return
     */
    int getNewDocID(String url);

    /**
     * @param max
     * @param result
     * @throws InterruptedException
     */
    void getNextURLs(int max, List<WebURL> result);

    /**
     * @param url
     * @return
     */
    boolean isSeenBefore(String url);

    /**
     * Clear all stored crawl tracking data in preparation for a new crawl.
     */
    void reset();

    /**
     * @param url
     */
    void schedule(WebURL url);

    /**
     * @param urls
     */
    void scheduleAll(List<WebURL> urls);

    /**
     * @param url
     */
    void setProcessed(WebURL url);

}
