package edu.uci.ics.crawler4j.robotstxt;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathRule {
    protected static final Logger logger = LoggerFactory.getLogger(PathRule.class);

    public int type;
    public Pattern pattern;

    /**
     * Match a pattern defined in a robots.txt file to a path
     * Following the pattern definition as stated on:
     * https://support.google.com/webmasters/answer/6062596?hl=en&ref_topic=6061961
     *
     * This page defines the following items:
     *    * matches any sequence of characters, including /
     *    $ matches the end of the line
     *
     * @param pattern The pattern to convert
     * @return The compiled regexp pattern created from the robots.txt pattern
     */
    public static Pattern robotsPatternToRegexp(String pattern) {
        StringBuilder regexp = new StringBuilder();
        regexp.append('^');
        StringBuilder quoteBuf = new StringBuilder();
        boolean terminated = false;

        // If the pattern is empty, match only completely empty entries, e.g., none as
        // there will always be a leading forward slash.
        if (pattern.isEmpty()) {
            return Pattern.compile("^$");
        }

        // Iterate over the characters
        for (int pos = 0; pos < pattern.length(); ++pos) {
            char ch = pattern.charAt(pos);

            if (ch == '\\') {
                // Handle escaped * and $ characters
                char nch = pos < pattern.length() - 1 ? pattern.charAt(pos + 1) : 0;
                if (nch == '*' || ch == '$') {
                    quoteBuf.append(nch);
                    ++pos; // We need to skip one character
                } else {
                    quoteBuf.append(ch);
                }
            } else if (ch == '*') {
                // * indicates any sequence of one or more characters
                if (quoteBuf.length() > 0) {
                    // The quoted character buffer is not empty, so add them before adding
                    // the wildcard matcher
                    regexp.append("\\Q").append(quoteBuf).append("\\E");
                    quoteBuf = new StringBuilder();
                }
                if (pos == pattern.length() - 1) {
                    terminated = true;
                    // A terminating * may match 0 or more characters
                    regexp.append(".*");
                } else {
                    // A non-terminating * may match 1 or more characters
                    regexp.append(".+");
                }
            } else if (ch == '$' && pos == pattern.length() - 1) {
                // A $ at the end of the pattern indicates that the path should end here in order
              // to match
                // This explicitly disallows prefix matching
                if (quoteBuf.length() > 0) {
                    // The quoted character buffer is not empty, so add them before adding
                    // the end-of-line matcher
                    regexp.append("\\Q").append(quoteBuf).append("\\E");
                    quoteBuf = new StringBuilder();
                }
                regexp.append(ch);
                terminated = true;
            } else {
                // Add the character as-is to the buffer for quoted characters
                quoteBuf.append(ch);
            }
        }

        // Add quoted string buffer: enclosed between \Q and \E
        if (quoteBuf.length() > 0) {
            regexp.append("\\Q").append(quoteBuf).append("\\E");
        }

        // Add a wildcard pattern after the path to allow matches where this
        // pattern matches a prefix of the path.
        if (!terminated) {
            regexp.append(".*");
        }

        // Return the compiled pattern
        return Pattern.compile(regexp.toString());
    }

    /**
     * Check if the specified path matches a robots.txt pattern
     *
     * @param pattern The pattern to match
     * @param path The path to match with the pattern
     * @return True when the pattern matches, false if it does not
     */
    public static boolean matchesRobotsPattern(String pattern, String path) {
        return robotsPatternToRegexp(pattern).matcher(path).matches();
    }

    /**
     * Create a new path rule, based on the specified pattern
     *
     * @param type Either HostDirectives.ALLOWS or HostDirectives.DISALLOWS
     * @param pattern The pattern for this rule
     */
    public PathRule(int type, String pattern) {
        this.type = type;
        this.pattern = robotsPatternToRegexp(pattern);
    }

    /**
     * Check if the specified path matches this rule
     *
     * @param path The path to match with this pattern
     * @return True when the path matches, false when it does not
     */
    public boolean matches(String path) {
        return this.pattern.matcher(path).matches();
    }
}
