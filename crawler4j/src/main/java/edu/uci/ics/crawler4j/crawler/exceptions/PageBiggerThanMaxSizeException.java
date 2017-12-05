package edu.uci.ics.crawler4j.crawler.exceptions;

/**
 * Created by Avi Hayun on 12/8/2014. Thrown when trying to fetch a page which is bigger than
 * allowed size
 */
@SuppressWarnings("serial")
public class PageBiggerThanMaxSizeException extends CrawlerException {

    long pageSize;

    public PageBiggerThanMaxSizeException(long pageSize) {
        super(String.format(
                "Aborted fetching of this URL as it's size ( %s ) exceeds the maximum size",
                pageSize));
        this.pageSize = pageSize;
    }

    public long getPageSize() {
        return pageSize;
    }
}