package edu.uci.ics.crawler4j.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.uci.ics.crawler4j.url.WebURL;

/**
 * Created by Avi on 8/19/2014.
 *
 */
public class WebURLTest {

    @Test
    public void testNoLastSlash() {
        WebURL webUrl = new WebURL();
        webUrl.setURL("http://google.com");
    }

    @Test
    public void testSchemeParsing() {
        WebURL webUrl = new WebURL();

        webUrl.setURL("http://example.com");
        assertEquals("http", webUrl.getScheme());

        webUrl.setURL("//example.com");
        assertEquals("http", webUrl.getScheme());

        webUrl.setURL("http://example.com");
        webUrl.setScheme("https");
        assertEquals("https", webUrl.getScheme());
        assertEquals("https://example.com", webUrl.toString());
    }
}