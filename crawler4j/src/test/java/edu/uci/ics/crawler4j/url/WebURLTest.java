package edu.uci.ics.crawler4j.url;

import java.util.*;

import org.junit.*;

public class WebURLTest {

    private WebURL webURL;

    @Test
    public void setGet() {
        webURL.setPriority(Byte.MAX_VALUE);
        webURL.setDepth(Short.MAX_VALUE);
        webURL.setId(3);
        webURL.setParentId(4);
        webURL.setURL("http://www.gmail.com/");
        webURL.setParentURL("http://www.google.com/");
        webURL.setAnchor("Click here");
        webURL.setTag("<a href=\"http://www.gmail.com\">Click here</a>");

        Assert.assertEquals(Byte.MAX_VALUE, webURL.getPriority());
        Assert.assertEquals(Short.MAX_VALUE, webURL.getDepth());
        Assert.assertEquals(3, webURL.getId());
        Assert.assertEquals(4, webURL.getParentId());
        Assert.assertEquals("http://www.gmail.com/", webURL.getURL());
        Assert.assertEquals("http://www.google.com/", webURL.getParentURL());
        Assert.assertEquals("Click here", webURL.getAnchor());
        Assert.assertEquals("<a href=\"http://www.gmail.com\">Click here</a>", webURL.getTag());
    }

    @Test
    public void attributes() {
        webURL.setTag("<link " + "href='//fonts.googleapis.com/css?family=Ubuntu:regular' "
                + "rel='stylesheet' " + "type='text/css'>");

        Map<String, String> attributes = new HashMap<>();
        attributes.put("href", "//fonts.googleapis.com/css?family=Ubuntu:regular");
        attributes.put("rel", "stylesheet");
        attributes.put("type", "text/css");

        webURL.setAttributes(attributes);

        Assert.assertEquals("//fonts.googleapis.com/css?family=Ubuntu:regular", webURL.getAttribute(
                "href"));
        Assert.assertEquals("text/css", webURL.getAttribute("type"));
    }

    @Test
    public void nullAttributes() {
        webURL.setAttributes(null);
        Assert.assertEquals("", webURL.getAttribute("any"));
    }

    @Test
    public void emptyAttribute() {
        webURL.setTag("<link " + "href='//fonts.googleapis.com/css?family=Ubuntu:regular' "
                + "rel='stylesheet' " + "type='text/css'>");

        Map<String, String> attributes = new HashMap<>();
        attributes.put("href", "//fonts.googleapis.com/css?family=Ubuntu:regular");
        attributes.put("rel", "stylesheet");
        attributes.put("type", "text/css");

        webURL.setAttributes(attributes);
        Assert.assertEquals("", webURL.getAttribute("doesntExist"));
    }

    @Test
    public void basicUrl() {
        webURL.setURL("http://www.google.com");
        assertUrlResults("http://www.google.com", "google.com", "www", "");
    }

    private void assertUrlResults(String url, String domain, String subDomain, String path) {
        Assert.assertEquals(url, webURL.getURL());
        Assert.assertEquals(domain, webURL.getDomain());
        Assert.assertEquals(subDomain, webURL.getSubDomain());
        Assert.assertEquals(path, webURL.getPath());
    }

    @Test
    public void basicUrlWithSlashPath() {
        webURL.setURL("http://www.google.com/");
        assertUrlResults("http://www.google.com/", "google.com", "www", "/");
    }

    @Test
    public void basicUrlWithSimplePath() {
        webURL.setURL("http://www.google.com/search");
        assertUrlResults("http://www.google.com/search", "google.com", "www", "/search");
    }

    @Test
    public void basicUrlWithComplexPath() {
        webURL.setURL("http://www.google.com/search/history/mine.html");
        assertUrlResults("http://www.google.com/search/history/mine.html", "google.com", "www",
                "/search/history/mine.html");
    }

    @Test
    public void basicUrlWithPathAndQueryString() {
        webURL.setURL("http://www.google.com/search/history.html?date=today");
        assertUrlResults("http://www.google.com/search/history.html?date=today", "google.com",
                "www", "/search/history.html");
    }

    @Test
    public void basicUrlWithPathAndQueryStringWithHash() {
        webURL.setURL("http://www.google.com/search/history.html?date=today#topOfPage");
        assertUrlResults("http://www.google.com/search/history.html?date=today#topOfPage",
                "google.com", "www", "/search/history.html");
    }

    @Test
    public void basicUrlWithPathAndHashOnly() {
        webURL.setURL("http://www.google.com/search/history.html#topOfPage");
        assertUrlResults("http://www.google.com/search/history.html#topOfPage", "google.com", "www",
                "/search/history.html#topOfPage");
    }

    @Before
    public void setup() {
        webURL = new WebURL();
    }

}
