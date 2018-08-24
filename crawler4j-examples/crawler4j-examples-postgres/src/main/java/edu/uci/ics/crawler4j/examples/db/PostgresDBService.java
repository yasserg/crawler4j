package edu.uci.ics.crawler4j.examples.db;

import edu.uci.ics.crawler4j.crawler.Page;

public interface PostgresDBService {

    void store(Page webPage);

    void close();
}