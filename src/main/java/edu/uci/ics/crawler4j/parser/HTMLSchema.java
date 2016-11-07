package edu.uci.ics.crawler4j.parser;

public class HTMLSchema extends org.ccil.cowan.tagsoup.HTMLSchema {

    public HTMLSchema(String htmlFilterTag) {
        super();
        if (htmlFilterTag != null && !htmlFilterTag.isEmpty()) {
            elementType(htmlFilterTag, M_PCDATA | M_INLINE | M_BLOCK, M_BLOCK, 0);
            parent(htmlFilterTag, "body");
        }
    }
}
