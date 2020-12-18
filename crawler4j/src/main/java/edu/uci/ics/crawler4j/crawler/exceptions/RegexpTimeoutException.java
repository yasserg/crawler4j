package edu.uci.ics.crawler4j.crawler.exceptions;

public class RegexpTimeoutException extends RuntimeException {
    private static final long serialVersionUID = 6437153127902393756L;

    private final String regularExpression;

    private final String stringToMatch;

    private final long timeoutMillis;

    public RegexpTimeoutException() {
        super();
        regularExpression = null;
        stringToMatch = null;
        timeoutMillis = 0;
    }

    public RegexpTimeoutException(String message, Throwable cause) {
        super(message, cause);
        regularExpression = null;
        stringToMatch = null;
        timeoutMillis = 0;
    }

    public RegexpTimeoutException(String message) {
        super(message);
        regularExpression = null;
        stringToMatch = null;
        timeoutMillis = 0;
    }

    public RegexpTimeoutException(Throwable cause) {
        super(cause);
        regularExpression = null;
        stringToMatch = null;
        timeoutMillis = 0;
    }

    public RegexpTimeoutException(String regularExpression, String stringToMatch, long timeoutMillis) {
        super("Timeout occurred after " + timeoutMillis + "ms while processing regular expression '"
                + regularExpression + "' on input '" + stringToMatch + "'!");
        this.regularExpression = regularExpression;
        this.stringToMatch = stringToMatch;
        this.timeoutMillis = timeoutMillis;
    }

    public String getRegularExpression() {
        return regularExpression;
    }

    public String getStringToMatch() {
        return stringToMatch;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

}
