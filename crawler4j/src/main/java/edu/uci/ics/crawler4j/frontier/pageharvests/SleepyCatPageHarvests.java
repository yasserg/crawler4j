package edu.uci.ics.crawler4j.frontier.pageharvests;

import org.slf4j.*;

import com.sleepycat.je.*;

import edu.uci.ics.crawler4j.util.Util;

public class SleepyCatPageHarvests implements PageHarvests {

    private static final Logger logger = LoggerFactory.getLogger(SleepyCatPageHarvests.class);

    private static final String DATABASE_NAME = "PageHarvest";

    private final Object mutex = new Object();

    private final Database database;

    private int lastId;

    public SleepyCatPageHarvests(Environment environment, boolean resumable) {
        super();
        this.database = environment.openDatabase(null, DATABASE_NAME, config(resumable));
    }

    private static DatabaseConfig config(boolean resumable) {
        DatabaseConfig config = new DatabaseConfig();
        config.setAllowCreate(true);
        config.setTransactional(resumable);
        config.setDeferredWrite(!resumable);
        return config;
    }

    @Override
    public int getId(String url) {
        synchronized (mutex) {
            DatabaseEntry value = new DatabaseEntry();
            try {
                DatabaseEntry key = new DatabaseEntry(url.getBytes());
                OperationStatus result = database.get(null, key, value, null);
                if (OperationStatus.SUCCESS == result && 0 < value.getData().length) {
                    return Util.byteArray2Int(value.getData());
                }
            } catch (Exception e) {
                logger.error("Exception thrown while getting DocID", e);
            }
            return -1;
        }
    }

    @Override
    public int add(String url) {
        synchronized (mutex) {
            DatabaseEntry key = new DatabaseEntry(url.getBytes());
            ++lastId;
            database.put(null, key, new DatabaseEntry(Util.int2ByteArray(lastId)));
            return lastId;
        }
    }

    @Override
    public void add(int id, String url) {
        synchronized (mutex) {
            if (id <= lastId) {
                throw new RuntimeException(String.format(
                        "Requested doc id: %s is not larger than: %s", id, lastId));
            }
            int previousId = getId(url);
            if (0 < previousId) {
                if (previousId == id) {
                    return;
                }
                throw new RuntimeException(String.format(
                        "Doc id: %s is already assigned to URL: %s", previousId, url));
            }

            database.put(null, new DatabaseEntry(url.getBytes()), new DatabaseEntry(Util
                    .int2ByteArray(id)));
            lastId = id;
        }
    }

    @Override
    public boolean isAlreadySeen(String url) {
        return -1 != getId(url);
    }

    @Override
    public int count() {
        try {
            return (int) database.count();
        } catch (DatabaseException e) {
            logger.error("Exception thrown while getting record count", e);
            return 0;
        }
    }

    @Override
    public void close() {
        try {
            database.close();
        } catch (DatabaseException e) {
            logger.error("Exception thrown while closing " + DATABASE_NAME + " database", e);
        }
    }

}
