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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

import edu.uci.ics.crawler4j.url.WebURL;

/**
 * This class maintains the list of pages which are
 * assigned to crawlers but are not yet processed.
 * It is used for resuming a previous crawl.
 *
 * @author Yasser Ganjisaffar
 */
public class InProcessPagesDB extends WorkQueues {
  private static final Logger logger = LoggerFactory.getLogger(InProcessPagesDB.class);

  private static final String DATABASE_NAME = "InProcessPagesDB";

  public InProcessPagesDB(Environment env) {
    super(env, DATABASE_NAME, true);
    long docCount = getLength();
    if (docCount > 0) {
      logger.info("Loaded {} URLs that have been in process in the previous crawl.", docCount);
    }
  }

  public boolean removeURL(WebURL webUrl) {
    synchronized (mutex) {
      DatabaseEntry key = getDatabaseEntryKey(webUrl);
      DatabaseEntry value = new DatabaseEntry();
      Transaction txn = beginTransaction();
      try (Cursor cursor = openCursor(txn)) {
        OperationStatus result = cursor.getSearchKey(key, value, null);

        if (result == OperationStatus.SUCCESS) {
          result = cursor.delete();
          if (result == OperationStatus.SUCCESS) {
            return true;
          }
        }
      } finally {
        commit(txn);
      }
    }
    return false;
  }
}