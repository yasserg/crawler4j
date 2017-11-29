package edu.uci.ics.crawler4j.frontier.pageharvests;

import com.sleepycat.je.Environment;

import edu.uci.ics.crawler4j.dao.*;
import edu.uci.ics.crawler4j.dao.tuplebinding.*;

public class SleepyCatPageHarvests extends SleepyCatDao<String, Integer> implements PageHarvests {

    private static final String DATABASE_NAME = "PageHarvest";

    private final Object mutex = new Object();

    private int lastId;

    public SleepyCatPageHarvests(Environment environment, boolean transactional) {
        super(environment, new StringTupleBinding(), new IntegerTupleBinding(), DATABASE_NAME,
                transactional);
    }

    @Override
    public int add(String url) {
        synchronized (mutex) {
            ++lastId;
            super.put(url, lastId);
            return lastId;
        }
    }

    @Override
    public void put(String url, Integer id) {
        synchronized (mutex) {
            if (id <= lastId) {
                throw new RuntimeException(String.format(
                        "Requested doc id: %s is not larger than: %s", id, lastId));
            }
            Integer previousId = get(url);
            if (null != previousId && 0 < previousId) {
                if (previousId == id) {
                    return;
                }
                throw new RuntimeException(String.format(
                        "Doc id: %s is already assigned to URL: %s", previousId, url));
            }
            super.put(url, id);
            lastId = id;
        }
    }

}
