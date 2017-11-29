package edu.uci.ics.crawler4j.frontier.pagestatistics;

public interface PageStatistics {

    long getValue(PageStatisticsType type);

    void setValue(PageStatisticsType type, long value);

    void increment(PageStatisticsType type);

    void increment(PageStatisticsType type, long addition);

    void close();

}
