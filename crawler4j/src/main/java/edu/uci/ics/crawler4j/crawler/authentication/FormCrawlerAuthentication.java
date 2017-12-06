package edu.uci.ics.crawler4j.crawler.authentication;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;

/**
 * Created by Avi Hayun on 11/25/2014.
 *
 * FormAuthInfo contains the authentication information needed for FORM authentication (extending
 * AuthInfo which has all common auth info in it) Basically, this is the most common authentication,
 * where you will get to a site and you will need to enter a username and password into an HTML form
 */
public class FormCrawlerAuthentication extends AbstractCrawlerAuthentication {

    private String usernameFormName;

    private String passwordFormName;

    /**
     * Constructor
     *
     * @param username
     *            Username to login with
     * @param password
     *            Password to login with
     * @param url
     *            Full login URL, starting with "http"... ending with the full URL
     * @param usernameFormName
     *            "Name" attribute of the username form field
     * @param passwordFormName
     *            "Name" attribute of the password form field
     *
     * @throws MalformedURLException
     *             Make sure your URL is valid
     */
    public FormCrawlerAuthentication(String username, String password, String url,
            String usernameFormName, String passwordFormName) throws MalformedURLException {
        super(url, username, password);
        this.usernameFormName = usernameFormName;
        this.passwordFormName = passwordFormName;
    }

    @Override
    public void configure(HttpClientBuilder clientBuilder) {
        // empty
    }

    @Override
    public void login(CloseableHttpClient httpClient) {
        logger.info("Logging into: {} as {}", targetHost, username);
        HttpPost httpPost = new HttpPost(loginUri());
        List<NameValuePair> formParams = new ArrayList<>();
        formParams.add(new BasicNameValuePair(usernameFormName, username));
        formParams.add(new BasicNameValuePair(passwordFormName, password));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));
            httpClient.execute(httpPost);
            logger.debug(String.format("Successfully Logged in with user: %s to: %s", username,
                    host));
        } catch (UnsupportedEncodingException e) {
            logger.error("Encountered a non supported encoding while trying to login to: "
                    + targetHost, e);
        } catch (ClientProtocolException e) {
            logger.error("While trying to login to: " + targetHost
                    + " - Client protocol not supported", e);
        } catch (IOException e) {
            logger.error("While trying to login to: " + targetHost + " - Error making request", e);
        }
    }

    private String loginUri() {
        return protocol + "://" + host + ":" + port + file;
    }

}
