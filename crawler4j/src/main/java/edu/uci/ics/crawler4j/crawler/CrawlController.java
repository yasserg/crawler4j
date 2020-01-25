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

package edu.uci.ics.crawler4j.crawler;


import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.TLDList;

/**
 * The controller that manages a crawling session. This class creates the
 * crawler threads and monitors their progress.
 *
 * @author Yasser Ganjisaffar
 */
public class CrawlController extends GenericCrawlController<WebCrawler> {

	public CrawlController(CrawlConfig config, PageFetcher pageFetcher, Parser parser, RobotstxtServer robotstxtServer,
			TLDList tldList) throws Exception {
		super(config, pageFetcher, parser, robotstxtServer, tldList);
	}

	public CrawlController(CrawlConfig config, PageFetcher pageFetcher, RobotstxtServer robotstxtServer,
			TLDList tldList) throws Exception {
		super(config, pageFetcher, robotstxtServer, tldList);
	}

	public CrawlController(CrawlConfig config, PageFetcher pageFetcher, RobotstxtServer robotstxtServer)
			throws Exception {
		super(config, pageFetcher, robotstxtServer);
	}

    
    
}
