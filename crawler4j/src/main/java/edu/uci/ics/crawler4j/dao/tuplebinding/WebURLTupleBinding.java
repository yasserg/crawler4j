package edu.uci.ics.crawler4j.dao.tuplebinding;

import com.sleepycat.bind.tuple.*;

import edu.uci.ics.crawler4j.url.WebURL;

public class WebURLTupleBinding extends TupleBinding<WebURL> {

    @Override
    public WebURL entryToObject(TupleInput input) {
        WebURL webURL = new WebURL();
        webURL.setURL(input.readString());
        webURL.setId(input.readInt());
        webURL.setParentId(input.readInt());
        webURL.setParentURL(input.readString());
        webURL.setDepth(input.readShort());
        webURL.setPriority(input.readByte());
        webURL.setAnchor(input.readString());
        return webURL;
    }

    @Override
    public void objectToEntry(WebURL url, TupleOutput output) {
        output.writeString(url.getURL());
        output.writeInt(url.getId());
        output.writeInt(url.getParentId());
        output.writeString(url.getParentURL());
        output.writeShort(url.getDepth());
        output.writeByte(url.getPriority());
        output.writeString(url.getAnchor());
    }
}