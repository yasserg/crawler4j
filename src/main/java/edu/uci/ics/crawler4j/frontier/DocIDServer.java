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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */

public class DocIDServer extends Configurable {

  protected static final Logger logger = LoggerFactory.getLogger(DocIDServer.class);

  protected Database docIDsDB = null;

  protected final Object mutex = new Object();

  protected int lastDocID;

  public DocIDServer(Environment env, CrawlConfig config) throws DatabaseException {
    super(config);
    DatabaseConfig dbConfig = new DatabaseConfig();
    dbConfig.setAllowCreate(true);
    dbConfig.setTransactional(config.isResumableCrawling());
    dbConfig.setDeferredWrite(!config.isResumableCrawling());
    docIDsDB = env.openDatabase(null, "DocIDs", dbConfig);
    if (config.isResumableCrawling()) {
      int docCount = getDocCount();
      if (docCount > 0) {
        logger.info("Loaded {} URLs that had been detected in previous crawl.", docCount);
        lastDocID = docCount;
      }
    } else {
      lastDocID = 0;
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
      if (docIDsDB == null) {
        return -1;
      }
      OperationStatus result;
      DatabaseEntry value = new DatabaseEntry();
      try {
        DatabaseEntry key = new DatabaseEntry(url.getBytes());
        result = docIDsDB.get(null, key, value, null);

        if (result == OperationStatus.SUCCESS && value.getData().length > 0) {
          return Util.byteArray2Int(value.getData());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return -1;
    }
  }

  public int getNewDocID(String url) {
    synchronized (mutex) {
      try {
        // Make sure that we have not already assigned a docid for this URL
        int docid = getDocId(url);
        if (docid > 0) {
          return docid;
        }

        lastDocID++;
        docIDsDB.put(null, new DatabaseEntry(url.getBytes()), new DatabaseEntry(Util.int2ByteArray(lastDocID)));
        return lastDocID;
      } catch (Exception e) {
        e.printStackTrace();
      }
      return -1;
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

      docIDsDB.put(null, new DatabaseEntry(url.getBytes()), new DatabaseEntry(Util.int2ByteArray(docId)));
      lastDocID = docId;
    }
  }

  public boolean isSeenBefore(String url) {
    return getDocId(url) != -1;
  }

  public int getDocCount() {
    try {
      return (int) docIDsDB.count();
    } catch (DatabaseException e) {
      e.printStackTrace();
    }
    return -1;
  }

  public void sync() {
    if (config.isResumableCrawling()) {
      return;
    }
    if (docIDsDB == null) {
      return;
    }
    try {
      docIDsDB.sync();
    } catch (DatabaseException e) {
      e.printStackTrace();
    }
  }

  public void close() {
    try {
      docIDsDB.close();
    } catch (DatabaseException e) {
      e.printStackTrace();
    }
  }
}