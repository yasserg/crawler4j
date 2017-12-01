package edu.uci.ics.crawler4j.url;

public class WebURLKey {

    private byte priority;

    private byte depth;

    private int id;

    public WebURLKey() {
        super();
    }

    public WebURLKey(WebURL webURL) {
        super();
        this.priority = webURL.getPriority();
        this.depth = Byte.MAX_VALUE < webURL.getDepth() ? Byte.MAX_VALUE : (byte) webURL.getDepth();
        this.id = webURL.getId();
    }

    public byte getPriority() {
        return priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    public byte getDepth() {
        return depth;
    }

    public void setDepth(byte depth) {
        this.depth = depth;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
