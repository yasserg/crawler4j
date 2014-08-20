package edu.uci.ics.crawler4j.tests;

import edu.uci.ics.crawler4j.url.WebURL;
import org.junit.Test;


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
}