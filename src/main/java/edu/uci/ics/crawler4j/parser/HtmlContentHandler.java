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

package edu.uci.ics.crawler4j.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class HtmlContentHandler extends DefaultHandler {

  private static final int MAX_ANCHOR_LENGTH = 100;

  private enum Element {
    A,
    AREA,
    LINK,
    IFRAME,
    FRAME,
    EMBED,
    IMG,
    BASE,
    META,
    BODY
  }

  private static class HtmlFactory {
    private static final Map<String, Element> name2Element;

    static {
      name2Element = new HashMap<>();
      for (Element element : Element.values()) {
        name2Element.put(element.toString().toLowerCase(), element);
      }
    }

    public static Element getElement(String name) {
      return name2Element.get(name);
    }
  }

  private String base;
  private String metaRefresh;
  private String metaLocation;
  private final Map<String, String> metaTags = new HashMap<>();

  private boolean isWithinBodyElement;
  private final StringBuilder bodyText;

  private final List<ExtractedUrlAnchorPair> outgoingUrls;

  private ExtractedUrlAnchorPair curUrl = null;
  private boolean anchorFlag = false;
  private final StringBuilder anchorText = new StringBuilder();

  public HtmlContentHandler() {
    isWithinBodyElement = false;
    bodyText = new StringBuilder();
    outgoingUrls = new ArrayList<>();
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    Element element = HtmlFactory.getElement(localName);

    if ((element == Element.A) || (element == Element.AREA) || (element == Element.LINK)) {
      String href = attributes.getValue("href");
      if (href != null) {
        anchorFlag = true;
        addToOutgoingUrls(href, localName);

      }
    } else if (element == Element.IMG) {
      String imgSrc = attributes.getValue("src");
      if (imgSrc != null) {
        addToOutgoingUrls(imgSrc, localName);

      }
    } else if ((element == Element.IFRAME) || (element == Element.FRAME) || (element == Element.EMBED)) {
      String src = attributes.getValue("src");
      if (src != null) {
        addToOutgoingUrls(src, localName);

      }
    } else if (element == Element.BASE) {
      if (base != null) { // We only consider the first occurrence of the Base element.
        String href = attributes.getValue("href");
        if (href != null) {
          base = href;
        }
      }
    } else if (element == Element.META) {
      String equiv = attributes.getValue("http-equiv");
      if (equiv == null) { // This condition covers several cases of XHTML meta
        equiv = attributes.getValue("name");
      }

      String content = attributes.getValue("content");
      if ((equiv != null) && (content != null)) {
        equiv = equiv.toLowerCase();
        metaTags.put(equiv, content);

        // http-equiv="refresh" content="0;URL=http://foo.bar/..."
        if ("refresh".equals(equiv) && (metaRefresh == null)) {
          int pos = content.toLowerCase().indexOf("url=");
          if (pos != -1) {
            metaRefresh = content.substring(pos + 4);
          }
          addToOutgoingUrls(metaRefresh, localName);
        }

        // http-equiv="location" content="http://foo.bar/..."
        if ("location".equals(equiv) && (metaLocation == null)) {
          metaLocation = content;
          addToOutgoingUrls(metaRefresh, localName);
        }
      }
    } else if (element == Element.BODY) {
      isWithinBodyElement = true;
    }
  }

  private void addToOutgoingUrls(String href, String tag) {
    curUrl = new ExtractedUrlAnchorPair();
    curUrl.setHref(href);
    curUrl.setTag(tag);
    outgoingUrls.add(curUrl);
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    Element element = HtmlFactory.getElement(localName);
    if ((element == Element.A) || (element == Element.AREA) || (element == Element.LINK)) {
      anchorFlag = false;
      if (curUrl != null) {
        String anchor = anchorText.toString().replaceAll("\n", " ").replaceAll("\t", " ").trim();
        if (!anchor.isEmpty()) {
          if (anchor.length() > MAX_ANCHOR_LENGTH) {
            anchor = anchor.substring(0, MAX_ANCHOR_LENGTH) + "...";
          }
          curUrl.setTag(localName);
          curUrl.setAnchor(anchor);
        }
        anchorText.delete(0, anchorText.length());
      }
      curUrl = null;
    } else if (element == Element.BODY) {
      isWithinBodyElement = false;
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (isWithinBodyElement) {
      bodyText.append(ch, start, length);

      if (anchorFlag) {
        anchorText.append(new String(ch, start, length));
      }
    }
  }

  public String getBodyText() {
    return bodyText.toString();
  }

  public List<ExtractedUrlAnchorPair> getOutgoingUrls() {
    return outgoingUrls;
  }

  public String getBaseUrl() {
    return base;
  }

  public Map<String, String> getMetaTags() {
    return metaTags;
  }
}