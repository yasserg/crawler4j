package edu.uci.ics.crawler4j.robotstxt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.exceptions.RegexpTimeoutException;
import edu.uci.ics.crawler4j.util.RegularExpressionUtils;

public class TimeoutablePathRule extends PathRule {
    protected static final Logger logger = LoggerFactory.getLogger(TimeoutablePathRule.class);
    public static final int defaultCheckInterval = 30000000;

    private final long timeout;
    private final boolean matchOnTimeout;
    private final int checkInterval;

    /**
     * Check if the specified path matches a robots.txt pattern
     *
     * @param pattern The pattern to match
     * @param path The path to match with the pattern
     * @return True when the pattern matches, false if it does not
     * @throws RegexpTimeoutException if the regexp timeouts.
     */
    public static boolean matchesRobotsPattern(String pattern, String path, long timeout, boolean matchOnTimeout, int checkInterval) throws RegexpTimeoutException{
        try {
            return RegularExpressionUtils.createMatcherWithTimeout(path, robotsPatternToRegexp(pattern), timeout, checkInterval).matches();
        } catch(RegexpTimeoutException e) {
            if (matchOnTimeout) {
                return true;
            }
            throw e;
        }
    }

    public static boolean matchesRobotsPattern(String pattern, String path, long timeout, boolean matchOnTimeout) {
        return matchesRobotsPattern(pattern, path, timeout, matchOnTimeout, defaultCheckInterval);
    }
    /**
     * Create a new path rule, based on the specified pattern
     *
     * @param type Either HostDirectives.ALLOWS or HostDirectives.DISALLOWS
     * @param pattern The pattern for this rule
     */
    public TimeoutablePathRule(int type, String pattern, long timeout, boolean matchOnTimeout, int checkInterval) {
        super(type, pattern);
        this.timeout = timeout;
        this.matchOnTimeout = matchOnTimeout;
        this.checkInterval = checkInterval;
    }

    /**
     * Create a new path rule, based on the specified pattern
     *
     * @param type Either HostDirectives.ALLOWS or HostDirectives.DISALLOWS
     * @param pattern The pattern for this rule
     */
    public TimeoutablePathRule(int type, String pattern, long timeout, boolean matchOnTimeout) {
        this(type, pattern, timeout, matchOnTimeout, defaultCheckInterval);
    }

    /**
     * Check if the specified path matches this rule
     *
     * @param path The path to match with this pattern
     * @return True when the path matches, false when it does not
     */
    public boolean matches(String path) {
        try {
            return RegularExpressionUtils.createMatcherWithTimeout(path, pattern, timeout, checkInterval).matches();
        } catch(RegexpTimeoutException e) {
            logger.warn(e.toString());
            if (matchOnTimeout) {
                return true;
            } else {
                return false;
            }
        }
    }
}
