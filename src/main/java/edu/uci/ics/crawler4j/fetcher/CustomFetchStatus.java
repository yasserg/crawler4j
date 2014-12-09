/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"; you may not use this file except in compliance with
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

package edu.uci.ics.crawler4j.fetcher;

import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * @author Yasser Ganjisaffar [lastname at gmail dot com]
 */
public class CustomFetchStatus {

  public static final int SC_PERMANENT_REDIRECT = 308; // todo follow https://issues.apache.org/jira/browse/HTTPCORE-389
  public static final int PageTooBig = 1001;
  public static final int FatalTransportError = 1005;
  public static final int UnknownHostError = 1007;
  public static final int SocketTimeoutError = 1008;
  public static final int UnknownError = 1006;

  private static Logger logger = LoggerFactory.getLogger(CustomFetchStatus.class);

  public static String getStatusDescription(int code) {
    String reason = null;

    try {
      reason = EnglishReasonPhraseCatalog.INSTANCE.getReason(code, Locale.ENGLISH); // Finds the status reason for all known statuses
    } catch (IllegalArgumentException iae) {
      logger.debug("Custom Status Code: {} is being used", code);
    }

    if (reason == null) { // Finding status reason for our custom status codes
      switch (code) {
        case SC_PERMANENT_REDIRECT:
          reason = "Permanent redirect";
          break;
        case PageTooBig:
          reason = "Page size was too big";
          break;
        case UnknownHostError:
          reason = "Transport error - Unknown Host";
          break;
        case SocketTimeoutError:
          reason = "Transport error - Socket Timeout";
          break;
        case FatalTransportError:
          reason = "Fatal transport error - Is the server down ?";
          break;
        case UnknownError:
          reason = "Unknown error";
          break;
        default:
          reason = "( " + code + " )";
      }
    }

    return reason;
  }
}