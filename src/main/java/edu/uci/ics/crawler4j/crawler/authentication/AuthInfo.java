package edu.uci.ics.crawler4j.crawler.authentication;

import javax.swing.text.html.FormSubmitEvent.MethodType;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Avi Hayun on 11/23/2014.
 *
 * Abstract class containing authentication information needed to login into a user/password protected site<br/>
 * This class should be extended by specific authentication types like form authentication and basic authentication etc<br/>
 * <br/>
 * This class contains all of the mutual authentication data for all authentication types
 */
public abstract class AuthInfo {
  public enum AuthenticationType {
    BASIC_AUTHENTICATION, FORM_AUTHENTICATION
  }

  protected AuthenticationType authenticationType;
  protected MethodType httpMethod;
  protected String protocol;
  protected String host;
  protected String loginTarget;
  protected int port;
  protected String username;
  protected String password;

  public AuthInfo() {
  }

  /** This constructor should only be used by extending classes */
  protected AuthInfo(AuthenticationType authenticationType, MethodType httpMethod, String loginUrl, String username, String password) throws MalformedURLException {
    this.authenticationType = authenticationType;
    this.httpMethod = httpMethod;
    URL url = new URL(loginUrl);
    this.protocol = url.getProtocol();
    this.host = url.getHost();
    this.port = url.getDefaultPort();
    this.loginTarget = url.getFile();

    this.username = username;
    this.password = password;
  }

  /** Returns the Authentication type (BASIC, FORM) */
  public AuthenticationType getAuthenticationType() {
    return authenticationType;
  }

  /** Should be set only by extending classes (BASIC, FORM) */
  public void setAuthenticationType(AuthenticationType authenticationType) {
    this.authenticationType = authenticationType;
  }

  /** Returns the httpMethod (POST, GET) */
  public MethodType getHttpMethod() {
    return httpMethod;
  }

  /** Should be set by extending classes (POST, GET) */
  public void setHttpMethod(MethodType httpMethod) {
    this.httpMethod = httpMethod;
  }

  /** Returns protocol type (http, https) */
  public String getProtocol() {
    return protocol;
  }

  /** Don't set this one unless you know what you are doing (protocol: http, https) */
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  /** Returns the host (www.sitename.com) */
  public String getHost() {
    return host;
  }

  /** Don't set this one unless you know what you are doing (sets the domain name) */
  public void setHost(String host) {
    this.host = host;
  }

  /** Returns the file/path which is the rest of the url after the domain name (eg: /login.php) */
  public String getLoginTarget() {
    return loginTarget;
  }

  /** Don't set this one unless you know what you are doing (eg: /login.php) */
  public void setLoginTarget(String loginTarget) {
    this.loginTarget = loginTarget;
  }

  /** Returns the port number (eg: 80, 443) */
  public int getPort() {
    return port;
  }

  /** Don't set this one unless you know what you are doing (eg: 80, 443) */
  public void setPort(int port) {
    this.port = port;
  }

  /** Returns the username used for Authentication */
  public String getUsername() {
    return username;
  }

  /** Sets the username used for Authentication */
  public void setUsername(String username) {
    this.username = username;
  }

  /** Returns the password used for Authentication */
  public String getPassword() {
    return password;
  }

  /** Sets the password used for Authentication */
  public void setPassword(String password) {
    this.password = password;
  }
}