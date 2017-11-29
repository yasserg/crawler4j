package edu.uci.ics.crawler4j.dao;

import java.util.*;

public interface Dao<K extends Object, V extends Object> {

    V get(K keyObject);

    void put(K keyObject, V valueObject);

    void load(Map<K, V> data);

    boolean containsKey(K keyObject);

    int size();

    Collection<V> nextRecords(int max);

    void deleteNextRecords(int count);

    void close();

}
