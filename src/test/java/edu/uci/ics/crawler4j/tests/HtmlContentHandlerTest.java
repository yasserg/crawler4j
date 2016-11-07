package edu.uci.ics.crawler4j.tests;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlMapper;
import org.apache.tika.parser.html.HtmlParser;
import org.ccil.cowan.tagsoup.Schema;
import org.junit.Test;

import edu.uci.ics.crawler4j.parser.HTMLSchema;
import edu.uci.ics.crawler4j.parser.HtmlContentHandler;
import edu.uci.ics.crawler4j.parser.HtmlContentMapper;

public class HtmlContentHandlerTest {

    private HtmlParser parser = new HtmlParser();
    private ParseContext parseContext = new ParseContext();

    private HtmlContentHandler parseHtml(String html, String htmlFilterTag) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(html.getBytes());
        Metadata metadata = new Metadata();
        parseContext.set(Schema.class, new HTMLSchema(htmlFilterTag));
        parseContext.set(HtmlMapper.class, new HtmlContentMapper(htmlFilterTag));
        HtmlContentHandler contentHandler = new HtmlContentHandler(htmlFilterTag);
        parser.parse(bais, contentHandler, metadata, parseContext);
        return contentHandler;
    }

    @Test
    public void testEmpty() throws Exception {
        HtmlContentHandler parse = parseHtml("<html></html>", null);
        assertEquals("", parse.getBodyText());
    }

    @Test
    public void testParaInBody() throws Exception {
        HtmlContentHandler parse = parseHtml("<html><body><p>Hello there</p></html>", null);
        assertEquals("Hello there", parse.getBodyText());
    }

    @Test
    public void test2ParaInBody() throws Exception {
        HtmlContentHandler parse =
            parseHtml("<html><body><p>Hello there</p><p>mr</p></html>", null);
        assertEquals("Hello there mr", parse.getBodyText());
    }

    @Test
    public void testTableInBody() throws Exception {
        HtmlContentHandler parse = parseHtml(
            "<html><body><table><tr><th>Hello</th><th>there</th></tr>" +
            "<tr><td>mr</td><td>bear</td></tr></html>", null);
        assertEquals("Hello there mr bear", parse.getBodyText());
    }

    @Test
    public void testFilterHtmlTagInBody() throws Exception {
        HtmlContentHandler parse = parseHtml(
            "<html><body><p>Hello there</p><crawlfilter>should not be in </crawlfilter></html>",
            "crawlfilter");
        assertEquals("Hello there", parse.getBodyText());
    }

}
