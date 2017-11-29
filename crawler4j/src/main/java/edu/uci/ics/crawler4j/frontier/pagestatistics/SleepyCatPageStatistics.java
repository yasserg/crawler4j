package edu.uci.ics.crawler4j.frontier.pagestatistics;

import com.sleepycat.je.*;

import edu.uci.ics.crawler4j.util.Util;

public class SleepyCatPageStatistics extends AbstractPageStatistics {

    private static final String DATABASE_NAME = "PageStatistics";

    private final Environment environment;

    private final Database database;

    public SleepyCatPageStatistics(Environment environment) {
        super();
        this.environment = environment;
        this.database = environment.openDatabase(null, DATABASE_NAME, config());

        Transaction transaction = environment.beginTransaction(null, null);
        try (Cursor cursor = database.openCursor(transaction, null);) {
            DatabaseEntry key = new DatabaseEntry();
            DatabaseEntry value = new DatabaseEntry();
            OperationStatus result = cursor.getFirst(key, value, null);
            while (OperationStatus.SUCCESS == result) {
                if (0 < value.getData().length) {
                    super.setValue(PageStatisticsType.valueOf(new String(key.getData())), Util
                            .byteArray2Long(value.getData()));
                }
                result = cursor.getNext(key, value, null);
            }
        }
        transaction.commit();
    }

    private static DatabaseConfig config() {
        DatabaseConfig config = new DatabaseConfig();
        config.setAllowCreate(true);
        config.setTransactional(true);
        config.setDeferredWrite(false);
        return config;
    }

    @Override
    public void setValue(PageStatisticsType type, long value) {
        synchronized (mutex) {
            super.setValue(type, value);
            Transaction transaction = environment.beginTransaction(null, null);
            database.put(transaction, new DatabaseEntry(type.name().getBytes()), new DatabaseEntry(
                    Util.long2ByteArray(value)));
            transaction.commit();
        }
    }

}
