package edu.uci.ics.crawler4j.frontier.pagequeue;

import edu.uci.ics.crawler4j.dao.DefaultDao;
import edu.uci.ics.crawler4j.url.*;

public class DefaultPageQueue extends DefaultDao<WebURLKey, WebURL> implements PageQueue {

    @Override
    public void put(WebURL webURL) {
        super.put(new WebURLKey(webURL), webURL);
    }

    @Override
    public boolean deleteRecord(WebURL webURL) {
        return super.deleteRecord(new WebURLKey(webURL));
    }

}
