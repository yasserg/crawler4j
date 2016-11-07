package edu.uci.ics.crawler4j.parser;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.tika.parser.html.DefaultHtmlMapper;

public class HtmlContentMapper extends DefaultHtmlMapper {
    private static Map<String, String> customSafeElements = new LinkedHashMap<>();

    public HtmlContentMapper(String htmlFilterTag) {
        customSafeElements.put(htmlFilterTag.toUpperCase(), htmlFilterTag.toLowerCase());
    }

    @Override
    public String mapSafeElement(String name) {
        String mapSafeElement = super.mapSafeElement(name);
        if (mapSafeElement == null) {
            mapSafeElement = customSafeElements.get(name);
        }
        return mapSafeElement;
    }
}
