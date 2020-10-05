package edu.uci.ics.crawler4j.frontier;

public interface DocIDServerInterface {

    /**
     * Returns the docid of an already seen url.
     *
     * @param url the URL for which the docid is returned.
     * @return the docid of the url if it is seen before. Otherwise -1 is returned.
     */
    int getDocId(String url);

    int getNewDocID(String url);

    void addUrlAndDocId(String url, int docId);

    boolean isSeenBefore(String url);

    int getDocCount();

    void close();

}