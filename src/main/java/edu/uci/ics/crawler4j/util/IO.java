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

package edu.uci.ics.crawler4j.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class IO {

  public static boolean deleteFolder(File folder) {
    return deleteFolderContents(folder) && folder.delete();
  }

  public static boolean deleteFolderContents(File folder) {
    System.out.println("Deleting content of: " + folder.getAbsolutePath());
    File[] files = folder.listFiles();
    for (File file : files) {
      if (file.isFile()) {
        if (!file.delete()) {
          return false;
        }
      } else {
        if (!deleteFolder(file)) {
          return false;
        }
      }
    }
    return true;
  }

  public static void writeBytesToFile(byte[] bytes, String destination) {
    try {
      FileChannel fc = new FileOutputStream(destination).getChannel();
      fc.write(ByteBuffer.wrap(bytes));
      fc.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}