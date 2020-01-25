package edu.uci.ics.crawler4j.crawler.exceptions;

/**
 * Created by Dario Goikoetxea on 24/1/2020.
 *
 * Thrown when there is a problem with the configuration
 */
public class ConfigException extends Exception {
    private static final long serialVersionUID = -7376208295930945704L;

    public ConfigException() {
        super();
    }

    public ConfigException(Throwable cause) {
        super(cause);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigException(String message) {
        super(message);
    }

}