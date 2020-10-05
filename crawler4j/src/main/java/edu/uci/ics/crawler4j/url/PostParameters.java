package edu.uci.ics.crawler4j.url;

import java.util.List;

import org.apache.http.message.BasicNameValuePair;

/**
 *
 * @author Dario
 *
 */
public interface PostParameters {

    String encode();

    boolean addParameter(String key, String value) throws IllegalArgumentException;

    /**
     * Remove a parameter from list using the key.
     * Implementation may throw IllegalArgumentException if key is <code>null</code> or return <code>false</code>
     *
     * @param key name of the pair to be removed
     * @param maxOcurrences maximum number of ocurences to remove (http accepts duplicated keys)
     * @return <code>true</code> if there are changes <code>false</code> otherwise
     * @throws IllegalArgumentException if key is <code>null</code> or maxOcurrences < 1
     */
    boolean removeParameter(String key, int maxOcurrences) throws IllegalArgumentException;

    boolean isEmpty();

    /**
     * Gets the parameters as a List of BasicNameValuePair.
     *
     * Implementations may return a copy that does not affect the internal list, but they should clearly state it.
     * @return
     */
    List<BasicNameValuePair> getAsList();
}
