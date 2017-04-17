package edu.uci.ics.crawler4j.parser;

import java.util.HashMap;
import java.util.Map;

public class ExtractedUrlAnchorPair {

    private String href;
    private String anchor;
    private String tag;
    private Map<String, String> attributes = new HashMap<String, String>();

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getAnchor() {
        return anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, String val) {
        attributes.put(name, val);
    }
}