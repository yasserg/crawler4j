//****************************************

/*
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
package edu.uci.ics.crawler4j.parser.tagsoup;

import javax.xml.XMLConstants;
import java.util.Locale;

import org.apache.tika.sax.ContentHandlerDecorator;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Content handler decorator that downgrades XHTML elements to
 * old-style HTML elements before passing them on to the decorated
 * content handler. This downgrading consists of dropping all namespaces
 * (and namespaced attributes) and uppercasing all element names.
 * Used by the {@link HtmlParser} to make all incoming HTML look the same.
 */
class XHTMLDowngradeHandler extends ContentHandlerDecorator {

    public XHTMLDowngradeHandler(ContentHandler handler) {
        super(handler);
    }

    @Override
    public void startElement(
            String uri, String localName, String name, Attributes atts)
            throws SAXException {
        String upper = localName.toUpperCase(Locale.ENGLISH);

        AttributesImpl attributes = new AttributesImpl();
        for (int i = 0; i < atts.getLength(); i++) {
            String auri = atts.getURI(i);
            String local = atts.getLocalName(i);
            String qname = atts.getQName(i);
            if (XMLConstants.NULL_NS_URI.equals(auri)
                    && !local.equals(XMLConstants.XMLNS_ATTRIBUTE)
                    && !qname.startsWith(XMLConstants.XMLNS_ATTRIBUTE + ":")) {
                attributes.addAttribute(
                        auri, local, qname, atts.getType(i), atts.getValue(i));
            }
        }

        super.startElement(XMLConstants.NULL_NS_URI, upper, upper, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        String upper = localName.toUpperCase(Locale.ENGLISH);
        super.endElement(XMLConstants.NULL_NS_URI, upper, upper);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) {
    }

    @Override
    public void endPrefixMapping(String prefix) {
    }

}
