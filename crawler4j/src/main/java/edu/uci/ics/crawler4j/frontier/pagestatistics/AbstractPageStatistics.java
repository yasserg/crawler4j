package edu.uci.ics.crawler4j.frontier.pagestatistics;

import java.util.*;

public class AbstractPageStatistics implements PageStatistics {

    private final Map<PageStatisticsType, Long> pageStatistics = new HashMap<>();

    protected Object mutex = new Object();

    @Override
    public long getValue(PageStatisticsType type) {
        synchronized (mutex) {
            if (!pageStatistics.containsKey(type)) {
                return 0;
            }
            return pageStatistics.get(type);
        }
    }

    @Override
    public void setValue(PageStatisticsType type, long value) {
        synchronized (mutex) {
            pageStatistics.put(type, value);
        }
    }

    @Override
    public void increment(PageStatisticsType type) {
        increment(type, 1);
    }

    @Override
    public void increment(PageStatisticsType type, long addition) {
        synchronized (mutex) {
            setValue(type, getValue(type) + addition);
        }
    }

    @Override
    public void close() {
        // empty
    }

}
