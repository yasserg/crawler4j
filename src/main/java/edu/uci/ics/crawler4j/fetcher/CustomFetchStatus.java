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

import org.apache.http.HttpStatus;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class CustomFetchStatus {

  public static final int SC_PERMANENT_REDIRECT = 308; // todo follow https://issues.apache.org/jira/browse/HTTPCORE-389
  public static final int PageTooBig = 1001;
  public static final int FatalTransportError = 1005;
  public static final int UnknownError = 1006;

  public static String getStatusDescription(int code) {
    switch (code) {
    case HttpStatus.SC_OK:
      return "OK";
    case HttpStatus.SC_CREATED:
      return "Created";
    case HttpStatus.SC_ACCEPTED:
      return "Accepted";
    case HttpStatus.SC_NO_CONTENT:
      return "No Content";
    case HttpStatus.SC_MOVED_PERMANENTLY:
      return "Moved Permanently";
    case HttpStatus.SC_MOVED_TEMPORARILY:
      return "Moved Temporarily";
    case HttpStatus.SC_NOT_MODIFIED:
      return "Not Modified";
    case HttpStatus.SC_BAD_REQUEST:
      return "Bad Request";
    case HttpStatus.SC_UNAUTHORIZED:
      return "Unauthorized";
    case HttpStatus.SC_FORBIDDEN:
      return "Forbidden";
    case HttpStatus.SC_NOT_FOUND:
      return "Not Found";
    case HttpStatus.SC_INTERNAL_SERVER_ERROR:
      return "Internal Server Error";
    case HttpStatus.SC_NOT_IMPLEMENTED:
      return "Not Implemented";
    case HttpStatus.SC_BAD_GATEWAY:
      return "Bad Gateway";
    case HttpStatus.SC_SERVICE_UNAVAILABLE:
      return "Service Unavailable";
    case HttpStatus.SC_CONTINUE:
      return "Continue";
    case HttpStatus.SC_TEMPORARY_REDIRECT:
      return "Temporary Redirect";
    case HttpStatus.SC_METHOD_NOT_ALLOWED:
      return "Method Not Allowed";
    case HttpStatus.SC_CONFLICT:
      return "Conflict";
    case HttpStatus.SC_PRECONDITION_FAILED:
      return "Precondition Failed";
    case HttpStatus.SC_REQUEST_TOO_LONG:
      return "Request Too Long";
    case HttpStatus.SC_REQUEST_URI_TOO_LONG:
      return "Request-URI Too Long";
    case HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE:
      return "Unsupported Media Type";
    case HttpStatus.SC_MULTIPLE_CHOICES:
      return "Multiple Choices";
    case HttpStatus.SC_SEE_OTHER:
      return "See Other";
    case HttpStatus.SC_USE_PROXY:
      return "Use Proxy";
    case HttpStatus.SC_PAYMENT_REQUIRED:
      return "Payment Required";
    case HttpStatus.SC_NOT_ACCEPTABLE:
      return "Not Acceptable";
    case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
      return "Proxy Authentication Required";
    case HttpStatus.SC_REQUEST_TIMEOUT:
      return "Request Timeout";
    case PageTooBig:
      return "Page size was too big";
    case FatalTransportError:
      return "Fatal transport error - Is the server down ?";
    case UnknownError:
      return "Unknown error";
    default:
      return "(" + code + ")";
    }
  }
}