package edu.uci.ics.crawler4j.tests;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlMapper;
import org.apache.tika.parser.html.HtmlParser;
import org.junit.Test;

import edu.uci.ics.crawler4j.parser.AllTagMapper;
import edu.uci.ics.crawler4j.parser.ExtractedUrlAnchorPair;
import edu.uci.ics.crawler4j.parser.HtmlContentHandler;

public class HtmlContentHandlerTest {

    private HtmlParser parser = new HtmlParser();
    private ParseContext parseContext = new ParseContext();

    private HtmlContentHandler parseHtml(String html) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(html.getBytes());
        Metadata metadata = new Metadata();
        HtmlContentHandler contentHandler = new HtmlContentHandler();
        parseContext.set(HtmlMapper.class, AllTagMapper.class.newInstance());
        parser.parse(bais, contentHandler, metadata, parseContext);
        return contentHandler;
    }

    @Test
    public void testEmpty() throws Exception {
        HtmlContentHandler parse = parseHtml("<html></html>");
        assertEquals("", parse.getBodyText());
    }

    @Test
    public void testParaInBody() throws Exception {
        HtmlContentHandler parse = parseHtml("<html><body><p>Hello there</p></html>");
        assertEquals("Hello there", parse.getBodyText());
    }

    @Test
    public void test2ParaInBody() throws Exception {
        HtmlContentHandler parse = parseHtml("<html><body><p>Hello there</p><p>mr</p></html>");
        assertEquals("Hello there mr", parse.getBodyText());
    }

    @Test
    public void testTableInBody() throws Exception {
        HtmlContentHandler parse = parseHtml(
            "<html><body><table><tr><th>Hello</th><th>there</th></tr>" +
            "<tr><td>mr</td><td>bear</td></tr></html>");
        assertEquals("Hello there mr bear", parse.getBodyText());
    }

    @Test
    public void testSciptInHead() throws Exception {

        HtmlContentHandler parse = parseHtml("<html><head>" +
            "<script src=\"/js/app.js\"></script>" +
            "</head></html>");

        ExtractedUrlAnchorPair script = parse.getOutgoingUrls().get(0);
        assertEquals("/js/app.js", script.getHref());
    }

    @Test
    public void testLinkAttributes() throws Exception {

        HtmlContentHandler parse = parseHtml("<html><body>" +
            "<a href=\"www.example.com\" rel=\"nofollow\">Example</a>" +
            "</body></html>");

        ExtractedUrlAnchorPair link = parse.getOutgoingUrls().get(0);
        assertEquals("nofollow", link.getAttribute("rel"));
    }

}
