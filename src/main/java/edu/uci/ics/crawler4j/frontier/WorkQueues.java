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

    protected final Object mutex = new Object();

    public WorkQueues(Environment env, String dbName) {
        this.env = env;
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        urlsDB = env.openDatabase(null, dbName, dbConfig);
    }

    protected Transaction beginTransaction() {
        return null;
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
        return null;
    }

    public void delete(int count) {

    }

    public void put(WebURL url) {
    }

    public void close() {
        urlsDB.close();
    }
}