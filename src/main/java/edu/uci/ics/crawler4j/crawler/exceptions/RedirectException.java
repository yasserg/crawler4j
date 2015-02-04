package edu.uci.ics.crawler4j.crawler.exceptions;

import uk.org.lidalia.slf4jext.Level;

/**
 * Created by Avi Hayun on 12/8/2014.
 *
 * Occurs when the crawler encounters a Redirect problem, like redirecting to a visited-already page, or redirecting
 * to nothing
 */
public class RedirectException extends Exception {
  public Level level;

  public RedirectException(Level level, String msg) {
    super(msg);
    this.level = level;
  }
}