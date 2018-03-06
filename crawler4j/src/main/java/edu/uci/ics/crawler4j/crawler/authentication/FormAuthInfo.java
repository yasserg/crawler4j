package edu.uci.ics.crawler4j.crawler.authentication;

import java.net.MalformedURLException;

import javax.swing.text.html.FormSubmitEvent.MethodType;

/**
 * Created by Avi Hayun on 11/25/2014.
 *
 * FormAuthInfo contains the authentication information needed for FORM authentication (extending
 * AuthInfo which has
 * all common auth info in it)
 * Basically, this is the most common authentication, where you will get to a site and you will
 * need to enter a
 * username and password into an HTML form
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
     *
     * @throws MalformedURLException Make sure your URL is valid
     */
    public FormAuthInfo(String username, String password, String loginUrl, String usernameFormStr,
                        String passwordFormStr) throws MalformedURLException {
        super(AuthenticationType.FORM_AUTHENTICATION, MethodType.POST, loginUrl, username,
              password);

        this.usernameFormStr = usernameFormStr;
        this.passwordFormStr = passwordFormStr;
    }

    /**
     * @return username html "name" form attribute
     */
    public String getUsernameFormStr() {
        return usernameFormStr;
    }

    /**
     * @param usernameFormStr username html "name" form attribute
     */
    public void setUsernameFormStr(String usernameFormStr) {
        this.usernameFormStr = usernameFormStr;
    }

    /**
     * @return password html "name" form attribute
     */
    public String getPasswordFormStr() {
        return passwordFormStr;
    }

    /**
     * @param passwordFormStr password html "name" form attribute
     */
    public void setPasswordFormStr(String passwordFormStr) {
        this.passwordFormStr = passwordFormStr;
    }
}