package edu.uci.ics.crawler4j.dao.tuplebinding;

import com.sleepycat.bind.tuple.*;

public class LongTupleBinding extends TupleBinding<Long> {

    @Override
    public Long entryToObject(TupleInput input) {
        return input.readLong();
    }

    @Override
    public void objectToEntry(Long object, TupleOutput output) {
        output.writeLong(object);
    }

}
