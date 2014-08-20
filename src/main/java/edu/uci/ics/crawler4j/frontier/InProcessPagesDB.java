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


import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class maintains the list of pages which are
 * assigned to crawlers but are not yet processed.
 * It is used for resuming a previous crawl. 
 * 
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class InProcessPagesDB extends WorkQueues {

  private static final Logger logger = LoggerFactory.getLogger(InProcessPagesDB.class);

  public InProcessPagesDB(Environment env) throws DatabaseException {
    super(env, "InProcessPagesDB", true);
    long docCount = getLength();
    if (docCount > 0) {
      logger.info("Loaded {} URLs that have been in process in the previous crawl.", docCount);
    }
  }

  public boolean removeURL(WebURL webUrl) {
    synchronized (mutex) {
      try {
        DatabaseEntry key = getDatabaseEntryKey(webUrl);
        Cursor cursor = null;
        OperationStatus result;
        DatabaseEntry value = new DatabaseEntry();
        Transaction txn = env.beginTransaction(null, null);
        try {
          cursor = urlsDB.openCursor(txn, null);
          result = cursor.getSearchKey(key, value, null);

          if (result == OperationStatus.SUCCESS) {
            result = cursor.delete();
            if (result == OperationStatus.SUCCESS) {
              return true;
            }
          }
        } catch (DatabaseException e) {
          if (txn != null) {
            txn.abort();
            txn = null;
          }
          throw e;
        } finally {
          if (cursor != null) {
            cursor.close();
          }
          if (txn != null) {
            txn.commit();
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return false;
  }
}