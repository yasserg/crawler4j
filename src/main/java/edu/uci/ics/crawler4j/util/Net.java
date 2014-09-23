package edu.uci.ics.crawler4j.util;

import edu.uci.ics.crawler4j.url.WebURL;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Avi Hayun on 9/22/2014.
 * Net related Utils
 */
public class Net {
  private static Pattern pattern = initializePattern();

  public static Set<WebURL> extractUrls(String input) {
    Set<WebURL> extractedUrls = new HashSet<>();

    if (input != null) {
      Matcher matcher = pattern.matcher(input);
      while (matcher.find()) {
        WebURL webURL = new WebURL();
        String urlStr = matcher.group();
        if (!urlStr.startsWith("http"))
          urlStr = "http://" + urlStr;

        webURL.setURL(urlStr);
        extractedUrls.add(webURL);
      }
    }

    return extractedUrls;
  }

  /** Singleton like one time call to initialize the Pattern */
  private static Pattern initializePattern() {
    return Pattern.compile(
      "\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/)|www.)" +
      "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov" +
      "|mil|biz|info|mobi|name|aero|jobs|museum" +
      "|travel|[a-z]{2}))(:[\\d]{1,5})?" +
      "(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?" +
      "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
      "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)" +
      "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
      "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*" +
      "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b");
  }
}