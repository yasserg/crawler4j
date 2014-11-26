package edu.uci.ics.crawler4j.crawler.authentication;

import javax.swing.text.html.FormSubmitEvent.MethodType;
import java.net.MalformedURLException;

/**
 * Created by Avi Hayun on 11/25/2014.
 *
 * FormAuthInfo contains the authentication information needed for FORM authentication (extending AuthInfo which has all common auth info in it)
 */
public class FormAuthInfo extends AuthInfo {

  private String usernameFormStr;
  private String passwordFormStr;

  /**
   * Constructor
   *
   * @param username Username to login with
   * @param password Password to login with
   * @param loginUrl Full login URL, starting with "http"... ending with the full URL
   * @param usernameFormStr "Name" attribute of the username form field
   * @param passwordFormStr "Name" attribute of the password form field
   */
  public FormAuthInfo(String username, String password, String loginUrl, String usernameFormStr, String passwordFormStr) throws MalformedURLException {
    super(AuthenticationType.FORM_AUTHENTICATION, MethodType.POST, loginUrl, username, password);

    this.usernameFormStr = usernameFormStr;
    this.passwordFormStr = passwordFormStr;
  }

  /** Returns the username html "name" form attribute */
  public String getUsernameFormStr() {
    return usernameFormStr;
  }

  /** Sets the username html "name" form attribute */
  public void setUsernameFormStr(String usernameFormStr) {
    this.usernameFormStr = usernameFormStr;
  }

  /** Returns the password html "name" form attribute */
  public String getPasswordFormStr() {
    return passwordFormStr;
  }

  /** Sets the password html "name" form attribute */
  public void setPasswordFormStr(String passwordFormStr) {
    this.passwordFormStr = passwordFormStr;
  }
}