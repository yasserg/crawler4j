package edu.uci.ics.crawler4j.parser;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.exceptions.ParseException;

public interface HtmlParser {

    HtmlParseData parse(Page page, String contextURL) throws ParseException;

}
