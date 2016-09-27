package edu.uci.ics.crawler4j.tests;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.fetcher.politness.PolitenessServer;
import edu.uci.ics.crawler4j.url.WebURL;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by zowallar on 19.09.2016.
 */
public class PolitenessServerTestCase {

    private PolitenessServer politenessServer;

    @Before
    public void init() {
        CrawlConfig config = new CrawlConfig();

        config.setPolitenessDelay(100);
        config.setPolitenessMaximumHostEntries(3);

        politenessServer = new PolitenessServer(config);
    }

    @Test
    public void testApplyPoliteness1() {

        WebURL webUrl = new WebURL();
        webUrl.setURL("https://github.com/yasserg/crawler4j");

        long politenessDelay = politenessServer.applyPoliteness(webUrl);

        assertEquals(PolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

        politenessDelay = politenessServer.applyPoliteness(webUrl);

        assertTrue(politenessDelay > 0);

    }

    @Test
    public void testApplyPoliteness2() {

        WebURL webUrl = new WebURL();
        webUrl.setURL("https://github.com/yasserg/crawler4j");

        long politenessDelay = politenessServer.applyPoliteness(webUrl);

        assertEquals(PolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

        webUrl.setURL("https://github.com/yasserg/crawler4j/blob/master/pom.xml");

        politenessDelay = politenessServer.applyPoliteness(webUrl);

        assertTrue(politenessDelay > 0);

        //let's wait some time, it should not be listed anymore
        sleep(1000);

        politenessDelay = politenessServer.applyPoliteness(webUrl);

        assertEquals(PolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

    }

    @Test
    public void testApplyPoliteness3() {

        WebURL webUrl = new WebURL();
        webUrl.setURL("https://github.com/yasserg/crawler4j");

        long politenessDelay = politenessServer.applyPoliteness(webUrl);

        assertEquals(PolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

        webUrl.setURL("http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentLinkedQueue.html");

        politenessDelay = politenessServer.applyPoliteness(webUrl);

        assertEquals(PolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

        webUrl.setURL("https://github.com/yasserg/crawler4j/blob/master/pom.xml");

        politenessDelay = politenessServer.applyPoliteness(webUrl);

        assertTrue(politenessDelay > 0);

        //let's wait some time, it should not be listed anymore
        sleep(3000);

        politenessDelay = politenessServer.applyPoliteness(webUrl);

        assertEquals(PolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

    }

    @Test
    public void testRemoveExpiredEntries() {

        WebURL webUrl = new WebURL();
        webUrl.setURL("https://github.com/yasserg/crawler4j");

        long politenessDelay = politenessServer.applyPoliteness(webUrl);

        assertEquals(PolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

        webUrl.setURL("http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentLinkedQueue.html");

        politenessDelay = politenessServer.applyPoliteness(webUrl);

        assertEquals(PolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

        webUrl.setURL("https://www.google.de/?gws_rd=ssl");

        politenessDelay = politenessServer.applyPoliteness(webUrl);

        assertEquals(PolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

        webUrl.setURL("https://stackoverflow.com/");

        politenessDelay = politenessServer.applyPoliteness(webUrl);

        assertEquals(PolitenessServer.NO_POLITENESS_APPLIED, politenessDelay);

        //let's wait some time, it should not be listed anymore
        sleep(5000);

        //one entry should be evicted...
        assertEquals(3, politenessServer.getSize());

    }


    private void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            //nothing to do here
        }
    }


}
