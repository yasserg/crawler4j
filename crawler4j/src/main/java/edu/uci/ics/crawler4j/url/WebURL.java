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

import java.util.Map;

/**
 * @author Yasser Ganjisaffar
 */

public class WebURL {

    private byte priority;

    private short depth;

    private int id;

    private int parentId;

    private String uRL;

    private String subDomain;

    private String domain;

    private String path;

    private String parentURL;

    private String anchor;

    private String tag;

    private Map<String, String> attributes;

    public String getSubDomain() {
        return subDomain;
    }

    /**
     * @return domain of this Url. For 'http://www.example.com/sample.htm', domain will be 'example
     *         .com'
     */
    public String getDomain() {
        return domain;
    }

    /**
     * @return path of this Url. For 'http://www.example.com/sample.htm', domain will be
     *         'sample.htm'
     */
    public String getPath() {
        return path;
    }

    public String getAttribute(String key) {
        if (null == attributes || !attributes.containsKey(key)) {
            return "";
        }
        return attributes.get(key);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anchor == null) ? 0 : anchor.hashCode());
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + depth;
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + id;
        result = prime * result + parentId;
        result = prime * result + ((parentURL == null) ? 0 : parentURL.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + priority;
        result = prime * result + ((subDomain == null) ? 0 : subDomain.hashCode());
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
        result = prime * result + ((uRL == null) ? 0 : uRL.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        WebURL other = (WebURL) obj;
        if (anchor == null) {
            if (other.anchor != null) {
                return false;
            }
        } else if (!anchor.equals(other.anchor)) {
            return false;
        }
        if (attributes == null) {
            if (other.attributes != null) {
                return false;
            }
        } else if (!attributes.equals(other.attributes)) {
            return false;
        }
        if (depth != other.depth) {
            return false;
        }
        if (domain == null) {
            if (other.domain != null) {
                return false;
            }
        } else if (!domain.equals(other.domain)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (parentId != other.parentId) {
            return false;
        }
        if (parentURL == null) {
            if (other.parentURL != null) {
                return false;
            }
        } else if (!parentURL.equals(other.parentURL)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (priority != other.priority) {
            return false;
        }
        if (subDomain == null) {
            if (other.subDomain != null) {
                return false;
            }
        } else if (!subDomain.equals(other.subDomain)) {
            return false;
        }
        if (tag == null) {
            if (other.tag != null) {
                return false;
            }
        } else if (!tag.equals(other.tag)) {
            return false;
        }
        if (uRL == null) {
            if (other.uRL != null) {
                return false;
            }
        } else if (!uRL.equals(other.uRL)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return uRL;
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

    public short getDepth() {
        return depth;
    }

    public void setDepth(short depth) {
        this.depth = depth;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getURL() {
        return uRL;
    }

    public void setURL(String uRL) {
        this.uRL = uRL;

        int domainStartIdx = uRL.indexOf("//") + 2;
        int domainEndIdx = uRL.indexOf('/', domainStartIdx);
        domainEndIdx = (domainStartIdx < domainEndIdx) ? domainEndIdx : uRL.length();
        domain = uRL.substring(domainStartIdx, domainEndIdx);
        subDomain = "";
        String[] parts = domain.split("\\.");
        if (2 < parts.length) {
            domain = parts[parts.length - 2] + "." + parts[parts.length - 1];
            int limit = 2;
            if (TLDList.getInstance().contains(domain)) {
                domain = parts[parts.length - 3] + "." + domain;
                limit = 3;
            }
            for (int i = 0; i < (parts.length - limit); i++) {
                if (!subDomain.isEmpty()) {
                    subDomain += ".";
                }
                subDomain += parts[i];
            }
        }
        path = uRL.substring(domainEndIdx);
        int pathEndIdx = path.indexOf('?');
        if (0 <= pathEndIdx) {
            path = path.substring(0, pathEndIdx);
        }
    }

    public String getParentURL() {
        return parentURL;
    }

    public void setParentURL(String parentURL) {
        this.parentURL = parentURL;
    }

    /**
     * @return anchor string. For example, in <a href="example.com">A sample anchor</a> the anchor
     *         string is 'A sample anchor'
     */
    public String getAnchor() {
        return anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    /**
     * @return tag in which this URL is found
     */
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

}
