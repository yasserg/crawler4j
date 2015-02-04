package edu.uci.ics.crawler4j.parser;

/**
 * Created by Avi on 8/19/2014.
 *
 * This Exception will be thrown whenever the parser tries to parse not allowed content<br>
 * For example when the parser tries to parse binary content although the user configured it not to do it
 */
public class NotAllowedContentException extends Exception {
  public NotAllowedContentException() {
    super("Not allowed to parse this type of content");
  }
}