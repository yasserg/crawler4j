package edu.uci.ics.crawler4j.selenium;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Cookie;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;

import edu.uci.ics.crawler4j.url.WebURL;

/**
 * Basic Selenmium Web Driver based on JBrowserDriver. Allows to use a persistent CookieStore
 *
 * Cookies will only be saved uppon driver close
 *
 * @author Dario Goikoetxea
 *
 */
public class SeleniumWebDriver extends JBrowserDriver {

    protected final CookieStore cookieStore;

    public SeleniumWebDriver() {
        super();
        this.cookieStore = null;
    }

    public SeleniumWebDriver(Capabilities capabilities) {
        super(capabilities);
        this.cookieStore = null;
    }

    public SeleniumWebDriver(Settings settings) {
        super(settings);
        this.cookieStore = null;
    }

    public SeleniumWebDriver(CookieStore cookieStore) {
        super();
        this.cookieStore = cookieStore;
    }

    public SeleniumWebDriver(CookieStore cookieStore, Capabilities capabilities) {
        super(capabilities);
        this.cookieStore = cookieStore;
    }

    public SeleniumWebDriver(CookieStore cookieStore, Settings settings) {
        super(settings);
        this.cookieStore = cookieStore;
    }

    public void get(WebURL url) {
        if (cookieStore != null) {
            importCookies(url.getDomain());
        }
        super.get(url.getURL());
    }

    @Override
    public void get(String url) {
        if (cookieStore != null) {
            int domainStartIdx = url.indexOf("//") + 2;
            int domainEndIdx = url.indexOf('/', domainStartIdx);
            domainEndIdx = (domainEndIdx > domainStartIdx) ? domainEndIdx : url.length();
            String domain = url.substring(domainStartIdx, domainEndIdx);
            importCookies(domain);
        }
        super.get(url);
    }

    @Override
    public void quit() {
        if (cookieStore != null) {
            for (Cookie cookie : manage().getCookies()) {
                BasicClientCookie newCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
                newCookie.setDomain(cookie.getDomain());
                newCookie.setPath(cookie.getPath());
                newCookie.setExpiryDate(cookie.getExpiry());
                newCookie.setSecure(cookie.isSecure());
                cookieStore.addCookie(newCookie);
            }
        }
        super.quit();
    }

    private void importCookies(String domain) {
        Options options = manage();
        for (org.apache.http.cookie.Cookie cookie : cookieStore.getCookies()) {
            /*
            if (cookie.getDomain().equals(domain)) {
                options.addCookie(new Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(),
                                                cookie.getExpiryDate(), cookie.isSecure()));
            }
            */
            options.addCookie(new Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(),
                                            cookie.getExpiryDate(), cookie.isSecure()));
        }
    }
}
