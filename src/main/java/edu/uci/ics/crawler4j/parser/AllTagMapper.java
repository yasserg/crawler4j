package edu.uci.ics.crawler4j.parser;

import org.apache.tika.parser.html.HtmlMapper;

/**
 * Maps all HTML tags (not ignore some of this)
 *
 * @author Andrey Nikolaev (vajadhava@gmail.com)
 */
public class AllTagMapper implements HtmlMapper {

    @Override
    public String mapSafeElement(String name) {
        return name.toLowerCase();
    }

    @Override
    public boolean isDiscardElement(String name) {
        return false;
    }

    @Override
    public String mapSafeAttribute(String elementName, String attributeName) {
        return attributeName.toLowerCase();
    }

}
