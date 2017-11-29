package edu.uci.ics.crawler4j.frontier.pagequeue;

import edu.uci.ics.crawler4j.dao.Dao;
import edu.uci.ics.crawler4j.url.*;

public interface PageQueue extends Dao<WebURLKey, WebURL> {

    void put(WebURL webURL);

    boolean deleteRecord(WebURL webURL);

}
