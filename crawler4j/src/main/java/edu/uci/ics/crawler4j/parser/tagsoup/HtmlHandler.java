//**************************************************
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.extractor.EmbeddedDocumentUtil;
import org.apache.tika.metadata.HTML;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlMapper;
import org.apache.tika.sax.TextContentHandler;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

class HtmlHandler extends TextContentHandler {

    // List of attributes that need to be resolved.
    private static final Set<String> URI_ATTRIBUTES =
            new HashSet<String>(Arrays.asList("src", "href", "longdesc", "cite"));
    private static final Pattern ICBM =
            Pattern.compile("\\s*(-?\\d+\\.\\d+)[,\\s]+(-?\\d+\\.\\d+)\\s*");
    private static final Attributes EMPTY_ATTS = new AttributesImpl();
    private final HtmlMapper mapper;
    private final XHTMLContentHandler xhtml;
    private final Metadata metadata;
    private final ParseContext context;
    private final boolean extractScripts;
    private final StringBuilder title = new StringBuilder();
    private int bodyLevel = 0;
    private int discardLevel = 0;
    private int titleLevel = 0;
    private int scriptLevel = 0;
    private Attributes scriptAtts = EMPTY_ATTS; //attributes from outermost script element
    private final StringBuilder script = new StringBuilder();

    private boolean isTitleSetToMetadata = false;

    private HtmlHandler(
            HtmlMapper mapper, XHTMLContentHandler xhtml, Metadata metadata,
            ParseContext context, boolean extractScripts) {
        super(xhtml);
        this.mapper = mapper;
        this.xhtml = xhtml;
        this.metadata = metadata;
        this.context = context;
        this.extractScripts = extractScripts;
        // Try to determine the default base URL, if one has not been given
        if (metadata.get(Metadata.CONTENT_LOCATION) == null) {
            String name = metadata.get(Metadata.RESOURCE_NAME_KEY);
            if (name != null) {
                name = name.trim();
                try {
                    new URL(name); // test URL format
                    metadata.set(Metadata.CONTENT_LOCATION, name);
                } catch (MalformedURLException e) {
                    // The resource name is not a valid URL, ignore it
                }
            }
        }
    }

    HtmlHandler(
            HtmlMapper mapper, ContentHandler handler, Metadata metadata, ParseContext context,
            boolean extractScripts) {
        this(mapper, new XHTMLContentHandler(handler, metadata), metadata, context, extractScripts);
    }

    /**
     * @deprecated use {@link HtmlHandler#HtmlHandler(HtmlMapper, ContentHandler, Metadata, ParseContext, boolean)}
     * @param mapper
     * @param handler
     * @param metadata
     */
    @Deprecated
    HtmlHandler(
            HtmlMapper mapper, ContentHandler handler, Metadata metadata) {
        this(mapper, new XHTMLContentHandler(handler, metadata), metadata, new ParseContext(), false);
    }

    @Override
    public void startElement(
            String uri, String local, String name, Attributes atts)
            throws SAXException {

        if ("SCRIPT".equals(name)) {
            scriptLevel++;
        }
        if ("TITLE".equals(name) || titleLevel > 0) {
            titleLevel++;
        }
        if ("BODY".equals(name) || ("FRAMESET".equals(name)) || bodyLevel > 0) {
            bodyLevel++;
        }
        if (mapper.isDiscardElement(name) || discardLevel > 0) {
            discardLevel++;
        }

        if (bodyLevel == 0 && discardLevel == 0) {
            if ("META".equals(name) && atts.getValue("content") != null) {
                // TIKA-478: For cases where we have either a name or
                // "http-equiv", assume that XHTMLContentHandler will emit
                // these in the <head>, thus passing them through safely.
                if (atts.getValue("http-equiv") != null) {
                    addHtmlMetadata(
                            atts.getValue("http-equiv"),
                            atts.getValue("content"));
                } else if (atts.getValue("name") != null) {
                    // Record the meta tag in the metadata
                    addHtmlMetadata(
                            atts.getValue("name"),
                            atts.getValue("content"));
                } else if (atts.getValue("property") != null) {
                    // TIKA-983: Handle <meta property="og:xxx" content="yyy" /> tags
                    metadata.add(
                            atts.getValue("property"),
                            atts.getValue("content"));
                }
            } else if ("BASE".equals(name) && atts.getValue("href") != null) {
                startElementWithSafeAttributes("base", atts);
                xhtml.endElement("base");
                metadata.set(
                        Metadata.CONTENT_LOCATION,
                        resolve(atts.getValue("href")));
            } else if ("LINK".equals(name)) {
                startElementWithSafeAttributes("link", atts);
                xhtml.endElement("link");
            } else if ("SCRIPT".equals(name)) {
                scriptAtts = atts;
            }
        }

        if (bodyLevel > 0 && discardLevel == 0) {
            String safe = mapper.mapSafeElement(name);
            if (safe != null) {
                startElementWithSafeAttributes(safe, atts);
            }
        }

        title.setLength(0);
    }

