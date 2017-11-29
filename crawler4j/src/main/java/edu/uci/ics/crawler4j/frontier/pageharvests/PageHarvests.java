package edu.uci.ics.crawler4j.frontier.pageharvests;

import edu.uci.ics.crawler4j.dao.Dao;

public interface PageHarvests extends Dao<String, Integer> {

    int add(String url);

}
