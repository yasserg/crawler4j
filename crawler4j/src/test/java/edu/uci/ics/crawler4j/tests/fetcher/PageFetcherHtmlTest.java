package edu.uci.ics.crawler4j.examples.fetcher;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.exceptions.PageBiggerThanMaxSizeException;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.url.WebURL;

public class PageFetcherHtmlTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Test
    public void testCustomPageFetcher()
        throws InterruptedException, PageBiggerThanMaxSizeException, IOException {

        WireMock.stubFor(WireMock.head(WireMock.urlEqualTo("/some/index.html"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(200)
                                                     .withHeader("Content-Type", "text/html")));

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/some/index.html"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(200)
                                                     .withHeader("Content-Type", "text/html")
                                                     .withHeader("Content-Length", "47")
                                                     .withBody("<html><body><h1>this is " +
                                                               "html</h1></body></html>")));

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/some/invoice.pdf"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(200)
                                                     .withHeader("Content-Type", "application/pdf")
                                                     .withBody(new byte[] {1, 2, 3, 4})));

        WireMock.stubFor(WireMock.head(WireMock.urlEqualTo("/some/invoice.pdf"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(200)
                                                     .withHeader("Content-Type",
                                                                 "application/pdf")));

        CrawlConfig cfg = new CrawlConfig();
        WebURL url = new WebURL();

        url.setURL("http://localhost:8080/some/index.html");
        PageFetcher pf = new PageFetcherHtmlOnly(cfg);
        pf.fetchPage(url).fetchContent(new Page(url), 47);

        WireMock.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/some/index.html")));
        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/some/index.html")));

        url.setURL("http://localhost:8080/some/invoice.pdf");
        pf = new PageFetcherHtmlOnly(cfg);
        pf.fetchPage(url).fetchContent(new Page(url), 4);

        WireMock.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/some/invoice.pdf")));
        WireMock.verify(0, WireMock.getRequestedFor(WireMock.urlEqualTo("/some/invoice.pdf")));
    }
}