    /**
     * Adds a metadata setting from the HTML <head/> to the Tika metadata
     * object. The name and value are normalized where possible.
     */
    private void addHtmlMetadata(String name, String value) {
        if (name == null || value == null) {
            // ignore
        } else if (name.equalsIgnoreCase("ICBM")) {
            Matcher m = ICBM.matcher(value);
            if (m.matches()) {
                metadata.set("ICBM", m.group(1) + ", " + m.group(2));
                metadata.set(Metadata.LATITUDE, m.group(1));
                metadata.set(Metadata.LONGITUDE, m.group(2));
            } else {
                metadata.set("ICBM", value);
            }
        } else if (name.equalsIgnoreCase(Metadata.CONTENT_TYPE)) {
            //don't overwrite Metadata.CONTENT_TYPE!
            MediaType type = MediaType.parse(value);
            if (type != null) {
                metadata.set(TikaCoreProperties.CONTENT_TYPE_HINT, type.toString());
            } else {
                metadata.set(TikaCoreProperties.CONTENT_TYPE_HINT, value);
            }
        } else {
            metadata.add(name, value);
        }
    }

    private void startElementWithSafeAttributes(String name, Attributes atts) throws SAXException {
        if (atts.getLength() == 0) {
            xhtml.startElement(name);
            return;
        }

        boolean isObject = name.equals("object");
        String codebase = null;
        if (isObject) {
            codebase = atts.getValue("", "codebase");
            if (codebase != null) {
                codebase = resolve(codebase);
            } else {
                codebase = metadata.get(Metadata.CONTENT_LOCATION);
            }
        }

        AttributesImpl newAttributes = new AttributesImpl(atts);
        for (int att = 0; att < newAttributes.getLength(); att++) {
            String attrName = newAttributes.getLocalName(att);
            String normAttrName = mapper.mapSafeAttribute(name, attrName);
            if (normAttrName == null) {
                newAttributes.removeAttribute(att);
                att--;
            } else {
                // We have a remapped attribute name, so set it as it might have changed.
                newAttributes.setLocalName(att, normAttrName);

                // And resolve relative links. Eventually this should be pushed
                // into the HtmlMapper code.
                if (URI_ATTRIBUTES.contains(normAttrName)) {
                    newAttributes.setValue(att, resolve(newAttributes.getValue(att)));
                } else if (isObject && "codebase".equals(normAttrName)) {
                    newAttributes.setValue(att, codebase);
                } else if (isObject
                        && ("data".equals(normAttrName)
                        || "classid".equals(normAttrName))) {
                    newAttributes.setValue(
                            att,
                            resolve(codebase, newAttributes.getValue(att)));
                }
            }
        }

        if ("img".equals(name) && newAttributes.getValue("", "alt") == null) {
            newAttributes.addAttribute("", "alt", "alt", "CDATA", "");
        }

        xhtml.startElement(name, newAttributes);
    }

