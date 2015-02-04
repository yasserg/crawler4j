package edu.uci.ics.crawler4j.crawler.authentication;

import java.net.MalformedURLException;

import javax.swing.text.html.FormSubmitEvent.MethodType;

/**
 * Created by Avi Hayun on 11/25/2014.
 *
 * BasicAuthInfo contains the authentication information needed for BASIC authentication (extending AuthInfo which
 * has all common auth info in it)
 *
 * BASIC authentication in PHP:
 * <ul>
 *  <li>http://php.net/manual/en/features.http-auth.php</li>
 *  <li>http://stackoverflow.com/questions/4150507/how-can-i-use-basic-http-authentication-in-php</li>
 * </ul>
 */
public class BasicAuthInfo extends AuthInfo {

  /**
   * Constructor
   *
   * @param username Username used for Authentication
   * @param password Password used for Authentication
   * @param loginUrl Full Login URL beginning with "http..." till the end of the url
   *
   * @throws MalformedURLException Make sure your URL is valid
   */
  public BasicAuthInfo(String username, String password, String loginUrl) throws MalformedURLException {
    super(AuthenticationType.BASIC_AUTHENTICATION, MethodType.GET, loginUrl, username, password);
  }
}