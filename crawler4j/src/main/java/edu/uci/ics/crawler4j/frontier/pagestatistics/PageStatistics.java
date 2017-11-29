package edu.uci.ics.crawler4j.frontier.pagestatistics;

import edu.uci.ics.crawler4j.dao.Dao;

public interface PageStatistics extends Dao<PageStatisticsType, Long> {

    void increment(PageStatisticsType type);

    void increment(PageStatisticsType type, long addition);

}
