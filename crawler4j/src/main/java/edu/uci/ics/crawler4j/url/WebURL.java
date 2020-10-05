/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.url;

import java.io.Serializable;
import java.util.Map;

import com.google.common.net.InternetDomainName;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * @author Yasser Ganjisaffar
 */

@Entity
public class WebURL implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String POST_SEPARATOR = "<<<POST_DATA>>>";

    @PrimaryKey
    private String url;

    private int docid = -1;
    private int parentDocid;
    private String parentUrl;
    private short depth;
    private String registeredDomain;
    private String subDomain;
    private String path;
    private String anchor;
    private byte priority;
    private String tag;
    private Map<String, String> attributes;
    private TLDList tldList;
    private boolean post;
    private PostParameters paramsPost;

    public PostParameters getParamsPost() {
        return paramsPost;
    }

    public void setParamsPost(PostParameters paramsPost) {
        this.paramsPost = paramsPost;
    }

    /**
     * Adds a POST key / value pair. Subclases may override this behaviour for optimization.
     *
     * @param key
     * @param value
     * @return
     * @throws IllegalArgumentException if <code>key</code> is <code>null</code>
     * @see PostParameters#addParameter(String, String)
     */
    public boolean addPostParameter(String key, String value) throws IllegalArgumentException {
        if (paramsPost == null) {
            paramsPost = createEmptyPostParams();
        }
        return paramsPost.addParameter(key, value);
    }

    /**
     * Returns true if this WebURL represents a POST request.
     *
     * @return
     */
    public boolean isPost() {
        return post;
    }

    /**
     * Configures the HTTP request type to be POST (true) or GET(false)
     * @param post <code>true</code> to configure POST request, <code>false</code> for a GET request.
     */
    public void setPost(boolean post) {
        this.post = post;
    }

    /**
     * Set the TLDList if you want {@linkplain #getDomain()} and
     * {@link #getSubDomain()} to properly identify effective top level registeredDomain as
     * defined at <a href="https://publicsuffix.org">publicsuffix.org</a>
     */
    public void setTldList(TLDList tldList) {
        this.tldList = tldList;
    }

    /**
     * @return unique document id assigned to this Url.
     */
    public int getDocid() {
        return docid;
    }

    public void setDocid(int docid) {
        this.docid = docid;
    }

    /**
     * @return Url string
     */
    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;

        int domainStartIdx = url.indexOf("//") + 2;
        int domainEndIdx = url.indexOf('/', domainStartIdx);
        domainEndIdx = (domainEndIdx > domainStartIdx) ? domainEndIdx : url.length();
        String domain = url.substring(domainStartIdx, domainEndIdx);
        registeredDomain = domain;
        subDomain = "";
        if (tldList != null && !(domain.isEmpty()) && InternetDomainName.isValid(domain)) {
            String candidate = null;
            String rd = null;
            String sd = null;
            String[] parts = domain.split("\\.");
            for (int i = parts.length - 1; i >= 0; i--) {
                if (rd == null) {
                    if (candidate == null) {
                        candidate = parts[i];
                    } else {
                        candidate = parts[i] + "." + candidate;
                    }
                    if (tldList.isRegisteredDomain(candidate)) {
                        rd = candidate;
                    }
                } else {
                    if (sd == null) {
                        sd = parts[i];
                    } else {
                        sd = parts[i] + "." + sd;
                    }
                }
            }
            if (rd != null) {
                registeredDomain = rd;
            }
            if (sd != null) {
                subDomain = sd;
            }
        }
        path = url.substring(domainEndIdx);
        int pathEndIdx = path.indexOf('?');
        if (pathEndIdx >= 0) {
            path = path.substring(0, pathEndIdx);
        }
    }

    /**
     * @return
     *      unique document id of the parent page. The parent page is the
     *      page in which the Url of this page is first observed.
     */
    public int getParentDocid() {
        return parentDocid;
    }

    public void setParentDocid(int parentDocid) {
        this.parentDocid = parentDocid;
    }

    /**
     * @return
     *      url of the parent page. The parent page is the page in which
     *      the Url of this page is first observed.
     */
    public String getParentUrl() {
        return parentUrl;
    }

    public void setParentUrl(String parentUrl) {
        this.parentUrl = parentUrl;
    }

    /**
     * @return
     *      crawl depth at which this Url is first observed. Seed Urls
     *      are at depth 0. Urls that are extracted from seed Urls are at depth 1, etc.
     */
    public short getDepth() {
        return depth;
    }

    public void setDepth(short depth) {
        this.depth = depth;
    }

    /**
     * If {@link WebURL} was provided with a {@link TLDList} then domain will be the
     * privately registered domain which is an immediate child of an effective top
     * level domain as defined at
     * <a href="https://publicsuffix.org">publicsuffix.org</a>. Otherwise it will be
     * the entire domain.
     *
     * @return Domain of this Url. For 'http://www.example.com/sample.htm',
     *         effective top level domain is 'example.com'. For
     *         'http://www.my.company.co.uk' the domain is 'company.co.uk'.
     */
    public String getDomain() {
        return registeredDomain;
    }

    /**
     * If {@link WebURL} was provided with a {@link TLDList} then subDomain will be
     * the private portion of the entire domain which is a child of the identified
     * registered domain. Otherwise it will be empty. e.g. in
     * "http://www.example.com" the subdomain is "www". In
     * "http://www.my.company.co.uk" the subdomain would be "www.my".
     */
    public String getSubDomain() {
        return subDomain;
    }

    /**
     * @return
     *      path of this Url. For 'http://www.example.com/sample.htm', registeredDomain will be 'sample.htm'
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return
     *      anchor string. For example, in <a href="example.com">A sample anchor</a>
     *      the anchor string is 'A sample anchor'
     */
    public String getAnchor() {
        return anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    /**
     * @return priority for crawling this URL. A lower number results in higher priority.
     */
    public byte getPriority() {
        return priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    /**
     * @return tag in which this URL is found
     * */
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getAttribute(String name) {
        if (attributes == null) {
            return "";
        }
        return attributes.getOrDefault(name, "");
    }

    protected PostParameters createEmptyPostParams() {
        return new SimplePostParameters();
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        WebURL otherUrl = (WebURL) o;
        return (url != null) && url.equals(otherUrl.getURL());

    }

    @Override
    public String toString() {
        return url;
    }

    /**
     * Encodes the URL and the post parameters in a string to store in the DocIDServer.
     *
     * This is what identifies this URL as already visited or new.
     * @return
     */
    public String encode() {
        return encodeWebURL(this);
    }

    /**
     *    Encodes the URL and the post parameters in a string to store in the DocIDServer.
     *
     * This is what identifies this URL as already visited or new.
     * @param url
     * @return
     */
    public static String encodeWebURL(WebURL url) {
        if (url == null || url.getURL() == null) {
            return null;
        }
        if (!url.isPost()) {
            return url.getURL();
        }
        if (url.getParamsPost() != null) {
            return url.getURL() + POST_SEPARATOR + url.getParamsPost().encode();
        } else {
            return url.getURL() + POST_SEPARATOR;
        }
    }

    /**
     * Converts an encoded String in an instance of WebURL.
     *
     * String encoded by subclases of WebURL may not be compatible.
     *
     * @param url string with the URL and POST parameters included
     * @return the {@link WebURL} that represents the string provided
     */
    public static WebURL decodeString(String url) {
        if (url == null) {
            return null;
        }
        WebURL result = new WebURL();
        if (isPost(url)) {
            result.setPost(true);
            // Check if there's something usefull after POST_SEPARATOR.
            if (hasPostParams(url)) {
                // There are valid parameters.
                String[] splitted = url.split(POST_SEPARATOR, 2);
                result.setURL(splitted[0]);
                if (splitted.length > 1) {
                    result.setParamsPost(SimplePostParameters.decodePostAtributes(splitted[1]));
                }
            } else {
                result.setURL(url.replaceAll(POST_SEPARATOR, ""));
            }
        } else {
            result.setURL(url);
        }
        return result;
    }

    public static boolean isPost(String encodedUrl) {
        if (encodedUrl == null) {
            return false;
        }
        if (encodedUrl.contains(POST_SEPARATOR)) {
            return true;
        }
        return false;
    }

    protected static boolean hasPostParams(String encodedUrl) {
        // Check if the URL has post parameters
        if (encodedUrl == null) {
            return false;
        }
        String[] parts = encodedUrl.split(POST_SEPARATOR);
        if (parts.length > 1) {
            for (int i = 1; i < parts.length; i++) {
                if (parts[i] != null && !parts[i].isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

}