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

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.util.Util;

/**
 * @author Yasser Ganjisaffar
 */
public class WorkQueues {
  private final Database urlsDB;
  private final Environment env;

  private final boolean resumable;

  private final WebURLTupleBinding webURLBinding;

  protected final Object mutex = new Object();

  public WorkQueues(Environment env, String dbName, boolean resumable) {
    this.env = env;
    this.resumable = resumable;
    DatabaseConfig dbConfig = new DatabaseConfig();
    dbConfig.setAllowCreate(true);
    dbConfig.setTransactional(resumable);
    dbConfig.setDeferredWrite(!resumable);
    urlsDB = env.openDatabase(null, dbName, dbConfig);
    webURLBinding = new WebURLTupleBinding();
  }

  protected Transaction beginTransaction() {
    return resumable ? env.beginTransaction(null, null) : null;
  }

  protected static void commit(Transaction tnx) {
    if (tnx != null) {
      tnx.commit();
    }
  }

  protected Cursor openCursor(Transaction txn) {
    return urlsDB.openCursor(txn, null);
  }

  public List<WebURL> get(int max) {
    synchronized (mutex) {
      List<WebURL> results = new ArrayList<>(max);
      DatabaseEntry key = new DatabaseEntry();
      DatabaseEntry value = new DatabaseEntry();
      Transaction txn = beginTransaction();
      try (Cursor cursor = openCursor(txn)) {
        OperationStatus result = cursor.getFirst(key, value, null);
        int matches = 0;
        while ((matches < max) && (result == OperationStatus.SUCCESS)) {
          if (value.getData().length > 0) {
            results.add(webURLBinding.entryToObject(value));
            matches++;
          }
          result = cursor.getNext(key, value, null);
        }
      }
      commit(txn);
      return results;
    }
  }

  public void delete(int count) {
    synchronized (mutex) {
      DatabaseEntry key = new DatabaseEntry();
      DatabaseEntry value = new DatabaseEntry();
      Transaction txn = beginTransaction();
      try (Cursor cursor = openCursor(txn)) {
        OperationStatus result = cursor.getFirst(key, value, null);
        int matches = 0;
        while ((matches < count) && (result == OperationStatus.SUCCESS)) {
          cursor.delete();
          matches++;
          result = cursor.getNext(key, value, null);
        }
      }
      commit(txn);
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
  protected static DatabaseEntry getDatabaseEntryKey(WebURL url) {
    byte[] keyData = new byte[6];
    keyData[0] = url.getPriority();
    keyData[1] = ((url.getDepth() > Byte.MAX_VALUE) ? Byte.MAX_VALUE : (byte) url.getDepth());
    Util.putIntInByteArray(url.getDocid(), keyData, 2);
    return new DatabaseEntry(keyData);
  }

  public void put(WebURL url) {
    DatabaseEntry value = new DatabaseEntry();
    webURLBinding.objectToEntry(url, value);
    Transaction txn = beginTransaction();
    urlsDB.put(txn, getDatabaseEntryKey(url), value);
    commit(txn);
  }

  public long getLength() {
    return urlsDB.count();
  }

  public void close() {
    urlsDB.close();
  }
}