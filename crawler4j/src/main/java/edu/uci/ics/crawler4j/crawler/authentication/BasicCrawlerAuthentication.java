package edu.uci.ics.crawler4j.crawler.authentication;

import java.net.MalformedURLException;

import org.apache.http.auth.*;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

/**
 * Created by Avi Hayun on 11/25/2014.
 *
 * BasicAuthInfo contains the authentication information needed for BASIC authentication (extending
 * AuthInfo which has all common auth info in it)
 *
 * BASIC authentication in PHP:
 * <ul>
 * <li>http://php.net/manual/en/features.http-auth.php</li>
 * <li>http://stackoverflow.com/questions/4150507/how-can-i-use-basic-http-authentication-in-php
 * </li>
 * </ul>
 */
public class BasicCrawlerAuthentication extends AbstractCrawlerAuthentication {

    /**
     * Constructor
     *
     * @param username
     *            Username used for Authentication
     * @param password
     *            Password used for Authentication
     * @param url
     *            Full Login URL beginning with "http..." till the end of the url
     *
     * @throws MalformedURLException
     *             Make sure your URL is valid
     */
    public BasicCrawlerAuthentication(String username, String password, String url)
            throws MalformedURLException {
        super(url, username, password);
    }

    @Override
    protected CredentialsProvider credentialsProvider() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost
                .getPort()), new UsernamePasswordCredentials(username, password));
        return credentialsProvider;
    }

}
