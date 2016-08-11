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
public class Counters extends Configurable {
	private static final Logger logger = LoggerFactory.getLogger(Counters.class);

	public static class ReservedCounterNames {
		public static final String SCHEDULED_PAGES = "Scheduled-Pages";
		public static final String PROCESSED_PAGES = "Processed-Pages";
	}

	public static final int DATABASE_INDEX = 0;
	protected Jedis statisticsDB = null;


	protected final Object mutex = new Object();

	public Counters(CrawlConfig config) {
		super(config);
		statisticsDB = new Jedis(config.getRedisHost(), config.getRedisPort());
		statisticsDB.select(DATABASE_INDEX);
	}

	public long getValue(String name) {
		String value = statisticsDB.get(name);
		return value == null ? 0 : Long.parseLong(value);
	}

	public void setValue(String name, Long value) {
		statisticsDB.set(name, value.toString());
	}

	public void increment(String name) {
		statisticsDB.incr(name);
	}

	public void increment(String name, long addition) {
		statisticsDB.incrBy(name, addition);
	}

	public void close() {
		if (statisticsDB != null) {
			statisticsDB.close();
		}
	}
}