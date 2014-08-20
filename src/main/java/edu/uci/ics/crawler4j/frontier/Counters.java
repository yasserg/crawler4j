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

import com.sleepycat.je.*;
import edu.uci.ics.crawler4j.crawler.Configurable;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */

public class Counters extends Configurable {

  public class ReservedCounterNames {
    public final static String SCHEDULED_PAGES = "Scheduled-Pages";
    public final static String PROCESSED_PAGES = "Processed-Pages";
  }

  protected Database statisticsDB = null;
  protected Environment env;

  protected final Object mutex = new Object();

  protected Map<String, Long> counterValues;

  public Counters(Environment env, CrawlConfig config) throws DatabaseException {
    super(config);

    this.env = env;
    this.counterValues = new HashMap<>();

    /*
     * When crawling is set to be resumable, we have to keep the statistics
     * in a transactional database to make sure they are not lost if crawler
     * is crashed or terminated unexpectedly.
     */
    if (config.isResumableCrawling()) {
      DatabaseConfig dbConfig = new DatabaseConfig();
      dbConfig.setAllowCreate(true);
      dbConfig.setTransactional(true);
      dbConfig.setDeferredWrite(false);
      statisticsDB = env.openDatabase(null, "Statistics", dbConfig);

      OperationStatus result;
      DatabaseEntry key = new DatabaseEntry();
      DatabaseEntry value = new DatabaseEntry();
      Transaction tnx = env.beginTransaction(null, null);
      Cursor cursor = statisticsDB.openCursor(tnx, null);
      result = cursor.getFirst(key, value, null);

      while (result == OperationStatus.SUCCESS) {
        if (value.getData().length > 0) {
          String name = new String(key.getData());
          long counterValue = Util.byteArray2Long(value.getData());
          counterValues.put(name, new Long(counterValue));
        }
        result = cursor.getNext(key, value, null);
      }
      cursor.close();
      tnx.commit();
    }
  }

  public long getValue(String name) {
    synchronized (mutex) {
      Long value = counterValues.get(name);
      if (value == null) {
        return 0;
      }
      return value.longValue();
    }
  }

  public void setValue(String name, long value) {
    synchronized (mutex) {
      try {
        counterValues.put(name, new Long(value));
        if (statisticsDB != null) {
          Transaction txn = env.beginTransaction(null, null);
          statisticsDB.put(txn, new DatabaseEntry(name.getBytes()),
              new DatabaseEntry(Util.long2ByteArray(value)));
          txn.commit();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void increment(String name) {
    increment(name, 1);
  }

  public void increment(String name, long addition) {
    synchronized (mutex) {
      long prevValue = getValue(name);
      setValue(name, prevValue + addition);
    }
  }

  public void sync() {
    if (config.isResumableCrawling()) {
      return;
    }
    if (statisticsDB == null) {
      return;
    }
    try {
      statisticsDB.sync();
    } catch (DatabaseException e) {
      e.printStackTrace();
    }
  }

  public void close() {
    try {
      if (statisticsDB != null) {
        statisticsDB.close();
      }
    } catch (DatabaseException e) {
      e.printStackTrace();
    }
  }
}