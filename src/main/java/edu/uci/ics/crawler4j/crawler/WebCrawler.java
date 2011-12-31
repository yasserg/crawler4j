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

import edu.uci.ics.crawler4j.fetcher.PageFetchStatus;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.DocIDServer;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class WebCrawler implements Runnable {

	protected static final Logger logger = Logger.getLogger(WebCrawler.class.getName());

	protected Thread myThread;

	protected Parser parser;

	protected int myId;

	protected CrawlController myController;

	protected PageFetcher pageFetcher;

	private RobotstxtServer robotstxtServer;
	private DocIDServer docIdServer;
	private Frontier frontier;
	
	private boolean isWaitingForNewURLs;

	public void init(int myId, CrawlController crawlController) {
		this.myId = myId;
		this.pageFetcher = crawlController.getPageFetcher();
		this.robotstxtServer = crawlController.getRobotstxtServer();
		this.docIdServer = crawlController.getDocIdServer();
		this.frontier = crawlController.getFrontier();
		this.parser = new Parser(crawlController.getConfig());
		this.myController = crawlController;
		this.isWaitingForNewURLs = false;
	}

	public int getMyId() {
		return myId;
	}

	public CrawlController getMyController() {
		return myController;
	}

	public void onStart() {
	}

	public void onBeforeExit() {
	}

	public Object getMyLocalData() {
		return null;
	}

	public void run() {
		onStart();
		while (true) {
			List<WebURL> assignedURLs = new ArrayList<WebURL>(50);
			isWaitingForNewURLs = true;
			frontier.getNextURLs(50, assignedURLs);
			isWaitingForNewURLs = false;
			if (assignedURLs.size() == 0) {
				if (frontier.isFinished()) {
					return;
				}
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				for (WebURL curURL : assignedURLs) {
					if (curURL != null) {
						processPage(curURL);
						frontier.setProcessed(curURL);
					}
					if (myController.isShuttingDown()) {
						logger.info("Exiting because of controller shutdown.");
						return;
					}
				}
			}
		}
	}

	public boolean shouldVisit(WebURL url) {
		return true;
	}

	public void visit(Page page) {
		// Should be implemented in sub classes
	}

	private int processPage(WebURL curURL) {
		if (curURL == null) {
			return -1;
		}
		try {
			int statusCode = pageFetcher.fetchHeader(curURL);
			if (statusCode != PageFetchStatus.OK) {
				if (statusCode == PageFetchStatus.Moved) {
					if (myController.getConfig().isFollowRedirects()) {
						String movedToUrl = curURL.getURL();
						if (movedToUrl == null) {
							return PageFetchStatus.MovedToUnknownLocation;
						}
						int newDocId = docIdServer.getDocId(movedToUrl);
						if (newDocId > 0) {
							return PageFetchStatus.RedirectedPageIsSeen;
						} else {
							WebURL webURL = new WebURL();
							webURL.setURL(movedToUrl);
							webURL.setParentDocid(curURL.getParentDocid());
							webURL.setDepth(curURL.getDepth());
							webURL.setDocid(-1);
							if (shouldVisit(webURL) && robotstxtServer.allows(webURL)) {
								webURL.setDocid(docIdServer.getNewDocID(movedToUrl));
								frontier.schedule(webURL);
							}
						}
					}
					return PageFetchStatus.Moved;
				} else if (statusCode == PageFetchStatus.PageTooBig) {
					logger.info("Skipping a page which was bigger than max allowed size: " + curURL.getURL());
				}
				return statusCode;
			}

			if (!curURL.getURL().equals(pageFetcher.getFetchedUrl())) {
				if (docIdServer.isSeenBefore(pageFetcher.getFetchedUrl())) {
					return PageFetchStatus.RedirectedPageIsSeen;
				}
				curURL.setURL(pageFetcher.getFetchedUrl());
				curURL.setDocid(docIdServer.getNewDocID(pageFetcher.getFetchedUrl()));
			}

			Page page = new Page(curURL);
			int docid = curURL.getDocid();
			if (pageFetcher.fetchContent(page) && parser.parse(page, curURL.getURL())) {
				ParseData parseData = page.getParseData();
				if (parseData instanceof HtmlParseData) {
					HtmlParseData htmlParseData = (HtmlParseData) parseData;

					List<WebURL> toSchedule = new ArrayList<WebURL>();
					int maxCrawlDepth = myController.getConfig().getMaxDepthOfCrawling();
					for (WebURL webURL : htmlParseData.getOutgoingUrls()) {
						webURL.setParentDocid(docid);
						int newdocid = docIdServer.getDocId(webURL.getURL());
						if (newdocid > 0) {
							// This is not the first time that this Url is
							// visited
							// So, we set the depth to a negative number.
							webURL.setDepth((short) -1);
							webURL.setDocid(newdocid);
						} else {
							webURL.setDocid(-1);
							webURL.setDepth((short) (curURL.getDepth() + 1));
							if (shouldVisit(webURL) && robotstxtServer.allows(webURL)) {
								if (maxCrawlDepth == -1 || curURL.getDepth() < maxCrawlDepth) {
									webURL.setDocid(docIdServer.getNewDocID(webURL.getURL()));
									toSchedule.add(webURL);
								}
							}
						}
					}
					frontier.scheduleAll(toSchedule);
				}
				visit(page);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage() + ", while processing: " + curURL.getURL());
		} finally {
			pageFetcher.discardContentIfNotConsumed();
		}
		return 0;
	}

	public Thread getThread() {
		return myThread;
	}

	public void setThread(Thread myThread) {
		this.myThread = myThread;
	}
	
	public boolean isNotWaitingForNewURLs() {
		return !isWaitingForNewURLs;
	}

}
