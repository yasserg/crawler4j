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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.util.Util;
import redis.clients.jedis.Jedis;

/**
 * @author Yasser Ganjisaffar
 */
public class WorkQueues {
	public static final String KEY_PREFIX = "url:";
	public static final String ALL_URLS = "url:urls";
	private final Jedis urlsDB;

	private final WebURLTupleBinding webURLBinding;

	protected final Object mutex = new Object();

	public WorkQueues(int databaseIndex,CrawlConfig crawlConfig) {
		urlsDB = new Jedis(crawlConfig.getRedisHost(), crawlConfig.getRedisPort());
		urlsDB.select(databaseIndex);
		webURLBinding = new WebURLTupleBinding();
	}

	public List<WebURL> get(int max) {
		synchronized (mutex) {
			List<WebURL> results = new ArrayList<>(max);
			Set<String> zrange = urlsDB.zrange(ALL_URLS, 0, max);
			for (String s : zrange) {
				Map<String, String> hmap = urlsDB.hgetAll(s);
				WebURL webURL = webURLBinding.entryToObject(hmap);
				results.add(webURL);
			}
			return results;
		}
	}

	public void delete(int count) {
		synchronized (mutex) {
			if (count != 0) {
				Set<String> zrange = urlsDB.zrange(ALL_URLS, 0, count - 1);
				urlsDB.zrem(ALL_URLS, zrange.toArray(new String[zrange.size()]));
				for (String key : zrange) {
					urlsDB.del(key);
				}
			}
		}
	}

	/*
	 * The key that is used for storing URLs determines the order
	 * they are crawled. Lower key values results in earlier crawling.
	 * Here our keys are 6 bytes. The first byte comes from the URL priority.
	 * The second byte comes from depth of crawl at which this URL is first found.
	 * The rest of the 4 bytes come from the docid of the URL. As a result,
	 * URLs with lower priority numbers will be crawled earlier. If priority
	 * numbers are the same, those found at lower depths will be crawled earlier.
	 * If depth is also equal, those found earlier (therefore, smaller docid) will
	 * be crawled earlier.
	 */
	protected static String getDatabaseEntryKey(WebURL url) {
		byte depth = (url.getDepth() > Byte.MAX_VALUE) ? Byte.MAX_VALUE : (byte) url.getDepth();
		byte priority = url.getPriority();
		String key = KEY_PREFIX + priority + ":" + depth + ":" + url.getDocid();
		return key;
	}

	protected static double getScore(WebURL url) {
		byte[] keyData = new byte[8];
		keyData[0] = url.getPriority();
		keyData[1] = ((url.getDepth() > Byte.MAX_VALUE) ? Byte.MAX_VALUE : (byte) url.getDepth());
		Util.putIntInByteArray(url.getDocid(), keyData, 2);
		return ByteBuffer.wrap(keyData).getDouble();
	}

	public void put(WebURL url) {
		Map<String, String> value = new HashMap<>();
		webURLBinding.objectToEntry(url, value);
		String key = getDatabaseEntryKey(url);
		urlsDB.hmset(key, value);
		urlsDB.zadd(ALL_URLS, getScore(url), key);
	}

	public Jedis getUrlsDB() {
		return urlsDB;
	}

	public long getLength() {
		return urlsDB.zcard(ALL_URLS);
	}

	public void close() {
		urlsDB.close();
	}
}