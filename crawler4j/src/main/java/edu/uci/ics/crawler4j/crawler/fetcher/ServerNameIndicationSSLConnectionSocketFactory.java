package edu.uci.ics.crawler4j.crawler.fetcher;

import java.io.IOException;
import java.net.Socket;
import java.security.*;

import javax.net.ssl.*;

import org.apache.http.conn.ssl.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;

/**
 * Class to work around the exception thrown by the SSL subsystem when the server is incorrectly
 * configured for Server Name Indication (SNI). In this case, it may return a warning:
 * "handshake alert: unrecognized_name". Browsers usually ignore this warning, while Java SSL throws
 * an exception.
 *
 * This class extends the SSLConnectionSocketFactory to remove the hostname used in the request,
 * which basically disabled SNI for this host.
 *
 * Based on the code provided by Ivan Shcheklein, available at:
 *
 * http://stackoverflow.com/questions/7615645/ssl-handshake-alert-unrecognized-name-error-since
 * -upgrade-to-java-1-7-0/28571582#28571582
 */
public class ServerNameIndicationSSLConnectionSocketFactory extends SSLConnectionSocketFactory {

    public static final String ENABLE_SNI = "__enable_sni__";

    /*
     * Implement any constructor you need for your particular application -
     * SSLConnectionSocketFactory has many variants
     */
    public ServerNameIndicationSSLConnectionSocketFactory(HostnameVerifier verifier)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        super(sslContext(), verifier);
    }

    // Fixing: https://code.google.com/p/crawler4j/issues/detail?id=174
    // By always trusting the ssl certificate
    private static SSLContext sslContext() throws KeyManagementException, NoSuchAlgorithmException,
            KeyStoreException {
        return SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
            @Override
            public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                return true;
            }
        }).build();
    }

    @Override
    public Socket createLayeredSocket(final Socket socket, final String target, final int port,
            final HttpContext context) throws IOException {
        Boolean enableSniValue = (Boolean) context.getAttribute(ENABLE_SNI);
        boolean enableSni = enableSniValue == null || enableSniValue;
        return super.createLayeredSocket(socket, enableSni ? target : "", port, context);
    }

}
