package edu.uci.ics.crawler4j.dao.tuplebinding;

import com.sleepycat.bind.tuple.*;

public class IntegerTupleBinding extends TupleBinding<Integer> {

    @Override
    public Integer entryToObject(TupleInput input) {
        return input.readInt();
    }

    @Override
    public void objectToEntry(Integer object, TupleOutput output) {
        output.writeInt(object);
    }

}
