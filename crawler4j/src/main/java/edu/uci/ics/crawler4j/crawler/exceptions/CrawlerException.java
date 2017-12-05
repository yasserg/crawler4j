package edu.uci.ics.crawler4j.crawler.exceptions;

@SuppressWarnings("serial")
public abstract class CrawlerException extends Exception {

    public CrawlerException() {
        super();
    }

    public CrawlerException(String message) {
        super(message);
    }

}
