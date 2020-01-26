package edu.uci.ics.crawler4j.url;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

public class SimplePostParameters implements PostParameters {

    public static final String PAIR_SEPARATOR = "``--``";

    public static final String VALUE_SEPARATOR = "=";

    private List<BasicNameValuePair> paramsPost;

    public SimplePostParameters() {
        this.paramsPost = new ArrayList<BasicNameValuePair>();
    }

    public SimplePostParameters(List<BasicNameValuePair> paramsPost) {
        this.paramsPost = new ArrayList<BasicNameValuePair>();
        if (paramsPost != null) {
            this.paramsPost.addAll(paramsPost);
        }
    }

    @Override
    public String encode() {
        return encodePostAttributes(paramsPost);
    }

    public boolean addParameter(BasicNameValuePair pair) throws IllegalArgumentException {
        if (pair == null) {
            throw new IllegalArgumentException("pair cannot be null");
        }
        if (pair.getName() == null || pair.getName().isEmpty()) {
            throw new IllegalArgumentException("key cannot be null or empty");
        }
        return paramsPost.add(pair);
    }

    @Override
    public boolean addParameter(String key, String value) throws IllegalArgumentException {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key cannot be null or empty");
        }
        return this.paramsPost.add(new BasicNameValuePair(key, value));
    }

    @Override
    public boolean removeParameter(String key, int maxOcurrences) throws IllegalArgumentException {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key cannot be null or empty");
        }
        if (maxOcurrences < 1) {
            throw new IllegalArgumentException("maxOcurrences must be a positive number");
        }
        Iterator<BasicNameValuePair> it = paramsPost.iterator();
        boolean changes = false;
        while (maxOcurrences > 0 && it.hasNext()) {
            BasicNameValuePair curr = it.next();
            if (key.equals(curr.getName())) {
                it.remove();
                maxOcurrences--;
                changes = true;
            }
        }
        return changes;
    }

    protected static String encodePostAttributes(List<BasicNameValuePair> postAttributes) {
        if (postAttributes == null || postAttributes.isEmpty()) {
            return "";
        }
        List<String> pares = new ArrayList<String>();
        for (BasicNameValuePair par : postAttributes) {
            if (par == null) {
                continue;
            }
            pares.add(par.getName() + VALUE_SEPARATOR + par.getValue());
        }
        return String.join(PAIR_SEPARATOR, pares);
    }

    public static SimplePostParameters decodePostAtributes(String encodedUrl) {
        if (encodedUrl == null || encodedUrl.isEmpty()) {
            return null;
        }
        List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
        for (String pair : encodedUrl.split(PAIR_SEPARATOR)) {
            if (pair == null) {
                continue;
            }
            String[] splitted = pair.split(VALUE_SEPARATOR, 2);
            if (splitted.length > 1) {
                list.add(new BasicNameValuePair(splitted[0], splitted[1]));
            } else {
                list.add(new BasicNameValuePair(splitted[0], ""));
            }

        }
        return new SimplePostParameters(list);
    }

    @Override
    public boolean isEmpty() {
        return paramsPost.isEmpty();
    }

    @Override
    public List<BasicNameValuePair> getAsList() {
        return paramsPost;
    }

}
