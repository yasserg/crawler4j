package edu.uci.ics.crawler4j.crawler.authentication;

import java.net.*;

import org.apache.http.auth.*;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

/**
 * Authentication information for Microsoft Active Directory
 */
public class NtCrawlerAuthentication extends BasicCrawlerAuthentication {

    private String domain;

    public NtCrawlerAuthentication(String username, String password, String url, String domain)
            throws MalformedURLException {
        super(url, username, password);
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    protected CredentialsProvider credentialsProvider() {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        try {
            credsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost
                    .getPort()), new NTCredentials(username, password, InetAddress.getLocalHost()
                            .getHostName(), domain));
        } catch (UnknownHostException e) {
            logger.error("Error creating NT credentials", e);
        }
        return credsProvider;
    }

}
