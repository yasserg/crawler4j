package edu.uci.ics.crawler4j.robotstxt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The UserAgentDirectives class stores the configuration for a single
 * user agent as defined in the robots.txt. The user agent string used
 * depends on the most recent User-agent: definition in the robots.txt file.
 */
public class UserAgentDirectives {
    public static final Logger logger = LoggerFactory.getLogger(UserAgentDirectives.class);

    public Set<String> userAgents;
    private List<String> sitemap = null;
    private String preferredHost = null;
    private Double crawlDelay = null;
    private Set<PathRule> pathRules = new HashSet<>();

    /**
     * Comparator used to order the list of matching path rules in such a way
     * that the most specific match (= longest) match comes first.
     */
    static class PathComparator implements Comparator<PathRule> {
        /** The path to compare the path rules with */
        String path;

        /** Initialize with the path */
        PathComparator(String path) {
            this.path = path;
        }

        /**
         * Compare two paths.
         * If lhs matches and rhs does not, this will return -1
         * If rhs matches and lhs does not, this will return 1
         * If both match or both do not match,, this will return the result of
         *    a numeric comparison of the length of both patterns, where
         *    the longest (=most specific) one will come first.
         */
        @Override
        public int compare(PathRule lhs, PathRule rhs) {
            boolean p1Match = lhs.matches(path);
            boolean p2Match = rhs.matches(path);

            // Matching patterns come first
            if (p1Match && !p2Match) {
                return -1;
            } else if (p2Match && !p1Match) {
                return 1;
            }

            // Most specific pattern first
            String p1 = lhs.pattern.toString();
            String p2 = rhs.pattern.toString();

            if (p1.length() != p2.length()) {
                return Integer.compare(p2.length(), p1.length());
            }

            // Just order alphabetically if the patterns are of the same length
            return p1.compareTo(p2);
        }
    }

    /**
     * Create a UserAgentDirectives clause
     *
     * @param userAgents The list user agents for this rule
     */
    public UserAgentDirectives(Set<String> userAgents) {
        this.userAgents = userAgents;
    }

    /**
     * Match the current user agent directive set with the given
     * user agent. The returned value will be the maximum match length
     * of any user agent.
     *
     * @param userAgent The user agent used by the crawler
     * @return The maximum length of a matching user agent in this set of directives
     */
    public int match(String userAgent) {
        userAgent = userAgent.toLowerCase();
        int maxLength = 0;
        for (String ua : userAgents) {
            if (ua.equals("*") || userAgent.contains(ua)) {
                maxLength = Math.max(maxLength, ua.length());
            }
        }
        return maxLength;
    }

    public boolean isWildcard() {
        return userAgents.contains("*");
    }

    public boolean isEmpty() {
        return pathRules.isEmpty();
    }

    public int checkAccess(String path, String userAgent) {
        // If the user agent does not match, the verdict is known
        if (match(userAgent) == 0) {
            return HostDirectives.UNDEFINED;
        }

        // Order the rules based on their match with the path
        Set<PathRule> rules = new TreeSet<>(new PathComparator(path));
        rules.addAll(pathRules);

        // Return the verdict of the best matching rule
        for (PathRule rule : rules) {
            if (rule.matches(path)) {
                return rule.type;
            }
        }

        return HostDirectives.UNDEFINED;
    }

    public static class UserAgentComparator implements Comparator<UserAgentDirectives> {
        String crawlUserAgent;

        UserAgentComparator(String myUA) {
            crawlUserAgent = myUA;
        }

        @Override
        public int compare(UserAgentDirectives lhs, UserAgentDirectives rhs) {
            int matchLhs = lhs.match(crawlUserAgent);
            int matchRhs = rhs.match(crawlUserAgent);
            if (matchLhs != matchRhs) {
                return Integer.compare(matchRhs, matchLhs); // Sort descending
            }

            // Return the shortest list of user-agents unequal
            if (lhs.userAgents.size() != rhs.userAgents.size()) {
                return Integer.compare(lhs.userAgents.size(), rhs.userAgents.size());
            }

            // Alphabetic sort when length of lists is equal
            Iterator<String> i1 = lhs.userAgents.iterator();
            Iterator<String> i2 = rhs.userAgents.iterator();

            // Find first non-equal user agent
            while (i1.hasNext()) {
                String ua1 = i1.next();
                String ua2 = i2.next();
                int order = ua1.compareTo(ua2);
                if (order != 0) {
                    return order;
                }
            }

            // List of user agents was also equal, so these directives are equal
            return 0;
        }
    }

    /**
     * Add a rule to the list of rules for this user agent.
     * Valid rules are: sitemap, crawl-delay, host, allow and disallow.
     * These are based on the wikipedia article at:
     *
     * https://en.wikipedia.org/wiki/Robots_exclusion_standard
     *
     * and the Google documentation at:
     *
     * https://support.google.com/webmasters/answer/6062596
     *
     * @param rule The name of the rule
     * @param value The value of the rule
     */
    public void add(String rule, String value) {
        if (rule.equals("sitemap")) {
            if (this.sitemap == null) {
                this.sitemap = new ArrayList<String>();
            }
            this.sitemap.add(value);
        } else if (rule.equals("crawl-delay")) {
            try {
                this.crawlDelay = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid number format for crawl-delay robots.txt: {}", value);
            }
        } else if (rule.equals("host")) {
            this.preferredHost = value;
        } else if (rule.equals("allow")) {
            this.pathRules.add(new PathRule(HostDirectives.ALLOWED, value));
        } else if (rule.equals("disallow")) {
            this.pathRules.add(new PathRule(HostDirectives.DISALLOWED, value));
        } else {
            logger.error("Invalid key in robots.txt passed to UserAgentRules: {}", rule);
        }
    }

    /**
     * Return the configured crawl delay in seconds
     *
     * @return The configured crawl delay, or null if none was specified
     */
    public Double getCrawlDelay() {
        return crawlDelay;
    }

    /**
     * Return the specified preferred host name in robots.txt.
     *
     * @return The specified hostname, or null if it was not specified
     */
    public String getPreferredHost() {
        return preferredHost;
    }

    /**
     * Return the listed sitemaps, or null if none was specified
     *
     * @return The list of sitemap-links specified in robots.txt
     */
    public List<String> getSitemap() {
        return sitemap;
    }
}
