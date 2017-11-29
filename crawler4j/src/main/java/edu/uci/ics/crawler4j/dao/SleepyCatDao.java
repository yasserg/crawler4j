package edu.uci.ics.crawler4j.dao;

import java.util.*;

import org.slf4j.*;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.*;

public class SleepyCatDao<K extends Object, V extends Object> implements Dao<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(SleepyCatDao.class);

    private final Environment environment;

    private final boolean transactional;

    private final Database database;

    private final TupleBinding<K> keyBinding;

    private final TupleBinding<V> valueBinding;

    private final Object mutex = new Object();

    public SleepyCatDao(Environment environment, TupleBinding<K> keyBinding,
            TupleBinding<V> valueBinding, String databaseName, boolean transactional) {
        super();
        this.environment = environment;
        this.transactional = transactional;
        this.keyBinding = keyBinding;
        this.valueBinding = valueBinding;
        this.database = environment.openDatabase(null, databaseName, config(transactional));
    }

    private static DatabaseConfig config(boolean transactional) {
        DatabaseConfig config = new DatabaseConfig();
        config.setAllowCreate(true);
        config.setTransactional(transactional);
        config.setDeferredWrite(!transactional);
        return config;
    }

    @Override
    public V get(K keyObject) {
        synchronized (mutex) {
            try {
                DatabaseEntry key = new DatabaseEntry();
                keyBinding.objectToEntry(keyObject, key);

                DatabaseEntry value = new DatabaseEntry();
                OperationStatus result = database.get(null, key, value, null);
                if (OperationStatus.SUCCESS == result && 0 < value.getData().length) {
                    V valueObject = valueBinding.entryToObject(value);
                    logger.debug("Got from {} key: {} value: {}", database.getDatabaseName(),
                            keyObject, valueObject);
                    return valueObject;
                }
            } catch (Exception e) {
                logger.error("Exception thrown while getting value", e);
            }
            logger.debug("Value from {} for key: {} not found", database.getDatabaseName(),
                    keyObject);
            return null;
        }
    }

    @Override
    public void put(K keyObject, V valueObject) {
        synchronized (mutex) {
            DatabaseEntry key = new DatabaseEntry();
            keyBinding.objectToEntry(keyObject, key);
            DatabaseEntry value = new DatabaseEntry();
            valueBinding.objectToEntry(valueObject, value);
            logger.debug("putting to {} key: {} value: {}", database.getDatabaseName(), keyObject,
                    valueObject);
            database.put(null, key, value);
        }
    }

    @Override
    public boolean containsKey(K keyObject) {
        return null != get(keyObject);
    }

    @Override
    public int size() {
        try {
            return (int) database.count();
        } catch (DatabaseException e) {
            logger.error("Exception thrown while getting record count", e);
            return 0;
        }
    }

    @Override
    public Collection<V> nextRecords(int max) {
        synchronized (mutex) {
            Collection<V> results = new ArrayList<>(max);

            DatabaseEntry key = new DatabaseEntry();
            DatabaseEntry value = new DatabaseEntry();

            Transaction transaction = beginTransaction();
            try (Cursor cursor = openCursor(transaction)) {
                OperationStatus result = cursor.getFirst(key, value, null);
                int matches = 0;
                while (matches < max && OperationStatus.SUCCESS == result) {
                    if (0 < value.getData().length) {
                        matches++;
                        results.add(valueBinding.entryToObject(value));
                    }
                    result = cursor.getNext(key, value, null);
                }
            }
            commit(transaction);
            logger.debug("Got from {} {} records", database.getDatabaseName(), results.size());
            return results;
        }
    }

    private Transaction beginTransaction() {
        return transactional ? environment.beginTransaction(null, null) : null;
    }

    private Cursor openCursor(Transaction txn) {
        return database.openCursor(txn, null);
    }

    private static void commit(Transaction transaction) {
        if (null != transaction) {
            transaction.commit();
        }
    }

    @Override
    public void deleteNextRecords(int count) {
        synchronized (mutex) {
            logger.debug("Deleting from {} next {} records", database.getDatabaseName(), count);
            DatabaseEntry key = new DatabaseEntry();
            DatabaseEntry value = new DatabaseEntry();
            Transaction transaction = beginTransaction();
            try (Cursor cursor = openCursor(transaction)) {
                OperationStatus result = cursor.getFirst(key, value, null);
                int matches = 0;
                while (matches < count && OperationStatus.SUCCESS == result) {
                    cursor.delete();
                    matches++;
                    result = cursor.getNext(key, value, null);
                }
            }
            commit(transaction);
        }
    }

    @Override
    public void close() {
        try {
            database.close();
        } catch (DatabaseException e) {
            logger.error(String.format("Exception thrown while closing {} database", database
                    .getDatabaseName()), e);
        }
    }

}
