package edu.uci.ics.crawler4j.dao.tuplebinding;

import com.sleepycat.bind.tuple.*;

public class StringTupleBinding extends TupleBinding<String> {

    @Override
    public String entryToObject(TupleInput input) {
        return input.readString();
    }

    @Override
    public void objectToEntry(String object, TupleOutput output) {
        output.writeString(object);
    }

}