    @Override
    public void endElement(
            String uri, String local, String name) throws SAXException {
        if ("SCRIPT".equals(name)) {
            scriptLevel--;
            if (scriptLevel == 0) {
                if (scriptAtts.getLength() > 0) {
                    startElementWithSafeAttributes("script", scriptAtts);
                    xhtml.endElement("script");
                }
                scriptAtts = EMPTY_ATTS;
                if (extractScripts) {
                    writeScript();
                }
            }
        }

        if (bodyLevel > 0 && discardLevel == 0) {
            String safe = mapper.mapSafeElement(name);
            if (safe != null) {
                xhtml.endElement(safe);
            } else if (XHTMLContentHandler.ENDLINE.contains(
                    name.toLowerCase(Locale.ENGLISH))) {
                // TIKA-343: Replace closing block tags (and <br/>) with a
                // newline unless the HtmlMapper above has already mapped
                // them to something else
                xhtml.newline();
            }
        }

        if (titleLevel > 0) {
            titleLevel--;
            if (titleLevel == 0 && !isTitleSetToMetadata) {
                metadata.set(TikaCoreProperties.TITLE, title.toString().trim());
                isTitleSetToMetadata = true;
            }
        }
        if (bodyLevel > 0) {
            bodyLevel--;
        }
        if (discardLevel > 0) {
            discardLevel--;
        }
    }

    private void writeScript() throws SAXException {
        //don't write an attached macro if there is no content
        //we may want to revisit this behavior
        if (script.toString().trim().length() == 0) {
            return;
        }
        //do anything with attrs?
        Metadata m = new Metadata();
        m.set(Metadata.EMBEDDED_RESOURCE_TYPE,
                TikaCoreProperties.EmbeddedResourceType.MACRO.toString());
        String src = scriptAtts.getValue("src");
        if (src != null) {
            m.set(HTML.SCRIPT_SOURCE, src);
        }

        EmbeddedDocumentExtractor embeddedDocumentExtractor =
                EmbeddedDocumentUtil.getEmbeddedDocumentExtractor(context);
        try (InputStream stream = new ByteArrayInputStream(
                script.toString().getBytes(StandardCharsets.UTF_8))) {
            embeddedDocumentExtractor.parseEmbedded(
                    stream, xhtml, m, false
            );
        } catch (IOException e) {
            //shouldn't ever happen
        } finally {
            script.setLength(0);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (scriptLevel > 0 && extractScripts) {
            script.append(ch, start, length);
        }
        if (titleLevel > 0 && bodyLevel == 0) {
            title.append(ch, start, length);
        }
        if (bodyLevel > 0 && discardLevel == 0) {
            super.characters(ch, start, length);
        }

    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        if (bodyLevel > 0 && discardLevel == 0) {
            super.ignorableWhitespace(ch, start, length);
        }
    }

    private String resolve(String url) {
        return resolve(metadata.get(Metadata.CONTENT_LOCATION), url);
    }

    private String resolve(String base, String url) {
        url = url.trim();

        // Return the URL as-is if no base URL is available or if the URL
        // matches a common non-hierarchical or pseudo URI prefix
        String lower = url.toLowerCase(Locale.ENGLISH);
        if (base == null
                || lower.startsWith("urn:")
                || lower.startsWith("mailto:")
                || lower.startsWith("tel:")
                || lower.startsWith("data:")
                || lower.startsWith("javascript:")
                || lower.startsWith("about:")) {
            return url;
        }

        try {
            URL baseURL = new URL(base.trim());

            // We need to handle one special case, where the relativeUrl is
            // just a query string (like "?pid=1"), and the baseUrl doesn't
            // end with a '/'. In that case, the URL class removes the last
            // portion of the path, which we don't want.
            String path = baseURL.getPath();
            if (url.startsWith("?") && path.length() > 0 && !path.endsWith("/")) {
                return new URL(
                        baseURL.getProtocol(),
                        baseURL.getHost(), baseURL.getPort(),
                        baseURL.getPath() + url).toExternalForm();
            } else {
                return new URL(baseURL, url).toExternalForm();
            }
        } catch (MalformedURLException e) {
            // Unknown or broken format; just return the URL as received.
            return url;
        }
    }

}
