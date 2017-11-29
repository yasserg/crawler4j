package edu.uci.ics.crawler4j.frontier.pagequeue;

import com.sleepycat.je.Environment;

import edu.uci.ics.crawler4j.dao.SleepyCatDao;
import edu.uci.ics.crawler4j.dao.tuplebinding.*;
import edu.uci.ics.crawler4j.url.*;

public class SleepyCatPageQueue extends SleepyCatDao<WebURLKey, WebURL> implements PageQueue {

    public SleepyCatPageQueue(Environment environment, String databaseName, boolean transactional) {
        super(environment, new WebURLKeyTupleBinding(), new WebURLTupleBinding(), databaseName,
                transactional);
    }

    @Override
    public void put(WebURL webURL) {
        super.put(new WebURLKey(webURL), webURL);
    }

    @Override
    public boolean deleteRecord(WebURL webURL) {
        return super.deleteRecord(new WebURLKey(webURL));
    }

}
