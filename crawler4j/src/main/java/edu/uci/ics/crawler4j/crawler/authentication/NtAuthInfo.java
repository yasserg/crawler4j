package edu.uci.ics.crawler4j.crawler.authentication;

import java.net.MalformedURLException;

import javax.swing.text.html.FormSubmitEvent.MethodType;

/**
 * Authentication information for Microsoft Active Directory
 */
public class NtAuthInfo extends AuthInfo {
    private String domain;

    public NtAuthInfo(String username, String password, String loginUrl, String domain)
        throws MalformedURLException {
        super(AuthenticationType.NT_AUTHENTICATION, MethodType.GET, loginUrl, username, password);
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}