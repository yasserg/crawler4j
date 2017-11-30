package edu.uci.ics.crawler4j.dao;

import java.util.*;

public class DefaultDao<K extends Object, V extends Object> implements Dao<K, V> {

    @Override
    public V get(K keyObject) {
        return null;
    }

    @Override
    public void put(K keyObject, V valueObject) {
        // empty
    }

    @Override
    public void load(Map<K, V> data) {
        // empty
    }

    @Override
    public boolean containsKey(K keyObject) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Collection<V> nextRecords(int max) {
        return Collections.emptyList();
    }

    @Override
    public boolean deleteRecord(K keyObject) {
        return false;
    }

    @Override
    public void deleteNextRecords(int count) {
        // empty
    }

    @Override
    public void close() {
        // empty
    }

}
