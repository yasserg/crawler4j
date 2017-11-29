package edu.uci.ics.crawler4j.dao.tuplebinding;

import com.sleepycat.bind.tuple.*;

public class EnumTupleBinding<T extends Enum<T>> extends TupleBinding<T> {

    private final Class<T> clazz;

    public EnumTupleBinding(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public T entryToObject(TupleInput input) {
        return Enum.valueOf(clazz, input.readString());
    }

    @Override
    public void objectToEntry(T object, TupleOutput output) {
        output.writeString(object.name());
    }

}
