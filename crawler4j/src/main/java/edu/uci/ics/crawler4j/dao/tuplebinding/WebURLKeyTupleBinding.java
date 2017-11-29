package edu.uci.ics.crawler4j.dao.tuplebinding;

import com.sleepycat.bind.tuple.*;

import edu.uci.ics.crawler4j.url.WebURLKey;

public class WebURLKeyTupleBinding extends TupleBinding<WebURLKey> {

    @Override
    public WebURLKey entryToObject(TupleInput input) {
        WebURLKey obj = new WebURLKey();
        obj.setPriority(input.readByte());
        obj.setDepth(input.readByte());
        obj.setId(input.readInt());
        return obj;
    }

    @Override
    public void objectToEntry(WebURLKey url, TupleOutput output) {
        output.writeByte(url.getPriority());
        output.writeByte(url.getDepth());
        output.writeInt(url.getId());
    }
}