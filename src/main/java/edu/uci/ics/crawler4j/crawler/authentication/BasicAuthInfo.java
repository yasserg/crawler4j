package edu.uci.ics.crawler4j.crawler.authentication;

import javax.swing.text.html.FormSubmitEvent.MethodType;
import java.net.MalformedURLException;

/**
 * Created by Avi Hayun on 11/25/2014.
 *
 * BasicAuthInfo contains the authentication information needed for BASIC authentication (extending AuthInfo which has all common auth info in it)
 */
public class BasicAuthInfo extends AuthInfo {

  /**
   * Constructor
   *
   * @param username Username used for Authentication
   * @param password Password used for Authentication
   * @param loginUrl Full Login URL beginning with "http..." till the end of the url
   */
  public BasicAuthInfo(String username, String password, String loginUrl) throws MalformedURLException {
    super(AuthenticationType.BASIC_AUTHENTICATION, MethodType.GET, loginUrl, username, password);
  }
}