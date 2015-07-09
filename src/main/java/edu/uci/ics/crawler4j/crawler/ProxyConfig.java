package edu.uci.ics.crawler4j.crawler;

import java.io.Serializable;
import java.net.InetAddress;
import com.google.common.net.HostSpecifier;
import com.google.common.net.InetAddresses;


public class ProxyConfig implements Comparable<ProxyConfig>, Serializable
{
    /**
     * Generated serial UID
     * @see java.io.Serializable 
     */
    private static final long serialVersionUID = -8062176688254941878L;

    /**
     * If crawler should run behind a proxy, this parameter can be used for
     * specifying the proxy host.
     */
    private final String proxyHost;

    /**
     * If crawler should run behind a proxy, this parameter can be used for
     * specifying the proxy port.
     */
    private final int proxyPort;

    /**
     * If crawler should run behind a proxy and user/pass is needed for
     * authentication in proxy, this parameter can be used for specifying the
     * username.
     */
    private final String proxyUsername;

    /**
     * If crawler should run behind a proxy and user/pass is needed for
     * authentication in proxy, this parameter can be used for specifying the
     * password.
     */
    private final String proxyPassword;

    /**
     * Create a proxy object
     * @param proxyHost proxy host
     * @param proxyPort proxy port
     */
    public ProxyConfig(String proxyHost, int proxyPort)
    {
        checkHost(proxyHost);
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = null;
        this.proxyPassword = null;
    }
    
    /**
     * Create a proxy object
     * @param proxyHost proxy host
     * @param proxyPort proxy port
     * @param proxyUsername proxy username
     * @param proxyPassword proxy password
     */
    public ProxyConfig(String proxyHost, int proxyPort, String proxyUsername, String proxyPassword)
    {
        checkHost(proxyHost);
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
    }
    
    private static void checkHost(String host)
    {
        if (host == null) {
            throw new IllegalArgumentException("proxyHost cannot be null !");
        }
        InetAddress ip = null;
        try {
            ip = InetAddresses.forString(host);
        } catch (IllegalArgumentException e) {
            
        }
        if (!HostSpecifier.isValid(host) && ip == null) {
            throw new IllegalArgumentException("proxyHost is invalid (must be an IP or an host) !");
        }
    }
    
    /**
     * Get proxy host
     * @return proxy host
     */
    public String getProxyHost()
    {
        return proxyHost;
    }

    /**
     * Get proxy port
     * @return proxy port
     */
    public int getProxyPort()
    {
        return proxyPort;
    }

    /**
     * Get proxy username
     * @return proxy username
     */
    public String getProxyUsername()
    {
        return proxyUsername;
    }

    /**
     * Get proxy password
     * @return proxy password
     */
    public String getProxyPassword()
    {
        return proxyPassword;
    }
    
    @Override
    public int hashCode()
    {
        return proxyHost.hashCode() + proxyPort;
    }
    
    @Override
    public String toString()
    {
        return proxyHost + ":" + proxyPort + "/" + proxyUsername + ":" + proxyPassword;
    }
    
    @Override
    public int compareTo(ProxyConfig o)
    {
        return this.getProxyHost().compareTo(o.getProxyHost());
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof ProxyConfig)) {
            return false;
        }
        ProxyConfig proxy = (ProxyConfig) o;
        return proxy.hashCode() == this.hashCode();
    }
}