package edu.uci.ics.crawler4j.frontier.pagestatistics;

import java.util.*;

import com.sleepycat.je.Environment;

import edu.uci.ics.crawler4j.dao.SleepyCatDao;
import edu.uci.ics.crawler4j.dao.tuplebinding.*;

public class SleepyCatPageStatistics extends SleepyCatDao<PageStatisticsType, Long> implements
        PageStatistics {

    private static final String DATABASE_NAME = "PageStatistics";

    private final Map<PageStatisticsType, Long> data = new HashMap<>();

    private Object mutex = new Object();

    public SleepyCatPageStatistics(Environment environment) {
        super(environment, new EnumTupleBinding<>(PageStatisticsType.class), new LongTupleBinding(),
                DATABASE_NAME, true);
        load(data);
    }

    @Override
    public Long get(PageStatisticsType type) {
        synchronized (mutex) {
            if (!data.containsKey(type)) {
                return 0L;
            }
            return data.get(type);
        }
    }

    @Override
    public void put(PageStatisticsType type, Long value) {
        synchronized (mutex) {
            data.put(type, value);
            super.put(type, value);
        }
    }

    @Override
    public void increment(PageStatisticsType type) {
        increment(type, 1);
    }

    @Override
    public void increment(PageStatisticsType type, long addition) {
        synchronized (mutex) {
            put(type, get(type) + addition);
        }
    }

    @Override
    public void close() {
        // empty
    }

    @Override
    public boolean containsKey(PageStatisticsType keyObject) {
        return data.containsKey(keyObject);
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public Collection<Long> nextRecords(int max) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteNextRecords(int count) {
        throw new UnsupportedOperationException();
    }

}
