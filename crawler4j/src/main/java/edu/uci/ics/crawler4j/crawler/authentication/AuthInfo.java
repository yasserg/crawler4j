package edu.uci.ics.crawler4j.crawler.authentication;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.text.html.FormSubmitEvent.MethodType;

/**
 * Created by Avi Hayun on 11/23/2014.
 *
 * Abstract class containing authentication information needed to login into a user/password
 * protected site<br>
 * This class should be extended by specific authentication types like form authentication and
 * basic authentication
 * etc<br>
 * <br>
 * This class contains all of the mutual authentication data for all authentication types
 */
public abstract class AuthInfo {
    public enum AuthenticationType {
        BASIC_AUTHENTICATION,
        FORM_AUTHENTICATION,
        NT_AUTHENTICATION
    }

    protected AuthenticationType authenticationType;
    protected MethodType httpMethod;
    protected String protocol;
    protected String host;
    protected String loginTarget;
    protected int port;
    protected String username;
    protected String password;

    /**
     * This constructor should only be used by extending classes
     *
     * @param authenticationType Pick the one which matches your authentication
     * @param httpMethod Choose POST / GET
     * @param loginUrl Full URL of the login page
     * @param username Username for Authentication
     * @param password Password for Authentication
     *
     * @throws MalformedURLException Make sure your URL is valid
     */
    protected AuthInfo(AuthenticationType authenticationType, MethodType httpMethod,
                       String loginUrl, String username, String password)
            throws MalformedURLException {
        this.authenticationType = authenticationType;
        this.httpMethod = httpMethod;
        URL url = new URL(loginUrl);
        this.protocol = url.getProtocol();
        this.host = url.getHost();
        this.port =
                url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
        this.loginTarget = url.getFile();

        this.username = username;
        this.password = password;
    }

    /**
     * @return Authentication type (BASIC, FORM)
     */
    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    /**
     *
     * @param authenticationType Should be set only by extending classes (BASICAuthInfo,
     * FORMAuthInfo)
     */
    public void setAuthenticationType(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    /**
     *
     * @return httpMethod (POST, GET)
     */
    public MethodType getHttpMethod() {
        return httpMethod;
    }

    /**
     * @param httpMethod Should be set by extending classes (POST, GET)
     */
    public void setHttpMethod(MethodType httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * @return protocol type (http, https)
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @param protocol Don't set this one unless you know what you are doing (protocol: http, https)
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * @return host (www.sitename.com)
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host Don't set this one unless you know what you are doing (sets the domain name)
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return file/path which is the rest of the url after the domain name (eg: /login.php)
     */
    public String getLoginTarget() {
        return loginTarget;
    }

    /**
     * @param loginTarget Don't set this one unless you know what you are doing (eg: /login.php)
     */
    public void setLoginTarget(String loginTarget) {
        this.loginTarget = loginTarget;
    }

    /**
     * @return port number (eg: 80, 443)
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port Don't set this one unless you know what you are doing (eg: 80, 443)
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return username used for Authentication
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username username used for Authentication
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return password used for Authentication
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password password used for Authentication
     */
    public void setPassword(String password) {
        this.password = password;
    }
}