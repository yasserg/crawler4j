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

package edu.uci.ics.crawler4j.fetcher;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class PageFetchStatus {

	public static final int OK = 1000;

	public static final int PageTooBig = 1001;

    public static final int FatalTransportError = 1005;

	public static final int UnknownError = 1006;

	public static final int RedirectedPageIsSeen = 1010;

	public static final int NotInTextFormat = 1011;

	public static final int PageIsBinary = 1012;

	public static final int Moved = 1013;

	public static final int MovedToUnknownLocation = 1014;

}
