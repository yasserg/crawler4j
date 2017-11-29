package edu.uci.ics.crawler4j.frontier.pageharvests;

public interface PageHarvests {

    int getId(String url);

    int add(String url);

    void add(int id, String url);

    boolean isAlreadySeen(String url);

    int count();

    void close();
}
