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

package edu.uci.ics.crawler4j.frontier;

import java.util.Map;

import edu.uci.ics.crawler4j.url.WebURL;

/**
 * @author Yasser Ganjisaffar
 */
public class WebURLTupleBinding {


  public static final String URL = "url";
  public static final String DOCID = "docid";
  public static final String PARENT_DOCID = "parent_docid";
  public static final String PARENT_URL = "parent_url";
  public static final String DEPTH = "depth";
  public static final String PRIORITY = "priority";
  public static final String ANCHOR = "anchor";

  public WebURL entryToObject(Map<String,String> input) {
    WebURL webURL = new WebURL();
    webURL.setURL(input.get(URL));
    webURL.setDocid(Integer.parseInt(input.get(DOCID)));
    webURL.setParentDocid(Integer.parseInt(input.get(PARENT_DOCID)));
    webURL.setParentUrl(input.get(PARENT_URL));
    webURL.setDepth(Short.parseShort(input.get(DEPTH)));
    webURL.setPriority(Byte.parseByte(input.get(PRIORITY)));
    webURL.setAnchor(input.get(ANCHOR));
    return webURL;
  }


  public void objectToEntry(WebURL url, Map<String,String> output) {
    output.put(URL, url.getURL());
    output.put(DOCID, String.valueOf(url.getDocid()));
    output.put(PARENT_DOCID, String.valueOf(url.getParentDocid()));
    String parentUrl = url.getParentUrl();
    if(parentUrl != null) {
      output.put(PARENT_URL, parentUrl);
    }
    output.put(DEPTH, String.valueOf(url.getDepth()));
    output.put(PRIORITY, String.valueOf(url.getPriority()));
    String anchor = url.getAnchor();
    if(anchor != null) {
      output.put(ANCHOR, anchor);
    }

  }
}