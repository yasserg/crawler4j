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

package edu.uci.ics.crawler4j.frontier;


import edu.uci.ics.crawler4j.crawler.Configurable;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * @author Yasser Ganjisaffar
 */

public class DocIDServer extends Configurable {
	private static final Logger logger = LoggerFactory.getLogger(DocIDServer.class);

	private final Jedis docIDsDB;
	public static final int DATABASE_INDEX = 1;
	public static final String KEY_PREFIX = "url:";

	private final Object mutex = new Object();

	private int lastDocID;

	public DocIDServer(CrawlConfig config) {
		super(config);
		lastDocID = 0;
		docIDsDB = new Jedis(config.getRedisHost(), config.getRedisPort());
		docIDsDB.select(DATABASE_INDEX);
		if (config.isResumableCrawling()) {
			int docCount = getDocCount();
			if (docCount > 0) {
				logger.info("Loaded {} URLs that had been detected in previous crawl.", docCount);
				lastDocID = docCount;
			}
		}
	}

	/**
	 * Returns the docid of an already seen url.
	 *
	 * @param url the URL for which the docid is returned.
	 * @return the docid of the url if it is seen before. Otherwise -1 is returned.
	 */
	public int getDocId(String url) {
		synchronized (mutex) {
			String result = docIDsDB.get(getKey(url));
			return result == null ? -1 : Integer.valueOf(result);
		}
	}

	public int getNewDocID(String url) {
		synchronized (mutex) {
			try {
				// Make sure that we have not already assigned a docid for this URL
				int docID = getDocId(url);
				if (docID > 0) {
					return docID;
				}

				++lastDocID;
				docIDsDB.set(getKey(url), String.valueOf(lastDocID));
				return lastDocID;
			}
			catch (Exception e) {
				logger.error("Exception thrown while getting new DocID", e);
				return -1;
			}
		}
	}

	public void addUrlAndDocId(String url, int docId) throws Exception {
		synchronized (mutex) {
			if (docId <= lastDocID) {
				throw new Exception("Requested doc id: " + docId + " is not larger than: " + lastDocID);
			}

			// Make sure that we have not already assigned a docid for this URL
			int prevDocid = getDocId(url);
			if (prevDocid > 0) {
				if (prevDocid == docId) {
					return;
				}
				throw new Exception("Doc id: " + prevDocid + " is already assigned to URL: " + url);
			}

			docIDsDB.set(getKey(url), String.valueOf(docId));
			lastDocID = docId;
		}
	}

	private String getKey(String url) {
		return KEY_PREFIX + url;
	}

	public boolean isSeenBefore(String url) {
		return getDocId(url) != -1;
	}

	public final int getDocCount() {
		return docIDsDB.keys(KEY_PREFIX + "*").size();
	}

	public void close() {
		docIDsDB.close();
	}
}