package edu.uci.ics.crawler4j.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.exceptions.RegexpTimeoutException;

/**
 * Allows to create timeoutable regular expressions.
 *
 * Limitations: Can only throw RuntimeException. Decreases performance.
 *
 * Posted by Kris in stackoverflow.
 *
 * Modified by dgoiko to  ejecute timeout check only every n chars.
 * Now timeout < 0 means no timeout.
 *
 * @author Kris https://stackoverflow.com/a/910798/9465588
 *
 */
public class RegularExpressionUtils {

    // demonstrates behavior for regular expression running into catastrophic backtracking for given input
    public static void main(String[] args) {
        long millis = System.currentTimeMillis();
        // This checkInterval produces a < 500 ms delay. Higher checkInterval will produce higher delays on timeout.
        Matcher matcher = createMatcherWithTimeout(
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", "(x+x+)+y", 2000, 30000000);
        try {
            System.out.println(matcher.matches());
        } catch(RuntimeException e) {
            System.out.println("Operation timed out after "+ (System.currentTimeMillis() - millis)+" milliseconds");
        }
    }

    public static Matcher createMatcherWithTimeout(String stringToMatch, String regularExpression, long timeoutMillis,
                                                      int checkInterval) {
        Pattern pattern = Pattern.compile(regularExpression);
        return createMatcherWithTimeout(stringToMatch, pattern, timeoutMillis, checkInterval);
    }

    public static Matcher createMatcherWithTimeout(String stringToMatch, Pattern regularExpressionPattern,
                                                    long timeoutMillis, int checkInterval) {
        if    ( timeoutMillis < 0) {
            return regularExpressionPattern.matcher(stringToMatch);
        }
        CharSequence charSequence = new TimeoutRegexCharSequence(stringToMatch, timeoutMillis, stringToMatch,
                regularExpressionPattern.pattern(), checkInterval);
        return regularExpressionPattern.matcher(charSequence);
    }

    private static class TimeoutRegexCharSequence implements CharSequence {

        private final CharSequence inner;

        private final long timeoutMillis;

        private final long timeoutTime;

        private final String stringToMatch;

        private final String regularExpression;

        private int checkInterval;

        private int attemps;

        TimeoutRegexCharSequence(CharSequence inner, long timeoutMillis, String stringToMatch,
                                  String regularExpression, int checkInterval) {
            super();
            this.inner = inner;
            this.timeoutMillis = timeoutMillis;
            this.stringToMatch = stringToMatch;
            this.regularExpression = regularExpression;
            timeoutTime = System.currentTimeMillis() + timeoutMillis;
            this.checkInterval = checkInterval;
            this.attemps = 0;
        }

        public char charAt(int index) {
            if (this.attemps == this.checkInterval) {
                if (System.currentTimeMillis() > timeoutTime) {
                    throw new RegexpTimeoutException(regularExpression, stringToMatch, timeoutMillis);
                }
                this.attemps = 0;
            } else {
                this.attemps++;
            }

            return inner.charAt(index);
        }

        public int length() {
            return inner.length();
        }

        public CharSequence subSequence(int start, int end) {
            return new TimeoutRegexCharSequence(inner.subSequence(start, end), timeoutMillis, stringToMatch,
                                                regularExpression, checkInterval);
        }

        @Override
        public String toString() {
            return inner.toString();
        }
    }

}