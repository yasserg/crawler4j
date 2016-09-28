package edu.uci.ics.crawler4j.crawler.exceptions;

/**
 * Created by gustavofoa on 8/8/2014.
 *
 * Occurs when the crawler encounters a configuration problem
 */
public class CrawlerConfigurationException extends Exception {

	public CrawlerConfigurationException(String msg) {
		super(msg);
	}

	public CrawlerConfigurationException(String msg, ReflectiveOperationException e) {
		super(msg, e);
	}

	private static final long serialVersionUID = -657488361530411997L;

}